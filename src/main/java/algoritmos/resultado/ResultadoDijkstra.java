package algoritmos.resultado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResultadoDijkstra<T> {

    private final T origen;
    private final Map<T, Integer> distancias;
    private final Map<T, T> predecesores;
    private final ArrayList<String> traza;
    private final ArrayList<PasoDijkstra<T>> pasos;

    public ResultadoDijkstra(T origen, Map<T, Integer> distancias,
                             Map<T, T> predecesores,
                             ArrayList<String> traza,
                             ArrayList<PasoDijkstra<T>> pasos) {
        this.origen = origen;
        this.distancias = distancias;
        this.predecesores = predecesores;
        this.traza = traza;
        this.pasos = pasos;
    }

    public int getDistancia(T destino) {
        if (destino == null) { throw new IllegalArgumentException("El destino no puede ser nulo"); }
        if (!esAlcanzable(destino)) { throw new IllegalArgumentException("No es alcanzable"); }

        return distancias.get(destino);
    }

    public boolean esAlcanzable(T destino) {
        return distancias.containsKey(destino);
    }

    public ArrayList<T> getCamino(T destino) {
        if (destino == null) { throw new IllegalArgumentException("El destino no puede ser nulo"); }

        ArrayList<T> camino = new ArrayList<>();
        if (!esAlcanzable(destino)) { return camino; }

        T actual = destino;
        while (actual != null) {
            camino.add(actual);
            actual = predecesores.get(actual);
        }

        Collections.reverse(camino);
        return camino;
    }

    public String describirCadena(T destino) {
        if (destino == null) { throw new IllegalArgumentException("El destino no puede ser nulo"); }

        if (!esAlcanzable(destino)) {
            return "No existe una cadena de " + origen + " a " + destino;
        }

        StringBuilder sb = new StringBuilder("Cadena: ");
        ArrayList<T> camino = getCamino(destino);
        for (int i = 0; i < camino.size(); i++) {
            if (i > 0) { sb.append(" -> "); }
            sb.append(camino.get(i));
        }
        sb.append(" | Tiempo total: ").append(getDistancia(destino)).append(" min");
        return sb.toString();
    }

    public ArrayList<String> getTraza() {
        return traza;
    }

    public List<PasoDijkstra<T>> getPasos() {
        return Collections.unmodifiableList(pasos);
    }

    public T getOrigen() {
        return origen;
    }
}
