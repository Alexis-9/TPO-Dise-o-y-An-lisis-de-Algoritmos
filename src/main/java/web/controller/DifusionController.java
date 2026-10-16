package web.controller;

import io.javalin.http.Context;
import service.DifusionService;
import web.dto.DifusionDto;

public class DifusionController {

    private final DifusionService service;

    public DifusionController(DifusionService service) {
        this.service = service;
    }

    /** GET /api/difusion?origen=&destino= */
    public void calcular(Context ctx) {
        int origen = ctx.queryParamAsClass("origen", Integer.class).get();
        int destino = ctx.queryParamAsClass("destino", Integer.class).get();
        ctx.json(DifusionDto.desde(service.calcular(origen, destino)));
    }
}
