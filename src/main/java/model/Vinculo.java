package main.java.model;

public class Vinculo {

    private final Usuario origen;
    private final Usuario destino;
    private final int minutos;

    public Vinculo(Usuario origen, Usuario destino, int minutos) {

        if (minutos <= 0){ throw new IllegalArgumentException("El tiempo debe ser positivo");}

        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
    }

    public Usuario getOrigen() {return origen; }
    public Usuario getDestino() {return destino; }
    public int getMinutos() {return minutos; }

    @Override
    public String toString() {
        return origen.getNombre() + " -> " + destino.getNombre() + " (" + minutos + " min)";
    }
}
