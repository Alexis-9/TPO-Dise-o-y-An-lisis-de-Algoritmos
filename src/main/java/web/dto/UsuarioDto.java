package web.dto;

import model.Usuario;

import java.util.List;

public record UsuarioDto(int id, String nombre) {

    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getNombre());
    }

    public static List<UsuarioDto> desde(List<Usuario> usuarios) {
        return usuarios.stream().map(UsuarioDto::desde).toList();
    }
}
