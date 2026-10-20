package controller;

import algoritmos.Dijsktra;
import algoritmos.resultado.ResultadoDijkstra;
import model.RedSocial;
import model.Usuario;

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
