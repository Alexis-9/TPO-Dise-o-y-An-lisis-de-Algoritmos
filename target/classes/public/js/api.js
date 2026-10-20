// Cliente de la API. Es el único archivo que hace fetch.

export class ErrorApi extends Error {
  constructor(mensaje, status) {
    super(mensaje);
    this.status = status;
  }
}

async function pedir(metodo, ruta, cuerpo) {
  const opciones = { method: metodo, headers: {} };
  if (cuerpo !== undefined) {
    opciones.headers['Content-Type'] = 'application/json';
    opciones.body = JSON.stringify(cuerpo);
  }

  let respuesta;
  try {
    respuesta = await fetch('/api' + ruta, opciones);
  } catch {
    throw new ErrorApi('No se pudo conectar con el servidor', 0);
  }

  if (respuesta.status === 204) return null;

  const datos = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    throw new ErrorApi(datos?.error ?? `Error ${respuesta.status}`, respuesta.status);
  }
  return datos;
}

const q = encodeURIComponent;

export const api = {
  // Consultas
  red: () => pedir('GET', '/red'),
  difusion: (origen, destino) => pedir('GET', `/difusion?origen=${q(origen)}&destino=${q(destino)}`),
  publicaciones: (orden) => pedir('GET', `/publicaciones?orden=${q(orden)}`),
  iniciativas: () => pedir('GET', '/iniciativas'),
  seleccion: (presupuesto) => pedir('GET', `/iniciativas/seleccion?presupuesto=${q(presupuesto)}`),

  // Carga
  crearUsuario: (nombre) => pedir('POST', '/usuarios', { nombre }),
  eliminarUsuario: (id) => pedir('DELETE', `/usuarios/${q(id)}`),
  crearVinculo: (origen, destino, minutos) => pedir('POST', '/vinculos', { origen, destino, minutos }),
  eliminarVinculo: (origen, destino) => pedir('DELETE', `/vinculos/${q(origen)}/${q(destino)}`),
  crearPublicacion: (publicacion) => pedir('POST', '/publicaciones', publicacion),
  eliminarPublicacion: (id) => pedir('DELETE', `/publicaciones/${q(id)}`),
  crearIniciativa: (costo, impacto) => pedir('POST', '/iniciativas', { costo, impacto }),
  eliminarIniciativa: (id) => pedir('DELETE', `/iniciativas/${q(id)}`),

  // Todo junto
  cargarDemo: () => pedir('POST', '/datos/demo'),
  vaciar: () => pedir('POST', '/datos/vaciar'),
};

/** Valor numérico de un input; vacío viaja como null y el servidor decide. */
export function numero(input) {
  return input.value.trim() === '' ? null : Number(input.value);
}
