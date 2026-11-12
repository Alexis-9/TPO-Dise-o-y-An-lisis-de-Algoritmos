// Vista Alternativas: todas las cadenas de difusión que respetan un tiempo máximo, un límite de
// intermediarios y un conjunto de excluidos (Backtracking). Las cadenas las encuentra la API;
// acá solo se ordenan para mostrarlas y se elige cuál se resalta en el grafo.
import { api, numero } from './api.js';
import { Grafo } from './grafo.js';

const ESPERA = 200; // debounce de los campos numéricos, en ms

const estado = {
  usuarios: [],
  vinculos: [],
  origen: null,
  destino: null,
  excluidos: new Set(),
  resultado: null,    // respuesta de /api/cadenas
  elegida: null,      // posición (en resultado.cadenas) de la cadena resaltada
  pedido: 0,
};

let grafo;
let el;
let temporizador = null;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    ayuda: $('alt-ayuda'),
    datos: $('alt-datos'),
    cadena: $('alt-cadena'),
    tiempo: $('alt-tiempo'),
    tiempoNumero: $('alt-tiempo-numero'),
    detalle: $('alt-detalle'),
    vacio: $('alt-vacio'),
    origen: $('alt-origen'),
    destino: $('alt-destino'),
    tiempoMax: $('alt-tiempo-max'),
    intermediarios: $('alt-intermediarios'),
    excluidos: $('alt-excluidos'),
    error: $('alt-error'),
    resultados: $('alt-resultados'),
    tituloLista: $('alt-titulo-lista'),
    lista: $('alt-lista'),
  };
  grafo = new Grafo($('alt-grafo'), { alClickNodo });

  el.origen.addEventListener('change', () => elegirExtremos(valorDe(el.origen), estado.destino));
  el.destino.addEventListener('change', () => elegirExtremos(estado.origen, valorDe(el.destino)));
  for (const input of [el.tiempoMax, el.intermediarios]) {
    input.addEventListener('input', () => {
      clearTimeout(temporizador);
      temporizador = setTimeout(calcular, ESPERA);
    });
  }
}

