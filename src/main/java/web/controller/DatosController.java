package web.controller;

import io.javalin.http.Context;
import service.DatosService;

public class DatosController {

    private final DatosService service;

    public DatosController(DatosService service) {
        this.service = service;
    }

    /** POST /api/datos/demo */
    public void cargarDemo(Context ctx) {
        service.cargarDemo();
        ctx.status(204);
    }

    /** POST /api/datos/vaciar */
    public void vaciar(Context ctx) {
        service.vaciar();
        ctx.status(204);
    }
}
