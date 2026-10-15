package main.java.algoritmos;
import java.util.ArrayList;

public class MergeSort {

    public static <T extends Comparable<T>> void mergeSort(T[] lista, int inicio, int fin) {
        if (inicio < fin) {
            int medio = (fin + inicio) / 2;
            mergeSort(lista, inicio, medio);
            mergeSort(lista, medio + 1, fin);
            merge(lista, inicio, fin);
        }
    }

    private static <T extends Comparable<T>> void merge(T[] lista, int inicio, int fin) {
        ArrayList<T> ordenados = new ArrayList<>();
        int medio = (inicio + fin) / 2;
        int izq = inicio;
        int der = medio + 1;

        for (int i = 0; i <= fin - inicio; i++) {
            if ((der > fin || lista[izq].compareTo(lista[der]) <= 0) && izq <= medio) {
                ordenados.add(lista[izq]);
                izq++;
            } else {
                ordenados.add(lista[der]);
                der++;
            }
        }

        for (int i = 0; i <= fin - inicio; i++) {
            lista[inicio + i] = ordenados.get(i);
        }
    }
}
