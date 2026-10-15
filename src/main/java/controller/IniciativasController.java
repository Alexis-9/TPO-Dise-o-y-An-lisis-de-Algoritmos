package main.java.controller;

import main.java.algoritmos.Mochila;
import main.java.model.Iniciativa;

import java.util.ArrayList;

public class IniciativasController {

    private final ArrayList<Iniciativa> iniciativas;
    private final Mochila<Iniciativa> mochilaIniciativas;

    public IniciativasController(int presupuesto, ArrayList<Iniciativa> iniciativas) {

        this.iniciativas = iniciativas;
        this.mochilaIniciativas = new Mochila<>(presupuesto, iniciativas);

    }

    public void agregarIniciativa(Iniciativa nuevaIniciativa) {

        if (iniciativas.contains(nuevaIniciativa)) { return; }

        iniciativas.add(nuevaIniciativa);
        mochilaIniciativas.agregarObjeto(nuevaIniciativa);

    }

    public void eliminarIniciativa(Iniciativa iniciativa) throws IllegalArgumentException {

        if (!iniciativas.contains(iniciativa)) { throw new IllegalArgumentException("No se encontró la iniciativa de ID: " + iniciativa.getId()); }
        mochilaIniciativas.eliminarObjeto(iniciativa);

    }

    public ArrayList<Iniciativa> totalIniciativas() { return iniciativas; }

    public ArrayList<Iniciativa> seleccionIniciativas() { return mochilaIniciativas.getSeleccionados(); }

    public int impactoObtenido() { return mochilaIniciativas.getValorTotal(); }

    public int costoTotal() { return mochilaIniciativas.getPesoTotal(); }

    public int presupuestoRestante() { return mochilaIniciativas.getSobrante(); }

}
