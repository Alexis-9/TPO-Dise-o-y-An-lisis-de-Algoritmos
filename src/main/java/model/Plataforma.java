package model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Plataforma {

    private final RedSocial red = new RedSocial();
    private final HashMap<Integer, Publicacion> publicaciones = new HashMap<>();
    private final HashMap<Integer, Iniciativa> iniciativas = new HashMap<>();

    public RedSocial getRed(){
        return red;
    }

    public void agregarPublicacion(Publicacion p){

        if (p == null){throw new IllegalArgumentException("Publicación no puede ser nula");}

        if (publicaciones.containsKey(p.getId())){
            throw new IllegalArgumentException("Id repetido.");
        }

        publicaciones.put(p.getId(),p);
    }

    public void agregarIniciativa(Iniciativa i){

        if (i == null){throw new IllegalArgumentException("Iniciativa no puede ser nula");}

        if (iniciativas.containsKey(i.getId())){
            throw new IllegalArgumentException("Id repetido");
        }

        iniciativas.put(i.getId(),i);
    }

    public Collection<Publicacion> getPublicaciones(){
        return Collections.unmodifiableCollection(publicaciones.values());
    }

    public Collection<Iniciativa> getIniciativas(){
        return Collections.unmodifiableCollection(iniciativas.values());
    }

}
