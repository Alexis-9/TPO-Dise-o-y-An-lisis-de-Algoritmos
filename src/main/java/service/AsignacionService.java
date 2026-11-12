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

        int[][] costosMatriz = new int[moderadores.size()][comunidades.size()];
        int[] limites = new int[moderadores.size()];

        for (int moderador = 0; moderador < moderadores.size(); moderador++) {

            Moderador mod = moderadores.get(moderador);
            limites[moderador] = mod.getLimiteComunidades();

            for (int comunidad = 0; comunidad < comunidades.size(); comunidad++) {

                Comunidad com = comunidades.get(comunidad);

                Integer costoMod = costos.get(mod.getId()).get(com.getId());

                if (costoMod == null) {
                    throw new IllegalStateException("Falta definir el costo del moderador " + mod.getId() + " para la comunidad " + com.getId());
                }

                costosMatriz[moderador][comunidad] = costoMod;

            }
        }

        ResultadoAsignacion resultado = AsignacionModeradores.resolver(costosMatriz, limites);
        if (!resultado.haySolucion()) { throw new IllegalArgumentException("No se pudieron realizar las asignaciones con los datos recibidos."); }

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

    public Moderador agregarModerador(String nombreMod, int limiteCom) { return plataforma.agregarModerador(nombreMod, limiteCom); }

    public void eliminarModerador(int idMod) { plataforma.eliminarModerador(idMod); }

    public Comunidad agregarComunidad(String nombreCom) { return plataforma.agregarComunidad(nombreCom); }

    public void eliminarComunidad(int idCom) { plataforma.eliminarComunidad(idCom); }

    public void definirCosto(int idModerador, int idComunidad, int costo) { plataforma.definirCosto(idModerador, idComunidad, costo); }

}
