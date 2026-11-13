// Vista Moderadores: tabla de costos editable y asignación de menor costo total
// (Branch and Bound). La asignación la calcula la API; acá solo se resalta.
import { api, numero } from './api.js';

let datos = { moderadores: [], comunidades: [], costos: [] };  // respuesta de /api/moderacion
let plan = null;        // respuesta de /api/moderacion/asignacion, o null si no hay solución
const INCOMPATIBLE = 'incompatible';
let costos = new Map(); // `${moderador}>${comunidad}` -> costo o INCOMPATIBLE; falta si no se definió
let pedido = 0;
let el;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    formModerador: $('form-moderador'),
    nombreModerador: $('mod-nombre'),
    limite: $('mod-limite'),
    formComunidad: $('form-comunidad'),
    nombreComunidad: $('com-nombre'),
    costo: $('mod-costo'),
    cubiertas: $('mod-cubiertas'),
    cupo: $('mod-cupo'),
    errorPlan: $('mod-error-plan'),
    matriz: $('mod-matriz'),
    vacio: $('mod-vacio'),
    error: $('mod-error'),
  };

  el.formModerador.addEventListener('submit', (e) => enviar(e, el.formModerador,
    () => api.crearModerador(el.nombreModerador.value, numero(el.limite)),
    () => { el.nombreModerador.value = ''; el.limite.value = '1'; el.nombreModerador.focus(); }));
  el.formComunidad.addEventListener('submit', (e) => enviar(e, el.formComunidad,
    () => api.crearComunidad(el.nombreComunidad.value),
    () => { el.nombreComunidad.value = ''; el.nombreComunidad.focus(); }));
  for (const form of [el.formModerador, el.formComunidad]) {
    form.addEventListener('input', () => { form.querySelector('.error').textContent = ''; });
  }
}

