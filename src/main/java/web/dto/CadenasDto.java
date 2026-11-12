package web.dto;

import algoritmos.resultado.CadenaDifusion;
import algoritmos.resultado.ResultadoBacktracking;
import model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta de GET /api/cadenas.
 *
 * @param cadenas en el orden en que las encontró el backtracking
 * @param mejor   posición de la de menor tiempo dentro de cadenas, o null si no hay ninguna
 */
public record CadenasDto(List<CadenaDto> cadenas, Integer mejor) {

    /** intermediarios = usuarios de la cadena sin contar origen y destino. */
    public record CadenaDto(List<UsuarioDto> usuarios, int tiempoTotal, int intermediarios) { }

    public static CadenasDto desde(ResultadoBacktracking<Usuario> r) {
        List<CadenaDifusion<Usuario>> cadenas = r.getCadenas();
        CadenaDifusion<Usuario> mejor = r.getMejor();

        List<CadenaDto> dtos = new ArrayList<>();
        Integer posicionMejor = null;
        for (int i = 0; i < cadenas.size(); i++) {
            CadenaDifusion<Usuario> c = cadenas.get(i);
            List<Usuario> usuarios = c.getUsuariosCadena();
            dtos.add(new CadenaDto(UsuarioDto.desde(usuarios), c.getTiempoTotal(), usuarios.size() - 2));
            if (c == mejor) { posicionMejor = i; }
        }
        return new CadenasDto(dtos, posicionMejor);
    }
}
