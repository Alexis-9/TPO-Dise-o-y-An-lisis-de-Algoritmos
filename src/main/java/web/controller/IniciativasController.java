package web.controller;

import io.javalin.http.Context;
import service.IniciativasService;
import web.dto.IniciativaDto;
import web.dto.NuevaIniciativaDto;
import web.dto.SeleccionDto;

public class IniciativasController {

    private final IniciativasService service;

    public IniciativasController(IniciativasService service) {
        this.service = service;
    }

    /** GET /api/iniciativas */
    public void listar(Context ctx) {
        ctx.json(service.listar().stream().map(IniciativaDto::desde).toList());
    }

    /** GET /api/iniciativas/seleccion?presupuesto= */
    public void seleccion(Context ctx) {
        int presupuesto = ctx.queryParamAsClass("presupuesto", Integer.class).get();
        ctx.json(SeleccionDto.desde(service.seleccionar(presupuesto)));
    }

    /** POST /api/iniciativas */
    public void crear(Context ctx) {
        NuevaIniciativaDto pedido = ctx.bodyAsClass(NuevaIniciativaDto.class);
        ctx.status(201).json(IniciativaDto.desde(service.agregar(pedido.costo(), pedido.impacto())));
    }

    /** DELETE /api/iniciativas/{id} */
    public void eliminar(Context ctx) {
        service.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
        ctx.status(204);
    }
}
