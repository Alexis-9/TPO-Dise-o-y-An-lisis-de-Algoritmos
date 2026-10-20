package datos;

import model.Iniciativa;
import model.Publicacion;
import model.RedSocial;
import model.Usuario;
import model.Vinculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PlataformaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 16, 18, 30);

    private Plataforma plataforma;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma();
    }

    @Test
    void asignaIdsConsecutivosDesdeUno() {
        assertEquals(1, plataforma.agregarUsuario("Ana").getId());
        assertEquals(2, plataforma.agregarUsuario("Bruno").getId());
        assertEquals(3, plataforma.agregarUsuario("Carla").getId());
    }

    @Test
    void cadaEntidadTieneSuPropiaSecuenciaDeIds() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");

        assertEquals(1, plataforma.agregarPublicacion("Ana", FECHA, 1, 1).getId());
        assertEquals(1, plataforma.agregarIniciativa(10, 5).getId());
        assertEquals(2, plataforma.agregarIniciativa(10, 5).getId());
    }

    @Test
    void elSiguienteIdEsElMayorEnUsoMasUno() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");
        plataforma.agregarUsuario("Carla");
        plataforma.eliminarUsuario(1);

        assertEquals(4, plataforma.agregarUsuario("Diego").getId());
    }

    @Test
    void agregaYEliminaUsuarios() {
        plataforma.agregarUsuario("Ana");
        Usuario bruno = plataforma.agregarUsuario("Bruno");

        plataforma.eliminarUsuario(bruno.getId());

        assertEquals(1, plataforma.getUsuarios().size());
        assertNull(plataforma.getUsuario(bruno.getId()));
    }

    @Test
    void eliminarUnUsuarioBorraSusVinculosDeEntradaYDeSalida() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");
        plataforma.agregarUsuario("Carla");
        plataforma.agregarVinculo(1, 2, 5);
        plataforma.agregarVinculo(2, 3, 5);
        plataforma.agregarVinculo(1, 3, 9);

        plataforma.eliminarUsuario(2);

        List<Vinculo> vinculos = plataforma.getVinculos();
        assertEquals(1, vinculos.size());
        assertEquals(1, vinculos.get(0).getOrigen().getId());
        assertEquals(3, vinculos.get(0).getDestino().getId());
    }

    @Test
    void agregarVinculoDevuelveElVinculoCreado() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");

        Vinculo vinculo = plataforma.agregarVinculo(1, 2, 7);

        assertEquals(1, vinculo.getOrigen().getId());
        assertEquals(2, vinculo.getDestino().getId());
        assertEquals(7, vinculo.getPeso());
    }

    @Test
    void eliminaUnVinculo() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");
        plataforma.agregarVinculo(1, 2, 7);
        plataforma.agregarVinculo(2, 1, 3);

        plataforma.eliminarVinculo(1, 2);

        List<Vinculo> vinculos = plataforma.getVinculos();
        assertEquals(1, vinculos.size());
        assertEquals(2, vinculos.get(0).getOrigen().getId());
    }

    @Test
    void eliminarAlgoQueNoExisteLanzaNoSuchElement() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");

        assertThrows(NoSuchElementException.class, () -> plataforma.eliminarUsuario(99));
        assertThrows(NoSuchElementException.class, () -> plataforma.eliminarVinculo(1, 2));
        assertThrows(NoSuchElementException.class, () -> plataforma.eliminarVinculo(1, 99));
        assertThrows(NoSuchElementException.class, () -> plataforma.eliminarPublicacion(1));
        assertThrows(NoSuchElementException.class, () -> plataforma.eliminarIniciativa(1));
    }

    @Test
    void agregaYEliminaPublicaciones() {
        Publicacion p1 = plataforma.agregarPublicacion("Ana", FECHA, 3, 1);
        plataforma.agregarPublicacion("Bruno", FECHA, 0, 0);

        plataforma.eliminarPublicacion(p1.getId());

        List<Publicacion> publicaciones = plataforma.getPublicaciones();
        assertEquals(1, publicaciones.size());
        assertEquals("Bruno", publicaciones.get(0).getAutor());
    }

    @Test
    void agregaYEliminaIniciativas() {
        Iniciativa i1 = plataforma.agregarIniciativa(10, 60);
        plataforma.agregarIniciativa(20, 100);

        plataforma.eliminarIniciativa(i1.getId());

        List<Iniciativa> iniciativas = plataforma.getIniciativas();
        assertEquals(1, iniciativas.size());
        assertEquals(20, iniciativas.get(0).getCosto());
    }

    @Test
    void lasConsultasDevuelvenCopias() {
        plataforma.agregarIniciativa(10, 60);

        plataforma.getIniciativas().clear();

        assertEquals(1, plataforma.getIniciativas().size());
    }

    @Test
    void laCopiaDeLaRedEsIndependiente() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");
        plataforma.agregarVinculo(1, 2, 4);

        RedSocial copia = plataforma.copiaDeRed();
        copia.agregarVinculo(2, 1, 8);
        plataforma.eliminarVinculo(1, 2);

        assertEquals(2, copia.getUsuarios().size());
        assertEquals(2, copia.getVinculos().size());
        assertTrue(plataforma.getVinculos().isEmpty());
    }

    @Test
    void vaciarDejaTodoVacioYReiniciaLosIds() {
        plataforma.agregarUsuario("Ana");
        plataforma.agregarUsuario("Bruno");
        plataforma.agregarVinculo(1, 2, 4);
        plataforma.agregarPublicacion("Ana", FECHA, 1, 1);
        plataforma.agregarIniciativa(10, 60);

        plataforma.vaciar();

        assertTrue(plataforma.getUsuarios().isEmpty());
        assertTrue(plataforma.getVinculos().isEmpty());
        assertTrue(plataforma.getPublicaciones().isEmpty());
        assertTrue(plataforma.getIniciativas().isEmpty());
        assertEquals(1, plataforma.agregarUsuario("Carla").getId());
    }
}
