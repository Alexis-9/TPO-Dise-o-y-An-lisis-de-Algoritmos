package web.dto;

import algoritmos.resultado.PasoDijkstra;
import model.Usuario;

import java.util.List;

/** Un paso de Dijkstra con los usuarios como ids. */
public record PasoDto(int elegido, int distancia, List<ActualizacionDto> actualizaciones, List<String> decisiones) {

    /** anterior es null si el usuario todavía no se había alcanzado. */
    public record ActualizacionDto(int usuario, Integer anterior, int nuevo) { }

    public static PasoDto desde(PasoDijkstra<Usuario> paso) {
        List<ActualizacionDto> actualizaciones = paso.actualizaciones().stream()
                .map(a -> new ActualizacionDto(a.vertice().getId(), a.anterior(), a.nuevo()))
                .toList();
        return new PasoDto(paso.elegido().getId(), paso.distancia(), actualizaciones, paso.decisiones());
    }
}
