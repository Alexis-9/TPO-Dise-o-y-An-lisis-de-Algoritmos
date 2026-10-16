package web.dto;

import model.Usuario;
import model.Vinculo;

import java.util.List;

public record RedDto(List<UsuarioDto> usuarios, List<VinculoDto> vinculos) {

    public static RedDto desde(List<Usuario> usuarios, List<Vinculo> vinculos) {
        return new RedDto(UsuarioDto.desde(usuarios), vinculos.stream().map(VinculoDto::desde).toList());
    }
}
