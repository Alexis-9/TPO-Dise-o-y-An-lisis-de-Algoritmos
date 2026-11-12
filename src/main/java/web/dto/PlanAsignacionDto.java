package web.dto;

import model.Asignacion;
import model.Comunidad;
import model.PlanAsignacion;

import java.util.List;

/** Respuesta de GET /api/moderacion/asignacion: qué comunidades atiende cada moderador y a qué costo. */
public record PlanAsignacionDto(int costoTotal, List<AsignacionDto> asignaciones) {

    public record AsignacionDto(int moderador, List<Integer> comunidades, int costo) { }

    public static PlanAsignacionDto desde(PlanAsignacion plan) {
        List<AsignacionDto> asignaciones = plan.getAsignaciones().stream()
                .map(PlanAsignacionDto::desde)
                .toList();
        return new PlanAsignacionDto(plan.getCostoTotal(), asignaciones);
    }

    private static AsignacionDto desde(Asignacion a) {
        return new AsignacionDto(a.getModerador().getId(),
                a.getComunidadesAsignadas().stream().map(Comunidad::getId).toList(), a.getCosto());
    }
}
