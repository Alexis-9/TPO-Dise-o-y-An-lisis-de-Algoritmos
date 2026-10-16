package main.java.controller;

import main.java.algoritmos.Mochila;
import main.java.algoritmos.resultado.ResultadoMochila;
import main.java.model.Iniciativa;

import java.util.ArrayList;

public class IniciativasController {

    private final int presupuesto;
    private final ArrayList<Iniciativa> iniciativas;
    private ResultadoMochila<Iniciativa> seleccionados;

    public IniciativasController(int presupuesto, ArrayList<Iniciativa> iniciativas) {

        this.iniciativas = iniciativas;
        this.presupuesto = presupuesto;
        this.seleccionados = Mochila.resolver(iniciativas, presupuesto);

    }

    public void agregarIniciativa(Iniciativa nuevaIniciativa) {

        if (iniciativas.contains(nuevaIniciativa)) { return; }

        iniciativas.add(nuevaIniciativa);
        seleccionados = Mochila.resolver(iniciativas, presupuesto);


    }

    public void eliminarIniciativa(int idIniciativa) throws IllegalArgumentException, IllegalStateException {

        if (iniciativas.size() == 1) { throw new IllegalStateException("No se puede hacer el cálculo sin iniciativas."); }

        Iniciativa iniciativa = buscarIniciativa(idIniciativa);
        if (iniciativa == null) { throw new IllegalArgumentException("No se encontró la iniciativa de ID: " + idIniciativa); }

        iniciativas.remove(iniciativa);
        seleccionados = Mochila.resolver(iniciativas, presupuesto);

    }

    public Iniciativa buscarIniciativa(int idIniciativa) {

        for (Iniciativa i : iniciativas) {
            if (i.getId() == idIniciativa) { return i; }
        }

        return null;
    }


    public ArrayList<Iniciativa> totalIniciativas() { return iniciativas; }

    public ArrayList<Iniciativa> seleccionIniciativas() { return seleccionados.getSeleccionados(); }

    public int impactoObtenido() { return seleccionados.getValorTotal(); }

    public int costoTotal() { return seleccionados.getPesoTotal(); }

    public int presupuestoRestante() { return seleccionados.getSobrante(); }

}
