// Dibujo del grafo en SVG, a mano. Solo presentación: posiciones en círculo, flechas,
// curvas y animaciones. Qué se resalta lo decide quien lo usa, a través de pintar().

const NS = 'http://www.w3.org/2000/svg';
const ANCHO = 800;
const ALTO = 500;
const CX = ANCHO / 2;
const CY = ALTO / 2;
const DURACION = 450;

// Clases de arista que se dibujan con la flecha del color de acento
const ACENTUADAS = new Set(['en-camino', 'seleccionada']);

const clave = (origen, destino) => `${origen}>${destino}`;
const redondear = (n) => Math.round(n * 10) / 10;
const suavizar = (t) => (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);

function crear(etiqueta, atributos = {}, padre = null) {
  const elemento = document.createElementNS(NS, etiqueta);
  for (const [nombre, valor] of Object.entries(atributos)) elemento.setAttribute(nombre, valor);
  if (padre) padre.appendChild(elemento);
  return elemento;
}

function abreviar(nombre) {
  return nombre.length > 9 ? nombre.slice(0, 8) + '…' : nombre;
}

/** Punto a distancia d de p, en dirección a hacia. */
function acercar(p, hacia, d) {
  const largo = Math.hypot(hacia.x - p.x, hacia.y - p.y) || 1;
  return { x: p.x + ((hacia.x - p.x) / largo) * d, y: p.y + ((hacia.y - p.y) / largo) * d };
}

export class Grafo {
  /**
   * @param svg     elemento <svg> con viewBox 0 0 800 500
   * @param eventos { alClickNodo(id), alClickArista(origen, destino), alClickFondo() }
   */
  constructor(svg, eventos = {}) {
    this.svg = svg;
    this.eventos = eventos;
    this.nodos = new Map();     // id -> { id, nombre, g, cuerpo, circulo, texto, etiqueta, pos, desde, hacia }
    this.aristas = new Map();   // "o>d" -> { origen, destino, g, linea, toque, texto, curva }
    this.radio = 27;
    this.estilo = {};
    this.previa = null;
    this.animacion = null;
    this.terminarAnimacion = null;
    this.armarBase();
  }

  armarBase() {
    const defs = crear('defs', {}, this.svg);
    for (const [id, tamano] of [['flecha', 9], ['flecha-acento', 11]]) {
      const marcador = crear('marker', {
        id, class: id, viewBox: '0 0 10 10', refX: 9, refY: 5,
        markerWidth: tamano, markerHeight: tamano, markerUnits: 'userSpaceOnUse', orient: 'auto',
      }, defs);
      crear('path', { d: 'M0,1 L10,5 L0,9 z' }, marcador);
    }

    const fondo = crear('rect', { class: 'fondo', x: 0, y: 0, width: ANCHO, height: ALTO }, this.svg);
    fondo.addEventListener('click', () => this.eventos.alClickFondo?.());

    this.capaAristas = crear('g', { class: 'capa-aristas' }, this.svg);
    this.capaPrevia = crear('g', { class: 'capa-previa' }, this.svg);
    this.capaNodos = crear('g', { class: 'capa-nodos' }, this.svg);
  }

  // ---------------------------------------------------------------- datos

  /** Recibe usuarios (en orden de id) y vínculos; acomoda el resto con una transición. */
  actualizar(usuarios, vinculos) {
    this.radio = usuarios.length > 14 ? 21 : 27;
    const destinos = this.layout(usuarios.map((u) => u.id));

    for (const [id, nodo] of this.nodos) {
      if (destinos.has(id)) continue;
      this.nodos.delete(id);
      nodo.g.classList.add('saliendo');
      setTimeout(() => nodo.g.remove(), 300);
    }

    for (const usuario of usuarios) {
      let nodo = this.nodos.get(usuario.id);
      if (!nodo) {
        nodo = this.crearNodo(usuario.id);
        nodo.pos = { ...destinos.get(usuario.id) };
        this.nodos.set(usuario.id, nodo);
      }
      nodo.nombre = usuario.nombre;
      nodo.texto.textContent = abreviar(usuario.nombre);
      nodo.titulo.textContent = usuario.nombre;
      nodo.g.setAttribute('aria-label', usuario.nombre);
      nodo.circulo.setAttribute('r', this.radio);
      nodo.desde = { ...nodo.pos };
      nodo.hacia = destinos.get(usuario.id);
    }

    const claves = new Set(vinculos.map((v) => clave(v.origen, v.destino)));
    for (const [k, arista] of this.aristas) {
      if (claves.has(k)) continue;
      arista.g.remove();
      this.aristas.delete(k);
    }
    for (const vinculo of vinculos) {
      const k = clave(vinculo.origen, vinculo.destino);
      let arista = this.aristas.get(k);
      if (!arista) {
        arista = this.crearArista(vinculo.origen, vinculo.destino);
        this.aristas.set(k, arista);
      }
      arista.texto.textContent = String(vinculo.minutos);
      // Si también existe la vuelta, las dos se curvan hacia lados opuestos
      arista.curva = claves.has(clave(vinculo.destino, vinculo.origen));
    }

    this.aplicarEstilo();
    return this.animar();
  }

