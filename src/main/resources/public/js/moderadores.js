// Vista Moderadores: tabla de costos editable y asignación de menor costo total
// (Branch and Bound). La asignación la calcula la API; acá solo se resalta.
import { api, numero } from './api.js';

let datos = { moderadores: [], comunidades: [], costos: [] };  // respuesta de /api/moderacion
let plan = null;        // respuesta de /api/moderacion/asignacion, o null si no hay solución
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

async function cambiarCosto(input, moderador, comunidad) {
  el.error.textContent = '';
  try {
    await api.definirCosto(moderador, comunidad, numero(input));
  } catch (e) {
    el.error.textContent = e.message;
    input.value = input.dataset.valor;   // vuelve al último costo válido
    return;
  }
  input.dataset.valor = input.value;
  input.classList.remove('falta');
  await calcular();
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
  const { moderadores, comunidades, costos } = datos;
  const hayAlgo = moderadores.length > 0 || comunidades.length > 0;
  el.vacio.hidden = hayAlgo;
  el.matriz.hidden = !hayAlgo;

  const costo = new Map(costos.map((c) => [`${c.moderador}>${c.comunidad}`, c.costo]));

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
      const input = document.createElement('input');
      input.type = 'number';
      input.min = '0';
      input.step = '1';
      const valor = costo.get(`${m.id}>${c.id}`);
      input.value = valor == null ? '' : String(valor);
      input.dataset.valor = input.value;
      input.classList.toggle('falta', valor == null);
      input.setAttribute('aria-label', `Costo de ${m.nombre} para ${c.nombre}`);
      input.addEventListener('change', () => cambiarCosto(input, m.id, c.id));
      td.appendChild(input);
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