export async function refrescar() {
  el.error.textContent = '';
  try {
    datos = await api.moderacion();
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  dibujarMatriz();
  await calcular();
}

// ---------------------------------------------------------------- carga

async function enviar(e, form, accion, limpiar) {
  e.preventDefault();
  const error = form.querySelector('.error');
  error.textContent = '';
  try {
    await accion();
  } catch (err) {
    error.textContent = err.message;
    return;
  }
  limpiar();
  await refrescar();
}

async function eliminar(accion) {
  el.error.textContent = '';
  try {
    await accion();
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  await refrescar();
}

async function cambiarCosto(input, td, m, c) {
  el.error.textContent = '';
  const clave = `${m.id}>${c.id}`;
  try {
    await api.definirCosto(m.id, c.id, numero(input));
  } catch (e) {
    el.error.textContent = e.message;
    if (costos.get(clave) === INCOMPATIBLE) {
      armarCelda(td, m, c);              // sigue siendo incompatible
    } else {
      input.value = input.dataset.valor; // vuelve al último costo válido
    }
    return;
  }
  costos.set(clave, Number(input.value));
  input.dataset.valor = input.value;
  input.classList.remove('falta');
  if (!td.querySelector('.vetar')) armarCelda(td, m, c);   // venía de incompatible: vuelve el ⊘
  await calcular();
}

async function marcarIncompatible(td, m, c) {
  el.error.textContent = '';
  try {
    await api.definirIncompatible(m.id, c.id);
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  costos.set(`${m.id}>${c.id}`, INCOMPATIBLE);
  armarCelda(td, m, c);
  td.querySelector('button').focus();
  await calcular();
}

/** Una celda incompatible pasa a input vacío; si se deja sin costo, vuelve a ser incompatible. */
function editarIncompatible(td, m, c) {
  const input = crearInput(td, m, c, null);
  input.classList.remove('falta');
  input.addEventListener('blur', () => {
    if (input.value.trim() === '' && costos.get(`${m.id}>${c.id}`) === INCOMPATIBLE) armarCelda(td, m, c);
  });
  td.replaceChildren(input);
  input.focus();
}

async function calcular() {
  const numeroPedido = ++pedido;
  if (datos.comunidades.length === 0 || datos.moderadores.length === 0) {
    plan = null;
    el.errorPlan.textContent = '';
    pintarPlan();
    return;
  }
  try {
    const respuesta = await api.asignacion();
    if (numeroPedido !== pedido) return;
    plan = respuesta;
    el.errorPlan.textContent = '';
  } catch (e) {
    if (numeroPedido !== pedido) return;
    plan = null;
    el.errorPlan.textContent = e.message;
  }
  pintarPlan();
}

// ---------------------------------------------------------------- dibujo

function botonQuitar(etiqueta, accion) {
  const boton = document.createElement('button');
  boton.type = 'button';
  boton.className = 'quitar';
  boton.textContent = '×';
  boton.title = 'Borrar';
  boton.setAttribute('aria-label', etiqueta);
  boton.addEventListener('click', () => eliminar(accion));
  return boton;
}

function dibujarMatriz() {
  const { moderadores, comunidades } = datos;
  const hayAlgo = moderadores.length > 0 || comunidades.length > 0;
  el.vacio.hidden = hayAlgo;
  el.matriz.hidden = !hayAlgo;

  costos = new Map(datos.costos.map((c) =>
    [`${c.moderador}>${c.comunidad}`, c.incompatible ? INCOMPATIBLE : c.costo]));

  const cabecera = document.createElement('tr');
  const esquina = document.createElement('th');
  esquina.className = 'esquina';
  esquina.textContent = 'Moderador';
  cabecera.appendChild(esquina);
  for (const c of comunidades) {
    const th = document.createElement('th');
    th.scope = 'col';
    th.className = 'col-comunidad';
    const nombre = document.createElement('span');
    nombre.textContent = c.nombre;
    th.append(nombre, botonQuitar(`Borrar la comunidad ${c.nombre}`, () => api.eliminarComunidad(c.id)));
    cabecera.appendChild(th);
  }
  for (const texto of ['Asignadas', 'Costo']) {
    const th = document.createElement('th');
    th.scope = 'col';
    th.className = 'col-resumen';
    th.textContent = texto;
    cabecera.appendChild(th);
  }
  el.matriz.tHead.replaceChildren(cabecera);

  el.matriz.tBodies[0].replaceChildren(...moderadores.map((m) => {
    const fila = document.createElement('tr');
    fila.dataset.moderador = String(m.id);

    const th = document.createElement('th');
    th.scope = 'row';
    const nombre = document.createElement('span');
    nombre.className = 'mod-nombre';
    nombre.textContent = m.nombre;
    const limite = document.createElement('span');
    limite.className = 'mod-limite';
    limite.textContent = m.limite === 1 ? 'hasta 1 comunidad' : `hasta ${m.limite} comunidades`;
    const textos = document.createElement('span');
    textos.className = 'mod-textos';
    textos.append(nombre, limite);
    const contenido = document.createElement('div');
    contenido.className = 'mod-cabecera';
    contenido.append(textos, botonQuitar(`Borrar al moderador ${m.nombre}`, () => api.eliminarModerador(m.id)));
    th.appendChild(contenido);
    fila.appendChild(th);

    for (const c of comunidades) {
      const td = document.createElement('td');
      td.dataset.comunidad = String(c.id);
      armarCelda(td, m, c);
      fila.appendChild(td);
    }

    const asignadas = document.createElement('td');
    asignadas.className = 'resumen asignadas';
    const costoFila = document.createElement('td');
    costoFila.className = 'resumen costo-fila';
    fila.append(asignadas, costoFila);
    return fila;
  }));
}

function crearInput(td, m, c, valor) {
  const input = document.createElement('input');
  input.type = 'number';
  input.min = '0';
  input.step = '1';
  input.value = valor == null ? '' : String(valor);
  input.dataset.valor = input.value;
  input.classList.toggle('falta', valor == null);
  input.setAttribute('aria-label', `Costo de ${m.nombre} para ${c.nombre}`);
  input.addEventListener('change', () => cambiarCosto(input, td, m, c));
  return input;
}

/** Contenido de la celda moderador × comunidad: el costo editable, o la marca de incompatible. */
function armarCelda(td, m, c) {
  const valor = costos.get(`${m.id}>${c.id}`);
  const incompatible = valor === INCOMPATIBLE;
  td.classList.toggle('incompatible', incompatible);

  if (incompatible) {
    const marca = document.createElement('button');
    marca.type = 'button';
    marca.className = 'marca-incompatible';
    marca.textContent = 'no puede';
    marca.title = 'Incompatible: el algoritmo nunca le asigna esta comunidad. Click para ponerle un costo';
    marca.setAttribute('aria-label', `${m.nombre} no puede atender ${c.nombre}. Asignarle un costo`);
    marca.addEventListener('click', () => editarIncompatible(td, m, c));
    td.replaceChildren(marca);
    return;
  }

  const vetar = document.createElement('button');
  vetar.type = 'button';
  vetar.className = 'vetar';
  vetar.textContent = '⊘';
  vetar.title = 'Marcar como incompatible';
  vetar.setAttribute('aria-label', `Marcar que ${m.nombre} no puede atender ${c.nombre}`);
  vetar.addEventListener('click', () => marcarIncompatible(td, m, c));
  td.replaceChildren(crearInput(td, m, c, valor), vetar);
}

function pintarPlan() {
  const { moderadores, comunidades } = datos;
  const cupo = moderadores.reduce((suma, m) => suma + m.limite, 0);
  el.cupo.textContent = moderadores.length ? String(cupo) : '–';
  el.costo.textContent = plan ? String(plan.costoTotal) : '–';
  el.cubiertas.textContent = comunidades.length
    ? `${plan ? comunidades.length : 0} de ${comunidades.length}` : '–';

  const porModerador = new Map((plan?.asignaciones ?? []).map((a) => [a.moderador, a]));
  for (const fila of el.matriz.tBodies[0].rows) {
    const m = moderadores.find((x) => x.id === Number(fila.dataset.moderador));
    const asignacion = porModerador.get(m.id);
    const elegidas = new Set(asignacion?.comunidades ?? []);

    for (const td of fila.querySelectorAll('td[data-comunidad]')) {
      td.classList.toggle('asignada', elegidas.has(Number(td.dataset.comunidad)));
    }
    fila.querySelector('.asignadas').textContent = plan ? `${elegidas.size} / ${m.limite}` : '';
    fila.querySelector('.costo-fila').textContent = plan ? String(asignacion?.costo ?? 0) : '';
    fila.classList.toggle('sin-asignar', Boolean(plan) && elegidas.size === 0);
  }
}
