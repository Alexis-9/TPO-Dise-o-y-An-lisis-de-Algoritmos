package main.java.model;

import java.time.LocalDateTime;

public class Publicacion {

    private final int id;
    private final String autor;
    private final LocalDateTime fechaHora;
    private final int reacciones;
    private final int comentarios;

    public Publicacion(int id, String autor, LocalDateTime fechaHora, int reacciones, int comentarios) {
        this.id = id;
        this.autor = autor;
        this.fechaHora = fechaHora;
        this.reacciones = reacciones;
        this.comentarios = comentarios;
    }

    public int getId() {return id;}
    public String getAutor() {return autor;}
    public LocalDateTime getFechaHora() {return fechaHora;}
    public int getReacciones() {return reacciones;}
    public int getComentarios() {return comentarios;}

    @Override
    public String toString() {
        return "id: " + id + ", autor: " + autor + ", " +
                fechaHora + ", " + "cantidad de reacciones: "
                + reacciones + "cantidad de comentarios: " + comentarios;
    }
}
