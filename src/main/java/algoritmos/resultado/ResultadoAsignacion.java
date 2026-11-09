package algoritmos.resultado;

public class ResultadoAsignacion {

    private final int[] mejorAsignacion;
    private final int costoMinimo;

    public ResultadoAsignacion(int[] mejorAsignacion, int costoMinimo) {
        this.mejorAsignacion = mejorAsignacion;
        this.costoMinimo = costoMinimo;
    }

    public int[] getMejorAsignacion() {
        return mejorAsignacion;
    }

    public int getCostoMinimo() {
        return costoMinimo;
    }

    public boolean haySolucion() { return (mejorAsignacion != null); }
}
