package main.java.algoritmos.resultado;

import main.java.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ResultadoDijkstra {

    private final Usuario origen;
    private final Map<Usuario, Integer> distancias;
    private final Map<Usuario, Usuario> predecesores;
    private final ArrayList<String> traza;

    public ResultadoDijkstra(Usuario origen, Map<Usuario, Integer> distancias,
                             Map<Usuario, Usuario> predecesores,
                             ArrayList<String> traza) {
        this.origen = origen;
        this.distancias = distancias;
        this.predecesores = predecesores;
        this.traza = traza;
    }

    public int getDistancia(Usuario destino) {
        if (destino == null) {throw new IllegalArgumentException("Usuario no puede ser nulo");}
        if (!esAlcanzable(destino)){ throw new IllegalArgumentException("No es alcanzable");}

        return distancias.get(destino);
    }

    public boolean esAlcanzable(Usuario destino){
        return distancias.containsKey(destino);
    }

    public ArrayList<Usuario> getCamino(Usuario destino) {
        if (destino == null) { throw new IllegalArgumentException("Usuario no puede ser nulo"); }

        ArrayList<Usuario> camino = new ArrayList<>();
        if (!esAlcanzable(destino)) { return camino; }

        Usuario actual = destino;
        while (actual != null) {
            camino.add(actual);
            actual = predecesores.get(actual);
        }

        Collections.reverse(camino);
        return camino;
    }

    public ArrayList<String> getTraza(){
        return traza;
    }

    public Usuario getOrigen(){
        return origen;
    }
}
