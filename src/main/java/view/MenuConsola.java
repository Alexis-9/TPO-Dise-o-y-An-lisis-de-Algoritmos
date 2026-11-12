package view;

import algoritmos.resultado.CadenaDifusion;
import algoritmos.resultado.ResultadoBacktracking;
import datos.DatosDemo;
import datos.Plataforma;
import model.Asignacion;
import model.Comunidad;
import model.Iniciativa;
import model.Moderador;
import model.PlanAsignacion;
import model.Publicacion;
import model.Usuario;
import model.Vinculo;
import service.AsignacionService;
import service.CadenasAlternativasService;
import service.ConsultaDifusion;
import service.ConsultasDifusionService;
import service.DatosService;
import service.DifusionService;
import service.IniciativasService;
import service.MuroService;
import service.RedService;
import service.ResultadoDifusion;
import service.SeleccionIniciativas;
import service.TablaDifusion;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Menú de consola. Usa los mismos services que la web, así que los resultados
 * salen de los mismos algoritmos y sobre los mismos datos de demo.
 */
public class MenuConsola {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int PRESUPUESTO_POR_DEFECTO = 50;
    private static final int TIEMPO_MAXIMO_POR_DEFECTO = 30;
    private static final int INTERMEDIARIOS_POR_DEFECTO = 3;

    private static final Plataforma plataforma = new Plataforma();
    private static final DatosService datosService = new DatosService(plataforma, new DatosDemo());
    private static final RedService redService = new RedService(plataforma);
    private static final MuroService muroService = new MuroService(plataforma);
    private static final DifusionService difusionService = new DifusionService(plataforma);
    private static final IniciativasService iniciativasService = new IniciativasService(plataforma);
    private static final ConsultasDifusionService consultasService = new ConsultasDifusionService(plataforma);
    private static final CadenasAlternativasService cadenasService = new CadenasAlternativasService(plataforma);
    private static final AsignacionService asignacionService = new AsignacionService(plataforma);

    private static boolean datosCargados = false;

