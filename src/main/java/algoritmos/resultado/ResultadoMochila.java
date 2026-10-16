package algoritmos.resultado;

import model.interfaces.Valuable;

import java.util.ArrayList;

public class ResultadoMochila<T extends Valuable> {

    private final ArrayList<T> seleccionados;
    private final int valorTotal;
    private final int pesoTotal;
    private final int sobrante;

    public ResultadoMochila(ArrayList<T> seleccionados, int valorTotal, int pesoTotal, int sobrante) {

        this.seleccionados = seleccionados;
        this.valorTotal = valorTotal;
        this.pesoTotal = pesoTotal;
        this.sobrante = sobrante;

    }

    public ArrayList<T> getSeleccionados() {
        return seleccionados;
    }

    public int getValorTotal() {
        return valorTotal;
    }

    public int getPesoTotal() {
        return pesoTotal;
    }

    public int getSobrante() {
        return sobrante;
    }
}
