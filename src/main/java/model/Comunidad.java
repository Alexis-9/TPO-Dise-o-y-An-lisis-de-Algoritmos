package model;

public class Comunidad {

    private final int id;
    private final String nombre;

    public Comunidad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
