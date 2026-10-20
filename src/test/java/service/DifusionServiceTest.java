package service;

import algoritmos.resultado.PasoDijkstra;
import datos.DatosDemo;
import datos.Plataforma;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Usa la red de la demo: 1 Ana, 2 Bruno, 3 Carla, 4 Diego, 5 Eva, 6 Franco, 7 Gina, 8 Hugo. */
class DifusionServiceTest {

    private Plataforma plataforma;
    private DifusionService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        new DatosDemo().cargarEn(plataforma);
        service = new DifusionService(plataforma);
    }

    private static List<String> nombres(List<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getNombre).toList();
    }

    @Test
    void elijeElCaminoIndirectoSiEsMasRapidoQueElDirecto() {
        ResultadoDifusion r = service.calcular(1, 2);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Ana", "Carla", "Bruno"), nombres(r.cadena()));
        assertEquals(7, r.tiempoTotal());
    }

    @Test
    void encuentraUnaCadenaLarga() {
        ResultadoDifusion r = service.calcular(1, 7);

        assertEquals(List.of("Ana", "Carla", "Eva", "Franco", "Gina"), nombres(r.cadena()));
        assertEquals(17, r.tiempoTotal());
        assertEquals("Ana", r.origen().getNombre());
        assertEquals("Gina", r.destino().getNombre());
    }

    @Test
    void unDestinoInalcanzableNoEsError() {
        ResultadoDifusion r = service.calcular(1, 8);

        assertFalse(r.alcanzable());
        assertTrue(r.cadena().isEmpty());
        assertNull(r.tiempoTotal());
        assertEquals(List.of("Ana", "Bruno", "Carla", "Diego", "Eva", "Franco", "Gina"), nombres(r.alcanzados()));
        assertFalse(r.decisiones().isEmpty());
    }

    @Test
    void desdeElUsuarioInalcanzableSiSeLlegaAlResto() {
        ResultadoDifusion r = service.calcular(8, 1);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Hugo", "Diego", "Franco", "Gina", "Ana"), nombres(r.cadena()));
        assertEquals(23, r.tiempoTotal());
    }

    @Test
    void origenIgualADestinoEsUnaCadenaDeUnoSinTiempo() {
        ResultadoDifusion r = service.calcular(3, 3);

        assertTrue(r.alcanzable());
        assertEquals(List.of("Carla"), nombres(r.cadena()));
        assertEquals(0, r.tiempoTotal());
    }

    @Test
    void rechazaIdsInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.calcular(99, 1));
        assertThrows(IllegalArgumentException.class, () -> service.calcular(1, 99));
    }

    @Test
    void devuelveLosPasosEstructurados() {
        ResultadoDifusion r = service.calcular(1, 2);
        List<PasoDijkstra<Usuario>> pasos = r.pasos();

        // Se elige cada usuario alcanzable una vez, empezando por el origen
        assertEquals(r.alcanzados().size(), pasos.size());
        assertEquals("Ana", pasos.get(0).elegido().getNombre());
        assertEquals(0, pasos.get(0).distancia());

        // Al elegir a Ana se alcanzan Bruno (10) y Carla (3) por primera vez
        List<PasoDijkstra.Actualizacion<Usuario>> primeras = pasos.get(0).actualizaciones();
        assertEquals(2, primeras.size());
        assertNull(primeras.get(0).anterior());

        // Al elegir a Carla, Bruno mejora de 10 a 7
        PasoDijkstra<Usuario> pasoCarla = pasos.get(1);
        assertEquals("Carla", pasoCarla.elegido().getNombre());
        PasoDijkstra.Actualizacion<Usuario> mejoraBruno = pasoCarla.actualizaciones().stream()
                .filter(a -> a.vertice().getNombre().equals("Bruno"))
                .findFirst().orElseThrow();
        assertEquals(10, mejoraBruno.anterior());
        assertEquals(7, mejoraBruno.nuevo());
        assertFalse(pasoCarla.decisiones().isEmpty());
    }

    @Test
    void reflejaLosCambiosDeLaRed() {
        plataforma.agregarVinculo(1, 8, 1);

        ResultadoDifusion r = service.calcular(1, 8);

        assertTrue(r.alcanzable());
        assertEquals(1, r.tiempoTotal());
    }
}
