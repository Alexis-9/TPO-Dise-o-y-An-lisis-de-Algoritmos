package service;

import algoritmos.BackTracking;
import algoritmos.resultado.ResultadoBacktracking;
import datos.Plataforma;
import model.RedSocial;
import model.Usuario;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Cadenas alternativas de difusión entre dos usuarios (Backtracking). */
public class CadenasAlternativasService {

    private final Plataforma plataforma;

    public CadenasAlternativasService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /**
     * Todas las cadenas de origen a destino que no repiten usuarios, no pasan de tiempoMax minutos,
     * tienen a lo sumo maxIntermediarios usuarios entre medio y no incluyen a ningún excluido.
     *
     * Que no haya ninguna cadena no es un error: el resultado lo indica con hayCadenas() = false.
     * Los ids inexistentes y los límites negativos se rechazan con IllegalArgumentException.
     */
    public ResultadoBacktracking<Usuario> calcular(int idOrigen, int idDestino, int tiempoMax,
                                                   int maxIntermediarios, List<Integer> idsExcluidos) {
        RedSocial red = plataforma.copiaDeRed();
        Usuario origen = buscarUsuario(red, idOrigen);
        Usuario destino = buscarUsuario(red, idDestino);

        Set<Usuario> excluidos = new HashSet<>();
        if (idsExcluidos != null) {
            for (Integer id : idsExcluidos) {
                excluidos.add(buscarUsuario(red, id));
            }
        }

        return BackTracking.cadenasAlternativas(red, origen, destino, tiempoMax, maxIntermediarios, excluidos);
    }

    private Usuario buscarUsuario(RedSocial red, int idUsuario) {
        Usuario usuario = red.getUsuario(idUsuario);
        if (usuario == null) { throw new IllegalArgumentException("No existe un usuario con id " + idUsuario); }
        return usuario;
    }
}