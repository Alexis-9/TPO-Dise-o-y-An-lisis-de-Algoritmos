package algoritmos.resultado;

import java.util.ArrayList;
import java.util.List;

public class CadenaDifusion<T> {

    private final List<T> usuariosCadena;
    private final int tiempoTotal;

    public CadenaDifusion(List<T> usuarios, int tiempoTotal) {
        this.usuariosCadena = new ArrayList<>(usuarios);
        this.tiempoTotal = tiempoTotal;
    }

    public List<T> getUsuariosCadena() {
        return new ArrayList<>(usuariosCadena);
    }

    public int getTiempoTotal() {
        return tiempoTotal;
    }

}