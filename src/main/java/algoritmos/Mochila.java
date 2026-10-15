package main.java.algoritmos;

import main.java.model.interfaces.Valuable;

import java.util.ArrayList;

public class Mochila<T extends Valuable> {

    private final int capacidadMax;
    private final ArrayList<T> objetos;
    private ArrayList<T> seleccionados;
    private int valorMax;

    public Mochila(int capacidadMax, ArrayList<T> objetos) {

        this.capacidadMax = capacidadMax;
        this.objetos = objetos;

        actualizarValores();

    }

    private void actualizarValores() {

        seleccionados = algoritmoMochila();
        valorMax = getValorTotal();

    }

    private ArrayList<T> algoritmoMochila() {

        int[][] matriz = new int[objetos.size()][capacidadMax + 1];

        for (int i = 0; i < objetos.size(); i++) {

            T objeto = objetos.get(i);

            for (int j = 0; j <= capacidadMax; j++) {

                if (i == 0 && objeto.getPeso() > j) {
                    matriz[i][j] = 0;

                } else {
                    if (i == 0) {
                        matriz[i][j] = objeto.getValor();

                    } else {
                        if (objeto.getPeso() > j) {
                            matriz[i][j] = matriz[i - 1][j];

                        } else {
                            matriz[i][j] = Math.max(matriz[i - 1][j], matriz[i - 1][j - objeto.getPeso()] + objeto.getValor());
                        }
                    }

                }

            }
        }

        valorMax = matriz[objetos.size() - 1][capacidadMax];

        return algoritmoReconstruccion(matriz);

    }

    private ArrayList<T> algoritmoReconstruccion(int[][] matriz) {

        int cantAcumulado = 0;
        ArrayList<T> seleccionados = new ArrayList<>();

        int i = objetos.size() - 1;
        int j = capacidadMax;

        while (cantAcumulado < valorMax) {

            T objeto = objetos.get(i);

            if (i > 0) {

                if (matriz[i][j] != matriz[i - 1][j]) {

                    seleccionados.add(objeto);

                    cantAcumulado += objeto.getValor();
                    j -= objeto.getPeso();
                    i -= 1;

                } else {

                    i -= 1;

                }
            } else {

                seleccionados.add(objeto);
                cantAcumulado += objeto.getValor();

            }

        }

        return seleccionados;

    }

    public void agregarObjeto(T nuevoObjeto) {

        objetos.add(nuevoObjeto);
        actualizarValores();

    }

    public void eliminarObjeto(T objeto) throws IllegalArgumentException {

        if (!objetos.contains(objeto)) { throw new IllegalArgumentException("Objeto no encontrado."); }

        objetos.remove(objeto);
        actualizarValores();

    }

    public ArrayList<T> getSeleccionados() { return seleccionados; }

    public int getValorTotal() { return valorMax; }

    public int getPesoTotal() {

        int pesoTotal = 0;

        for (T objeto : seleccionados) {
            pesoTotal += objeto.getPeso();
        }

        return pesoTotal;

    }

    public int getSobrante() { return capacidadMax - getPesoTotal(); }

    public void mostrarMochila() {

        System.out.println("-".repeat(20));
        for (T objeto : seleccionados) {
            System.out.println(objeto.toString());;
        }
        System.out.println("-".repeat(20));

    }

}
