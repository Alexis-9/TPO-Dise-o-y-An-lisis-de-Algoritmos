// Vista Difusión: grafo de la red, cadena más rápida (Dijkstra) con paso a paso y edición.
// Todo resultado sale de la API; acá solo se decide qué se ve en cada momento.
import { api, numero } from './api.js';
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
  // Editar
  seleccionado: null,  // usuario elegido con un click
  aristaSel: null,     // { origen, destino } elegida con un click
  nuevo: null,         // { origen, destino } del vínculo que se está creando con el atajo
  ancla: null,         // función que da el punto del SVG donde va el cuadro flotante
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
    flotante: $('flotante'),
    formUsuario: $('form-usuario'),
    nombre: $('usuario-nombre'),
    formVinculo: $('form-vinculo'),
    vinculoOrigen: $('vinculo-origen'),
    vinculoDestino: $('vinculo-destino'),
    vinculoMinutos: $('vinculo-minutos'),
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

  el.formUsuario.addEventListener('submit', agregarUsuario);
  el.formVinculo.addEventListener('submit', agregarVinculo);
  for (const form of [el.formUsuario, el.formVinculo]) {
    form.addEventListener('input', () => { form.querySelector('.error').textContent = ''; });
  }
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && estado.modo === 'editar' && hayEdicionEnCurso()) {
      cancelarEdicion();
      pintar();
    }
  });
  window.addEventListener('resize', ubicarFlotante);
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

  cancelarEdicion();
  for (const error of el.panelEditar.querySelectorAll('.error')) error.textContent = '';
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
  const selects = [el.selectOrigen, el.selectDestino, el.vinculoOrigen, el.vinculoDestino];
  const anteriores = selects.map((s) => s.value);
  selects.forEach((select, i) => {
    select.replaceChildren(opcion('', 'Elegí…'), ...estado.usuarios.map((u) => opcion(u.id, u.nombre)));
    select.disabled = estado.usuarios.length === 0;
    select.value = anteriores[i];
    if (select.selectedIndex < 0) select.value = '';
  });
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
  cancelarEdicion();

  if (modo === 'consultar' && estado.origen != null && estado.destino != null) {
    consultar();
  } else {
    pintar();
  }
}

// ---------------------------------------------------------------- clicks en el grafo

function alClickNodo(id) {
  if (estado.modo === 'editar') {
    clickNodoEditando(id);
    return;
  }
  // Primer click: origen. Segundo: destino. Tercero: reinicia.
  if (estado.origen == null) elegir(id, null);
  else if (estado.destino == null) elegir(estado.origen, id);
  else elegir(null, null);
}

function alClickArista(origen, destino) {
  if (estado.modo !== 'editar') return;
  cancelarEdicion();
  estado.aristaSel = { origen, destino };
  mostrarEliminarVinculo(origen, destino);
  pintar();
}

function alClickFondo() {
  if (estado.modo !== 'editar' || !hayEdicionEnCurso()) return;
  cancelarEdicion();
  pintar();
}

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

// ---------------------------------------------------------------- edición

function hayEdicionEnCurso() {
  return estado.seleccionado != null || estado.aristaSel != null || estado.nuevo != null;
}

function clickNodoEditando(id) {
  const seleccionado = estado.seleccionado;
  cancelarEdicion();

  if (seleccionado == null) {
    // Primer click: el usuario queda elegido; se puede eliminar o seguir con otro para vincular
    estado.seleccionado = id;
    mostrarEliminarUsuario(id);
  } else if (seleccionado !== id) {
    // Segundo click en otro usuario: atajo para crear el vínculo
    estado.nuevo = { origen: seleccionado, destino: id };
    grafo.mostrarPrevia(seleccionado, id);
    mostrarMinutos(seleccionado, id);
  }
  pintar();
}

function cancelarEdicion() {
  estado.seleccionado = null;
  estado.aristaSel = null;
  estado.nuevo = null;
  estado.ancla = null;
  grafo?.quitarPrevia();
  if (el) {
    el.flotante.hidden = true;
    el.flotante.replaceChildren();
  }
}

function crearElemento(etiqueta, clase, texto) {
  const elemento = document.createElement(etiqueta);
  if (clase) elemento.className = clase;
  if (texto != null) elemento.textContent = texto;
  return elemento;
}

