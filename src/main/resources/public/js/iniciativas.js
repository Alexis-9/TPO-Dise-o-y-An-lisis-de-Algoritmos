// Vista Iniciativas: el presupuesto se elige acá y la selección (mochila 0/1) la calcula la API.
import { api, numero } from './api.js';

const ESPERA = 150;            // debounce del slider, en ms
const PRESUPUESTO_INICIAL = 50;

let iniciativas = [];          // como las devuelve la API, por id
let maximo = 0;                // tope del slider: suma de los costos
let temporizador = null;
let pedido = 0;                // número del último pedido de selección, para descartar respuestas viejas
let segmentos = new Map();     // id -> segmento de la barra
let el;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    rango: $('presupuesto-rango'),
    numero: $('presupuesto-numero'),
    errorPresupuesto: $('error-presupuesto'),
    form: $('form-iniciativa'),
    costo: $('ini-costo'),
    impacto: $('ini-impacto'),
    impactoTotal: $('m-impacto'),
    costoTotal: $('m-costo'),
    restante: $('m-restante'),
    barra: $('barra-presupuesto'),
    escalaMax: $('escala-max'),
    tarjetas: $('tarjetas'),
    vacio: $('ini-vacio'),
    error: $('ini-error'),
  };

  el.rango.addEventListener('input', () => {
    el.numero.value = el.rango.value;
    programarCalculo();
  });
  el.numero.addEventListener('input', () => {
    if (el.numero.value !== '') el.rango.value = el.numero.value;
    programarCalculo();
  });

  el.form.addEventListener('submit', agregar);
  el.form.addEventListener('input', () => { el.form.querySelector('.error').textContent = ''; });
}

