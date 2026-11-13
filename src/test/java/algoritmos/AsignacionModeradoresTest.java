package algoritmos;

import algoritmos.resultado.ResultadoAsignacion;
import org.junit.jupiter.api.Test;

import static algoritmos.AsignacionModeradores.INCOMPATIBLE;
import static org.junit.jupiter.api.Assertions.*;

/** Matrices chicas: fila = moderador, columna = comunidad; mejorAsignacion[c] = moderador de la comunidad c. */
class AsignacionModeradoresTest {

    @Test
    void sinIncompatibilidadesEncuentraElOptimo() {
        int[][] costo = {
                { 4, 1 },
                { 2, 9 },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 1, 1 });

        assertTrue(r.haySolucion());
        assertArrayEquals(new int[] { 1, 0 }, r.getMejorAsignacion());
        assertEquals(3, r.getCostoMinimo());
    }

    @Test
    void noUsaUnParIncompatible() {
        // Sin la incompatibilidad lo mejor sería m0 para las dos (costo 1 + 1)
        int[][] costo = {
                { INCOMPATIBLE, 1 },
                { 10, 3 },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 2, 2 });

        assertTrue(r.haySolucion());
        assertArrayEquals(new int[] { 1, 0 }, r.getMejorAsignacion());
        assertEquals(11, r.getCostoMinimo());
    }

    @Test
    void encuentraSolucionAunqueLaGreedyInicialSeTrabe() {
        // Greedy: c0 -> m0 (el más barato) y c1 se queda sin nadie (m0 sin cupo, m1 incompatible).
        // La única asignación válida es c0 -> m1, c1 -> m0.
        int[][] costo = {
                { 1, 5 },
                { 2, INCOMPATIBLE },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 1, 1 });

        assertTrue(r.haySolucion());
        assertArrayEquals(new int[] { 1, 0 }, r.getMejorAsignacion());
        assertEquals(7, r.getCostoMinimo());
    }

    @Test
    void sinSolucionSiUnaComunidadNoTieneModeradorCompatible() {
        int[][] costo = {
                { 1, INCOMPATIBLE },
                { 2, INCOMPATIBLE },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 2, 2 });

        assertFalse(r.haySolucion());
    }

    @Test
    void sinSolucionSiLasIncompatibilidadesYElCupoNoCierran() {
        // c0 y c1 solo las puede atender m0, que tiene cupo para una sola
        int[][] costo = {
                { 1, 1, 1 },
                { INCOMPATIBLE, INCOMPATIBLE, 1 },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 1, 2 });

        assertFalse(r.haySolucion());
    }

    @Test
    void elCostoCeroNoEsIncompatible() {
        int[][] costo = {
                { 0, 0 },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 2 });

        assertTrue(r.haySolucion());
        assertArrayEquals(new int[] { 0, 0 }, r.getMejorAsignacion());
        assertEquals(0, r.getCostoMinimo());
    }

    @Test
    void elIncompatibleNoSeSumaAlCosto() {
        // Si -1 se tomara como costo, m1 para c0 parecería lo más barato
        int[][] costo = {
                { 3, 4 },
                { INCOMPATIBLE, 2 },
        };

        ResultadoAsignacion r = AsignacionModeradores.resolver(costo, new int[] { 1, 1 });

        assertArrayEquals(new int[] { 0, 1 }, r.getMejorAsignacion());
        assertEquals(5, r.getCostoMinimo());
    }
}
