package model.criterio;
import model.Publicacion;

public abstract class Criterio implements Comparable<Criterio>{
    protected Publicacion publicacion;

    public Criterio(Publicacion publicacion){
        this.publicacion = publicacion;
    }

    public Publicacion getPublicacion(){
        return publicacion;
    }
}
