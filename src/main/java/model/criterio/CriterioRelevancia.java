package main.java.model.criterio;

import main.java.model.Publicacion;

public class CriterioRelevancia extends Criterio{
    public CriterioRelevancia(Publicacion publicacion) {
        super(publicacion);
    }

    @Override
    public int compareTo(Criterio o) {
        Publicacion a = this.publicacion;
        Publicacion b = o.publicacion;

        int relevanciaA = a.getRelevancia();
        int relevanciaB = b.getRelevancia();

        int mayorRelevancia = Integer.compare(relevanciaB,relevanciaA);
        if (mayorRelevancia!=0) return mayorRelevancia;

        mayorRelevancia = b.getFechaHora().compareTo(a.getFechaHora());
        if (mayorRelevancia!=0) return mayorRelevancia;

        return Integer.compare(a.getId(),b.getId());
    }
}
