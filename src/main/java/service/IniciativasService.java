package service;

import algoritmos.Mochila;
import algoritmos.resultado.ResultadoMochila;
import datos.Plataforma;
import model.Iniciativa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Selección de iniciativas que maximiza el impacto sin pasar el presupuesto (mochila 0/1). */
public class IniciativasService {

    private final Plataforma plataforma;

    public IniciativasService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /** Iniciativas ordenadas por id. */
    public List<Iniciativa> listar() {
        List<Iniciativa> iniciativas = plataforma.getIniciativas();
        iniciativas.sort(Comparator.comparingInt(Iniciativa::getId));
        return iniciativas;
    }

    public SeleccionIniciativas seleccionar(int presupuesto) {
        if (presupuesto < 0) { throw new IllegalArgumentException("El presupuesto no puede ser negativo"); }

        ArrayList<Iniciativa> iniciativas = new ArrayList<>(listar());

        // Mochila falla con la lista vacía (bug conocido, lo arregla otro integrante):
        // sin iniciativas no hay nada que elegir, así que no se la llama.
        if (iniciativas.isEmpty()) {
            return new SeleccionIniciativas(presupuesto, List.of(), 0, 0, presupuesto);
        }

        // La tabla de la mochila tiene una columna por unidad de presupuesto. Si alcanza para
        // todas, de más no cambia la elección: se acota a la suma de los costos para que la
        // tabla no crezca sin sentido (y el restante se calcula sobre el presupuesto real).
        long sumaCostos = 0;
        for (Iniciativa i : iniciativas) {
            sumaCostos += i.getCosto();
        }
        int capacidad = (int) Math.min(presupuesto, sumaCostos);

        ResultadoMochila<Iniciativa> resultado = Mochila.resolver(iniciativas, capacidad);

        return new SeleccionIniciativas(presupuesto, List.copyOf(resultado.getSeleccionados()),
                resultado.getPesoTotal(), resultado.getValorTotal(),
                presupuesto - resultado.getPesoTotal());
    }

    public Iniciativa agregar(int costo, int impacto) {
        // Iniciativa valida costo > 0 e impacto >= 0
        return plataforma.agregarIniciativa(costo, impacto);
    }

    public void eliminar(int idIniciativa) {
        plataforma.eliminarIniciativa(idIniciativa);
    }
}
