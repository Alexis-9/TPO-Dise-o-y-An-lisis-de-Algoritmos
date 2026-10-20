package web.controller;

import io.javalin.http.Context;
import service.RedService;
import web.dto.NuevoUsuarioDto;
import web.dto.NuevoVinculoDto;
import web.dto.RedDto;
import web.dto.UsuarioDto;
import web.dto.VinculoDto;

public class RedController {

    private final RedService service;

    public RedController(RedService service) {
        this.service = service;
    }

    /** GET /api/red */
    public void red(Context ctx) {
        ctx.json(RedDto.desde(service.listarUsuarios(), service.listarVinculos()));
    }

    /** POST /api/usuarios */
    public void crearUsuario(Context ctx) {
        NuevoUsuarioDto pedido = ctx.bodyAsClass(NuevoUsuarioDto.class);
        ctx.status(201).json(UsuarioDto.desde(service.agregarUsuario(pedido.nombre())));
    }

    /** DELETE /api/usuarios/{id} */
    public void eliminarUsuario(Context ctx) {
        service.eliminarUsuario(ctx.pathParamAsClass("id", Integer.class).get());
        ctx.status(204);
    }

    /** POST /api/vinculos */
    public void crearVinculo(Context ctx) {
        NuevoVinculoDto pedido = ctx.bodyAsClass(NuevoVinculoDto.class);
        ctx.status(201).json(VinculoDto.desde(
                service.agregarVinculo(pedido.origen(), pedido.destino(), pedido.minutos())));
    }

    /** DELETE /api/vinculos/{origen}/{destino} */
    public void eliminarVinculo(Context ctx) {
        service.eliminarVinculo(ctx.pathParamAsClass("origen", Integer.class).get(),
                ctx.pathParamAsClass("destino", Integer.class).get());
        ctx.status(204);
    }
}
