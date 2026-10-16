package service;

import datos.DatosDemo;
import datos.Plataforma;
import model.Iniciativa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class IniciativasServiceTest {

    private Plataforma plataforma;
    private IniciativasService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        service = new IniciativasService(plataforma);
    }

    private static List<Integer> ids(List<Iniciativa> iniciativas) {
        return iniciativas.stream().map(Iniciativa::getId).toList();
    }

    // ---------------- Consultas ----------------

    @Test
    void listaLasIniciativasPorId() {
        new DatosDemo().cargarEn(plataforma);

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), ids(service.listar()));
    }

    @Test
    void conLaDemoYPresupuesto50EligeLaMejorCombinacion() {
        new DatosDemo().cargarEn(plataforma);

        SeleccionIniciativas s = service.seleccionar(50);

        // Greedy por impacto/costo llegaría a 200 con #1, #2 y #5
        assertEquals(List.of(2, 3), ids(s.seleccionadas()));
        assertEquals(50, s.presupuesto());
        assertEquals(50, s.costoTotal());
        assertEquals(220, s.impactoTotal());
        assertEquals(0, s.restante());
    }

    @Test
    void elRestanteEsLoQueSobraDelPresupuesto() {
        service.agregar(10, 60);
        service.agregar(30, 50);

        SeleccionIniciativas s = service.seleccionar(25);

        assertEquals(List.of(1), ids(s.seleccionadas()));
        assertEquals(10, s.costoTotal());
        assertEquals(60, s.impactoTotal());
        assertEquals(15, s.restante());
    }

    @Test
    void conPresupuestoCeroNoEligeNada() {
        new DatosDemo().cargarEn(plataforma);

        SeleccionIniciativas s = service.seleccionar(0);

        assertTrue(s.seleccionadas().isEmpty());
        assertEquals(0, s.costoTotal());
        assertEquals(0, s.impactoTotal());
        assertEquals(0, s.restante());
    }

    @Test
    void rechazaUnPresupuestoNegativo() {
        new DatosDemo().cargarEn(plataforma);

        assertThrows(IllegalArgumentException.class, () -> service.seleccionar(-1));
    }

    @Test
    void sinIniciativasDevuelveUnaSeleccionVacia() {
        SeleccionIniciativas s = service.seleccionar(100);

        assertTrue(s.seleccionadas().isEmpty());
        assertEquals(0, s.costoTotal());
        assertEquals(0, s.impactoTotal());
        assertEquals(100, s.restante());
    }

    @Test
    void conUnPresupuestoEnormeEntranTodasLasQueAportan() {
        service.agregar(10, 60);
        service.agregar(20, 0);
        service.agregar(30, 40);

        SeleccionIniciativas s = service.seleccionar(Integer.MAX_VALUE);

        assertEquals(List.of(1, 3), ids(s.seleccionadas()));
        assertEquals(40, s.costoTotal());
        assertEquals(100, s.impactoTotal());
        assertEquals(Integer.MAX_VALUE - 40, s.restante());
    }

    // ---------------- Carga ----------------

    @Test
    void agregaConElSiguienteId() {
        Iniciativa i = service.agregar(15, 40);

        assertEquals(1, i.getId());
        assertEquals(15, i.getCosto());
        assertEquals(40, i.getImpacto());
        assertEquals(2, service.agregar(5, 0).getId());
    }

    @Test
    void rechazaCostoNoPositivoEImpactoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.agregar(0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.agregar(-5, 10));
        assertThrows(IllegalArgumentException.class, () -> service.agregar(10, -1));
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void eliminaUnaIniciativaYLaSeleccionSeRecalcula() {
        new DatosDemo().cargarEn(plataforma);

        service.eliminar(3);

        assertEquals(7, service.listar().size());
        assertFalse(ids(service.seleccionar(50).seleccionadas()).contains(3));
    }

    @Test
    void eliminarHastaLaUltimaDejaLaSeleccionVacia() {
        service.agregar(10, 60);

        service.eliminar(1);

        assertTrue(service.seleccionar(50).seleccionadas().isEmpty());
    }

    @Test
    void eliminarUnaIniciativaInexistenteLanzaNoSuchElement() {
        assertThrows(NoSuchElementException.class, () -> service.eliminar(1));
    }
}
