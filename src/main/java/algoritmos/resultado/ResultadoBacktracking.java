package algoritmos.resultado;

import algoritmos.resultado.CadenaDifusion;
import java.util.ArrayList;
import java.util.List;

public class ResultadoBacktracking<T> {

    private final List<CadenaDifusion<T>> cadenas;

    public ResultadoBacktracking(List<CadenaDifusion<T>> cadenas) {
        this.cadenas = new ArrayList<>(cadenas);
    }

    public List<CadenaDifusion<T>> getCadenas() {
        return new ArrayList<>(cadenas);
    }

    public boolean hayCadenas() {
        return !cadenas.isEmpty();
    }

    public CadenaDifusion<T> getMejor() {
        CadenaDifusion<T> mejor = null;

        for (CadenaDifusion<T> c : cadenas) {
            if (mejor == null
                    || c.getTiempoTotal() < mejor.getTiempoTotal()
                    || (c.getTiempoTotal() == mejor.getTiempoTotal()
                    && c.getUsuariosCadena().size() < mejor.getUsuariosCadena().size())) {
                mejor = c;
            }
        }
        return mejor;
    }
}