package main.java.algoritmos;

import main.java.algoritmos.resultado.ResultadoDijkstra;
import main.java.model.RedSocial;
import main.java.model.Usuario;
import main.java.model.Vinculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dijsktra {

    private Dijsktra() { }


    public static ResultadoDijkstra calcular(RedSocial red, Usuario origen) {
        if (red == null) { throw new IllegalArgumentException("La red no puede ser nula"); }
        if (origen == null) { throw new IllegalArgumentException("El origen no puede ser nulo"); }

        Usuario inicio = red.getUsuario(origen.getId());
        if (inicio == null) { throw new IllegalArgumentException("El origen no pertenece a la red"); }

        Map<Usuario, Integer> distancias = new HashMap<>();
        Map<Usuario, Usuario> predecesores = new HashMap<>();
        Set<Usuario> visitados = new HashSet<>();
        ArrayList<String> traza = new ArrayList<>();

        distancias.put(inicio, 0);
        traza.add("Inicio: " + inicio + " con tiempo 0 min");

        int n = red.getUsuarios().size();
        for (int i = 0; i < n; i++) {

            Usuario actual = seleccionarMasCercano(red, distancias, visitados);
            if (actual == null) {
                traza.add("No quedan usuarios alcanzables sin visitar: fin del cálculo");
                break;
            }

            visitados.add(actual);
            int tiempoActual = distancias.get(actual);
            traza.add("Selecciona " + actual + ": tiempo definitivo " + tiempoActual + " min");

            for (Vinculo vinculo : red.getVinculosSalientes(actual)) {
                Usuario vecino = vinculo.getDestino();
                if (visitados.contains(vecino)) { continue; }

                int tiempoNuevo = tiempoActual + vinculo.getMinutos();
                Integer tiempoConocido = distancias.get(vecino);

                if (tiempoConocido == null || tiempoNuevo < tiempoConocido) {
                    distancias.put(vecino, tiempoNuevo);
                    predecesores.put(vecino, actual);
                    traza.add("   Actualiza " + vecino + ": "
                            + (tiempoConocido == null ? "sin camino" : tiempoConocido + " min")
                            + " -> " + tiempoNuevo + " min (vía " + actual + ")");
                } else {
                    traza.add("   Descarta " + vinculo + ": " + tiempoNuevo
                            + " min no mejora " + tiempoConocido + " min");
                }
            }
        }

        return new ResultadoDijkstra(inicio, distancias, predecesores, traza);
    }

    private static Usuario seleccionarMasCercano(RedSocial red, Map<Usuario, Integer> distancias,
                                                 Set<Usuario> visitados) {
        Usuario mejor = null;
        int mejorTiempo = 0;

        for (Usuario u : red.getUsuarios()) {
            if (visitados.contains(u) || !distancias.containsKey(u)) { continue; }

            int tiempo = distancias.get(u);
            if (mejor == null || tiempo < mejorTiempo
                    || (tiempo == mejorTiempo && u.getId() < mejor.getId())) {
                mejor = u;
                mejorTiempo = tiempo;
            }
        }
        return mejor;
    }
}
