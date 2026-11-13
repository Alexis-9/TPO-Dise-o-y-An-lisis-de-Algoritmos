package web.controller;

import io.javalin.http.Context;
import model.Comunidad;
import model.Moderador;
import service.AsignacionService;
import web.dto.CostoDto;
import web.dto.ModeracionDto;
import web.dto.NuevaComunidadDto;
import web.dto.NuevoModeradorDto;
import web.dto.PlanAsignacionDto;

public class ModeracionController {

    private final AsignacionService service;

    public ModeracionController(AsignacionService service) {
        this.service = service;
    }

    /** GET /api/moderacion */
    public void datos(Context ctx) {
        ctx.json(ModeracionDto.desde(service.listarModeradores(), service.listarComunidades(), service.costos()));
    }

    /** GET /api/moderacion/asignacion */
    public void asignacion(Context ctx) {
        ctx.json(PlanAsignacionDto.desde(service.asignarModeradores()));
    }

    /** POST /api/moderadores */
    public void crearModerador(Context ctx) {
        NuevoModeradorDto pedido = ctx.bodyAsClass(NuevoModeradorDto.class);
        if (pedido.limite() == null) { throw new IllegalArgumentException("Falta el límite de comunidades"); }
        Moderador m = service.agregarModerador(pedido.nombre(), pedido.limite());
        ctx.status(201).json(new ModeracionDto.ModeradorDto(m.getId(), m.getNombre(), m.getLimiteComunidades()));
    }

    /** DELETE /api/moderadores/{id} */
    public void eliminarModerador(Context ctx) {
        service.eliminarModerador(ctx.pathParamAsClass("id", Integer.class).get());
        ctx.status(204);
    }

    /** POST /api/comunidades */
    public void crearComunidad(Context ctx) {
        NuevaComunidadDto pedido = ctx.bodyAsClass(NuevaComunidadDto.class);
        Comunidad c = service.agregarComunidad(pedido.nombre());
        ctx.status(201).json(new ModeracionDto.ComunidadDto(c.getId(), c.getNombre()));
    }

    /** DELETE /api/comunidades/{id} */
    public void eliminarComunidad(Context ctx) {
        service.eliminarComunidad(ctx.pathParamAsClass("id", Integer.class).get());
        ctx.status(204);
    }

    /** PUT /api/costos  con cuerpo { moderador, comunidad, costo } o { moderador, comunidad, incompatible: true } */
    public void definirCosto(Context ctx) {
        CostoDto pedido = ctx.bodyAsClass(CostoDto.class);
        if (pedido.moderador() == null || pedido.comunidad() == null) {
            throw new IllegalArgumentException("Faltan el moderador o la comunidad");
        }
        if (Boolean.TRUE.equals(pedido.incompatible())) {
            service.definirIncompatible(pedido.moderador(), pedido.comunidad());
        } else {
            if (pedido.costo() == null) { throw new IllegalArgumentException("Falta el costo"); }
            service.definirCosto(pedido.moderador(), pedido.comunidad(), pedido.costo());
        }
        ctx.status(204);
    }
}
