// Vista Consultas: tiempos mínimos entre todos los pares (Floyd). La API corre Floyd una vez y
// guarda las matrices; la tabla y cada consulta salen de ahí sin volver a calcular.
import { api } from './api.js';
import { Grafo } from './grafo.js';

const estado = {
  usuarios: [],
  vinculos: [],
  tabla: null,        // respuesta de /api/consultas/tabla
  origen: null,
  destino: null,
  resultado: null,    // respuesta de /api/consultas
  pedido: 0,          // número del último pedido, para descartar respuestas viejas
};

let grafo;
let el;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    ayuda: $('cons-ayuda'),
    datos: $('cons-datos'),
    cadena: $('cons-cadena'),
    tiempo: $('cons-tiempo'),
    tiempoNumero: $('cons-tiempo-numero'),
    detalle: $('cons-detalle'),
    estado: $('cons-estado'),
    matriz: $('cons-matriz'),
    error: $('cons-error'),
    vacio: $('cons-vacio'),
  };
  grafo = new Grafo($('cons-grafo'), { alClickNodo });
}

export async function refrescar() {
  el.error.textContent = '';
  let red;
  let tabla;
  try {
    [red, tabla] = await Promise.all([api.red(), api.tablaConsultas()]);
  } catch (e) {
    el.error.textContent = e.message;
    return;
  }
  estado.usuarios = red.usuarios;
  estado.vinculos = red.vinculos;
  estado.tabla = tabla;

  const ids = new Set(estado.usuarios.map((u) => u.id));
  if (!ids.has(estado.origen) || (estado.destino != null && !ids.has(estado.destino))) {
    estado.origen = null;
    estado.destino = null;
    estado.resultado = null;
  }

  el.vacio.hidden = estado.usuarios.length > 0;
  grafo.actualizar(estado.usuarios, estado.vinculos);
  dibujarMatriz();
  pintarEstado();

  if (estado.origen != null && estado.destino != null) await consultar(estado.origen, estado.destino);
  else pintar();
}

// ---------------------------------------------------------------- consulta

function alClickNodo(id) {
  // En el grafo: primer click origen, segundo destino, tercero reinicia
  if (estado.origen == null || estado.destino != null) {
    estado.origen = id;
    estado.destino = null;
    estado.resultado = null;
    estado.pedido++;
    pintar();
  } else {
    consultar(estado.origen, id);
  }
}

async function consultar(origen, destino) {
  estado.origen = origen;
  estado.destino = destino;
  const numero = ++estado.pedido;
  try {
    const resultado = await api.consulta(origen, destino);
    if (numero !== estado.pedido) return;
    estado.resultado = resultado;
    el.error.textContent = '';
  } catch (e) {
    if (numero !== estado.pedido) return;
    estado.resultado = null;
    el.error.textContent = e.message;
  }
  pintar();
}

// ---------------------------------------------------------------- dibujo

function nombreDe(id) {
  return estado.usuarios.find((u) => u.id === id)?.nombre ?? '';
}

function pintarEstado() {
  const n = estado.tabla.usuarios.length;
  el.estado.textContent = n === 0 ? ''
    : estado.tabla.recalculada
      ? `Floyd se volvió a calcular porque la red cambió (${n} usuarios, ${estado.vinculos.length} vínculos).`
      : `Floyd ya estaba calculado para esta red: la tabla sale de las matrices guardadas, sin recalcular.`;
}