export async function refrescar() {
  let red;
  try {
    red = await api.red();
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  estado.usuarios = red.usuarios;
  estado.vinculos = red.vinculos;

  const ids = new Set(estado.usuarios.map((u) => u.id));
  if (!ids.has(estado.origen)) estado.origen = null;
  if (!ids.has(estado.destino)) estado.destino = null;
  for (const id of estado.excluidos) if (!ids.has(id)) estado.excluidos.delete(id);

  el.vacio.hidden = estado.usuarios.length > 0;
  llenarSelects();
  dibujarExcluidos();
  grafo.actualizar(estado.usuarios, estado.vinculos);
  await calcular();
}

// ---------------------------------------------------------------- entrada

function valorDe(select) {
  return select.value === '' ? null : Number(select.value);
}

function nombreDe(id) {
  return estado.usuarios.find((u) => u.id === id)?.nombre ?? '';
}

function opcion(valor, texto) {
  const o = document.createElement('option');
  o.value = String(valor);
  o.textContent = texto;
  return o;
}

function llenarSelects() {
  for (const select of [el.origen, el.destino]) {
    select.replaceChildren(opcion('', 'Elegí…'), ...estado.usuarios.map((u) => opcion(u.id, u.nombre)));
    select.disabled = estado.usuarios.length === 0;
  }
  el.origen.value = estado.origen ?? '';
  el.destino.value = estado.destino ?? '';
}

function elegirExtremos(origen, destino) {
  estado.origen = origen;
  estado.destino = destino;
  // Origen y destino no pueden estar excluidos: no habría ninguna cadena
  estado.excluidos.delete(origen);
  estado.excluidos.delete(destino);
  dibujarExcluidos();
  calcular();
}

function alClickNodo(id) {
  if (id === estado.origen || id === estado.destino) return;
  if (estado.excluidos.has(id)) estado.excluidos.delete(id);
  else estado.excluidos.add(id);
  dibujarExcluidos();
  calcular();
}

/** Un checkbox por usuario que no sea el origen ni el destino. */
function dibujarExcluidos() {
  const candidatos = estado.usuarios.filter((u) => u.id !== estado.origen && u.id !== estado.destino);
  el.excluidos.replaceChildren(...candidatos.map((u) => {
    const label = document.createElement('label');
    label.className = 'excluido-opcion';
    const check = document.createElement('input');
    check.type = 'checkbox';
    check.checked = estado.excluidos.has(u.id);
    check.addEventListener('change', () => {
      if (check.checked) estado.excluidos.add(u.id);
      else estado.excluidos.delete(u.id);
      calcular();
    });
    const nombre = document.createElement('span');
    nombre.textContent = u.nombre;
    label.append(check, nombre);
    return label;
  }));
}

// ---------------------------------------------------------------- cálculo

async function calcular() {
  clearTimeout(temporizador);
  const numeroPedido = ++estado.pedido;

  if (estado.origen == null || estado.destino == null) {
    estado.resultado = null;
    el.error.textContent = '';
    pintar();
    return;
  }

  try {
    // Los números se mandan tal cual: si no sirven (vacío, negativo) lo dice el servidor
    const resultado = await api.cadenas({
      origen: estado.origen,
      destino: estado.destino,
      tiempoMax: numero(el.tiempoMax),
      intermediarios: numero(el.intermediarios),
      excluidos: [...estado.excluidos],
    });
    if (numeroPedido !== estado.pedido) return;
    estado.resultado = resultado;
    estado.elegida = resultado.mejor;
    el.error.textContent = '';
  } catch (e) {
    if (numeroPedido !== estado.pedido) return;
    estado.resultado = null;
    el.error.textContent = e.message;
  }
  pintar();
}

// ---------------------------------------------------------------- dibujo

function pintar() {
  pintarEncabezado();
  pintarLista();
  grafo.pintar(estiloGrafo());
}

function cadenaElegida() {
  const r = estado.resultado;
  return r && estado.elegida != null ? r.cadenas[estado.elegida] : null;
}

function estiloGrafo() {
  const marcas = (id) => [
    id === estado.origen ? 'origen' : '',
    id === estado.destino ? 'destino' : '',
    estado.excluidos.has(id) ? 'excluido' : '',
  ];
  const c = cadenaElegida();
  if (!c) return { nodo: (id) => marcas(id).join(' ') };

  // Tiempo acumulado en cada usuario de la cadena, con los minutos de cada vínculo
  const minutos = new Map(estado.vinculos.map((v) => [`${v.origen}>${v.destino}`, v.minutos]));
  const acumulado = new Map();
  let total = 0;
  c.usuarios.forEach((u, i) => {
    if (i > 0) total += minutos.get(`${c.usuarios[i - 1].id}>${u.id}`) ?? 0;
    acumulado.set(u.id, total);
  });

  const camino = c.usuarios.map((u) => u.id);
  const tramos = new Set(camino.slice(1).map((id, i) => `${camino[i]}>${id}`));
  return {
    nodo: (id) => [...marcas(id), acumulado.has(id) ? 'en-camino' : 'tenue'].join(' '),
    etiqueta: (id) => (acumulado.has(id) ? { texto: acumulado.get(id), clase: 'fija' } : null),
    arista: (o, d) => (tramos.has(`${o}>${d}`) ? 'en-camino' : 'tenue'),
  };
}

function ruta(usuarios) {
  const partes = [];
  usuarios.forEach((u, i) => {
    if (i > 0) {
      const flecha = document.createElement('span');
      flecha.className = 'flecha';
      flecha.textContent = '→';
      partes.push(flecha);
    }
    const nombre = document.createElement('span');
    nombre.textContent = u.nombre;
    partes.push(nombre);
  });
  return partes;
}

function textoIntermediarios(n) {
  return n === 0 ? 'directa' : n === 1 ? '1 intermediario' : `${n} intermediarios`;
}

function pintarEncabezado() {
  const r = estado.resultado;
  const c = cadenaElegida();
  el.ayuda.hidden = Boolean(c);
  el.datos.hidden = !c;

  if (!c) {
    el.ayuda.textContent = estado.usuarios.length === 0 ? ''
      : estado.origen == null || estado.destino == null ? 'Elegí el origen y el destino en el panel.'
        : r ? `Ninguna cadena de ${nombreDe(estado.origen)} a ${nombreDe(estado.destino)} cumple las condiciones.`
          : '';
    return;
  }

  el.cadena.replaceChildren(...ruta(c.usuarios));
  el.tiempoNumero.textContent = String(c.tiempoTotal);
  el.detalle.textContent = estado.elegida === r.mejor
    ? `La de menor tiempo de las ${r.cadenas.length} válidas · ${textoIntermediarios(c.intermediarios)}.`
    : `${textoIntermediarios(c.intermediarios)} · ${c.tiempoTotal - r.cadenas[r.mejor].tiempoTotal} min más que la más rápida.`;
}

function pintarLista() {
  const r = estado.resultado;
  el.resultados.hidden = !r || r.cadenas.length === 0;
  if (el.resultados.hidden) {
    el.lista.replaceChildren();
    return;
  }

  el.tituloLista.textContent = r.cadenas.length === 1 ? '1 cadena válida' : `${r.cadenas.length} cadenas válidas`;

  // Se muestran de la más rápida a la más lenta (a igual tiempo, la de menos usuarios)
  const orden = r.cadenas.map((c, i) => ({ c, i }))
    .sort((a, b) => a.c.tiempoTotal - b.c.tiempoTotal || a.c.usuarios.length - b.c.usuarios.length);

  el.lista.replaceChildren(...orden.map(({ c, i }) => {
    const li = document.createElement('li');
    const boton = document.createElement('button');
    boton.type = 'button';
    boton.className = 'cadena-fila';
    boton.classList.toggle('mejor', i === r.mejor);
    boton.setAttribute('aria-pressed', String(i === estado.elegida));

    const texto = document.createElement('span');
    texto.className = 'cadena-ruta';
    texto.append(...ruta(c.usuarios));
    const tiempo = document.createElement('span');
    tiempo.className = 'cadena-tiempo';
    tiempo.textContent = `${c.tiempoTotal} min`;
    const detalle = document.createElement('span');
    detalle.className = 'cadena-detalle';
    detalle.textContent = i === r.mejor
      ? `más rápida · ${textoIntermediarios(c.intermediarios)}`
      : textoIntermediarios(c.intermediarios);

    boton.append(texto, tiempo, detalle);
    boton.addEventListener('click', () => {
      estado.elegida = i;
      pintar();
    });
    li.appendChild(boton);
    return li;
  }));
}
