package web.dto;

/** Costo de que un moderador atienda una comunidad. También es el cuerpo de PUT /api/costos. */
public record CostoDto(Integer moderador, Integer comunidad, Integer costo) { }
