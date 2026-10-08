package main.java.model;

public class Iniciativa {

    private final int id;
    private final int costo;
    private final int impacto;

    public Iniciativa(int id, int costo, int impacto) {

        if (costo <= 0) { throw new IllegalArgumentException("El costo debe ser positivo");}

        if (impacto < 0) { throw new IllegalArgumentException("El impacto no puede ser negativo");}

        this.id = id;
        this.costo = costo;
        this.impacto = impacto;
    }

    public int getId() {return id;}
    public int getCosto() {return costo;}
    public int getImpacto() {return impacto;}

    @Override
    public String toString() {
        return "Iniciativa #" + id + " (costo=" + costo + ", impacto=" + impacto + ")";
    }
}
