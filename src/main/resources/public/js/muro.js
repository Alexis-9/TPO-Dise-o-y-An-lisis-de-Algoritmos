// Vista Muro: publicaciones en el orden que devuelve la API (MergeSort por fecha o por relevancia).
// Acá no se ordena nada: solo se dibujan las filas y se animan cuando cambian de lugar.
import { api, numero } from './api.js';

const CRITERIOS = {
  fecha: 'De la más reciente a la más antigua. Si dos tienen la misma fecha y hora, va primero la de id menor.',
  relevancia: 'Relevancia = reacciones + 3 × comentarios, de mayor a menor. Si empatan, va primero la más '
    + 'reciente; si también coinciden en fecha y hora, la de id menor.',
};

const FORMATO_DIA = new Intl.DateTimeFormat('es-AR', { weekday: 'short', day: 'numeric', month: 'short' });

let orden = 'fecha';
let filas = new Map(); // id -> <li>
let el;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    form: $('form-publicacion'),
    autor: $('pub-autor'),
    autores: $('autores'),
    fecha: $('pub-fecha'),
    reacciones: $('pub-reacciones'),
    comentarios: $('pub-comentarios'),
    orden: $('muro-orden'),
    criterio: $('muro-criterio'),
    encabezado: $('muro-encabezado'),
    lista: $('muro-lista'),
    vacio: $('muro-vacio'),
    error: $('muro-error'),
  };

  el.fecha.value = ahora();
  el.form.addEventListener('submit', publicar);
  el.form.addEventListener('input', () => { el.form.querySelector('.error').textContent = ''; });

  for (const boton of el.orden.querySelectorAll('button')) {
    boton.addEventListener('click', () => cambiarOrden(boton.dataset.orden));
  }
  el.criterio.textContent = CRITERIOS[orden];
}

export async function refrescar(opciones = {}) {
  el.error.textContent = '';
  try {
    const [publicaciones, red] = await Promise.all([api.publicaciones(orden), api.red()]);
    llenarAutores(red.usuarios);
    dibujar(publicaciones, opciones);
  } catch (e) {
    el.error.textContent = e.message;
  }
}

// ---------------------------------------------------------------- acciones

function cambiarOrden(nuevo) {
  if (nuevo === orden) return;
  orden = nuevo;
  for (const boton of el.orden.querySelectorAll('button')) {
    boton.setAttribute('aria-checked', String(boton.dataset.orden === orden));
  }
  el.criterio.textContent = CRITERIOS[orden];
  refrescar({ animar: true });
}

async function publicar(e) {
  e.preventDefault();
  const error = el.form.querySelector('.error');
  error.textContent = '';
  try {
    const creada = await api.crearPublicacion({
      autor: el.autor.value,
      fechaHora: el.fecha.value,
      reacciones: numero(el.reacciones),
      comentarios: numero(el.comentarios),
    });
    el.reacciones.value = '0';
    el.comentarios.value = '0';
    el.fecha.value = ahora();
    // Se vuelve a pedir la lista con el orden actual y se resalta dónde quedó la nueva
    await refrescar({ animar: true, resaltar: creada.id });
  } catch (err) {
    error.textContent = err.message;
  }
}

async function eliminar(id) {
  el.error.textContent = '';
  try {
    await api.eliminarPublicacion(id);
    await refrescar({ animar: true });
  } catch (e) {
    el.error.textContent = e.message;
  }
}

// ---------------------------------------------------------------- dibujo

function llenarAutores(usuarios) {
  el.autores.replaceChildren(...usuarios.map((u) => {
    const opcion = document.createElement('option');
    opcion.value = u.nombre;
    return opcion;
  }));
}

