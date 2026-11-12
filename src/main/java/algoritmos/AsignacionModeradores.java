package algoritmos;

import algoritmos.resultado.ResultadoAsignacion;

import java.util.Arrays;
import java.util.PriorityQueue;

public class AsignacionModeradores {

    public static final int INCOMPATIBLE = -1;
    private static final int SIN_SOLUCION = Integer.MAX_VALUE;

    private AsignacionModeradores() { }

    static class Estado implements Comparable<Estado> {
        final int[] moderadorDe;
        final int[] cupo;
        int nivel;
        int costoAcumulado;
        int cotaInferior;

        Estado(int cantComunidades, int[] limites) {
            moderadorDe = new int[cantComunidades];
            Arrays.fill(moderadorDe, -1);
            cupo = limites.clone();
        }

        Estado(Estado otro) {
            moderadorDe = otro.moderadorDe.clone();
            cupo = otro.cupo.clone();
            nivel = otro.nivel;
            costoAcumulado = otro.costoAcumulado;
            cotaInferior = otro.cotaInferior;
        }

        @Override
        public int compareTo(Estado otro) {
            int porCota = Integer.compare(cotaInferior, otro.cotaInferior);

            return porCota != 0 ? porCota : Integer.compare(otro.nivel, nivel);
        }
    }

    public static ResultadoAsignacion resolver(int[][] costo, int[] limites) {
        if (costo == null || costo.length == 0 || costo[0].length == 0) {
            return new ResultadoAsignacion(null, 0);
        }
        if (limites.length != costo.length) {
            return new ResultadoAsignacion(null, 0);
        }

        int cantModeradores = costo.length;
        int cantComunidades = costo[0].length;

        long cupoTotal = 0;
        for (int limite : limites) { cupoTotal += limite; }
        if (cupoTotal < cantComunidades) {
            return new ResultadoAsignacion(null, 0);
        }

        int[] mejor = solucionGreedy(costo, limites);
        int costoMinimo = (mejor == null) ? SIN_SOLUCION : costoDe(costo, mejor);

        Estado raiz = new Estado(cantComunidades, limites);
        raiz.cotaInferior = calcularCotaInferior(costo, raiz.cupo, 0, 0);

        PriorityQueue<Estado> vivos = new PriorityQueue<>();
        if (raiz.cotaInferior != SIN_SOLUCION) { vivos.add(raiz); }

        while (!vivos.isEmpty()) {
            Estado actual = vivos.poll();

            if (actual.cotaInferior >= costoMinimo) {
                break;
            }

            if (actual.nivel == cantComunidades) {
                costoMinimo = actual.costoAcumulado;
                mejor = actual.moderadorDe;
                break;
            }

            int c = actual.nivel;
            for (int m = 0; m < cantModeradores; m++) {
                if (actual.cupo[m] == 0 || costo[m][c] == INCOMPATIBLE) {
                    continue;
                }

                Estado hijo = new Estado(actual);
                hijo.moderadorDe[c] = m;
                hijo.cupo[m]--;
                hijo.nivel = c + 1;
                hijo.costoAcumulado = actual.costoAcumulado + costo[m][c];
                hijo.cotaInferior = calcularCotaInferior(costo, hijo.cupo, hijo.nivel, hijo.costoAcumulado);

                if (hijo.cotaInferior < costoMinimo) {
                    vivos.add(hijo);
                }
            }
        }

        return new ResultadoAsignacion(mejor, costoMinimo);
    }

    private static int calcularCotaInferior(int[][] costo, int[] cupo, int nivel, int costoAcumulado) {
        int cota = costoAcumulado;

        for (int c = nivel; c < costo[0].length; c++) {
            int minimo = SIN_SOLUCION;
            for (int m = 0; m < costo.length; m++) {
                if (cupo[m] > 0 && costo[m][c] != INCOMPATIBLE && costo[m][c] < minimo) { minimo = costo[m][c]; }
            }

            if (minimo == SIN_SOLUCION) { return SIN_SOLUCION; }
            cota += minimo;
        }
        return cota;
    }

    private static int[] solucionGreedy(int[][] costo, int[] limites) {
        int[] cupo = limites.clone();
        int[] moderadorDe = new int[costo[0].length];

        for (int c = 0; c < moderadorDe.length; c++) {
            int elegido = -1;

            for (int m = 0; m < costo.length; m++) {
                if (cupo[m] > 0  && costo[m][c] != INCOMPATIBLE && (elegido == -1 || costo[m][c] < costo[elegido][c])) {
                    elegido = m;
                }

            }

            if (elegido == -1) { return null; }

            moderadorDe[c] = elegido;
            cupo[elegido]--;
        }
        return moderadorDe;
    }

    private static int costoDe(int[][] costo, int[] moderadorDe) {
        int total = 0;
        for (int c = 0; c < moderadorDe.length; c++) { total += costo[moderadorDe[c]][c]; }
        return total;
    }
}