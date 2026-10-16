// Vista Difusión: grafo de la red y cadena más rápida (Dijkstra) con paso a paso.
// Todo resultado sale de la API; acá solo se decide qué se ve en cada momento.
import { api } from './api.js';
import { Grafo } from './grafo.js';

const VELOCIDAD = 1100; // ms entre pasos al reproducir

const estado = {
  modo: 'consultar',
  usuarios: [],
  vinculos: [],
  origen: null,
  destino: null,
  resultado: null,     // respuesta de /api/difusion
  cuadro: 0,           // 0 = inicio, 1..n = después de cada paso, n + 1 = resultado
  reproduccion: null,
  consulta: 0,         // número del último pedido, para descartar respuestas viejas
};

let grafo;
let el;

export function iniciar() {
  const $ = (id) => document.getElementById(id);
  el = {
    svg: $('grafo'),
    lienzo: $('lienzo'),
    vacio: $('grafo-vacio'),
    ayuda: $('resultado-ayuda'),
    datos: $('resultado-datos'),
    cadena: $('cadena'),
    tiempo: $('tiempo'),
    tiempoNumero: $('tiempo-numero'),
    detalle: $('resultado-detalle'),
    modo: $('modo'),
    panelConsultar: $('panel-consultar'),
    panelEditar: $('panel-editar'),
    selectOrigen: $('select-origen'),
    selectDestino: $('select-destino'),
    errorConsulta: $('error-consulta'),
    pasos: $('pasos'),
    contador: $('pasos-contador'),
    barra: $('pasos-barra'),
    reiniciar: $('paso-reiniciar'),
    anterior: $('paso-anterior'),
    siguiente: $('paso-siguiente'),
    reproducir: $('paso-reproducir'),
    decision: $('decision'),
    traza: $('traza'),
  };

  grafo = new Grafo(el.svg, { alClickNodo, alClickArista, alClickFondo });

  for (const boton of el.modo.querySelectorAll('button')) {
    boton.addEventListener('click', () => cambiarModo(boton.dataset.modo));
  }

  el.selectOrigen.addEventListener('change', () => elegir(valorDe(el.selectOrigen), estado.destino));
  el.selectDestino.addEventListener('change', () => elegir(estado.origen, valorDe(el.selectDestino)));

  el.reiniciar.addEventListener('click', () => { pausar(); irA(0); });
  el.anterior.addEventListener('click', () => { pausar(); irA(estado.cuadro - 1); });
  el.siguiente.addEventListener('click', () => { pausar(); irA(estado.cuadro + 1); });
  el.reproducir.addEventListener('click', alternarReproduccion);
}

export async function refrescar() {
  let red;
  try {
    red = await api.red();
  } catch (e) {
    el.errorConsulta.textContent = e.message;
    return;
  }
  estado.usuarios = red.usuarios;
  estado.vinculos = red.vinculos;

  const ids = new Set(estado.usuarios.map((u) => u.id));
  if (!ids.has(estado.origen)) estado.origen = null;
  if (!ids.has(estado.destino)) estado.destino = null;

  llenarSelects();
  el.vacio.hidden = estado.usuarios.length > 0;
  const animacion = grafo.actualizar(estado.usuarios, estado.vinculos);

  if (estado.modo === 'consultar' && estado.origen != null && estado.destino != null) {
    consultar();
  } else {
    estado.resultado = null;
    pintar();
  }
  return animacion;
}

export function ocultar() {
  pausar();
  pintarPasos();
}

// ---------------------------------------------------------------- utilidades

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
  for (const select of [el.selectOrigen, el.selectDestino]) {
    select.replaceChildren(opcion('', 'Elegí…'), ...estado.usuarios.map((u) => opcion(u.id, u.nombre)));
    select.disabled = estado.usuarios.length === 0;
  }
  el.selectOrigen.value = estado.origen ?? '';
  el.selectDestino.value = estado.destino ?? '';
}

// ---------------------------------------------------------------- modo

function cambiarModo(modo) {
  if (modo === estado.modo) return;
  estado.modo = modo;
  for (const boton of el.modo.querySelectorAll('button')) {
    boton.setAttribute('aria-checked', String(boton.dataset.modo === modo));
  }
  el.panelConsultar.hidden = modo !== 'consultar';
  el.panelEditar.hidden = modo !== 'editar';
  el.lienzo.classList.toggle('editando', modo === 'editar');
  pausar();

  if (modo === 'consultar' && estado.origen != null && estado.destino != null) {
    consultar();
  } else {
    pintar();
  }
}

