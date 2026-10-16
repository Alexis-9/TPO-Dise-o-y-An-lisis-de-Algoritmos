package algoritmos.resultado;

import java.util.Collections;
import java.util.List;

/**
 * Un paso de Dijkstra: el vértice que se elige (su distancia ya es definitiva),
 * las distancias que mejoraron al recorrer sus aristas salientes y las líneas
 * de la traza de texto que se escribieron en ese paso.
 */
public record PasoDijkstra<T>(T elegido, int distancia,
                              List<Actualizacion<T>> actualizaciones,
                              List<String> decisiones) {

    /** Una distancia que mejoró: anterior es null si el vértice todavía no se había alcanzado. */
    public record Actualizacion<T>(T vertice, Integer anterior, int nuevo) { }

    @Override
    public List<Actualizacion<T>> actualizaciones() {
        return Collections.unmodifiableList(actualizaciones);
    }

    @Override
    public List<String> decisiones() {
        return Collections.unmodifiableList(decisiones);
    }
}
