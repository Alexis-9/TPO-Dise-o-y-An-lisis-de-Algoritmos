package model;

import java.util.List;

public class Asignacion {

    private final Moderador moderador;
    private final List<Comunidad> comunidadesAsignadas;
    private final int costo;

    public Asignacion(Moderador moderador, List<Comunidad> comunidadesAsignadas, int costo) {
        this.moderador = moderador;
        this.comunidadesAsignadas = comunidadesAsignadas;
        this.costo = costo;
    }

    public Moderador getModerador() {
        return moderador;
    }

    public List<Comunidad> getComunidadesAsignadas() {
        return comunidadesAsignadas;
    }

    public int getCosto() {
        return costo;
    }
}
