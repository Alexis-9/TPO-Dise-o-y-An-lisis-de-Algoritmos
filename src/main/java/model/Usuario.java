package main.java.model;

public class Usuario implements Comparable<Usuario> {

    private final int id;
    private final String nombre;

    public Usuario(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() { return nombre + "( " + id + " )"; }

    @Override
    public int compareTo(Usuario o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public boolean equals(Object o){
        return o instanceof Usuario u && u.id == id;
    }

    @Override
    public int hashCode(){
        return Integer.hashCode(id);
    }
}
