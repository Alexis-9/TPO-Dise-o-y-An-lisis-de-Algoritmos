package service;

import datos.DatosDemo;
import datos.Plataforma;

/** Carga del escenario de demo y vaciado de la plataforma. */
public class DatosService {

    private final Plataforma plataforma;
    private final DatosDemo datosDemo;

    public DatosService(Plataforma plataforma, DatosDemo datosDemo) {
        this.plataforma = plataforma;
        this.datosDemo = datosDemo;
    }

    /** Reemplaza todo por el escenario de demo. */
    public void cargarDemo() {
        datosDemo.cargarEn(plataforma);
    }

    public void vaciar() {
        plataforma.vaciar();
    }
}
