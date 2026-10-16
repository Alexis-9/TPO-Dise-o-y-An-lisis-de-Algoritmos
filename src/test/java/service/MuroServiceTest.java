package service;

import datos.DatosDemo;
import datos.Plataforma;
import model.Publicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MuroServiceTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 18, 30);

    private Plataforma plataforma;
    private MuroService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        service = new MuroService(plataforma);
    }

    private List<Integer> ids(String orden) {
        return service.listar(orden).stream().map(Publicacion::getId).toList();
    }

    // ---------------- Consultas ----------------

    @Test
    void ordenaLaDemoPorFechaDesempatandoPorId() {
        new DatosDemo().cargarEn(plataforma);

        // #7 y #12 comparten fecha, igual que #4 y #9 y que #2 y #3
        assertEquals(List.of(7, 12, 10, 5, 4, 9, 11, 2, 3, 8, 1, 6), ids("fecha"));
    }

    @Test
    void ordenaLaDemoPorRelevanciaDesempatandoPorFechaYDespuesPorId() {
        new DatosDemo().cargarEn(plataforma);

        // 35: #4 y #9 (misma fecha, gana el id menor); 16: #8 más reciente que #1;
        // 8: #5 más reciente que #2 y #3, que empatan en todo
        assertEquals(List.of(6, 4, 9, 11, 8, 1, 12, 5, 2, 3, 7, 10), ids("relevancia"));
    }

    @Test
    void sinOrdenUsaFecha() {
        new DatosDemo().cargarEn(plataforma);

        assertEquals(ids("fecha"), ids(null));
        assertEquals(ids("fecha"), ids(""));
    }

    @Test
    void rechazaUnOrdenInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.listar("autor"));
    }

    @Test
    void unMuroVacioDevuelveUnaListaVacia() {
        assertTrue(service.listar("fecha").isEmpty());
        assertTrue(service.listar("relevancia").isEmpty());
    }

    @Test
    void laRelevanciaPesaTresVecesLosComentarios() {
        service.publicar("Ana", FECHA, 10, 0);                  // 10
        service.publicar("Bruno", FECHA.minusDays(1), 1, 4);    // 13

        assertEquals(List.of(2, 1), ids("relevancia"));
        assertEquals(List.of(1, 2), ids("fecha"));
    }

    // ---------------- Carga ----------------

    @Test
    void publicaConElSiguienteId() {
        Publicacion p = service.publicar("Ana", FECHA, 4, 2);

        assertEquals(1, p.getId());
        assertEquals("Ana", p.getAutor());
        assertEquals(FECHA, p.getFechaHora());
        assertEquals(10, p.getRelevancia());
        assertEquals(2, service.publicar("Bruno", FECHA, 0, 0).getId());
    }

    @Test
    void aceptaCerosEnReaccionesYComentarios() {
        assertEquals(0, service.publicar("Ana", FECHA, 0, 0).getRelevancia());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.publicar(null, FECHA, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.publicar("  ", FECHA, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.publicar("Ana", null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.publicar("Ana", FECHA, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> service.publicar("Ana", FECHA, 1, -1));
        assertTrue(service.listar("fecha").isEmpty());
    }

    @Test
    void eliminaUnaPublicacion() {
        service.publicar("Ana", FECHA, 1, 1);
        service.publicar("Bruno", FECHA, 1, 1);

        service.eliminar(1);

        assertEquals(List.of(2), ids("fecha"));
    }

    @Test
    void eliminarUnaPublicacionInexistenteLanzaNoSuchElement() {
        assertThrows(NoSuchElementException.class, () -> service.eliminar(1));
    }
}
