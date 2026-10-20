package web.controller;

import io.javalin.http.Context;
import service.MuroService;
import web.dto.NuevaPublicacionDto;
import web.dto.PublicacionDto;

public class MuroController {

    private final MuroService service;

    public MuroController(MuroService service) {
        this.service = service;
    }

    /** GET /api/publicaciones?orden=fecha|relevancia */
    public void listar(Context ctx) {
        ctx.json(service.listar(ctx.queryParam("orden")).stream().map(PublicacionDto::desde).toList());
    }

    /** POST /api/publicaciones */
    public void crear(Context ctx) {
        NuevaPublicacionDto pedido = ctx.bodyAsClass(NuevaPublicacionDto.class);
        ctx.status(201).json(PublicacionDto.desde(service.publicar(
                pedido.autor(), pedido.fecha(), pedido.reacciones(), pedido.comentarios())));
    }

    /** DELETE /api/publicaciones/{id} */
    public void eliminar(Context ctx) {
        service.eliminar(ctx.pathParamAsClass("id", Integer.class).get());
        ctx.status(204);
    }
}
