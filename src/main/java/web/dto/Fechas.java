package web.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Las fechas viajan como texto ISO, en el formato del input datetime-local: 2026-10-16T18:30. */
public final class Fechas {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Fechas() { }

    public static String aTexto(LocalDateTime fechaHora) {
        return fechaHora.format(FORMATO);
    }

    /** null si no vino; acepta también segundos (2026-10-16T18:30:00). */
    public static LocalDateTime desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) { return null; }
        try {
            return LocalDateTime.parse(texto.strip());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha y hora inválida: usar el formato 2026-10-16T18:30");
        }
    }
}
