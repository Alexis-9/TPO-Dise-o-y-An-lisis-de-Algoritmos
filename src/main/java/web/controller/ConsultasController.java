package web.controller;

import io.javalin.http.Context;
import service.ConsultasDifusionService;
import web.dto.ConsultaDto;
import web.dto.TablaDto;

public class ConsultasController {

    private final ConsultasDifusionService service;

    public ConsultasController(ConsultasDifusionService service) {
        this.service = service;
    }

    /** GET /api/consultas/tabla */
    public void tabla(Context ctx) {
        // Solo informativo: si la red cambió desde el último Floyd, esta consulta lo vuelve a correr
        boolean recalculada = !service.estaActualizado();
        ctx.json(TablaDto.desde(service.tablaDeTiempos(), recalculada));
    }

    /** GET /api/consultas?origen=&destino= */
    public void consultar(Context ctx) {
        int origen = ctx.queryParamAsClass("origen", Integer.class).get();
        int destino = ctx.queryParamAsClass("destino", Integer.class).get();
        ctx.json(ConsultaDto.desde(service.consultar(origen, destino)));
    }
}
