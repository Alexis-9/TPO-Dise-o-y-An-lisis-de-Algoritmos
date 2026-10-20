package service;

import algoritmos.Dijsktra;
import algoritmos.MergeSort;
import algoritmos.resultado.ResultadoDijkstra;
import datos.Plataforma;
import model.RedSocial;
import model.Usuario;

import java.util.ArrayList;
import java.util.List;

/** Cadena de difusión más rápida entre dos usuarios (Dijkstra). */
public class DifusionService {

    private final Plataforma plataforma;

    public DifusionService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /** Un destino inalcanzable no es un error: el resultado lo indica con alcanzable = false. */
    public ResultadoDifusion calcular(int idOrigen, int idDestino) {
        RedSocial red = plataforma.copiaDeRed();
        Usuario origen = buscarUsuario(red, idOrigen);
        Usuario destino = buscarUsuario(red, idDestino);

        ResultadoDijkstra<Usuario> resultado = Dijsktra.calcular(red, origen);

        boolean alcanzable = resultado.esAlcanzable(destino);
        Integer tiempoTotal = alcanzable ? resultado.getDistancia(destino) : null;

        return new ResultadoDifusion(origen, destino, alcanzable,
                resultado.getCamino(destino), tiempoTotal,
                resultado.getPasos(), resultado.getTraza(),
                alcanzados(red, resultado));
    }

    private Usuario buscarUsuario(RedSocial red, int idUsuario) {
        Usuario usuario = red.getUsuario(idUsuario);
        if (usuario == null) { throw new IllegalArgumentException("No existe un usuario con id " + idUsuario); }
        return usuario;
    }

    private List<Usuario> alcanzados(RedSocial red, ResultadoDijkstra<Usuario> resultado) {
        Usuario[] usuarios = red.getUsuarios().toArray(new Usuario[0]);
        MergeSort.mergeSort(usuarios, 0, usuarios.length - 1);

        List<Usuario> alcanzados = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (resultado.esAlcanzable(u)) { alcanzados.add(u); }
        }
        return alcanzados;
    }
}
