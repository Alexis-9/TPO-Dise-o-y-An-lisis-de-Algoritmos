package service;

import algoritmos.MergeSort;
import datos.Plataforma;
import model.Publicacion;
import model.criterio.Criterio;
import model.criterio.CriterioFecha;
import model.criterio.CriterioRelevancia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Muro de publicaciones ordenado con MergeSort por fecha o por relevancia. */
public class MuroService {

    public static final String ORDEN_FECHA = "fecha";
    public static final String ORDEN_RELEVANCIA = "relevancia";

    private final Plataforma plataforma;

    public MuroService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /**
     * fecha: de la más reciente a la más antigua; a igual fecha, id menor primero.
     * relevancia: de mayor a menor; a igual relevancia, la más reciente; después, id menor.
     * Sin orden se usa fecha.
     */
    public List<Publicacion> listar(String orden) {
        if (orden == null || orden.isBlank() || orden.equals(ORDEN_FECHA)) {
            return ordenar(CriterioFecha::new);
        }
        if (orden.equals(ORDEN_RELEVANCIA)) {
            return ordenar(CriterioRelevancia::new);
        }
        throw new IllegalArgumentException("Orden inválido: usar \"" + ORDEN_FECHA + "\" o \"" + ORDEN_RELEVANCIA + "\"");
    }

    public Publicacion publicar(String autor, LocalDateTime fechaHora, int reacciones, int comentarios) {
        // Publicacion no valida sus datos
        if (autor == null || autor.isBlank()) { throw new IllegalArgumentException("El autor no puede estar vacío"); }
        if (fechaHora == null) { throw new IllegalArgumentException("La fecha y hora es obligatoria"); }
        if (reacciones < 0) { throw new IllegalArgumentException("Las reacciones no pueden ser negativas"); }
        if (comentarios < 0) { throw new IllegalArgumentException("Los comentarios no pueden ser negativos"); }

        return plataforma.agregarPublicacion(autor.strip(), fechaHora, reacciones, comentarios);
    }

    public void eliminar(int idPublicacion) {
        plataforma.eliminarPublicacion(idPublicacion);
    }

    private List<Publicacion> ordenar(Function<Publicacion, Criterio> crearCriterio) {
        List<Publicacion> publicaciones = plataforma.getPublicaciones();

        Criterio[] criterios = new Criterio[publicaciones.size()];
        for (int i = 0; i < criterios.length; i++) {
            criterios[i] = crearCriterio.apply(publicaciones.get(i));
        }

        MergeSort.mergeSort(criterios, 0, criterios.length - 1);

        List<Publicacion> ordenadas = new ArrayList<>();
        for (Criterio c : criterios) {
            ordenadas.add(c.getPublicacion());
        }
        return ordenadas;
    }
}
