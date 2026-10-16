package service;

import datos.Plataforma;
import model.Usuario;
import model.Vinculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class RedServiceTest {

    private Plataforma plataforma;
    private RedService service;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        service = new RedService(plataforma);
        service.agregarUsuario("Ana");
        service.agregarUsuario("Bruno");
        service.agregarUsuario("Carla");
    }

    // ---------------- Usuarios ----------------

    @Test
    void agregaUnUsuarioConElSiguienteId() {
        Usuario diego = service.agregarUsuario("Diego");

        assertEquals(4, diego.getId());
        assertEquals("Diego", diego.getNombre());
        assertEquals(4, service.listarUsuarios().size());
    }

    @Test
    void recortaLosEspaciosDelNombre() {
        assertEquals("Diego", service.agregarUsuario("  Diego ").getNombre());
    }

    @Test
    void rechazaNombresVacios() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarUsuario(null));
        assertThrows(IllegalArgumentException.class, () -> service.agregarUsuario(""));
        assertThrows(IllegalArgumentException.class, () -> service.agregarUsuario("   "));
    }

    @Test
    void listaLosUsuariosOrdenadosPorId() {
        service.eliminarUsuario(1);
        service.agregarUsuario("Diego");

        List<Integer> ids = service.listarUsuarios().stream().map(Usuario::getId).toList();

        assertEquals(List.of(2, 3, 4), ids);
    }

    @Test
    void eliminaUnUsuario() {
        service.eliminarUsuario(2);

        List<String> nombres = service.listarUsuarios().stream().map(Usuario::getNombre).toList();
        assertEquals(List.of("Ana", "Carla"), nombres);
    }

    @Test
    void eliminarUnUsuarioBorraSusVinculos() {
        service.agregarVinculo(1, 2, 5);
        service.agregarVinculo(2, 3, 5);
        service.agregarVinculo(3, 2, 5);
        service.agregarVinculo(1, 3, 9);

        service.eliminarUsuario(2);

        List<Vinculo> vinculos = service.listarVinculos();
        assertEquals(1, vinculos.size());
        assertEquals("Ana", vinculos.get(0).getOrigen().getNombre());
        assertEquals("Carla", vinculos.get(0).getDestino().getNombre());
    }

    @Test
    void eliminarUnUsuarioInexistenteLanzaNoSuchElement() {
        assertThrows(NoSuchElementException.class, () -> service.eliminarUsuario(99));
    }

    // ---------------- Vínculos ----------------

    @Test
    void agregaUnVinculo() {
        Vinculo vinculo = service.agregarVinculo(1, 2, 10);

        assertEquals("Ana", vinculo.getOrigen().getNombre());
        assertEquals("Bruno", vinculo.getDestino().getNombre());
        assertEquals(10, vinculo.getPeso());
        assertEquals(1, service.listarVinculos().size());
    }

    @Test
    void permiteElVinculoInverso() {
        service.agregarVinculo(1, 2, 10);
        service.agregarVinculo(2, 1, 4);

        assertEquals(2, service.listarVinculos().size());
    }

    @Test
    void listaLosVinculosOrdenadosPorOrigenYDestino() {
        service.agregarVinculo(3, 1, 1);
        service.agregarVinculo(1, 3, 1);
        service.agregarVinculo(1, 2, 1);

        List<String> pares = service.listarVinculos().stream()
                .map(v -> v.getOrigen().getId() + "->" + v.getDestino().getId())
                .toList();

        assertEquals(List.of("1->2", "1->3", "3->1"), pares);
    }

    @Test
    void rechazaUnVinculoDuplicado() {
        service.agregarVinculo(1, 2, 10);

        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(1, 2, 3));
    }

    @Test
    void rechazaUnVinculoAUnoMismo() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(1, 1, 3));
    }

    @Test
    void rechazaMinutosNoPositivos() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(1, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(1, 2, -5));
    }

    @Test
    void rechazaUsuariosInexistentes() {
        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(1, 99, 3));
        assertThrows(IllegalArgumentException.class, () -> service.agregarVinculo(99, 1, 3));
    }

    @Test
    void eliminaUnVinculo() {
        service.agregarVinculo(1, 2, 10);
        service.agregarVinculo(2, 1, 4);

        service.eliminarVinculo(1, 2);

        List<Vinculo> vinculos = service.listarVinculos();
        assertEquals(1, vinculos.size());
        assertEquals("Bruno", vinculos.get(0).getOrigen().getNombre());
    }

    @Test
    void eliminarUnVinculoInexistenteLanzaNoSuchElement() {
        assertThrows(NoSuchElementException.class, () -> service.eliminarVinculo(1, 2));
        assertThrows(NoSuchElementException.class, () -> service.eliminarVinculo(1, 99));
    }
}
