package web;

import com.fasterxml.jackson.core.JacksonException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.controller.DatosController;
import web.controller.DifusionController;
import web.controller.IniciativasController;
import web.controller.MuroController;
import web.controller.RedController;
import web.dto.ErrorDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Servidor HTTP: rutas de la API, archivos del frontend y traducción de errores a respuestas. */
public class ApiServer {

    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);

    private final Javalin app;

    public ApiServer(RedController red, DifusionController difusion, MuroController muro,
                     IniciativasController iniciativas, DatosController datos) {
        app = Javalin.create(config -> {
            config.startup.showJavalinBanner = false;
            config.staticFiles.add("/public", Location.CLASSPATH);

            // Consultas
            config.routes.get("/api/red", red::red);
            config.routes.get("/api/difusion", difusion::calcular);
            config.routes.get("/api/publicaciones", muro::listar);
            config.routes.get("/api/iniciativas", iniciativas::listar);
            config.routes.get("/api/iniciativas/seleccion", iniciativas::seleccion);

            // Carga
            config.routes.post("/api/usuarios", red::crearUsuario);
            config.routes.delete("/api/usuarios/{id}", red::eliminarUsuario);
            config.routes.post("/api/vinculos", red::crearVinculo);
            config.routes.delete("/api/vinculos/{origen}/{destino}", red::eliminarVinculo);
            config.routes.post("/api/publicaciones", muro::crear);
            config.routes.delete("/api/publicaciones/{id}", muro::eliminar);
            config.routes.post("/api/iniciativas", iniciativas::crear);
            config.routes.delete("/api/iniciativas/{id}", iniciativas::eliminar);
            config.routes.post("/api/datos/demo", datos::cargarDemo);
            config.routes.post("/api/datos/vaciar", datos::vaciar);

            // Errores
            config.routes.exception(IllegalArgumentException.class,
                    (e, ctx) -> error(ctx, 400, e.getMessage()));
            config.routes.exception(ValidationException.class,
                    (e, ctx) -> error(ctx, 400, mensaje(e)));
            config.routes.exception(JacksonException.class,
                    (e, ctx) -> error(ctx, 400, "El cuerpo del pedido no es un JSON válido o tiene datos del tipo equivocado"));
            config.routes.exception(NoSuchElementException.class,
                    (e, ctx) -> error(ctx, 404, e.getMessage()));
            config.routes.exception(Exception.class, (e, ctx) -> {
                log.error("Error no previsto en {} {}", ctx.method(), ctx.path(), e);
                error(ctx, 500, "Error interno del servidor");
            });
        });
    }

    public void iniciar(int puerto) {
        app.start(puerto);
    }

    public void detener() {
        app.stop();
    }

    private static void error(Context ctx, int status, String mensaje) {
        ctx.status(status).json(new ErrorDto(mensaje));
    }

    /** Un parámetro de la ruta o de la consulta que falta o no es un número. */
    private static String mensaje(ValidationException e) {
        List<String> partes = new ArrayList<>();
        for (Map.Entry<String, List<ValidationError<Object>>> entrada : e.getErrors().entrySet()) {
            boolean falta = entrada.getValue().stream().anyMatch(v -> "NULLCHECK_FAILED".equals(v.getMessage()));
            partes.add(falta
                    ? "Falta el parámetro " + entrada.getKey()
                    : "El parámetro " + entrada.getKey() + " tiene que ser un número entero");
        }
        return String.join(". ", partes);
    }
}
