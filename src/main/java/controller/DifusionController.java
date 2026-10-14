package main.java.controller;

import main.java.algoritmos.Dijsktra;
import main.java.algoritmos.resultado.ResultadoDijkstra;
import main.java.model.RedSocial;
import main.java.model.Usuario;

public class DifusionController {

    private final RedSocial red;

    public DifusionController(RedSocial red) {
        this.red = red;
    }

    public ResultadoDijkstra<Usuario> cadenaMasRapida(int idOrigen){
        return Dijsktra.calcular(red, buscarUsuario(idOrigen));
    }

    public Usuario buscarUsuario(int idUsuario){
        Usuario u = red.getUsuario(idUsuario);
        if (u == null){throw new IllegalArgumentException("No existe ese usuario.");}
        return u;
    }
}
