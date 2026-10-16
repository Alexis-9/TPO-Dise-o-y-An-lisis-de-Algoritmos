package main.java.algoritmos;

import main.java.algoritmos.resultado.ResultadoMochila;
import main.java.model.interfaces.Valuable;

import java.util.ArrayList;
import java.util.Collections;

public class Mochila<T extends Valuable> {

    private Mochila() { }

    public static <T extends Valuable> ResultadoMochila<T> resolver(ArrayList<T> objetos, int capacidadMax) {

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

        return reconstruir(matriz, objetos, capacidadMax);

    }

    private static <T extends Valuable> ResultadoMochila<T> reconstruir(int[][] matriz, ArrayList<T> objetos, int capacidadMax) {

        int valorTotal = matriz[objetos.size() - 1][capacidadMax];

        int cantAcumulado = 0;
        ArrayList<T> seleccionados = new ArrayList<>();

        int i = objetos.size() - 1;
        int j = capacidadMax;

        while (cantAcumulado < valorTotal) {

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
        Collections.reverse(seleccionados);

        int pesoTotal = 0;
        for (T objeto : seleccionados) {
            pesoTotal += objeto.getPeso();
        }

        int sobrante = capacidadMax - pesoTotal;

        return new ResultadoMochila<>(seleccionados, valorTotal, pesoTotal, sobrante);

    }
}
