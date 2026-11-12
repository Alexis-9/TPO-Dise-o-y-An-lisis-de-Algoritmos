package model;

import java.util.List;

public class PlanAsignacion {

    private final List<Asignacion> asignaciones;
    private final int costoTotal;

    public PlanAsignacion(List<Asignacion> asignaciones, int costoTotal) {
        this.asignaciones = asignaciones;
        this.costoTotal = costoTotal;
    }

    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }

    public int getCostoTotal() {
        return costoTotal;
    }
}