export async function refrescar() {
  el.error.textContent = '';
  try {
    iniciativas = await api.iniciativas();
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  actualizarMaximo();
  dibujarTarjetas();
  await calcular();
}

// ---------------------------------------------------------------- presupuesto

function actualizarMaximo() {
  const anterior = maximo;
  // El slider va de 0 a la suma de todos los costos (más presupuesto no cambia la elección)
  maximo = iniciativas.reduce((suma, i) => suma + i.costo, 0);
  el.rango.max = String(maximo);
  el.rango.disabled = maximo === 0;

  const actual = Number(el.numero.value);
  if (anterior === 0 && maximo > 0) {
    fijarPresupuesto(Math.min(PRESUPUESTO_INICIAL, maximo));
  } else if (actual > maximo || el.numero.value === '') {
    fijarPresupuesto(maximo);
  } else {
    el.rango.value = el.numero.value;
  }
}

function fijarPresupuesto(valor) {
  el.rango.value = String(valor);
  el.numero.value = String(valor);
}

function programarCalculo() {
  clearTimeout(temporizador);
  temporizador = setTimeout(calcular, ESPERA);
}

async function calcular() {
  clearTimeout(temporizador);
  const numeroPedido = ++pedido;
  try {
    // Se manda tal cual lo escrito: si no sirve (vacío, negativo) lo dice el servidor
    const seleccion = await api.seleccion(el.numero.value);
    if (numeroPedido !== pedido) return;
    el.errorPresupuesto.textContent = '';
    dibujarSeleccion(seleccion);
  } catch (e) {
    if (numeroPedido !== pedido) return;
    el.errorPresupuesto.textContent = e.message;
  }
}

// ---------------------------------------------------------------- carga

async function agregar(e) {
  e.preventDefault();
  const error = el.form.querySelector('.error');
  error.textContent = '';
  try {
    await api.crearIniciativa(numero(el.costo), numero(el.impacto));
    el.costo.value = '';
    el.impacto.value = '';
    el.costo.focus();
    await refrescar();
  } catch (err) {
    error.textContent = err.message;
  }
}

async function eliminar(id) {
  el.error.textContent = '';
  try {
    await api.eliminarIniciativa(id);
    await refrescar();
  } catch (e) {
    el.error.textContent = e.message;
  }
}

// ---------------------------------------------------------------- dibujo

function dibujarTarjetas() {
  el.vacio.hidden = iniciativas.length > 0;
  el.tarjetas.replaceChildren(...iniciativas.map((i) => {
    const tarjeta = document.createElement('article');
    tarjeta.className = 'tarjeta';
    tarjeta.dataset.id = String(i.id);

    const cabeza = document.createElement('div');
    cabeza.className = 'tarjeta-cabeza';
    const id = document.createElement('span');
    id.className = 'tarjeta-id';
    id.textContent = `#${i.id}`;
    const quitar = document.createElement('button');
    quitar.type = 'button';
    quitar.className = 'quitar';
    quitar.textContent = '×';
    quitar.title = 'Borrar';
    quitar.setAttribute('aria-label', `Borrar la iniciativa #${i.id}`);
    quitar.addEventListener('click', () => eliminar(i.id));
    cabeza.append(id, quitar);

    const datos = document.createElement('dl');
    for (const [nombre, valor] of [['Costo', i.costo], ['Impacto', i.impacto]]) {
      const fila = document.createElement('div');
      const dt = document.createElement('dt');
      dt.textContent = nombre;
      const dd = document.createElement('dd');
      dd.textContent = String(valor);
      fila.append(dt, dd);
      datos.appendChild(fila);
    }

    tarjeta.append(cabeza, datos);
    return tarjeta;
  }));
}

function dibujarSeleccion(seleccion) {
  el.impactoTotal.textContent = String(seleccion.impactoTotal);
  el.costoTotal.textContent = String(seleccion.costoTotal);
  el.restante.textContent = String(seleccion.restante);

  const elegidas = new Set(seleccion.seleccionadas);
  for (const tarjeta of el.tarjetas.children) {
    tarjeta.classList.toggle('elegida', elegidas.has(Number(tarjeta.dataset.id)));
  }
  dibujarBarra(seleccion);
}

/** El ancho total es el presupuesto; cada elegida ocupa lo proporcional a su costo. */
function dibujarBarra(seleccion) {
  const presupuesto = seleccion.presupuesto;
  el.escalaMax.textContent = `presupuesto ${presupuesto}`;
  el.barra.setAttribute('aria-label',
    `Presupuesto ${presupuesto}: elegidas ${seleccion.seleccionadas.map((id) => '#' + id).join(', ') || 'ninguna'}, `
    + `restante ${seleccion.restante}`);
  el.barra.classList.toggle('sin-presupuesto', presupuesto === 0);

  const costos = new Map(iniciativas.map((i) => [i.id, i.costo]));
  const nuevos = new Map();
  for (const id of seleccion.seleccionadas) {
    let segmento = segmentos.get(id);
    if (!segmento) {
      segmento = document.createElement('div');
      segmento.className = 'segmento';
      segmento.style.width = '0%';
      const texto = document.createElement('span');
      texto.textContent = `#${id}`;
      segmento.appendChild(texto);
    }
    el.barra.appendChild(segmento); // respeta el orden de la API
    nuevos.set(id, segmento);
  }
  for (const [id, segmento] of segmentos) {
    if (!nuevos.has(id)) segmento.remove();
  }
  segmentos = nuevos;

  let restante = el.barra.querySelector('.restante');
  if (!restante) {
    restante = document.createElement('div');
    restante.className = 'restante';
    restante.appendChild(document.createElement('span'));
  }
  el.barra.appendChild(restante);
  restante.firstChild.textContent = presupuesto === 0 ? 'Sin presupuesto'
    : seleccion.restante > 0 ? `restante ${seleccion.restante}` : '';

  // Se fuerza el cálculo del layout para que los segmentos nuevos crezcan desde 0
  el.barra.getBoundingClientRect();
  for (const [id, segmento] of segmentos) {
    segmento.style.width = presupuesto > 0 ? `${(costos.get(id) / presupuesto) * 100}%` : '0%';
  }
}
