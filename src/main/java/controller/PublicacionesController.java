package main.java.controller;

import main.java.algoritmos.MergeSort;
import main.java.model.Publicacion;
import main.java.model.criterio.Criterio;
import main.java.model.criterio.CriterioFecha;
import main.java.model.criterio.CriterioRelevancia;

import java.util.ArrayList;
import java.util.function.Function;

public class PublicacionesController {

    private final ArrayList<Publicacion> publicaciones;

    public PublicacionesController(ArrayList<Publicacion> publicaciones) {
        this.publicaciones = publicaciones;
    }

    public void agregarPublicacion(Publicacion nueva) {
        if (nueva == null) { throw new IllegalArgumentException("La publicación no puede ser nula."); }

        for (Publicacion p : publicaciones) {
            if (p.getId() == nueva.getId()) {
                throw new IllegalArgumentException("Ya existe una publicación con ID: " + nueva.getId());
            }
        }
        publicaciones.add(nueva);
    }

    public ArrayList<Publicacion> listarMasRecientes() {
        return ordenar(publicacion -> new CriterioFecha(publicacion));
    }

    public ArrayList<Publicacion> listarMasRelevantes() {
        return ordenar(publicacion -> new CriterioRelevancia(publicacion));
    }

    private ArrayList<Publicacion> ordenar(Function<Publicacion, Criterio> crearCriterio) {
        Criterio[] criterios = new Criterio[publicaciones.size()];
        for (int i = 0; i < criterios.length; i++) {
            criterios[i] = crearCriterio.apply(publicaciones.get(i));
        }

        MergeSort.mergeSort(criterios, 0, criterios.length - 1);

        ArrayList<Publicacion> resultado = new ArrayList<>();
        for (Criterio c : criterios) {
            resultado.add(c.getPublicacion());
        }
        return resultado;
    }
}