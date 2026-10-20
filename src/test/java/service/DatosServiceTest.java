package service;

import datos.DatosDemo;
import datos.Plataforma;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatosServiceTest {

    private Plataforma plataforma;
    private DatosService datos;
    private RedService red;
    private MuroService muro;
    private IniciativasService iniciativas;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
        datos = new DatosService(plataforma, new DatosDemo());
        red = new RedService(plataforma);
        muro = new MuroService(plataforma);
        iniciativas = new IniciativasService(plataforma);
    }

    @Test
    void cargaLaDemo() {
        datos.cargarDemo();

        assertEquals(8, red.listarUsuarios().size());
        assertEquals(14, red.listarVinculos().size());
        assertEquals(12, muro.listar("fecha").size());
        assertEquals(8, iniciativas.listar().size());
    }

    @Test
    void vaciaTodo() {
        datos.cargarDemo();

        datos.vaciar();

        assertTrue(red.listarUsuarios().isEmpty());
        assertTrue(red.listarVinculos().isEmpty());
        assertTrue(muro.listar("relevancia").isEmpty());
        assertTrue(iniciativas.listar().isEmpty());
        assertTrue(iniciativas.seleccionar(50).seleccionadas().isEmpty());
    }

    @Test
    void vaciarYVolverACargarDejaLaDemoComoAlPrincipio() {
        datos.cargarDemo();
        red.agregarUsuario("Zoe");
        red.eliminarUsuario(1);
        muro.eliminar(6);
        iniciativas.agregar(5, 500);

        datos.vaciar();
        datos.cargarDemo();

        assertEquals(8, red.listarUsuarios().size());
        assertEquals("Ana", red.listarUsuarios().get(0).getNombre());
        assertEquals(14, red.listarVinculos().size());
        assertEquals(12, muro.listar("fecha").size());
        assertEquals(220, iniciativas.seleccionar(50).impactoTotal());
    }

    @Test
    void cargarLaDemoDosVecesNoDuplicaNada() {
        datos.cargarDemo();
        datos.cargarDemo();

        assertEquals(8, red.listarUsuarios().size());
        assertEquals(12, muro.listar("fecha").size());
    }
}
