package view;

import datos.DatosDemo;
import datos.Plataforma;
import model.Iniciativa;
import model.Publicacion;
import model.Usuario;
import model.Vinculo;
import service.DatosService;
import service.DifusionService;
import service.IniciativasService;
import service.MuroService;
import service.RedService;
import service.ResultadoDifusion;
import service.SeleccionIniciativas;

import java.time.format.DateTimeFormatter;
import java.util.List;
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

    private static final Plataforma plataforma = new Plataforma();
    private static final DatosService datosService = new DatosService(plataforma, new DatosDemo());
    private static final RedService redService = new RedService(plataforma);
    private static final MuroService muroService = new MuroService(plataforma);
    private static final DifusionService difusionService = new DifusionService(plataforma);
    private static final IniciativasService iniciativasService = new IniciativasService(plataforma);

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