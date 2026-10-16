package web.dto;

import model.Iniciativa;

public record IniciativaDto(int id, int costo, int impacto) {

    public static IniciativaDto desde(Iniciativa iniciativa) {
        return new IniciativaDto(iniciativa.getId(), iniciativa.getCosto(), iniciativa.getImpacto());
    }
}
