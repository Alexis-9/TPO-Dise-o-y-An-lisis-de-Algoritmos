package web.controller;

import io.javalin.http.Context;
import service.CadenasAlternativasService;
import web.dto.CadenasDto;

import java.util.ArrayList;
import java.util.List;

public class CadenasController {

    private final CadenasAlternativasService service;

    public CadenasController(CadenasAlternativasService service) {
        this.service = service;
    }

    /** GET /api/cadenas?origen=&destino=&tiempoMax=&intermediarios=&excluidos=1,2 */
    public void calcular(Context ctx) {
        int origen = ctx.queryParamAsClass("origen", Integer.class).get();
        int destino = ctx.queryParamAsClass("destino", Integer.class).get();
        int tiempoMax = ctx.queryParamAsClass("tiempoMax", Integer.class).get();
        int intermediarios = ctx.queryParamAsClass("intermediarios", Integer.class).get();
        List<Integer> excluidos = ids(ctx.queryParam("excluidos"));

        ctx.json(CadenasDto.desde(service.calcular(origen, destino, tiempoMax, intermediarios, excluidos)));
    }

    /** "1,2,3" -> [1, 2, 3]; vacío o ausente -> lista vacía. */
    private static List<Integer> ids(String texto) {
        List<Integer> ids = new ArrayList<>();
        if (texto == null || texto.isBlank()) { return ids; }

        for (String parte : texto.split(",")) {
            try {
                ids.add(Integer.parseInt(parte.strip()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Los excluidos tienen que ser ids separados por coma");
            }
        }
        return ids;
    }
}
