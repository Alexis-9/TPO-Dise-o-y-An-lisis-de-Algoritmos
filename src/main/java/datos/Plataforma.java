package datos;

import model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Estado en memoria de la aplicación: la red social, las publicaciones y las iniciativas.
 *
 * Javalin atiende cada pedido en su propio hilo, así que todos los métodos son synchronized:
 * un pedido nunca ve el estado a mitad de un cambio de otro. Las consultas devuelven copias,
 * para que los algoritmos trabajen sin que otro hilo modifique los datos mientras tanto.
 *
 * Los ids los asigna la plataforma: el siguiente libre es el mayor id en uso + 1.
 */
public class Plataforma {

    private RedSocial red = new RedSocial();
    private final List<Publicacion> publicaciones = new ArrayList<>();
    private final List<Iniciativa> iniciativas = new ArrayList<>();
    private final List<Moderador> moderadores = new ArrayList<>();
    private final List<Comunidad> comunidades = new ArrayList<>();
    private final Map<Integer, Map<Integer, Integer>> costos = new HashMap<>();

    /**
     * Sube cada vez que cambia la red (usuarios o vínculos). Sirve para saber si un resultado
     * calculado antes sigue valiendo, sin comparar la red entera.
     */
    private long versionRed = 0;

    // ------------------------------------------------------------------
    // Usuarios y vínculos
    // ------------------------------------------------------------------

    public synchronized Usuario agregarUsuario(String nombre) {
        int id = 1;
        for (Usuario u : red.getUsuarios()) {
            id = Math.max(id, u.getId() + 1);
        }

        Usuario usuario = new Usuario(id, nombre);
        red.agregarUsuario(usuario);
        versionRed++;
        return usuario;
    }

    public synchronized void eliminarUsuario(int idUsuario) {
        red.eliminarUsuario(idUsuario);
        versionRed++;
    }

    public synchronized Usuario getUsuario(int idUsuario) {
        return red.getUsuario(idUsuario);
    }

    public synchronized List<Usuario> getUsuarios() {
        return new ArrayList<>(red.getUsuarios());
    }

    public synchronized Vinculo agregarVinculo(int idOrigen, int idDestino, int minutos) {
        red.agregarVinculo(idOrigen, idDestino, minutos);
        versionRed++;

        Usuario origen = red.getUsuario(idOrigen);
        for (Vinculo v : red.getVinculosSalientes(origen)) {
            if (v.getDestino().getId() == idDestino) { return v; }
        }
        throw new IllegalStateException("El vínculo recién agregado no aparece en la red");
    }

    public synchronized void eliminarVinculo(int idOrigen, int idDestino) {
        red.eliminarVinculo(idOrigen, idDestino);
        versionRed++;
    }

    public synchronized List<Vinculo> getVinculos() {
        return new ArrayList<>(red.getVinculos());
    }

    /** Versión actual de la red; cambia con cada alta o baja de usuarios o vínculos. */
    public synchronized long getVersionRed() {
        return versionRed;
    }

    /**
     * Copia de la red para calcular sobre ella sin bloquear la plataforma.
     * Respeta el orden de las listas de adyacencia, así la traza de Dijkstra sale igual
     * que sobre la red original.
     */
    public synchronized RedSocial copiaDeRed() {
        RedSocial copia = new RedSocial();
        for (Usuario u : red.getUsuarios()) {
            copia.agregarUsuario(u);
        }
        for (Usuario u : red.getUsuarios()) {
            for (Vinculo v : red.getVinculosSalientes(u)) {
                copia.agregarVinculo(v.getOrigen().getId(), v.getDestino().getId(), v.getPeso());
            }
        }
        return copia;
    }

    // ------------------------------------------------------------------
    // Publicaciones
    // ------------------------------------------------------------------

    public synchronized Publicacion agregarPublicacion(String autor, LocalDateTime fechaHora,
                                                       int reacciones, int comentarios) {
        int id = 1;
        for (Publicacion p : publicaciones) {
            id = Math.max(id, p.getId() + 1);
        }

        Publicacion publicacion = new Publicacion(id, autor, fechaHora, reacciones, comentarios);
        publicaciones.add(publicacion);
        return publicacion;
    }

