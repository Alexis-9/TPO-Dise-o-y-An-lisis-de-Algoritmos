package web.dto;

import service.TablaDifusion;

import java.util.List;

/**
 * Respuesta de GET /api/consultas/tabla.
 *
 * @param tiempos    tiempos[i][j] son los minutos de usuarios[i] a usuarios[j], o null si no hay cadena
 * @param recalculada true si para responder hubo que volver a correr Floyd (la red había cambiado)
 */
public record TablaDto(List<UsuarioDto> usuarios, Integer[][] tiempos, boolean recalculada) {

    public static TablaDto desde(TablaDifusion t, boolean recalculada) {
        return new TablaDto(UsuarioDto.desde(t.usuarios()), t.tiempos(), recalculada);
    }
}
