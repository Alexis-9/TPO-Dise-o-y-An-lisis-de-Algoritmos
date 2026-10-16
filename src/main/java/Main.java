import datos.DatosDemo;
import datos.Plataforma;
import service.DatosService;
import service.DifusionService;
import service.IniciativasService;
import service.MuroService;
import service.RedService;
import web.ApiServer;
import web.controller.DatosController;
import web.controller.DifusionController;
import web.controller.IniciativasController;
import web.controller.MuroController;
import web.controller.RedController;

/**
 * Arma la aplicación: Plataforma + DatosDemo -> services -> controllers -> ApiServer.
 * Es el único lugar donde se crean las dependencias.
 *
 * Correr con mvn exec:java (puerto 7070) o mvn exec:java -Dexec.args=8080.
 * El menú de consola sigue en view.MenuConsola.
 */
public class Main {

    private static final int PUERTO_POR_DEFECTO = 7070;

    public static void main(String[] args) {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_POR_DEFECTO;

        Plataforma plataforma = new Plataforma();
        DatosDemo datosDemo = new DatosDemo();

        RedService redService = new RedService(plataforma);
        DifusionService difusionService = new DifusionService(plataforma);
        MuroService muroService = new MuroService(plataforma);
        IniciativasService iniciativasService = new IniciativasService(plataforma);
        DatosService datosService = new DatosService(plataforma, datosDemo);

        // Al arrancar, la aplicación muestra la demo
        datosService.cargarDemo();

        ApiServer servidor = new ApiServer(
                new RedController(redService),
                new DifusionController(difusionService),
                new MuroController(muroService),
                new IniciativasController(iniciativasService),
                new DatosController(datosService));
        servidor.iniciar(puerto);

        System.out.println("Red social universitaria en http://localhost:" + puerto);
    }
}