function dibujarMatriz() {
  const { usuarios, tiempos } = estado.tabla;
  const [thead, tbody] = [el.matriz.tHead, el.matriz.tBodies[0]];
  el.matriz.hidden = usuarios.length === 0;

  const cabecera = document.createElement('tr');
  const esquina = document.createElement('th');
  esquina.className = 'esquina';
  esquina.innerHTML = '<span>desde</span><span>hacia</span>';
  cabecera.appendChild(esquina);
  usuarios.forEach((u, j) => {
    const th = document.createElement('th');
    th.scope = 'col';
    th.textContent = u.nombre;
    th.dataset.columna = String(j);
    cabecera.appendChild(th);
  });
  thead.replaceChildren(cabecera);

  tbody.replaceChildren(...usuarios.map((origen, i) => {
    const fila = document.createElement('tr');
    const th = document.createElement('th');
    th.scope = 'row';
    th.textContent = origen.nombre;
    fila.appendChild(th);

    usuarios.forEach((destino, j) => {
      const td = document.createElement('td');
      const tiempo = tiempos[i][j];
      if (i === j) {
        td.className = 'diagonal';
        td.textContent = '0';
      } else {
        const boton = document.createElement('button');
        boton.type = 'button';
        boton.className = 'celda';
        boton.dataset.origen = String(origen.id);
        boton.dataset.destino = String(destino.id);
        boton.textContent = tiempo == null ? '—' : String(tiempo);
        boton.classList.toggle('sin-cadena', tiempo == null);
        boton.setAttribute('aria-label', tiempo == null
          ? `${origen.nombre} a ${destino.nombre}: sin cadena`
          : `${origen.nombre} a ${destino.nombre}: ${tiempo} minutos`);
        boton.addEventListener('click', () => consultar(origen.id, destino.id));
        boton.addEventListener('mouseenter', () => marcarCruz(i, j));
        boton.addEventListener('mouseleave', () => marcarCruz(null, null));
        td.appendChild(boton);
      }
      fila.appendChild(td);
    });
    return fila;
  }));
}

/** Resalta la fila y la columna de la celda bajo el mouse. */
function marcarCruz(i, j) {
  const filas = el.matriz.tBodies[0].rows;
  for (let f = 0; f < filas.length; f++) filas[f].cells[0].classList.toggle('cruz', f === i);
  for (const th of el.matriz.tHead.querySelectorAll('th[data-columna]')) {
    th.classList.toggle('cruz', Number(th.dataset.columna) === j);
  }
}

function pintar() {
  for (const celda of el.matriz.querySelectorAll('.celda')) {
    celda.classList.toggle('elegida', Number(celda.dataset.origen) === estado.origen
      && Number(celda.dataset.destino) === estado.destino);
  }
  pintarEncabezado();
  grafo.pintar(estiloGrafo());
}

function estiloGrafo() {
  const r = estado.resultado;
  const marcas = (id) => [id === estado.origen ? 'origen' : '', id === estado.destino ? 'destino' : ''];
  if (!r) return { nodo: (id) => marcas(id).join(' ') };
  if (!r.alcanzable) return { nodo: (id) => [...marcas(id), 'tenue'].join(' '), arista: () => 'tenue' };

  const camino = r.cadena.map((u) => u.id);
  const enCamino = new Set(camino);
  const tramos = new Set(camino.slice(1).map((id, i) => `${camino[i]}>${id}`));
  return {
    nodo: (id) => [...marcas(id), enCamino.has(id) ? 'en-camino' : 'tenue'].join(' '),
    arista: (o, d) => (tramos.has(`${o}>${d}`) ? 'en-camino' : 'tenue'),
  };
}

function pintarEncabezado() {
  const r = estado.resultado;
  el.ayuda.hidden = Boolean(r);
  el.datos.hidden = !r;

  if (!r) {
    el.ayuda.textContent = estado.usuarios.length === 0 ? ''
      : estado.origen == null ? 'Elegí un par: click en una celda de la tabla, o en dos usuarios del grafo.'
        : `Origen: ${nombreDe(estado.origen)}. Ahora elegí el destino en el grafo.`;
    return;
  }

  if (r.alcanzable) {
    const partes = [];
    r.cadena.forEach((u, i) => {
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
    el.cadena.replaceChildren(...partes);
    el.tiempoNumero.textContent = String(r.tiempoTotal);
    el.tiempo.hidden = false;
    el.detalle.textContent = r.cadena.length === 1
      ? 'Origen y destino son el mismo usuario.'
      : 'Respondido con las matrices de Floyd: la cadena se reconstruye siguiendo el siguiente salto guardado para cada par.';
  } else {
    el.cadena.textContent = `No existe una cadena de ${r.origen.nombre} a ${r.destino.nombre}`;
    el.tiempo.hidden = true;
    el.detalle.textContent = 'Ningún recorrido de vínculos lleva del origen al destino.';
  }
}
