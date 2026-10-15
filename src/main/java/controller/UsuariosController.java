package main.java.controller;

import main.java.algoritmos.MergeSort;
import main.java.model.RedSocial;
import main.java.model.Usuario;
import main.java.model.Vinculo;
import java.util.Arrays;
import java.util.List;

public class UsuariosController {

    private final RedSocial red;

    public UsuariosController(RedSocial red) {
        this.red = red;
    }

    public void agregarUsuario(int idUsuario, String nombre){
        red.agregarUsuario(new Usuario(idUsuario, nombre));
    }

    public void agregarVinculo(int idOrigen, int idDestino, int minutos){
        red.agregarVinculo(idOrigen, idDestino, minutos);
    }

    public List<Usuario> listarUsuarios(){
        Usuario[] usuarios = red.getUsuarios().toArray(new Usuario[0]);
        MergeSort.mergeSort(usuarios,0 , usuarios.length -1);
        return Arrays.asList(usuarios);
    }

    public List<Vinculo> listarVinculosSalientes(int idOrigen){

        if (!red.contiene(red.getUsuario(idOrigen))){
            throw new IllegalArgumentException("No existe ese usuario");
        }

        return red.getVinculosSalientes(red.getUsuario(idOrigen));
    }

    public List<Vinculo> listarVinculos(){
        return red.getVinculos();
    }

    public Usuario buscarUsuario(int idUsuario){

        if (!red.contiene(red.getUsuario(idUsuario))){
            throw new IllegalArgumentException("No existe ese usuario");
        }

        return red.getUsuario(idUsuario);
    }
}
