package service;

import datos.DatosDemo;
import datos.Plataforma;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Usa la red de la demo: 1 Ana, 2 Bruno, 3 Carla, 4 Diego, 5 Eva, 6 Franco, 7 Gina, 8 Hugo. */
class ConsultasDifusionServiceTest {

    private Plataforma plataforma;
    private ConsultasDifusionService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        new DatosDemo().cargarEn(plataforma);
        service = new ConsultasDifusionService(plataforma);
    }

    private static List<String> nombres(List<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getNombre).toList();
    }

    @Test
    void elijeElCaminoIndirectoSiEsMasRapidoQueElDirecto() {
        ConsultaDifusion r = service.consultar(1, 2);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Ana", "Carla", "Bruno"), nombres(r.cadena()));
        assertEquals(7, r.tiempoTotal());
    }

    @Test
    void encuentraUnaCadenaLarga() {
        ConsultaDifusion r = service.consultar(1, 7);

        assertEquals(List.of("Ana", "Carla", "Eva", "Franco", "Gina"), nombres(r.cadena()));
        assertEquals(17, r.tiempoTotal());
        assertEquals("Ana", r.origen().getNombre());
        assertEquals("Gina", r.destino().getNombre());
    }

    @Test
    void unParSinCadenaNoEsError() {
        ConsultaDifusion r = service.consultar(1, 8);

        assertFalse(r.alcanzable());
        assertTrue(r.cadena().isEmpty());
        assertNull(r.tiempoTotal());
    }

    @Test
    void desdeElUsuarioInalcanzableSiSeLlegaAlResto() {
        ConsultaDifusion r = service.consultar(8, 1);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Hugo", "Diego", "Franco", "Gina", "Ana"), nombres(r.cadena()));
        assertEquals(23, r.tiempoTotal());
    }

    @Test
    void origenIgualADestinoEsUnaCadenaDeUnoSinTiempo() {
        ConsultaDifusion r = service.consultar(3, 3);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Carla"), nombres(r.cadena()));
        assertEquals(0, r.tiempoTotal());
    }

    @Test
    void rechazaIdsInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.consultar(99, 1));
        assertThrows(IllegalArgumentException.class, () -> service.consultar(1, 99));
    }

    @Test
    void coincideConDijkstraEnTodosLosPares() {
        DifusionService dijkstra = new DifusionService(plataforma);

        for (Usuario o : plataforma.getUsuarios()) {
            for (Usuario d : plataforma.getUsuarios()) {
                ConsultaDifusion floyd = service.consultar(o.getId(), d.getId());
                ResultadoDifusion esperado = dijkstra.calcular(o.getId(), d.getId());

                assertEquals(esperado.alcanzable(), floyd.alcanzable(), o + " -> " + d);
                assertEquals(esperado.tiempoTotal(), floyd.tiempoTotal(), o + " -> " + d);
            }
        }
    }

    @Test
    void preprocesaUnaVezYQuedaGuardado() {
        assertFalse(service.estaActualizado());

        service.preprocesar();
        assertTrue(service.estaActualizado());

        service.consultar(1, 2);
        service.consultar(8, 1);
        assertTrue(service.estaActualizado());
    }

    @Test
    void recalculaSiCambiaLaRed() {
        assertFalse(service.consultar(1, 8).alcanzable());

        plataforma.agregarVinculo(1, 8, 1);
        assertFalse(service.estaActualizado());

        ConsultaDifusion r = service.consultar(1, 8);
        assertTrue(r.alcanzable());
        assertEquals(1, r.tiempoTotal());
        assertTrue(service.estaActualizado());
    }

    @Test
    void unCambioRechazadoNoInvalidaElResultado() {
        service.preprocesar();

        // Ana -> Bruno ya existe
        assertThrows(IllegalArgumentException.class, () -> plataforma.agregarVinculo(1, 2, 3));
        assertTrue(service.estaActualizado());
    }

    @Test
    void reconoceUnUsuarioNuevo() {
        service.preprocesar();
        Usuario nuevo = plataforma.agregarUsuario("Ivana");

        ConsultaDifusion r = service.consultar(1, nuevo.getId());
        assertFalse(r.alcanzable());
    }

    @Test
    void tablaConTodosLosPares() {
        TablaDifusion tabla = service.tablaDeTiempos();

        assertEquals(List.of("Ana", "Bruno", "Carla", "Diego", "Eva", "Franco", "Gina", "Hugo"),
                nombres(tabla.usuarios()));
        for (int i = 0; i < tabla.usuarios().size(); i++) {
            assertEquals(0, tabla.tiempos()[i][i]);
        }
        assertEquals(7, tabla.tiempos()[0][1]);   // Ana -> Bruno
        assertNull(tabla.tiempos()[0][7]);        // Ana -> Hugo
        assertEquals(23, tabla.tiempos()[7][0]);  // Hugo -> Ana
    }

    @Test
    void redVacia() {
        plataforma.vaciar();

        assertTrue(service.tablaDeTiempos().usuarios().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.consultar(1, 1));
    }
}
