package view;

import algoritmos.resultado.ResultadoDijkstra;
import controller.DifusionController;
import controller.IniciativasController;
import controller.PublicacionesController;
import controller.UsuariosController;
import model.Iniciativa;
import model.Publicacion;
import model.RedSocial;
import model.Usuario;
import model.Vinculo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuConsola {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int PRESUPUESTO_POR_DEFECTO = 50;

    private static UsuariosController usuariosController;
    private static DifusionController difusionController;
    private static PublicacionesController publicacionesController;
    private static ArrayList<Iniciativa> iniciativas;

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
                            publicacionesController.listarMasRecientes()); }
                    case 3 -> { if (hayDatos()) mostrarMuro("MURO: de la más relevante a la menos relevante",
                            publicacionesController.listarMasRelevantes()); }
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
        if (usuariosController == null) {
            System.out.println("Primero cargá los datos de prueba (opción 1).");
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Datos de prueba
    // ------------------------------------------------------------------

    private static void cargarDatosDePrueba() {

        RedSocial red = new RedSocial();
        usuariosController = new UsuariosController(red);
        difusionController = new DifusionController(red);

        usuariosController.agregarUsuario(1, "Ana");
        usuariosController.agregarUsuario(2, "Bruno");
        usuariosController.agregarUsuario(3, "Carla");
        usuariosController.agregarUsuario(4, "Diego");
        usuariosController.agregarUsuario(5, "Eva");
        usuariosController.agregarUsuario(6, "Franco");
        usuariosController.agregarUsuario(7, "Gina");   // nadie llega a Gina: destino inalcanzable

        usuariosController.agregarVinculo(1, 2, 10);
        usuariosController.agregarVinculo(1, 3, 3);
        usuariosController.agregarVinculo(3, 2, 4);     // 1->3->2 (7) le gana a 1->2 (10)
        usuariosController.agregarVinculo(2, 4, 5);
        usuariosController.agregarVinculo(3, 4, 12);
        usuariosController.agregarVinculo(3, 5, 8);
        usuariosController.agregarVinculo(4, 6, 7);
        usuariosController.agregarVinculo(5, 6, 2);     // mejor camino de Ana a Franco: 1->3->5->6 (13)
        usuariosController.agregarVinculo(7, 1, 5);     // Gina llega a Ana, pero no al revés

        ArrayList<Publicacion> publicaciones = new ArrayList<>();
        publicaciones.add(new Publicacion(3, "Carla",  LocalDateTime.of(2026, 10, 12, 18, 30), 8, 0));   // relevancia 8
        publicaciones.add(new Publicacion(6, "Franco", LocalDateTime.of(2026, 10, 8, 20, 0), 30, 4));    // relevancia 42
        publicaciones.add(new Publicacion(1, "Ana",    LocalDateTime.of(2026, 10, 10, 9, 0), 10, 2));    // relevancia 16
        publicaciones.add(new Publicacion(7, "Gina",   LocalDateTime.of(2026, 10, 16, 10, 0), 1, 0));    // relevancia 1
        publicaciones.add(new Publicacion(2, "Bruno",  LocalDateTime.of(2026, 10, 12, 18, 30), 5, 1));   // relevancia 8 (empata con #3 en todo: gana id menor)
        publicaciones.add(new Publicacion(5, "Eva",    LocalDateTime.of(2026, 10, 15, 8, 15), 2, 2));    // relevancia 8 (más reciente que #2 y #3)
        publicaciones.add(new Publicacion(4, "Diego",  LocalDateTime.of(2026, 10, 14, 12, 0), 20, 5));   // relevancia 35
        publicaciones.add(new Publicacion(8, "Hugo",   LocalDateTime.of(2026, 10, 11, 15, 45), 4, 4));   // relevancia 16 (empata con #1, #8 es más reciente)
        publicacionesController = new PublicacionesController(publicaciones);

        iniciativas = new ArrayList<>();
        iniciativas.add(new Iniciativa(1, 10, 60));
        iniciativas.add(new Iniciativa(2, 20, 100));
        iniciativas.add(new Iniciativa(3, 30, 120));
        iniciativas.add(new Iniciativa(4, 25, 55));
        iniciativas.add(new Iniciativa(5, 15, 40));
        iniciativas.add(new Iniciativa(6, 80, 200));    // costo mayor al presupuesto: nunca entra

        System.out.println("Datos de prueba cargados:");
        System.out.println(" - " + usuariosController.listarUsuarios().size() + " usuarios y "
                + usuariosController.listarVinculos().size() + " vínculos");
        System.out.println(" - " + publicaciones.size() + " publicaciones");
        System.out.println(" - " + iniciativas.size() + " iniciativas (presupuesto sugerido: "
                + PRESUPUESTO_POR_DEFECTO + ")");
    }

    // ------------------------------------------------------------------
    // Muro de publicaciones
    // ------------------------------------------------------------------

    private static void mostrarMuro(String titulo, ArrayList<Publicacion> ordenadas) {
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
        for (Usuario u : usuariosController.listarUsuarios()) {
            System.out.println(u);
        }
        System.out.println();
        System.out.println("--- VÍNCULOS (sentido de transmisión y tiempo) ---");
        for (Vinculo v : usuariosController.listarVinculos()) {
            System.out.println(v);
        }
    }

    // ------------------------------------------------------------------
    // Cadena de difusión más rápida (Dijkstra)
    // ------------------------------------------------------------------

    private static void mostrarCadenaMasRapida() {
        System.out.println("Usuarios disponibles:");
        for (Usuario u : usuariosController.listarUsuarios()) {
            System.out.println("  " + u);
        }

        int idOrigen = leerEntero("ID del usuario de origen: ");
        int idDestino = leerEntero("ID del usuario de destino: ");

        Usuario destino = difusionController.buscarUsuario(idDestino);
        ResultadoDijkstra<Usuario> resultado = difusionController.cadenaMasRapida(idOrigen);

        System.out.println();
        System.out.println("--- Decisiones tomadas durante el cálculo ---");
        for (String paso : resultado.getTraza()) {
            System.out.println(paso);
        }

        System.out.println();
        System.out.println("--- Resultado ---");
        if (!resultado.esAlcanzable(destino)) {
            System.out.println("El destino no es alcanzable desde " + resultado.getOrigen() + ".");
            return;
        }

        System.out.println("Usuarios de la cadena: " + resultado.getCamino(destino));
        System.out.println("Tiempo total estimado: " + resultado.getDistancia(destino) + " min");
        System.out.println(resultado.describirCadena(destino));
    }

    // ------------------------------------------------------------------
    // Selección de iniciativas
    // ------------------------------------------------------------------

    private static void mostrarSeleccionIniciativas() {
        int presupuesto = leerEnteroConDefecto("Presupuesto disponible (Enter = "
                + PRESUPUESTO_POR_DEFECTO + "): ", PRESUPUESTO_POR_DEFECTO);
        if (presupuesto < 0) {
            throw new IllegalArgumentException("El presupuesto no puede ser negativo");
        }

        IniciativasController controller = new IniciativasController(presupuesto, new ArrayList<>(iniciativas));

        System.out.println();
        System.out.println("--- Iniciativas disponibles ---");
        for (Iniciativa i : controller.totalIniciativas()) {
            System.out.println(i);
        }

        System.out.println();
        System.out.println("--- Iniciativas seleccionadas (presupuesto: " + presupuesto + ") ---");
        if (controller.seleccionIniciativas().isEmpty()) {
            System.out.println("Ninguna iniciativa entra en el presupuesto.");
        }
        for (Iniciativa i : controller.seleccionIniciativas()) {
            System.out.println(i);
        }

        System.out.println();
        System.out.println("Costo total:           " + controller.costoTotal());
        System.out.println("Impacto obtenido:      " + controller.impactoObtenido());
        System.out.println("Presupuesto restante:  " + controller.presupuestoRestante());
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