package algoritmos;

import algoritmos.resultado.PasoDijkstra;
import algoritmos.resultado.ResultadoDijkstra;
import model.interfaces.Arista;
import model.interfaces.Grafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dijsktra {

    private Dijsktra() { }


    public static <T extends Comparable<T>> ResultadoDijkstra<T> calcular(Grafo<T> grafo, T origen) {
        if (grafo == null) { throw new IllegalArgumentException("El grafo no puede ser nulo"); }
        if (origen == null) { throw new IllegalArgumentException("El origen no puede ser nulo"); }
        if (!grafo.contiene(origen)) { throw new IllegalArgumentException("El origen no pertenece al grafo"); }

        Map<T, Integer> distancias = new HashMap<>();
        Map<T, T> predecesores = new HashMap<>();
        Set<T> visitados = new HashSet<>();
        ArrayList<String> traza = new ArrayList<>();
        ArrayList<PasoDijkstra<T>> pasos = new ArrayList<>();

        distancias.put(origen, 0);
        traza.add("Inicio: " + origen + " con tiempo 0 min");

        int n = grafo.getVertices().size();
        for (int i = 0; i < n; i++) {

            T actual = seleccionarMasCercano(grafo, distancias, visitados);
            if (actual == null) {
                traza.add("No quedan vértices alcanzables sin visitar: fin del cálculo");
                break;
            }

            visitados.add(actual);
            int tiempoActual = distancias.get(actual);
            traza.add("Selecciona " + actual + ": tiempo definitivo " + tiempoActual + " min");
            ArrayList<PasoDijkstra.Actualizacion<T>> actualizaciones = new ArrayList<>();
            ArrayList<String> decisiones = new ArrayList<>();
            decisiones.add(traza.get(traza.size() - 1));
            pasos.add(new PasoDijkstra<>(actual, tiempoActual, actualizaciones, decisiones));

            for (Arista<T> arista : grafo.getAdyacentes(actual)) {
                T vecino = arista.getDestino();
                if (visitados.contains(vecino)) { continue; }

                int tiempoNuevo = tiempoActual + arista.getPeso();
                Integer tiempoConocido = distancias.get(vecino);

                if (tiempoConocido == null || tiempoNuevo < tiempoConocido) {
                    distancias.put(vecino, tiempoNuevo);
                    predecesores.put(vecino, actual);
                    traza.add("   Actualiza " + vecino + ": "
                            + (tiempoConocido == null ? "sin camino" : tiempoConocido + " min")
                            + " -> " + tiempoNuevo + " min (vía " + actual + ")");
                    actualizaciones.add(new PasoDijkstra.Actualizacion<>(vecino, tiempoConocido, tiempoNuevo));
                    decisiones.add(traza.get(traza.size() - 1));
                } else {
                    traza.add("   Descarta " + arista + ": " + tiempoNuevo
                            + " min no mejora " + tiempoConocido + " min");
                    decisiones.add(traza.get(traza.size() - 1));
                }
            }
        }

        return new ResultadoDijkstra<>(origen, distancias, predecesores, traza, pasos);
    }

    private static <T extends Comparable<T>> T seleccionarMasCercano(Grafo<T> grafo, Map<T, Integer> distancias,
                                                                      Set<T> visitados) {
        T mejor = null;
        int mejorTiempo = 0;

        for (T elem : grafo.getVertices()) {
            if (visitados.contains(elem) || !distancias.containsKey(elem)) { continue; }

            int tiempo = distancias.get(elem);
            if (mejor == null || tiempo < mejorTiempo
                    || (tiempo == mejorTiempo && elem.compareTo(mejor) < 0)) {
                mejor = elem;
                mejorTiempo = tiempo;
            }
        }
        return mejor;
    }
}
