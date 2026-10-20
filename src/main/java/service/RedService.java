package service;

import algoritmos.MergeSort;
import datos.Plataforma;
import model.Usuario;
import model.Vinculo;

import java.util.Arrays;
import java.util.List;

/** Casos de uso de la red: usuarios y vínculos. */
public class RedService {

    private final Plataforma plataforma;

    public RedService(Plataforma plataforma) {
        this.plataforma = plataforma;
    }

    /** Usuarios ordenados por id. */
    public List<Usuario> listarUsuarios() {
        Usuario[] usuarios = plataforma.getUsuarios().toArray(new Usuario[0]);
        MergeSort.mergeSort(usuarios, 0, usuarios.length - 1);
        return Arrays.asList(usuarios);
    }

    /** Vínculos ordenados por origen y destino. */
    public List<Vinculo> listarVinculos() {
        return plataforma.getVinculos();
    }

    public Usuario agregarUsuario(String nombre) {
        // Usuario no valida el nombre
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        return plataforma.agregarUsuario(nombre.strip());
    }

    /** Borra al usuario junto con sus vínculos de entrada y de salida. */
    public void eliminarUsuario(int idUsuario) {
        plataforma.eliminarUsuario(idUsuario);
    }

    public Vinculo agregarVinculo(int idOrigen, int idDestino, int minutos) {
        // RedSocial valida minutos, ids y duplicados, pero no los vínculos a uno mismo
        if (idOrigen == idDestino) {
            throw new IllegalArgumentException("Un usuario no puede tener un vínculo consigo mismo");
        }
        return plataforma.agregarVinculo(idOrigen, idDestino, minutos);
    }

    public void eliminarVinculo(int idOrigen, int idDestino) {
        plataforma.eliminarVinculo(idOrigen, idDestino);
    }
}
