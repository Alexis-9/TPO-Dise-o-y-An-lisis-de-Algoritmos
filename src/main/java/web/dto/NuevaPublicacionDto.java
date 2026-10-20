package web.dto;

import java.time.LocalDateTime;

/** Cuerpo de POST /api/publicaciones. */
public record NuevaPublicacionDto(String autor, String fechaHora, int reacciones, int comentarios) {

    public LocalDateTime fecha() {
        return Fechas.desdeTexto(fechaHora);
    }
}
