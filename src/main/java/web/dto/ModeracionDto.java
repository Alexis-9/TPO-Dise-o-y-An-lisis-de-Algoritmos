package web.dto;

import model.Comunidad;
import model.Moderador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Respuesta de GET /api/moderacion: candidatos, comunidades y la tabla de costos cargada. */
public record ModeracionDto(List<ModeradorDto> moderadores, List<ComunidadDto> comunidades, List<CostoDto> costos) {

    public record ModeradorDto(int id, String nombre, int limite) { }

    public record ComunidadDto(int id, String nombre) { }

    public static ModeracionDto desde(List<Moderador> moderadores, List<Comunidad> comunidades,
                                      Map<Integer, Map<Integer, Integer>> costos) {
        List<CostoDto> listaCostos = new ArrayList<>();
        for (Moderador m : moderadores) {
            Map<Integer, Integer> deModerador = costos.getOrDefault(m.getId(), Map.of());
            for (Comunidad c : comunidades) {
                Integer costo = deModerador.get(c.getId());
                if (costo != null) { listaCostos.add(new CostoDto(m.getId(), c.getId(), costo)); }
            }
        }
        return new ModeracionDto(
                moderadores.stream().map(m -> new ModeradorDto(m.getId(), m.getNombre(), m.getLimiteComunidades())).toList(),
                comunidades.stream().map(c -> new ComunidadDto(c.getId(), c.getNombre())).toList(),
                listaCostos);
    }
}
