package web.dto;

import model.Usuario;
import service.ResultadoDifusion;

import java.util.List;

public record DifusionDto(boolean alcanzable, UsuarioDto origen, UsuarioDto destino,
                          List<UsuarioDto> cadena, Integer tiempoTotal,
                          List<PasoDto> pasos, List<String> decisiones, List<Integer> alcanzados) {

    public static DifusionDto desde(ResultadoDifusion r) {
        return new DifusionDto(r.alcanzable(), UsuarioDto.desde(r.origen()), UsuarioDto.desde(r.destino()),
                UsuarioDto.desde(r.cadena()), r.tiempoTotal(),
                r.pasos().stream().map(PasoDto::desde).toList(), r.decisiones(),
                r.alcanzados().stream().map(Usuario::getId).toList());
    }
}
