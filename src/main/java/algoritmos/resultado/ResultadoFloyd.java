package algoritmos.resultado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultadoFloyd<T> {

    private static final int INF = Integer.MAX_VALUE;

    private final List<T> vertices;
    private final Map<T, Integer> indice;
    private final int[][] pesos;
    private final T[][] siguientes;

    public ResultadoFloyd(List<T> vertices, Map<T, Integer> indice, int[][] pesos, T[][] siguientes) {
        this.vertices = vertices;
        this.indice = indice;
        this.pesos = pesos;
        this.siguientes = siguientes;
    }

    public boolean esAlcanzable(T origen, T destino) {
        return pesos[posicion(origen)][posicion(destino)] != INF;
    }

    public int getDistancia(T origen, T destino) {
        if (!esAlcanzable(origen, destino)) { throw new IllegalArgumentException("No es alcanzable"); }

        return pesos[posicion(origen)][posicion(destino)];
    }

    public List<T> getCamino(T origen, T destino) {
        List<T> camino = new ArrayList<>();
        if (!esAlcanzable(origen, destino)) { return camino; }

        int j = posicion(destino);
        T actual = origen;
        camino.add(actual);

        while (!actual.equals(destino)) {
            actual = siguientes[posicion(actual)][j];
            camino.add(actual);
        }
        return camino;
    }

    public String describirCadena(T origen, T destino) {
        if (!esAlcanzable(origen, destino)) {
            return "No existe una cadena de " + origen + " a " + destino;
        }

        StringBuilder sb = new StringBuilder("Cadena: ");
        List<T> camino = getCamino(origen, destino);
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) { sb.append(" -> "); }
            sb.append(camino.get(i));
        }
        sb.append(" | Tiempo total: ").append(getDistancia(origen, destino)).append(" min");
        return sb.toString();
    }

    public List<T> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    private int posicion(T vertice) {
        if (vertice == null) { throw new IllegalArgumentException("El vértice no puede ser nulo"); }

        Integer i = indice.get(vertice);
        if (i == null) { throw new IllegalArgumentException("El vértice " + vertice + " no pertenece al grafo"); }
        return i;
    }
}