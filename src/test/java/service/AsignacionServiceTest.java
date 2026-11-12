package service;

import datos.DatosDemo;
import datos.Plataforma;
import model.Asignacion;
import model.Comunidad;
import model.PlanAsignacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Usa la moderación de la demo: moderadores 1 Lucía (2), 2 Mateo (2), 3 Sofía (1), 4 Tomás (2);
 * comunidades 1 Programación, 2 Matemática, 3 Diseño, 4 Física, 5 Economía.
 */
class AsignacionServiceTest {

    private Plataforma plataforma;
    private AsignacionService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        new DatosDemo().cargarEn(plataforma);
        service = new AsignacionService(plataforma);
    }

    private static Map<String, List<String>> porModerador(PlanAsignacion plan) {
        Map<String, List<String>> resultado = new HashMap<>();
        for (Asignacion a : plan.getAsignaciones()) {
            resultado.put(a.getModerador().getNombre(),
                    a.getComunidadesAsignadas().stream().map(Comunidad::getNombre).toList());
        }
        return resultado;
    }

    @Test
    void mejoraLaSolucionGreedyInicial() {
        // Greedy comunidad por comunidad cuesta 34; el óptimo es 31
        PlanAsignacion plan = service.asignarModeradores();

        assertEquals(31, plan.getCostoTotal());
        Map<String, List<String>> asignadas = porModerador(plan);
        assertEquals(List.of("Programación", "Diseño"), asignadas.get("Lucía"));
        assertEquals(List.of("Física"), asignadas.get("Mateo"));
        assertEquals(List.of("Matemática"), asignadas.get("Sofía"));
        assertEquals(List.of("Economía"), asignadas.get("Tomás"));
    }

    @Test
    void respetaElLimiteDeCadaModerador() {
        PlanAsignacion plan = service.asignarModeradores();

        int cubiertas = 0;
        for (Asignacion a : plan.getAsignaciones()) {
            assertTrue(a.getComunidadesAsignadas().size() <= a.getModerador().getLimiteComunidades());
            cubiertas += a.getComunidadesAsignadas().size();
        }
        assertEquals(5, cubiertas);
    }

    @Test
    void rechazaSiNoAlcanzaElCupo() {
        service.eliminarModerador(1);   // Lucía tenía 2 de cupo: quedan 5 para 5 comunidades
        service.eliminarModerador(3);   // Sofía tenía 1: quedan 4

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, service::asignarModeradores);
        assertTrue(e.getMessage().contains("4"));
    }

    @Test
    void avisaSiFaltaUnCosto() {
        service.agregarComunidad("Química");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, service::asignarModeradores);
        assertTrue(e.getMessage().contains("Química"));
    }

    @Test
    void validaLosDatosDeCarga() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarModerador(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> service.agregarModerador("Ema", 0));
        assertThrows(IllegalArgumentException.class, () -> service.agregarComunidad(""));
        assertThrows(IllegalArgumentException.class, () -> service.definirCosto(1, 1, -1));
    }
}