// ---------------------------------------------------------------- clicks en el grafo

function alClickNodo(id) {
  if (estado.modo !== 'consultar') return;
  // Primer click: origen. Segundo: destino. Tercero: reinicia.
  if (estado.origen == null) elegir(id, null);
  else if (estado.destino == null) elegir(estado.origen, id);
  else elegir(null, null);
}

function alClickArista() { }

function alClickFondo() { }

// ---------------------------------------------------------------- consulta

function elegir(origen, destino) {
  pausar();
  estado.origen = origen;
  estado.destino = destino;
  estado.resultado = null;
  estado.consulta++;
  el.selectOrigen.value = origen ?? '';
  el.selectDestino.value = destino ?? '';
  el.errorConsulta.textContent = '';

  if (origen != null && destino != null) consultar();
  else pintar();
}

async function consultar() {
  const numero = ++estado.consulta;
  pausar();
  try {
    const resultado = await api.difusion(estado.origen, estado.destino);
    if (numero !== estado.consulta) return;
    estado.resultado = resultado;
    estado.cuadro = ultimoCuadro();
    el.errorConsulta.textContent = '';
  } catch (e) {
    if (numero !== estado.consulta) return;
    estado.resultado = null;
    el.errorConsulta.textContent = e.message;
  }
  pintar();
}

// ---------------------------------------------------------------- paso a paso

function ultimoCuadro() {
  return estado.resultado.pasos.length + 1;
}

function irA(cuadro) {
  if (!estado.resultado) return;
  estado.cuadro = Math.max(0, Math.min(ultimoCuadro(), cuadro));
  pintar();

  const paso = estado.resultado.pasos[estado.cuadro - 1];
  if (paso && estado.cuadro < ultimoCuadro()) {
    grafo.destellar(paso.actualizaciones.map((a) => [paso.elegido, a.usuario]));
  }
}

function alternarReproduccion() {
  if (estado.reproduccion) {
    pausar();
    pintarPasos();
    return;
  }
  if (estado.cuadro >= ultimoCuadro()) irA(0);
  estado.reproduccion = setInterval(() => {
    irA(estado.cuadro + 1);
    if (estado.cuadro >= ultimoCuadro()) {
      pausar();
      pintarPasos();
    }
  }, VELOCIDAD);
  pintarPasos();
}

function pausar() {
  clearInterval(estado.reproduccion);
  estado.reproduccion = null;
}

// ---------------------------------------------------------------- dibujo

function pintar() {
  grafo.pintar(estiloActual());
  pintarEncabezado();
  pintarPasos();
}

function estiloActual() {
  const r = estado.resultado;
  if (!r) return estiloSeleccion();
  if (estado.cuadro >= ultimoCuadro()) return r.alcanzable ? estiloCamino(r) : estiloInalcanzable(r);
  return estiloPaso(r, estado.cuadro);
}

/** Clases de origen y destino, que se ven en todos los cuadros. */
function marcas(id) {
  const clases = [];
  if (id === estado.origen) clases.push('origen');
  if (id === estado.destino) clases.push('destino');
  return clases;
}

function distanciasDefinitivas(r) {
  const definitivas = new Map();
  for (const paso of r.pasos) definitivas.set(paso.elegido, paso.distancia);
  return definitivas;
}

function estiloSeleccion() {
  return { nodo: (id) => marcas(id).join(' ') };
}

function estiloCamino(r) {
  const camino = r.cadena.map((u) => u.id);
  const enCamino = new Set(camino);
  const tramos = new Set(camino.slice(1).map((id, i) => `${camino[i]}>${id}`));
  const definitivas = distanciasDefinitivas(r);
  return {
    nodo: (id) => [...marcas(id), enCamino.has(id) ? 'en-camino' : 'tenue'].join(' '),
    etiqueta: (id) => (enCamino.has(id) ? { texto: definitivas.get(id), clase: 'fija' } : null),
    arista: (o, d) => (tramos.has(`${o}>${d}`) ? 'en-camino' : 'tenue'),
  };
}

function estiloInalcanzable(r) {
  // Se ve hasta dónde llegó la difusión: los alcanzados con su tiempo, el resto atenuado
  const alcanzados = new Set(r.alcanzados);
  const definitivas = distanciasDefinitivas(r);
  return {
    nodo: (id) => [...marcas(id), alcanzados.has(id) ? 'alcanzado' : 'tenue'].join(' '),
    etiqueta: (id) => (alcanzados.has(id)
      ? { texto: definitivas.get(id), clase: 'fija' }
      : { texto: '∞', clase: 'infinito' }),
    arista: (o, d) => (alcanzados.has(o) && alcanzados.has(d) ? '' : 'tenue'),
  };
}

