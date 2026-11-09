package algoritmos;

import algoritmos.resultado.ResultadoFloyd;
import model.interfaces.Arista;
import model.interfaces.Grafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Floyd {

    private static final int INF = Integer.MAX_VALUE;

    private Floyd() {}

    public static <T> ResultadoFloyd<T> calcular(Grafo<T> grafo) {
        if (grafo == null) { throw new IllegalArgumentException("El grafo no puede ser nulo"); }

        List<T> vertices = armarListaVertices(grafo);
        Map<T, Integer> indice = armarIndice(vertices);
        int tamanio = vertices.size();

        int[][] matrizPesos = new int[tamanio][tamanio];
        T[][] matrizAristas = (T[][]) new Object[tamanio][tamanio];

        llenarMatrices(matrizPesos, matrizAristas, vertices);
        cargarAristas(grafo, matrizPesos, matrizAristas, vertices, indice);

        for (int i = 0; i < tamanio; i++) {              // i = vértice intermedio
            for (int j = 0; j < tamanio; j++) {          // j = origen
                if (matrizPesos[j][i] == INF) { continue; }

                for (int k = 0; k < tamanio; k++) {      // k = destino
                    if (matrizPesos[i][k] == INF) { continue; }

                    int pasandoPorI = matrizPesos[j][i] + matrizPesos[i][k];
                    if (pasandoPorI < matrizPesos[j][k]) {
                        matrizPesos[j][k] = pasandoPorI;
                        matrizAristas[j][k] = matrizAristas[j][i];
                    }
                }
            }
        }

        return new ResultadoFloyd<>(vertices, indice, matrizPesos, matrizAristas);
    }

    private static <T> List<T> armarListaVertices(Grafo<T> grafo) {
        return new ArrayList<>(grafo.getVertices());
    }

    private static <T> Map<T, Integer> armarIndice(List<T> vertices) {
        Map<T, Integer> indice = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            indice.put(vertices.get(i), i);
        }
        return indice;
    }

    private static <T> void llenarMatrices(int[][] matrizPesos, T[][] matrizAristas, List<T> vertices) {
        int tamanio = vertices.size();

        for (int i = 0; i < tamanio; i++) {
            for (int j = 0; j < tamanio; j++) {
                if (i == j) {
                    matrizPesos[i][j] = 0;
                    matrizAristas[i][j] = vertices.get(i);
                } else {
                    matrizPesos[i][j] = INF;
                    matrizAristas[i][j] = null;
                }
            }
        }
    }

    private static <T> void cargarAristas(Grafo<T> grafo, int[][] matrizPesos, T[][] matrizAristas,
                                          List<T> vertices, Map<T, Integer> indice) {
        for (T vertice : vertices) {
            int i = indice.get(vertice);

            for (Arista<T> arista : grafo.getAdyacentes(vertice)) {
                T destino = arista.getDestino();
                int j = indice.get(destino);

                if (arista.getPeso() < matrizPesos[i][j]) {
                    matrizPesos[i][j] = arista.getPeso();
                    matrizAristas[i][j] = destino;
                }
            }
        }
    }
}