/** Dibuja las filas en el orden recibido. Con animar, cada fila se desliza a su nuevo lugar (FLIP). */
function dibujar(publicaciones, { animar = false, resaltar = null } = {}) {
  // First: dónde estaba cada fila antes del cambio
  const antes = new Map();
  if (animar) {
    for (const [id, fila] of filas) antes.set(id, fila.getBoundingClientRect().top);
  }

  // La barra de cada fila es proporcional a la relevancia más alta de la lista (solo escala visual)
  const maxima = Math.max(1, ...publicaciones.map((p) => p.relevancia));
  const nuevas = new Map();
  publicaciones.forEach((p, i) => {
    const fila = filas.get(p.id) ?? crearFila(p.id);
    completarFila(fila, p, i + 1, maxima);
    el.lista.appendChild(fila); // appendChild mueve la fila si ya estaba
    nuevas.set(p.id, fila);
  });
  for (const [id, fila] of filas) {
    if (!nuevas.has(id)) fila.remove();
  }
  filas = nuevas;

  el.vacio.hidden = publicaciones.length > 0;
  el.encabezado.hidden = publicaciones.length === 0;

  // Last, Invert, Play: se arranca desde la posición vieja y se anima hasta la nueva
  if (animar) {
    for (const [id, fila] of filas) {
      const top = antes.get(id);
      if (top === undefined) continue;
      const delta = top - fila.getBoundingClientRect().top;
      if (delta === 0) continue;
      fila.animate(
        [{ transform: `translateY(${delta}px)` }, { transform: 'translateY(0)' }],
        { duration: 300, easing: 'cubic-bezier(.2, .7, .3, 1)' },
      );
    }
  }

  if (resaltar != null && filas.has(resaltar)) {
    const fila = filas.get(resaltar);
    fila.classList.remove('nueva');
    void fila.offsetWidth; // reinicia la animación
    fila.classList.add('nueva');
    fila.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
  }
}

function crearFila(id) {
  const fila = document.createElement('li');
  fila.className = 'fila';
  const celda = (clase) => {
    const span = document.createElement('span');
    span.className = clase;
    fila.appendChild(span);
    return span;
  };

  celda('c-pos');
  const autor = celda('c-autor');
  autor.append(document.createElement('span'), document.createElement('span'));
  autor.lastChild.className = 'id';
  celda('c-fecha');
  celda('c-reac');
  celda('c-com');
  const relevancia = celda('c-rel');
  const numeroRel = document.createElement('span');
  numeroRel.className = 'rel-numero';
  const barra = document.createElement('span');
  barra.className = 'rel-barra';
  barra.appendChild(document.createElement('span'));
  relevancia.append(numeroRel, barra);

  const quitar = document.createElement('button');
  quitar.type = 'button';
  quitar.className = 'quitar c-quitar';
  quitar.textContent = '×';
  quitar.addEventListener('click', () => eliminar(id));
  fila.appendChild(quitar);
  return fila;
}

function completarFila(fila, p, posicion, maxima) {
  const [pos, autor, fecha, reac, com, rel, quitar] = fila.children;
  pos.textContent = String(posicion);
  autor.firstChild.textContent = p.autor;
  autor.lastChild.textContent = `#${p.id}`;
  fecha.textContent = formatearFecha(p.fechaHora);
  fecha.title = p.fechaHora;
  reac.textContent = String(p.reacciones);
  reac.dataset.etiqueta = 'reacciones';
  com.textContent = String(p.comentarios);
  com.dataset.etiqueta = 'comentarios';
  rel.firstChild.textContent = String(p.relevancia);
  rel.lastChild.firstChild.style.width = `${(p.relevancia / maxima) * 100}%`;
  quitar.setAttribute('aria-label', `Borrar la publicación #${p.id} de ${p.autor}`);
  quitar.title = 'Borrar';
}

// ---------------------------------------------------------------- fechas (solo formato)

/** "2026-10-16T18:30" -> "vie, 16 oct · 18:30" */
function formatearFecha(iso) {
  const [dia, hora] = iso.split('T');
  const [a, m, d] = dia.split('-').map(Number);
  return `${FORMATO_DIA.format(new Date(a, m - 1, d))} · ${hora.slice(0, 5)}`;
}

/** Ahora en el formato del input datetime-local. */
function ahora() {
  const d = new Date();
  const dos = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}T${dos(d.getHours())}:${dos(d.getMinutes())}`;
}
