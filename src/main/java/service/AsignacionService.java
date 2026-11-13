package service;

import algoritmos.AsignacionModeradores;
import algoritmos.resultado.ResultadoAsignacion;
import datos.Plataforma;
import model.Asignacion;
import model.Comunidad;
import model.Moderador;
import model.PlanAsignacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AsignacionService {

    private final Plataforma plataforma;

    public AsignacionService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    public PlanAsignacion asignarModeradores() {

        List<Moderador> moderadores = plataforma.getModeradores();
        List<Comunidad> comunidades = plataforma.getComunidades();
        Map<Integer, Map<Integer, Integer>> costos = plataforma.getCostos();

        if (comunidades.isEmpty()) { throw new IllegalArgumentException("No hay comunidades para asignar"); }
        if (moderadores.isEmpty()) { throw new IllegalArgumentException("No hay moderadores candidatos"); }

        int cupoTotal = 0;
        for (Moderador m : moderadores) { cupoTotal += m.getLimiteComunidades(); }
        if (cupoTotal < comunidades.size()) {
            throw new IllegalArgumentException("Entre todos los moderadores pueden atender " + cupoTotal
                    + " comunidades y hay " + comunidades.size());
        }

        int[][] costosMatriz = new int[moderadores.size()][comunidades.size()];
        int[] limites = new int[moderadores.size()];

        for (int moderador = 0; moderador < moderadores.size(); moderador++) {

            Moderador mod = moderadores.get(moderador);
            limites[moderador] = mod.getLimiteComunidades();

            for (int comunidad = 0; comunidad < comunidades.size(); comunidad++) {

                Comunidad com = comunidades.get(comunidad);

                Integer costoMod = costos.get(mod.getId()).get(com.getId());

                if (costoMod == null) {
                    throw new IllegalArgumentException("Falta definir el costo de " + mod.getNombre() + " para " + com.getNombre());
                }

                costosMatriz[moderador][comunidad] = costoMod;

            }
        }

        for (int comunidad = 0; comunidad < comunidades.size(); comunidad++) {
            boolean hayCompatible = false;
            for (int moderador = 0; moderador < moderadores.size() && !hayCompatible; moderador++) {
                hayCompatible = costosMatriz[moderador][comunidad] != AsignacionModeradores.INCOMPATIBLE;
            }
            if (!hayCompatible) {
                throw new IllegalArgumentException("Ningún moderador puede atender " + comunidades.get(comunidad).getNombre());
            }
        }

        ResultadoAsignacion resultado = AsignacionModeradores.resolver(costosMatriz, limites);
        if (!resultado.haySolucion()) {
            throw new IllegalArgumentException("No hay forma de cubrir todas las comunidades respetando los límites"
                    + " y las incompatibilidades de los moderadores");
        }

        return generarPlanAsignacion(resultado, moderadores, comunidades, costosMatriz);

    }

    private PlanAsignacion generarPlanAsignacion(ResultadoAsignacion resultado, List<Moderador> moderadores, List<Comunidad> comunidades, int[][] costosMatriz) {

        int[] mejorAsignacion = resultado.getMejorAsignacion();
        List<Asignacion> asignaciones = new ArrayList<>();

        for (int mod = 0; mod < moderadores.size(); mod++) {
            List<Comunidad> comunidadesMod = new ArrayList<>();
            int costoAsignacion = 0;

            for (int com = 0; com < mejorAsignacion.length; com++) {
                if (mejorAsignacion[com] == mod) {

                    comunidadesMod.add(comunidades.get(com));
                    costoAsignacion += costosMatriz[mod][com];

                }
            }

            asignaciones.add(new Asignacion(moderadores.get(mod), comunidadesMod, costoAsignacion));
        }

        return new PlanAsignacion(asignaciones, resultado.getCostoMinimo());
    }

    /** Moderadores en el orden en que se cargaron (que es el de sus ids). */
    public List<Moderador> listarModeradores() { return plataforma.getModeradores(); }

    /** Comunidades en el orden en que se cargaron (que es el de sus ids). */
    public List<Comunidad> listarComunidades() { return plataforma.getComunidades(); }

    /** costos.get(idModerador).get(idComunidad); falta la entrada si todavía no se definió. */
    public Map<Integer, Map<Integer, Integer>> costos() { return plataforma.getCostos(); }

    public Moderador agregarModerador(String nombreMod, int limiteCom) {
        // Moderador no valida nada
        if (nombreMod == null || nombreMod.isBlank()) { throw new IllegalArgumentException("El nombre no puede estar vacío"); }
        if (limiteCom < 1) { throw new IllegalArgumentException("El límite de comunidades tiene que ser al menos 1"); }
        return plataforma.agregarModerador(nombreMod.strip(), limiteCom);
    }

    public void eliminarModerador(int idMod) { plataforma.eliminarModerador(idMod); }

    public Comunidad agregarComunidad(String nombreCom) {
        if (nombreCom == null || nombreCom.isBlank()) { throw new IllegalArgumentException("El nombre no puede estar vacío"); }
        return plataforma.agregarComunidad(nombreCom.strip());
    }

    public void eliminarComunidad(int idCom) { plataforma.eliminarComunidad(idCom); }

    public void definirCosto(int idModerador, int idComunidad, int costo) {
        if (costo < 0) { throw new IllegalArgumentException("El costo no puede ser negativo"); }
        plataforma.definirCosto(idModerador, idComunidad, costo);
    }

    public void definirIncompatible(int idModerador, int idComunidad) {
        plataforma.definirCosto(idModerador, idComunidad, AsignacionModeradores.INCOMPATIBLE);
    }

}
