package algoritmos;

import algoritmos.resultado.CadenaDifusion;
import algoritmos.resultado.ResultadoBacktracking;
import model.interfaces.Arista;
import model.interfaces.Grafo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** B3: todas las cadenas de difusión que cumplen tiempo máximo, intermediarios y exclusiones. */
public class BackTracking<T> {

    private final Grafo<T> grafo;
    private final T destino;
    private final int tiempoMax;
    private final int maxIntermediarios;
    private final Set<T> excluidos;

    private final ArrayList<T> listaActual = new ArrayList<>();
    private int tiempoActual = 0;
    private final ArrayList<CadenaDifusion<T>> soluciones = new ArrayList<>();

    private BackTracking(Grafo<T> grafo, T destino, int tiempoMax,
                         int maxIntermediarios, Set<T> excluidos) {
        this.grafo = grafo;
        this.destino = destino;
        this.tiempoMax = tiempoMax;
        this.maxIntermediarios = maxIntermediarios;
        this.excluidos = excluidos;
    }

    public static <T> ResultadoBacktracking<T> cadenasAlternativas(
            Grafo<T> grafo, T origen, T destino,
            int tiempoMax, int maxIntermediarios, Set<T> excluidos) {

        if (grafo == null) { throw new IllegalArgumentException("El grafo no puede ser nulo"); }
        if (origen == null || destino == null) { throw new IllegalArgumentException("Origen y destino no pueden ser nulos"); }
        if (!grafo.contiene(origen)) { throw new IllegalArgumentException("El origen no pertenece al grafo"); }
        if (!grafo.contiene(destino)) { throw new IllegalArgumentException("El destino no pertenece al grafo"); }
        if (tiempoMax < 0) { throw new IllegalArgumentException("El tiempo máximo no puede ser negativo"); }
        if (maxIntermediarios < 0) { throw new IllegalArgumentException("El límite de intermediarios no puede ser negativo"); }

        Set<T> excl = excluidos == null ? new HashSet<>() : new HashSet<>(excluidos);
        BackTracking<T> bt = new BackTracking<>(grafo, destino, tiempoMax, maxIntermediarios, excl);

        if (!origen.equals(destino) && !excl.contains(origen) && !excl.contains(destino)) {
            bt.listaActual.add(origen);
            bt.backtracking();
        }
        return new ResultadoBacktracking<>(bt.soluciones);
    }

    private void backtracking() {
        T ultimo = listaActual.get(listaActual.size() - 1);

        for (Arista<T> arista : generarValoresPosibles(ultimo)) {
            agregar(arista);
            if (factible()) {
                if (esSolucion()) {
                    guardarSolucion();
                } else {
                    backtracking();
                }
            }
            eliminar(arista);
        }
    }

    private List<? extends Arista<T>> generarValoresPosibles(T ultimo) {
        return grafo.getAdyacentes(ultimo);
    }

    private void agregar(Arista<T> arista) {
        listaActual.add(arista.getDestino());
        tiempoActual += arista.getPeso();
    }


    private boolean factible() {
        T v = listaActual.get(listaActual.size() - 1);

        if (excluidos.contains(v)) { return false; }

        for (int i = 0; i < listaActual.size() - 1; i++) {
            if (listaActual.get(i).equals(v)) { return false; }
        }

        if (tiempoActual > tiempoMax) { return false; }


        int intermediarios = v.equals(destino) ? listaActual.size() - 2 : listaActual.size() - 1;
        return intermediarios <= maxIntermediarios;
    }

    private boolean esSolucion() {
        return listaActual.get(listaActual.size() - 1).equals(destino);
    }

    private void eliminar(Arista<T> arista) {
        listaActual.remove(listaActual.size() - 1);
        tiempoActual -= arista.getPeso();
    }

    private void guardarSolucion() {
        soluciones.add(new CadenaDifusion<>(new ArrayList<>(listaActual), tiempoActual));
    }
}