  /** Círculo determinístico: el primer id arriba y el resto en sentido horario. */
  layout(ids) {
    const posiciones = new Map();
    const n = ids.length;
    if (n === 1) {
      posiciones.set(ids[0], { x: CX, y: CY });
      return posiciones;
    }
    const radio = Math.min(195, 70 + n * 16);
    const inicio = n === 2 ? Math.PI : -Math.PI / 2;
    ids.forEach((id, i) => {
      const angulo = inicio + (2 * Math.PI * i) / n;
      posiciones.set(id, { x: CX + radio * Math.cos(angulo), y: CY + radio * Math.sin(angulo) });
    });
    return posiciones;
  }

  crearNodo(id) {
    const g = crear('g', { class: 'nodo entrando', tabindex: 0, role: 'button' }, this.capaNodos);
    const titulo = crear('title', {}, g);
    const cuerpo = crear('g', { class: 'cuerpo' }, g);
    const circulo = crear('circle', { r: this.radio }, cuerpo);
    const texto = crear('text', { class: 'nombre' }, cuerpo);
    const etiqueta = crear('text', { class: 'dist' }, cuerpo);

    const click = (e) => {
      e.stopPropagation();
      this.eventos.alClickNodo?.(id);
    };
    g.addEventListener('click', click);
    g.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        click(e);
      }
    });
    // Aparece con un fundido (ver .nodo.entrando en el CSS)
    requestAnimationFrame(() => requestAnimationFrame(() => g.classList.remove('entrando')));

    return { id, g, titulo, cuerpo, circulo, texto, etiqueta, pos: { x: CX, y: CY } };
  }

  crearArista(origen, destino) {
    const g = crear('g', { class: 'arista' }, this.capaAristas);
    const linea = crear('path', { class: 'linea', 'marker-end': 'url(#flecha)' }, g);
    const toque = crear('path', { class: 'toque' }, g);
    const texto = crear('text', { class: 'minutos' }, g);

    const click = (e) => {
      e.stopPropagation();
      this.eventos.alClickArista?.(origen, destino);
    };
    toque.addEventListener('click', click);
    texto.addEventListener('click', click);

    return { origen, destino, g, linea, toque, texto, curva: false };
  }

  // ---------------------------------------------------------------- animación

  animar() {
    cancelAnimationFrame(this.animacion);
    this.terminarAnimacion?.();

    return new Promise((resolver) => {
      this.terminarAnimacion = resolver;
      const inicio = performance.now();
      const cuadro = (ahora) => {
        const t = Math.min(1, (ahora - inicio) / DURACION);
        const k = suavizar(t);
        for (const nodo of this.nodos.values()) {
          nodo.pos = {
            x: nodo.desde.x + (nodo.hacia.x - nodo.desde.x) * k,
            y: nodo.desde.y + (nodo.hacia.y - nodo.desde.y) * k,
          };
        }
        this.dibujar();
        if (t < 1) {
          this.animacion = requestAnimationFrame(cuadro);
        } else {
          this.animacion = null;
          this.terminarAnimacion = null;
          resolver();
        }
      };
      this.animacion = requestAnimationFrame(cuadro);
    });
  }

  dibujar() {
    for (const nodo of this.nodos.values()) {
      nodo.g.setAttribute('transform', `translate(${redondear(nodo.pos.x)} ${redondear(nodo.pos.y)})`);
      const e = this.lugarEtiqueta(nodo.pos);
      nodo.etiqueta.setAttribute('x', redondear(e.x));
      nodo.etiqueta.setAttribute('y', redondear(e.y));
    }
    for (const arista of this.aristas.values()) {
      const a = this.nodos.get(arista.origen);
      const b = this.nodos.get(arista.destino);
      if (!a || !b) continue;
      const { d, medio } = this.geometria(a.pos, b.pos, arista.curva);
      arista.linea.setAttribute('d', d);
      arista.toque.setAttribute('d', d);
      arista.texto.setAttribute('x', redondear(medio.x));
      arista.texto.setAttribute('y', redondear(medio.y));
    }
    this.dibujarPrevia();
  }

  /** La distancia de cada nodo va del lado de afuera del círculo, lejos de las flechas. */
  lugarEtiqueta(pos) {
    const dx = pos.x - CX;
    const dy = pos.y - CY;
    const largo = Math.hypot(dx, dy);
    const separacion = this.radio + 15;
    if (largo < 1) return { x: 0, y: -separacion };
    return { x: (dx / largo) * separacion, y: (dy / largo) * separacion };
  }

  geometria(a, b, curva) {
    const r = this.radio;
    const dx = b.x - a.x;
    const dy = b.y - a.y;
    const largo = Math.hypot(dx, dy) || 1;
    const ux = dx / largo;
    const uy = dy / largo;

    if (!curva) {
      const s = { x: a.x + ux * r, y: a.y + uy * r };
      const e = { x: b.x - ux * (r + 2), y: b.y - uy * (r + 2) };
      return {
        d: `M${redondear(s.x)} ${redondear(s.y)} L${redondear(e.x)} ${redondear(e.y)}`,
        medio: { x: (s.x + e.x) / 2, y: (s.y + e.y) / 2 },
      };
    }

    // Curva cuadrática: el punto de control se corre hacia la izquierda del sentido a -> b,
    // así la vuelta b -> a queda del otro lado
    const curvatura = Math.min(28, largo * 0.15);
    const c = { x: (a.x + b.x) / 2 + uy * curvatura * 2, y: (a.y + b.y) / 2 - ux * curvatura * 2 };
    const s = acercar(a, c, r);
    const e = acercar(b, c, r + 2);
    return {
      d: `M${redondear(s.x)} ${redondear(s.y)} Q${redondear(c.x)} ${redondear(c.y)} ${redondear(e.x)} ${redondear(e.y)}`,
      medio: { x: 0.25 * s.x + 0.5 * c.x + 0.25 * e.x, y: 0.25 * s.y + 0.5 * c.y + 0.25 * e.y },
    };
  }

  // ---------------------------------------------------------------- estilo

  /**
   * estilo = {
   *   nodo(id)        -> clases del nodo ('origen', 'en-camino', 'tenue', ...)
   *   etiqueta(id)    -> { texto, clase } o null
   *   arista(o, d)    -> clases de la arista
   * }
   */
  pintar(estilo) {
    this.estilo = estilo ?? {};
    this.aplicarEstilo();
  }

  aplicarEstilo() {
    const estilo = this.estilo;
    for (const nodo of this.nodos.values()) {
      nodo.cuerpo.setAttribute('class', `cuerpo ${estilo.nodo?.(nodo.id) ?? ''}`.trim());
      const etiqueta = estilo.etiqueta?.(nodo.id) ?? null;
      nodo.etiqueta.textContent = etiqueta ? String(etiqueta.texto) : '';
      nodo.etiqueta.setAttribute('class', `dist ${etiqueta?.clase ?? ''}`.trim());
    }
    for (const arista of this.aristas.values()) this.aplicarEstiloArista(arista);
  }

  aplicarEstiloArista(arista) {
    const clases = this.estilo.arista?.(arista.origen, arista.destino) ?? '';
    arista.g.setAttribute('class', `arista ${clases}`.trim());
    const acento = clases.split(' ').some((c) => ACENTUADAS.has(c)) || arista.linea.classList.contains('mejora');
    arista.linea.setAttribute('marker-end', acento ? 'url(#flecha-acento)' : 'url(#flecha)');
  }

  /** Resalta un instante las aristas indicadas (por ejemplo, las que mejoraron una distancia). */
  destellar(pares) {
    for (const [origen, destino] of pares) {
      const arista = this.aristas.get(clave(origen, destino));
      if (!arista) continue;
      clearTimeout(arista.destello);
      arista.linea.classList.remove('mejora');
      arista.linea.getBoundingClientRect(); // reinicia la animación CSS
      arista.linea.classList.add('mejora');
      this.aplicarEstiloArista(arista);
      arista.destello = setTimeout(() => {
        arista.linea.classList.remove('mejora');
        this.aplicarEstiloArista(arista);
      }, 1200);
    }
  }

  // ---------------------------------------------------------------- arista provisoria

  /** Flecha punteada de origen a destino mientras se carga un vínculo nuevo. */
  mostrarPrevia(origen, destino) {
    this.previa = { origen, destino, linea: crear('path', { class: 'previa', 'marker-end': 'url(#flecha-acento)' }) };
    this.capaPrevia.replaceChildren(this.previa.linea);
    this.dibujarPrevia();
  }

  quitarPrevia() {
    this.previa = null;
    this.capaPrevia.replaceChildren();
  }

  dibujarPrevia() {
    if (!this.previa) return;
    const a = this.nodos.get(this.previa.origen);
    const b = this.nodos.get(this.previa.destino);
    if (!a || !b) return;
    const curva = this.aristas.has(clave(this.previa.destino, this.previa.origen));
    this.previa.linea.setAttribute('d', this.geometria(a.pos, b.pos, curva).d);
  }

  // ---------------------------------------------------------------- coordenadas

  /** Centro final de un nodo (en coordenadas del SVG). */
  centro(id) {
    return this.nodos.get(id)?.hacia ?? null;
  }

  /** Punto medio final de la flecha origen -> destino (exista o no). */
  medioArista(origen, destino) {
    const a = this.centro(origen);
    const b = this.centro(destino);
    if (!a || !b) return null;
    const curva = this.aristas.has(clave(destino, origen));
    return this.geometria(a, b, curva).medio;
  }

  /** Pasa un punto del SVG a píxeles relativos al contenedor del SVG. */
  aPantalla(punto) {
    const matriz = this.svg.getScreenCTM();
    if (!matriz || !punto) return { left: 0, top: 0 };
    const p = new DOMPoint(punto.x, punto.y).matrixTransform(matriz);
    const caja = this.svg.parentElement.getBoundingClientRect();
    return { left: p.x - caja.left, top: p.y - caja.top };
  }

  get radioNodo() {
    return this.radio;
  }
}
