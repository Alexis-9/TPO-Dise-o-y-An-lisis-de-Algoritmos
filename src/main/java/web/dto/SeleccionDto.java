package web.dto;

import model.Iniciativa;
import service.SeleccionIniciativas;

import java.util.List;

public record SeleccionDto(int presupuesto, List<Integer> seleccionadas, int costoTotal, int impactoTotal, int restante) {

    public static SeleccionDto desde(SeleccionIniciativas s) {
        return new SeleccionDto(s.presupuesto(), s.seleccionadas().stream().map(Iniciativa::getId).toList(),
                s.costoTotal(), s.impactoTotal(), s.restante());
    }
}
