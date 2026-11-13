package web.dto;

import datos.Plataforma;
import org.junit.jupiter.api.Test;
import service.AsignacionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModeracionDtoTest {

    @Test
    void separaLosIncompatiblesDeLosCostos() {
        AsignacionService service = new AsignacionService(new Plataforma());
        service.agregarModerador("Ana", 1);
        service.agregarComunidad("Química");
        service.agregarComunidad("Historia");
        service.definirCosto(1, 1, 7);
        service.definirIncompatible(1, 2);

        ModeracionDto dto = ModeracionDto.desde(service.listarModeradores(), service.listarComunidades(), service.costos());

        assertEquals(List.of(
                new CostoDto(1, 1, 7, false),
                new CostoDto(1, 2, null, true)), dto.costos());
    }

    @Test
    void lasCeldasSinDefinirNoAparecen() {
        AsignacionService service = new AsignacionService(new Plataforma());
        service.agregarModerador("Ana", 1);
        service.agregarComunidad("Química");

        ModeracionDto dto = ModeracionDto.desde(service.listarModeradores(), service.listarComunidades(), service.costos());

        assertTrue(dto.costos().isEmpty());
    }
}