/** Cuadro chico sobre el grafo. lugar: 'debajo' de un nodo o en el 'medio' de una flecha. */
function mostrarFlotante(contenido, ancla, lugar) {
  el.flotante.replaceChildren(...contenido);
  el.flotante.dataset.lugar = lugar;
  el.flotante.hidden = false;
  estado.ancla = ancla;
  ubicarFlotante();
}

function ubicarFlotante() {
  if (!estado.ancla || el.flotante.hidden) return;
  const punto = estado.ancla();
  if (!punto) return;
  const { left, top } = grafo.aPantalla(punto);
  el.flotante.style.left = `${left}px`;
  el.flotante.style.top = `${top}px`;
}

function mostrarEliminarUsuario(id) {
  const error = crearElemento('p', 'error');
  const boton = crearElemento('button', 'boton-flotante peligro', 'Eliminar usuario');
  boton.type = 'button';
  boton.addEventListener('click', () => ejecutar(() => api.eliminarUsuario(id), error));
  const pista = crearElemento('p', 'flotante-pista', `o click en otro usuario para crear un vínculo desde ${nombreDe(id)}`);

  mostrarFlotante([boton, pista, error], () => {
    const c = grafo.centro(id);
    return c && { x: c.x, y: c.y + grafo.radioNodo + 8 };
  }, 'debajo');
}

function mostrarEliminarVinculo(origen, destino) {
  const vinculo = estado.vinculos.find((v) => v.origen === origen && v.destino === destino);
  const error = crearElemento('p', 'error');
  const ruta = crearElemento('p', 'flotante-ruta',
    `${nombreDe(origen)} → ${nombreDe(destino)} · ${vinculo?.minutos ?? ''} min`);
  const boton = crearElemento('button', 'boton-flotante peligro', 'Eliminar vínculo');
  boton.type = 'button';
  boton.addEventListener('click', () => ejecutar(() => api.eliminarVinculo(origen, destino), error));

  mostrarFlotante([ruta, boton, error], () => grafo.medioArista(origen, destino), 'medio');
}

function mostrarMinutos(origen, destino) {
  const error = crearElemento('p', 'error');
  const ruta = crearElemento('p', 'flotante-ruta', `${nombreDe(origen)} → ${nombreDe(destino)}`);
  const input = document.createElement('input');
  input.type = 'number';
  input.min = '1';
  input.step = '1';
  input.placeholder = 'min';
  input.setAttribute('aria-label', 'Minutos del vínculo');
  const pista = crearElemento('p', 'flotante-pista', 'Enter para crear · Esc para cancelar');

  input.addEventListener('input', () => { error.textContent = ''; });
  input.addEventListener('keydown', (e) => {
    if (e.key !== 'Enter') return;
    e.preventDefault();
    ejecutar(() => api.crearVinculo(origen, destino, numero(input)), error, input);
  });

  mostrarFlotante([ruta, input, pista, error], () => grafo.medioArista(origen, destino), 'medio');
  input.focus();
}

/** Ejecuta una carga contra la API: si sale bien vuelve a pedir la red, si no muestra el error. */
async function ejecutar(accion, error, foco = null) {
  error.textContent = '';
  try {
    await accion();
  } catch (e) {
    error.textContent = e.message;
    foco?.focus();
    foco?.select?.();
    return false;
  }
  cancelarEdicion();
  await refrescar();
  return true;
}

async function agregarUsuario(e) {
  e.preventDefault();
  const error = el.formUsuario.querySelector('.error');
  if (await ejecutar(() => api.crearUsuario(el.nombre.value), error, el.nombre)) {
    el.nombre.value = '';
    el.nombre.focus();
  }
}

async function agregarVinculo(e) {
  e.preventDefault();
  const error = el.formVinculo.querySelector('.error');
  const pedido = () => api.crearVinculo(valorDe(el.vinculoOrigen), valorDe(el.vinculoDestino), numero(el.vinculoMinutos));
  if (await ejecutar(pedido, error, el.vinculoMinutos)) {
    el.vinculoMinutos.value = '';
  }
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
  if (estado.modo === 'editar') return estiloEdicion();
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

function estiloEdicion() {
  const marcados = new Set([estado.seleccionado, estado.nuevo?.origen, estado.nuevo?.destino]);
  const arista = estado.aristaSel;
  return {
    nodo: (id) => (marcados.has(id) ? 'seleccionado' : ''),
    arista: (o, d) => (arista && arista.origen === o && arista.destino === d ? 'seleccionada' : ''),
  };
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