    public synchronized void eliminarPublicacion(int idPublicacion) {
        boolean eliminada = publicaciones.removeIf(p -> p.getId() == idPublicacion);
        if (!eliminada) { throw new NoSuchElementException("No existe una publicación con id " + idPublicacion); }
    }

    public synchronized List<Publicacion> getPublicaciones() {
        return new ArrayList<>(publicaciones);
    }

    // ------------------------------------------------------------------
    // Iniciativas
    // ------------------------------------------------------------------

    public synchronized Iniciativa agregarIniciativa(int costo, int impacto) {
        int id = 1;
        for (Iniciativa i : iniciativas) {
            id = Math.max(id, i.getId() + 1);
        }

        Iniciativa iniciativa = new Iniciativa(id, costo, impacto);
        iniciativas.add(iniciativa);
        return iniciativa;
    }

    public synchronized void eliminarIniciativa(int idIniciativa) {
        boolean eliminada = iniciativas.removeIf(i -> i.getId() == idIniciativa);
        if (!eliminada) { throw new NoSuchElementException("No existe una iniciativa con id " + idIniciativa); }
    }

    public synchronized List<Iniciativa> getIniciativas() {
        return new ArrayList<>(iniciativas);
    }

    // ------------------------------------------------------------------
    // Todo junto
    // ------------------------------------------------------------------

    /** Deja la plataforma vacía; los ids vuelven a empezar en 1. */
    public synchronized void vaciar() {
        red = new RedSocial();
        versionRed++;
        publicaciones.clear();
        iniciativas.clear();
        moderadores.clear();
    }

    public synchronized Moderador agregarModerador(String nombreModerador, int limiteComunidades) {

        int id = 1;
        for (Moderador i : moderadores) {
            id = Math.max(id, i.getId() + 1);
        }

        Moderador nuevoModerador = new Moderador(id, nombreModerador, limiteComunidades);

        moderadores.add(nuevoModerador);
        costos.put(id, new HashMap<>());

        return nuevoModerador;

    }

    public synchronized void eliminarModerador(int idModerador) {
        boolean eliminada = moderadores.removeIf(i -> i.getId() == idModerador);
        if (!eliminada) { throw new NoSuchElementException("No existe un moderador con id " + idModerador); }
        else { costos.remove(idModerador); }
    }

    public synchronized Comunidad agregarComunidad(String nombreComunidad) {

        int id = 1;
        for (Comunidad i : comunidades) {
            id = Math.max(id, i.getId() + 1);
        }

        Comunidad nuevaComunidad = new Comunidad(id, nombreComunidad);
        comunidades.add(nuevaComunidad);

        return nuevaComunidad;

    }

    public synchronized void eliminarComunidad(int idComunidad) {
        boolean eliminada = comunidades.removeIf(i -> i.getId() == idComunidad);
        if (!eliminada) { throw new NoSuchElementException("No existe una comunidad con id " + idComunidad); }
        else {
            for (Map<Integer, Integer> modComunidades : costos.values()) {

                modComunidades.remove(idComunidad);

            }
        }
    }

    public synchronized void definirCosto(int idModerador, int idComunidad, int costo) {
        if (!costos.containsKey(idModerador)) {
            throw new NoSuchElementException("No existe un moderador con id " + idModerador);
        }
        if (comunidades.stream().noneMatch(c -> c.getId() == idComunidad)) {
            throw new NoSuchElementException("No existe una comunidad con id " + idComunidad);
        }
        costos.get(idModerador).put(idComunidad, costo);
    }

    public synchronized List<Moderador> getModeradores() { return new ArrayList<>(moderadores); }

    public synchronized List<Comunidad> getComunidades() { return new ArrayList<>(comunidades); }

    public synchronized Map<Integer, Map<Integer, Integer>> getCostos() {
        Map<Integer, Map<Integer, Integer>> copiaCostos = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, Integer>> e : costos.entrySet()) {
            copiaCostos.put(e.getKey(), new HashMap<>(e.getValue()));
        }

        return copiaCostos;
    }
}
