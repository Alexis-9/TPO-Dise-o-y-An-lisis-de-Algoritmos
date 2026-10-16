package datos;

import model.Iniciativa;
import model.Publicacion;
import model.RedSocial;
import model.Usuario;
import model.Vinculo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        return usuario;
    }

    public synchronized void eliminarUsuario(int idUsuario) {
        red.eliminarUsuario(idUsuario);
    }

    public synchronized Usuario getUsuario(int idUsuario) {
        return red.getUsuario(idUsuario);
    }

    public synchronized List<Usuario> getUsuarios() {
        return new ArrayList<>(red.getUsuarios());
    }

    public synchronized Vinculo agregarVinculo(int idOrigen, int idDestino, int minutos) {
        red.agregarVinculo(idOrigen, idDestino, minutos);

        Usuario origen = red.getUsuario(idOrigen);
        for (Vinculo v : red.getVinculosSalientes(origen)) {
            if (v.getDestino().getId() == idDestino) { return v; }
        }
        throw new IllegalStateException("El vínculo recién agregado no aparece en la red");
    }

    public synchronized void eliminarVinculo(int idOrigen, int idDestino) {
        red.eliminarVinculo(idOrigen, idDestino);
    }

    public synchronized List<Vinculo> getVinculos() {
        return new ArrayList<>(red.getVinculos());
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
        publicaciones.clear();
        iniciativas.clear();
    }
}
