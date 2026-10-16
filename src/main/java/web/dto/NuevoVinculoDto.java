package web.dto;

/** Cuerpo de POST /api/vinculos. */
public record NuevoVinculoDto(int origen, int destino, int minutos) { }
