package web.dto;

import service.ConsultaDifusion;

import java.util.List;

/** Respuesta de GET /api/consultas: un par resuelto desde las matrices de Floyd. */
public record ConsultaDto(boolean alcanzable, UsuarioDto origen, UsuarioDto destino,
                          List<UsuarioDto> cadena, Integer tiempoTotal) {

    public static ConsultaDto desde(ConsultaDifusion c) {
        return new ConsultaDto(c.alcanzable(), UsuarioDto.desde(c.origen()), UsuarioDto.desde(c.destino()),
                UsuarioDto.desde(c.cadena()), c.tiempoTotal());
    }
}
