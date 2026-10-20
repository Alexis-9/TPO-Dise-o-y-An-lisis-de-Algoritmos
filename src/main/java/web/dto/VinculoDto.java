package web.dto;

import model.Vinculo;

public record VinculoDto(int origen, int destino, int minutos) {

    public static VinculoDto desde(Vinculo vinculo) {
        return new VinculoDto(vinculo.getOrigen().getId(), vinculo.getDestino().getId(), vinculo.getPeso());
    }
}