function estiloPaso(r, cuadro) {
  const hechos = r.pasos.slice(0, cuadro);
  const definitivos = new Set(hechos.map((p) => p.elegido));
  const elegido = cuadro > 0 ? hechos[cuadro - 1].elegido : null;

  // Distancias provisorias: se reproducen las actualizaciones que mandó la API hasta este paso
  const distancias = new Map([[r.origen.id, 0]]);
  for (const paso of hechos) {
    for (const a of paso.actualizaciones) distancias.set(a.usuario, a.nuevo);
  }

  return {
    nodo: (id) => {
      const clases = marcas(id);
      if (id === elegido) clases.push('elegido');
      else if (definitivos.has(id)) clases.push('definitivo');
      else if (!distancias.has(id)) clases.push('sin-alcanzar');
      return clases.join(' ');
    },
    etiqueta: (id) => (distancias.has(id)
      ? { texto: distancias.get(id), clase: definitivos.has(id) ? 'fija' : '' }
      : { texto: '∞', clase: 'infinito' }),
    arista: () => '',
  };
}

function pintarEncabezado() {
  const r = estado.modo === 'consultar' ? estado.resultado : null;
  el.ayuda.hidden = Boolean(r);
  el.datos.hidden = !r;

  if (!r) {
    el.ayuda.textContent = textoAyuda();
    return;
  }

  el.datos.classList.toggle('inalcanzable', !r.alcanzable);
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
    el.detalle.textContent = r.cadena.length === 1 ? 'Origen y destino son el mismo usuario.' : '';
  } else {
    el.cadena.textContent = `${r.destino.nombre} no es alcanzable desde ${r.origen.nombre}`;
    el.tiempo.hidden = true;
    el.detalle.textContent = `La difusión llega a ${r.alcanzados.length - 1} de ${estado.usuarios.length - 1} usuarios. `
      + 'Nadie de ese grupo tiene un vínculo hacia el destino.';
  }
}

function textoAyuda() {
  if (estado.modo === 'editar') {
    return 'Editando la red: click en un usuario para eliminarlo o para empezar un vínculo, click en una flecha para eliminarla.';
  }
  if (estado.usuarios.length === 0) return '';
  if (estado.origen == null) return 'Elegí el origen con un click en un usuario.';
  return `Origen: ${nombreDe(estado.origen)}. Ahora elegí el destino.`;
}

function pintarPasos() {
  const r = estado.resultado;
  el.pasos.hidden = !r || estado.modo !== 'consultar';
  if (el.pasos.hidden) return;

  const cuadro = estado.cuadro;
  const ultimo = ultimoCuadro();
  el.contador.textContent = cuadro === 0 ? 'Inicio' : cuadro === ultimo ? 'Resultado' : `Paso ${cuadro} de ${ultimo - 1}`;
  el.barra.style.width = `${(cuadro / ultimo) * 100}%`;
  el.reiniciar.disabled = cuadro === 0;
  el.anterior.disabled = cuadro === 0;
  el.siguiente.disabled = cuadro === ultimo;
  el.reproducir.textContent = estado.reproduccion ? 'Pausa' : 'Reproducir';

  let lineas;
  if (cuadro === 0) {
    lineas = [`Inicio: ${r.origen.nombre} arranca con 0 min.`, 'El resto todavía no se alcanzó (∞).'];
  } else if (cuadro === ultimo) {
    lineas = r.alcanzable
      ? [`Camino final: ${r.cadena.map((u) => u.nombre).join(' → ')}.`, `Tiempo total: ${r.tiempoTotal} min.`]
      : ['No quedan usuarios alcanzables sin visitar.', `${r.destino.nombre} quedó sin camino desde ${r.origen.nombre}.`];
  } else {
    lineas = r.pasos[cuadro - 1].decisiones;
  }
  el.decision.replaceChildren(...lineas.map((linea, i) => {
    const li = document.createElement('li');
    li.className = i === 0 ? 'principal' : 'detalle';
    li.textContent = linea.trim();
    return li;
  }));

  el.traza.replaceChildren(...r.decisiones.map((linea) => {
    const li = document.createElement('li');
    li.textContent = linea;
    return li;
  }));
}
