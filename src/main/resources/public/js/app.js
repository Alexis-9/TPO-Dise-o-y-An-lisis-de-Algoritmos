// Pestañas y botones globales. Cada vista expone iniciar(), refrescar() y, si hace falta, ocultar().
import { api } from './api.js';
import * as difusion from './difusion.js';
import * as muro from './muro.js';

const vistas = { difusion, muro };

const NOMBRES = ['difusion', 'muro', 'iniciativas'];
let vistaActual = null;

function mostrar(nombre, { foco = false } = {}) {
  if (!NOMBRES.includes(nombre)) nombre = 'difusion';
  if (vistaActual && vistaActual !== nombre) vistas[vistaActual]?.ocultar?.();
  vistaActual = nombre;

  for (const pestana of document.querySelectorAll('.pestana')) {
    const activa = pestana.dataset.vista === nombre;
    pestana.setAttribute('aria-selected', String(activa));
    pestana.tabIndex = activa ? 0 : -1;
    if (activa && foco) pestana.focus();
  }
  for (const n of NOMBRES) {
    document.getElementById('vista-' + n).hidden = n !== nombre;
  }
  if (location.hash !== '#' + nombre) history.replaceState(null, '', '#' + nombre);

  // Cada vista se vuelve a pedir al mostrarse: así siempre refleja lo que cambió en otra
  vistas[nombre]?.refrescar();
}

function iniciarPestanas() {
  const pestanas = [...document.querySelectorAll('.pestana')];
  for (const pestana of pestanas) {
    pestana.addEventListener('click', () => mostrar(pestana.dataset.vista));
    pestana.addEventListener('keydown', (e) => {
      if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
      const i = pestanas.indexOf(pestana);
      const siguiente = pestanas[(i + (e.key === 'ArrowRight' ? 1 : pestanas.length - 1)) % pestanas.length];
      mostrar(siguiente.dataset.vista, { foco: true });
    });
  }
  window.addEventListener('hashchange', () => mostrar(location.hash.slice(1)));
}

function iniciarBotonesGlobales() {
  const error = document.getElementById('error-global');
  const demo = document.getElementById('boton-demo');
  const vaciar = document.getElementById('boton-vaciar');
  const confirmar = document.getElementById('confirmar-vaciar');
  let espera = null;

  const cerrarConfirmacion = () => {
    clearTimeout(espera);
    confirmar.hidden = true;
    vaciar.hidden = false;
  };

  const ejecutar = async (accion) => {
    error.textContent = '';
    try {
      await accion();
      vistas[vistaActual]?.refrescar();
    } catch (e) {
      error.textContent = e.message;
    }
  };

  demo.addEventListener('click', () => ejecutar(api.cargarDemo));

  vaciar.addEventListener('click', () => {
    vaciar.hidden = true;
    confirmar.hidden = false;
    document.getElementById('boton-vaciar-no').focus();
    espera = setTimeout(cerrarConfirmacion, 6000);
  });
  document.getElementById('boton-vaciar-no').addEventListener('click', cerrarConfirmacion);
  document.getElementById('boton-vaciar-si').addEventListener('click', () => {
    cerrarConfirmacion();
    ejecutar(api.vaciar);
  });
}

for (const vista of Object.values(vistas)) vista.iniciar();
iniciarPestanas();
iniciarBotonesGlobales();
mostrar(location.hash.slice(1));
