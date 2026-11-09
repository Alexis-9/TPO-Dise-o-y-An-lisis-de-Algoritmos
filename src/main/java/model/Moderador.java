package model;

public class Moderador {

    private final int id;
    private final String nombre;
    private final int limiteComunidades;

    public Moderador(int id, String nombre, int limiteComunidades) {
        this.id = id;
        this.nombre = nombre;
        this.limiteComunidades = limiteComunidades;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getLimiteComunidades() {
        return limiteComunidades;
    }
}
