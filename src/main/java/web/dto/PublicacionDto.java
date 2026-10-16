package web.dto;

import model.Publicacion;

public record PublicacionDto(int id, String autor, String fechaHora, int reacciones, int comentarios, int relevancia) {

    public static PublicacionDto desde(Publicacion p) {
        return new PublicacionDto(p.getId(), p.getAutor(), Fechas.aTexto(p.getFechaHora()),
                p.getReacciones(), p.getComentarios(), p.getRelevancia());
    }
}
