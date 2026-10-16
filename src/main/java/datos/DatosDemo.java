package datos;

import java.time.LocalDateTime;

/**
 * Escenario de demo. Reemplaza todo lo que tenga la plataforma.
 *
 * Red (8 usuarios, 14 vínculos dirigidos):
 *  - Ana -> Bruno directo tarda 10 min, pero Ana -> Carla -> Bruno tarda 7.
 *  - Bruno <-> Diego y Carla <-> Eva son vínculos de ida y vuelta.
 *  - La cadena más rápida de Ana a Gina es Ana -> Carla -> Eva -> Franco -> Gina (17 min).
 *  - Hugo solo tiene un vínculo de salida: nadie le transmite, es inalcanzable desde el resto.
 *
 * Muro (12 publicaciones):
 *  - Mismo momento: #2 y #3, #4 y #9, #7 y #12 (desempata el id menor).
 *  - Misma relevancia: #2, #3 y #5 (8); #1 y #8 (16); #4 y #9 (35) (desempata la más reciente
 *    y, si también coinciden en fecha, el id menor).
 *
 * Iniciativas (8): con presupuesto 50, elegir por impacto/costo (Greedy) toma #1 (6 por unidad),
 * #2 (5) y #5 (2,67): costo 45, impacto 200. La mochila elige #2 y #3: costo 50, impacto 220.
 */
public class DatosDemo {

    public void cargarEn(Plataforma plataforma) {
        // Un solo bloqueo para todo: ningún pedido ve la demo a medio cargar
        synchronized (plataforma) {
            plataforma.vaciar();
            cargarRed(plataforma);
            cargarPublicaciones(plataforma);
            cargarIniciativas(plataforma);
        }
    }

    private void cargarRed(Plataforma plataforma) {
        plataforma.agregarUsuario("Ana");      // 1
        plataforma.agregarUsuario("Bruno");    // 2
        plataforma.agregarUsuario("Carla");    // 3
        plataforma.agregarUsuario("Diego");    // 4
        plataforma.agregarUsuario("Eva");      // 5
        plataforma.agregarUsuario("Franco");   // 6
        plataforma.agregarUsuario("Gina");     // 7
        plataforma.agregarUsuario("Hugo");     // 8

        plataforma.agregarVinculo(1, 2, 10);   // directo Ana -> Bruno...
        plataforma.agregarVinculo(1, 3, 3);
        plataforma.agregarVinculo(3, 2, 4);    // ...pierde contra Ana -> Carla -> Bruno (7)
        plataforma.agregarVinculo(2, 4, 5);
        plataforma.agregarVinculo(4, 2, 6);    // Bruno <-> Diego
        plataforma.agregarVinculo(3, 4, 12);
        plataforma.agregarVinculo(3, 5, 8);
        plataforma.agregarVinculo(5, 3, 5);    // Carla <-> Eva
        plataforma.agregarVinculo(5, 6, 2);
        plataforma.agregarVinculo(4, 6, 7);
        plataforma.agregarVinculo(6, 7, 4);
        plataforma.agregarVinculo(2, 7, 15);
        plataforma.agregarVinculo(7, 1, 9);
        plataforma.agregarVinculo(8, 4, 3);    // Hugo transmite a Diego, pero nadie le transmite a Hugo
    }

    private void cargarPublicaciones(Plataforma plataforma) {
        plataforma.agregarPublicacion("Ana",    LocalDateTime.of(2026, 10, 10, 9, 0),   10, 2);  // #1  relevancia 16
        plataforma.agregarPublicacion("Bruno",  LocalDateTime.of(2026, 10, 12, 18, 30), 5, 1);   // #2  relevancia 8
        plataforma.agregarPublicacion("Carla",  LocalDateTime.of(2026, 10, 12, 18, 30), 8, 0);   // #3  relevancia 8, misma fecha que #2
        plataforma.agregarPublicacion("Diego",  LocalDateTime.of(2026, 10, 14, 12, 0),  20, 5);  // #4  relevancia 35
        plataforma.agregarPublicacion("Eva",    LocalDateTime.of(2026, 10, 15, 8, 15),  2, 2);   // #5  relevancia 8, más reciente que #2 y #3
        plataforma.agregarPublicacion("Franco", LocalDateTime.of(2026, 10, 8, 20, 0),   30, 4);  // #6  relevancia 42
        plataforma.agregarPublicacion("Gina",   LocalDateTime.of(2026, 10, 16, 10, 0),  1, 0);   // #7  relevancia 1
        plataforma.agregarPublicacion("Hugo",   LocalDateTime.of(2026, 10, 11, 15, 45), 4, 4);   // #8  relevancia 16, más reciente que #1
        plataforma.agregarPublicacion("Ana",    LocalDateTime.of(2026, 10, 14, 12, 0),  11, 8);  // #9  relevancia 35, misma fecha que #4
        plataforma.agregarPublicacion("Bruno",  LocalDateTime.of(2026, 10, 15, 21, 0),  0, 0);   // #10 relevancia 0
        plataforma.agregarPublicacion("Carla",  LocalDateTime.of(2026, 10, 13, 17, 10), 14, 1);  // #11 relevancia 17
        plataforma.agregarPublicacion("Eva",    LocalDateTime.of(2026, 10, 16, 10, 0),  6, 2);   // #12 relevancia 12, misma fecha que #7
    }

    private void cargarIniciativas(Plataforma plataforma) {
        // Presupuesto 50: Greedy por impacto/costo -> #1, #2, #5 (impacto 200); mochila -> #2, #3 (impacto 220)
        plataforma.agregarIniciativa(10, 60);    // #1 impacto/costo 6
        plataforma.agregarIniciativa(20, 100);   // #2 5
        plataforma.agregarIniciativa(30, 120);   // #3 4
        plataforma.agregarIniciativa(25, 55);    // #4 2,2
        plataforma.agregarIniciativa(15, 40);    // #5 2,67
        plataforma.agregarIniciativa(80, 200);   // #6 2,5
        plataforma.agregarIniciativa(40, 90);    // #7 2,25
        plataforma.agregarIniciativa(35, 70);    // #8 2
    }
}
