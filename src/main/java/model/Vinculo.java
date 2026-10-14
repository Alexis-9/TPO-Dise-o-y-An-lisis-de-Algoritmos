package main.java.model;

import main.java.model.interfaces.Arista;

public class Vinculo implements Arista<Usuario> {

    private final Usuario origen;
    private final Usuario destino;
    private final int minutos;

    public Vinculo(Usuario origen, Usuario destino, int minutos) {

        if (minutos <= 0){ throw new IllegalArgumentException("El tiempo debe ser positivo");}

        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
    }

    @Override
    public Usuario getOrigen() {return origen;}

    @Override
    public Usuario getDestino() {return destino;}

    @Override
    public int getPeso() {return minutos;}

    @Override
    public String toString() {
        return origen.getNombre() + " -> " + destino.getNombre() + " (" + minutos + " min)";
    }
}
