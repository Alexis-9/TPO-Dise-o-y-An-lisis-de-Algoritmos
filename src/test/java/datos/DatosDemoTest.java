package datos;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DatosDemoTest {

    @Test
    void cargaElEscenarioCompleto() {
        Plataforma plataforma = new Plataforma();

        new DatosDemo().cargarEn(plataforma);

        assertEquals(8, plataforma.getUsuarios().size());
        assertEquals(14, plataforma.getVinculos().size());
        assertEquals(12, plataforma.getPublicaciones().size());
        assertEquals(8, plataforma.getIniciativas().size());
        assertEquals("Ana", plataforma.getUsuario(1).getNombre());
        assertEquals("Hugo", plataforma.getUsuario(8).getNombre());
    }

    @Test
    void reemplazaLoQueHabia() {
        Plataforma plataforma = new Plataforma();
        plataforma.agregarUsuario("Zoe");
        plataforma.agregarPublicacion("Zoe", LocalDateTime.of(2026, 1, 1, 0, 0), 1, 1);
        plataforma.agregarIniciativa(1, 1);

        new DatosDemo().cargarEn(plataforma);

        assertEquals(8, plataforma.getUsuarios().size());
        assertEquals("Ana", plataforma.getUsuario(1).getNombre());
        assertEquals(12, plataforma.getPublicaciones().size());
        assertEquals(8, plataforma.getIniciativas().size());
    }

    @Test
    void nadieTieneVinculosHaciaHugo() {
        Plataforma plataforma = new Plataforma();

        new DatosDemo().cargarEn(plataforma);

        assertTrue(plataforma.getVinculos().stream().noneMatch(v -> v.getDestino().getId() == 8));
    }
}
