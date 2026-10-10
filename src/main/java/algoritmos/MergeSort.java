package main.java.algoritmos;
import java.util.ArrayList;

public class MergeSort {

    public static <T extends Comparable<T>> void mergeSort(T[] s, int inicio, int fin) {
        if (inicio < fin) {
            int medio = (fin + inicio) / 2;
            mergeSort(s, inicio, medio);
            mergeSort(s, medio + 1, fin);
            merge(s, inicio, fin);
        }
    }

    private static <T extends Comparable<T>> void merge(T[] s, int inicio, int fin) {
        ArrayList<T> r = new ArrayList<>();
        int medio = (inicio + fin) / 2;
        int i = inicio;
        int j = medio + 1;

        for (int k = 0; k <= fin - inicio; k++) {
            if ((j > fin || s[i].compareTo(s[j]) <= 0) && i <= medio) {
                r.add(s[i]);
                i++;
            } else {
                r.add(s[j]);
                j++;
            }
        }

        for (int k = 0; k <= fin - inicio; k++) {
            s[inicio + k] = r.get(k);
        }
    }
}