    public static void main(String[] args) {
        int opcion = -1;

        do {
            mostrarMenu();
            try {
                opcion = leerEntero("Opción: ");
                System.out.println();

                switch (opcion) {
                    case 1 -> cargarDatosDePrueba();
                    case 2 -> { if (hayDatos()) mostrarMuro("MURO: de la más reciente a la más antigua",
                            muroService.listar(MuroService.ORDEN_FECHA)); }
                    case 3 -> { if (hayDatos()) mostrarMuro("MURO: de la más relevante a la menos relevante",
                            muroService.listar(MuroService.ORDEN_RELEVANCIA)); }
                    case 4 -> { if (hayDatos()) mostrarUsuariosYVinculos(); }
                    case 5 -> { if (hayDatos()) mostrarCadenaMasRapida(); }
                    case 6 -> { if (hayDatos()) mostrarSeleccionIniciativas(); }
                    case 7 -> { if (hayDatos()) mostrarTablaDeTiempos(); }
                    case 8 -> { if (hayDatos()) consultarPar(); }
                    case 9 -> { if (hayDatos()) mostrarCadenasAlternativas(); }
                    case 10 -> { if (hayDatos()) mostrarAsignacionModeradores(); }
                    case 0 -> System.out.println("¡Hasta luego!");
                    default -> System.out.println("Opción inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NoSuchElementException e) {
                opcion = 0; // se cerró la entrada estándar
            }
        } while (opcion != 0);
    }

    // ------------------------------------------------------------------
    // Menú
    // ------------------------------------------------------------------

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=========== PLATAFORMA UNIVERSITARIA ===========");
        System.out.println(" 1. Cargar datos de prueba");
        System.out.println(" 2. Muro: publicaciones más recientes");
        System.out.println(" 3. Muro: publicaciones más relevantes");
        System.out.println(" 4. Ver usuarios y vínculos");
        System.out.println(" 5. Cadena de difusión más rápida");
        System.out.println(" 6. Selección de iniciativas a financiar");
        System.out.println(" 7. Tiempos mínimos entre todos los usuarios");
        System.out.println(" 8. Consultar difusión entre dos usuarios (precalculada)");
        System.out.println(" 9. Cadenas alternativas de difusión");
        System.out.println("10. Asignación de moderadores");
        System.out.println(" 0. Salir");
        System.out.println("================================================");
    }

    private static boolean hayDatos() {
        if (!datosCargados) {
            System.out.println("Primero cargá los datos de prueba (opción 1).");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Datos de prueba (los mismos de la web: datos.DatosDemo)
    // ------------------------------------------------------------------

    private static void cargarDatosDePrueba() {
        datosService.cargarDemo();
        datosCargados = true;

        System.out.println("Datos de prueba cargados:");
        System.out.println(" - " + redService.listarUsuarios().size() + " usuarios y "
                + redService.listarVinculos().size() + " vínculos");
        System.out.println(" - " + muroService.listar(MuroService.ORDEN_FECHA).size() + " publicaciones");
        System.out.println(" - " + iniciativasService.listar().size() + " iniciativas (presupuesto sugerido: "
                + PRESUPUESTO_POR_DEFECTO + ")");
        System.out.println(" - " + asignacionService.listarModeradores().size() + " moderadores y "
                + asignacionService.listarComunidades().size() + " comunidades");
    }

    // ------------------------------------------------------------------
    // Muro de publicaciones
    // ------------------------------------------------------------------

    private static void mostrarMuro(String titulo, List<Publicacion> ordenadas) {
        System.out.println("--- " + titulo + " ---");
        System.out.printf("%-4s %-8s %-17s %-10s %-11s %s%n",
                "ID", "Autor", "Fecha y hora", "Reacciones", "Comentarios", "Relevancia");
        for (Publicacion p : ordenadas) {
            System.out.printf("%-4d %-8s %-17s %-10d %-11d %d%n",
                    p.getId(), p.getAutor(), p.getFechaHora().format(FORMATO_FECHA),
                    p.getReacciones(), p.getComentarios(), p.getRelevancia());
        }
        System.out.println("(relevancia = reacciones + 3 * comentarios)");
    }

    // ------------------------------------------------------------------
    // Usuarios y vínculos
    // ------------------------------------------------------------------

    private static void mostrarUsuariosYVinculos() {
        System.out.println("--- USUARIOS ---");
        for (Usuario u : redService.listarUsuarios()) {
            System.out.println(u);
        }
        System.out.println();
        System.out.println("--- VÍNCULOS (sentido de transmisión y tiempo) ---");
        for (Vinculo v : redService.listarVinculos()) {
            System.out.println(v);
        }
    }

    // ------------------------------------------------------------------
    // Cadena de difusión más rápida (Dijkstra)
    // ------------------------------------------------------------------

    private static void mostrarCadenaMasRapida() {
        System.out.println("Usuarios disponibles:");
        for (Usuario u : redService.listarUsuarios()) {
            System.out.println("  " + u);
        }

        int idOrigen = leerEntero("ID del usuario de origen: ");
        int idDestino = leerEntero("ID del usuario de destino: ");

        ResultadoDifusion resultado = difusionService.calcular(idOrigen, idDestino);

        System.out.println();
        System.out.println("--- Decisiones tomadas durante el cálculo ---");
        for (String paso : resultado.decisiones()) {
            System.out.println(paso);
        }

        System.out.println();
        System.out.println("--- Resultado ---");
        if (!resultado.alcanzable()) {
            System.out.println("El destino no es alcanzable desde " + resultado.origen() + ".");
            return;
        }

        System.out.println("Usuarios de la cadena: " + resultado.cadena());
        System.out.println("Tiempo total estimado: " + resultado.tiempoTotal() + " min");
    }

    // ------------------------------------------------------------------
    // Selección de iniciativas
    // ------------------------------------------------------------------

    private static void mostrarSeleccionIniciativas() {
        int presupuesto = leerEnteroConDefecto("Presupuesto disponible (Enter = "
                + PRESUPUESTO_POR_DEFECTO + "): ", PRESUPUESTO_POR_DEFECTO);

        SeleccionIniciativas seleccion = iniciativasService.seleccionar(presupuesto);

        System.out.println();
        System.out.println("--- Iniciativas disponibles ---");
        for (Iniciativa i : iniciativasService.listar()) {
            System.out.println(i);
        }

        System.out.println();
        System.out.println("--- Iniciativas seleccionadas (presupuesto: " + presupuesto + ") ---");
        if (seleccion.seleccionadas().isEmpty()) {
            System.out.println("Ninguna iniciativa entra en el presupuesto.");
        }
        for (Iniciativa i : seleccion.seleccionadas()) {
            System.out.println(i);
        }

        System.out.println();
        System.out.println("Costo total:           " + seleccion.costoTotal());
        System.out.println("Impacto obtenido:      " + seleccion.impactoTotal());
        System.out.println("Presupuesto restante:  " + seleccion.restante());
    }

    // ------------------------------------------------------------------
    // Consultas de difusión entre todos los usuarios (Floyd)
    // ------------------------------------------------------------------

    private static void mostrarTablaDeTiempos() {
        boolean recalcular = !consultasService.estaActualizado();
        TablaDifusion tabla = consultasService.tablaDeTiempos();
        List<Usuario> usuarios = tabla.usuarios();

        System.out.println("--- Tiempo mínimo de difusión en minutos (fila: origen, columna: destino) ---");
        System.out.printf("%-9s", "");
        for (Usuario u : usuarios) {
            System.out.printf("%8s", recortar(u.getNombre(), 7));
        }
        System.out.println();

        for (int i = 0; i < usuarios.size(); i++) {
            System.out.printf("%-9s", recortar(usuarios.get(i).getNombre(), 8));
            for (int j = 0; j < usuarios.size(); j++) {
                Integer tiempo = tabla.tiempos()[i][j];
                System.out.printf("%8s", tiempo == null ? "-" : tiempo);
            }
            System.out.println();
        }
        System.out.println("(- = no existe una cadena de difusión entre ese par)");
        System.out.println(recalcular
                ? "Floyd se calculó ahora sobre la red actual; las próximas consultas lo reutilizan."
                : "Floyd ya estaba calculado: la tabla sale de las matrices guardadas, sin recalcular.");
    }

    private static void consultarPar() {
        mostrarUsuariosDisponibles();
        boolean recalcular = !consultasService.estaActualizado();

        int idOrigen = leerEntero("ID del usuario de origen: ");
        int idDestino = leerEntero("ID del usuario de destino: ");

        ConsultaDifusion consulta = consultasService.consultar(idOrigen, idDestino);

        System.out.println();
        System.out.println("--- Resultado ---");
        if (!consulta.alcanzable()) {
            System.out.println("No existe una cadena de difusión de " + consulta.origen() + " a " + consulta.destino() + ".");
        } else {
            System.out.println("Usuarios de la cadena: " + consulta.cadena());
            System.out.println("Tiempo total mínimo:   " + consulta.tiempoTotal() + " min");
        }
        System.out.println(recalcular
                ? "(se corrió Floyd porque la red no estaba preprocesada o cambió)"
                : "(respondido desde las matrices de Floyd ya calculadas)");
    }

    // ------------------------------------------------------------------
    // Cadenas alternativas de difusión (Backtracking)
    // ------------------------------------------------------------------

    private static void mostrarCadenasAlternativas() {
        mostrarUsuariosDisponibles();

        int idOrigen = leerEntero("ID del usuario de origen: ");
        int idDestino = leerEntero("ID del usuario de destino: ");
        int tiempoMax = leerEnteroConDefecto("Tiempo máximo en minutos (Enter = "
                + TIEMPO_MAXIMO_POR_DEFECTO + "): ", TIEMPO_MAXIMO_POR_DEFECTO);
        int maxIntermediarios = leerEnteroConDefecto("Máximo de intermediarios (Enter = "
                + INTERMEDIARIOS_POR_DEFECTO + "): ", INTERMEDIARIOS_POR_DEFECTO);
        List<Integer> excluidos = leerIds("IDs excluidos separados por coma (Enter = ninguno): ");

        ResultadoBacktracking<Usuario> resultado =
                cadenasService.calcular(idOrigen, idDestino, tiempoMax, maxIntermediarios, excluidos);

        System.out.println();
        if (!resultado.hayCadenas()) {
            System.out.println("Ninguna cadena cumple las condiciones.");
            return;
        }

        List<CadenaDifusion<Usuario>> cadenas = resultado.getCadenas();
        System.out.println("--- Cadenas válidas (" + cadenas.size() + ", en el orden en que se encontraron) ---");
        for (int i = 0; i < cadenas.size(); i++) {
            CadenaDifusion<Usuario> c = cadenas.get(i);
            int intermediarios = c.getUsuariosCadena().size() - 2;
            System.out.printf("%2d. %-60s %4d min  (%d %s)%n", i + 1, c.getUsuariosCadena(), c.getTiempoTotal(),
                    intermediarios, intermediarios == 1 ? "intermediario" : "intermediarios");
        }

        CadenaDifusion<Usuario> mejor = resultado.getMejor();
        System.out.println();
        System.out.println("La de menor tiempo total: " + mejor.getUsuariosCadena() + " | " + mejor.getTiempoTotal() + " min");
    }

    // ------------------------------------------------------------------
    // Asignación de moderadores (Branch and Bound)
    // ------------------------------------------------------------------

    private static void mostrarAsignacionModeradores() {
        List<Moderador> moderadores = asignacionService.listarModeradores();
        List<Comunidad> comunidades = asignacionService.listarComunidades();
        Map<Integer, Map<Integer, Integer>> costos = asignacionService.costos();

        System.out.println("--- Costos de asignación (fila: moderador, columna: comunidad) ---");
        System.out.printf("%-16s", "Moderador (lím.)");
        for (Comunidad c : comunidades) {
            System.out.printf("%13s", recortar(c.getNombre(), 12));
        }
        System.out.println();
        for (Moderador m : moderadores) {
            System.out.printf("%-16s", recortar(m.getNombre(), 11) + " (" + m.getLimiteComunidades() + ")");
            for (Comunidad c : comunidades) {
                Integer costo = costos.getOrDefault(m.getId(), Map.of()).get(c.getId());
                System.out.printf("%13s", costo == null ? "?" : costo);
            }
            System.out.println();
        }

        PlanAsignacion plan = asignacionService.asignarModeradores();

        System.out.println();
        System.out.println("--- Asignación de menor costo ---");
        for (Asignacion a : plan.getAsignaciones()) {
            List<String> nombres = new ArrayList<>();
            for (Comunidad c : a.getComunidadesAsignadas()) {
                nombres.add(c.getNombre());
            }
            System.out.printf("%-10s %d/%d  costo %3d  %s%n", a.getModerador().getNombre(),
                    nombres.size(), a.getModerador().getLimiteComunidades(), a.getCosto(),
                    nombres.isEmpty() ? "(sin comunidades)" : String.join(", ", nombres));
        }
        System.out.println();
        System.out.println("Costo total mínimo: " + plan.getCostoTotal());
        System.out.println();
        System.out.println("Criterios de poda del Branch and Bound:");
        System.out.println(" - No se prueba un moderador que ya cubrió su límite de comunidades.");
        System.out.println(" - Cota inferior = costo acumulado + el menor costo de cada comunidad que falta,");
        System.out.println("   entre los moderadores que todavía tienen cupo.");
        System.out.println(" - Se descarta toda rama cuya cota iguale o supere la mejor solución conocida");
        System.out.println("   (la inicial sale de asignar cada comunidad al más barato con cupo).");
    }

    // ------------------------------------------------------------------
    // Auxiliares de presentación
    // ------------------------------------------------------------------

    private static void mostrarUsuariosDisponibles() {
        System.out.println("Usuarios disponibles:");
        for (Usuario u : redService.listarUsuarios()) {
            System.out.println("  " + u);
        }
    }

    private static String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }

    // ------------------------------------------------------------------
    // Lectura de consola
    // ------------------------------------------------------------------

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número entero válido.");
            }
        }
    }

    private static List<Integer> leerIds(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            List<Integer> ids = new ArrayList<>();
            if (linea.isEmpty()) {
                return ids;
            }
            try {
                for (String parte : linea.split(",")) {
                    ids.add(Integer.parseInt(parte.trim()));
                }
                return ids;
            } catch (NumberFormatException e) {
                System.out.println("Ingresá ids enteros separados por coma (por ejemplo: 3, 5).");
            }
        }
    }

    private static int leerEnteroConDefecto(String mensaje, int valorPorDefecto) {
        while (true) {
            System.out.print(mensaje);
            String linea = scanner.nextLine().trim();
            if (linea.isEmpty()) {
                return valorPorDefecto;
            }
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Ingresá un número entero válido.");
            }
        }
    }
}