package model;

import model.interfaces.Grafo;

import java.util.*;

public class RedSocial implements Grafo<Usuario> {

    private final Map<Integer, Usuario> usuarios = new HashMap<>();
    private final Map<Usuario, ArrayList<Vinculo>> adyacencia = new HashMap<>();

    public void agregarUsuario(Usuario u){

        if (u == null){throw new IllegalArgumentException("Usuario no puede ser nulo");}
        if (usuarios.containsKey(u.getId())){throw new IllegalArgumentException("Usuario ya está en la red.");}

        usuarios.put(u.getId(), u);
        adyacencia.put(u, new ArrayList<>());
    }

    public void agregarVinculo(int idOrigen, int idDestino, int minutos){
        if (minutos <= 0) {throw new IllegalArgumentException("Los minutos deben ser mayores a 0");}
        if (!usuarios.containsKey(idOrigen) || !usuarios.containsKey(idDestino)){
            throw new IllegalArgumentException("Id inválido");}

        Usuario origen = usuarios.get(idOrigen);
        Usuario destino = usuarios.get(idDestino);

        if (existeVinculo(origen, destino)){
            throw new IllegalArgumentException("Ya hay un vínculo");
        }

        Vinculo vinculo = new Vinculo(origen,destino,minutos);

        adyacencia.get(origen).add(vinculo);
    }

    public void eliminarUsuario(int idUsuario){
        Usuario usuario = usuarios.get(idUsuario);
        if (usuario == null){throw new NoSuchElementException("No existe un usuario con id " + idUsuario);}

        // Vínculos de entrada: están en las listas de los demás usuarios
        for (ArrayList<Vinculo> salientes : adyacencia.values()) {
            salientes.removeIf(vinculo -> vinculo.getDestino().equals(usuario));
        }

        // Vínculos de salida: se van con su propia lista de adyacencia
        adyacencia.remove(usuario);
        usuarios.remove(idUsuario);
    }

    public void eliminarVinculo(int idOrigen, int idDestino){
        Usuario origen = usuarios.get(idOrigen);
        Usuario destino = usuarios.get(idDestino);
        if (origen == null){throw new NoSuchElementException("No existe un usuario con id " + idOrigen);}
        if (destino == null){throw new NoSuchElementException("No existe un usuario con id " + idDestino);}

        boolean eliminado = adyacencia.get(origen).removeIf(vinculo -> vinculo.getDestino().equals(destino));
        if (!eliminado){
            throw new NoSuchElementException("No hay un vínculo de " + origen.getNombre() + " a " + destino.getNombre());
        }
    }

    private boolean existeVinculo(Usuario origen, Usuario destino){
        ArrayList<Vinculo> vinculos = adyacencia.get(origen);

        for (Vinculo vinculo : vinculos) {
            if (vinculo.getDestino().equals(destino)){
                return true;
            }
        }

        return false;
    }

    public List<Vinculo> getVinculosSalientes(Usuario usuario) {
        if (usuario == null) { throw new IllegalArgumentException("El usuario no puede ser nulo"); }
        return Collections.unmodifiableList(adyacencia.getOrDefault(usuario, new ArrayList<>()));
    }

    public List<Vinculo> getVinculos() {
        List<Vinculo> todos = new ArrayList<>();
        for (List<Vinculo> salientes : adyacencia.values()) {
            todos.addAll(salientes);
        }
        todos.sort(Comparator
                .comparingInt((Vinculo v) -> v.getOrigen().getId())
                .thenComparingInt(v -> v.getDestino().getId()));
        return Collections.unmodifiableList(todos);
    }

    public Usuario getUsuario(int idUsuario){
        return usuarios.get(idUsuario);
    }

    public Collection<Usuario> getUsuarios(){
        return Collections.unmodifiableCollection(usuarios.values());
    }

    @Override
    public Collection<Usuario> getVertices() {
        return getUsuarios();
    }

    @Override
    public List<Vinculo> getAdyacentes(Usuario u) {
        return getVinculosSalientes(u);
    }

    @Override
    public boolean contiene(Usuario u) {
        return u != null && usuarios.containsKey(u.getId());
    }
}
