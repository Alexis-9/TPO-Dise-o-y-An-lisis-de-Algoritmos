package service;

import algoritmos.resultado.CadenaDifusion;
import algoritmos.resultado.ResultadoBacktracking;
import datos.DatosDemo;
import datos.Plataforma;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Usa la red de la demo: 1 Ana, 2 Bruno, 3 Carla, 4 Diego, 5 Eva, 6 Franco, 7 Gina, 8 Hugo. */
class CadenasAlternativasServiceTest {

    private CadenasAlternativasService service;

    @BeforeEach
    void setUp() {
        Plataforma plataforma = new Plataforma();
        new DatosDemo().cargarEn(plataforma);
        service = new CadenasAlternativasService(plataforma);
    }

    private static List<String> nombres(CadenaDifusion<Usuario> c) {
        return c.getUsuariosCadena().stream().map(Usuario::getNombre).toList();
    }

    @Test
    void encuentraTodasLasCadenasDentroDelTiempo() {
        // De Ana a Gina hay 7 cadenas sin repetir usuarios; Ana-Carla-Diego-Bruno-Gina (36) pasa de 30
        ResultadoBacktracking<Usuario> r = service.calcular(1, 7, 30, 4, List.of());

        assertEquals(6, r.getCadenas().size());
        assertEquals(List.of("Ana", "Carla", "Eva", "Franco", "Gina"), nombres(r.getMejor()));
        assertEquals(17, r.getMejor().getTiempoTotal());
        for (CadenaDifusion<Usuario> c : r.getCadenas()) {
            assertTrue(c.getTiempoTotal() <= 30);
        }
    }

    @Test
    void respetaElLimiteDeIntermediarios() {
        ResultadoBacktracking<Usuario> r = service.calcular(1, 7, 30, 1, List.of());

        assertEquals(1, r.getCadenas().size());
        assertEquals(List.of("Ana", "Bruno", "Gina"), nombres(r.getMejor()));
    }

    @Test
    void noPasaPorLosExcluidos() {
        ResultadoBacktracking<Usuario> r = service.calcular(1, 7, 30, 4, List.of(5));

        assertTrue(r.hayCadenas());
        assertEquals(22, r.getMejor().getTiempoTotal());   // Ana -> Carla -> Bruno -> Gina
        for (CadenaDifusion<Usuario> c : r.getCadenas()) {
            assertFalse(nombres(c).contains("Eva"));
        }
    }

    @Test
    void sinCadenasNoEsUnError() {
        ResultadoBacktracking<Usuario> r = service.calcular(1, 8, 100, 7, List.of());

        assertFalse(r.hayCadenas());
        assertNull(r.getMejor());
    }

    @Test
    void rechazaIdsInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.calcular(1, 99, 30, 3, List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.calcular(1, 7, 30, 3, List.of(99)));
    }
}
