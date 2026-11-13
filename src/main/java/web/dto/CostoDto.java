package web.dto;

/**
 * Costo de que un moderador atienda una comunidad. También es el cuerpo de PUT /api/costos.
 * Si {@code incompatible} es true, el moderador no puede atender esa comunidad y {@code costo} va en null.
 */
public record CostoDto(Integer moderador, Integer comunidad, Integer costo, Boolean incompatible) { }
