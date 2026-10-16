package model.criterio;
import model.Publicacion;

public class CriterioFecha extends Criterio{
    public CriterioFecha(Publicacion publicacion) {
        super(publicacion);
    }

    @Override
    public int compareTo(Criterio o) {
        Publicacion a = this.publicacion;
        Publicacion b = o.publicacion;

        int compare = b.getFechaHora().compareTo(a.getFechaHora());
        if (compare !=0) return compare;
        return Integer.compare(a.getId(),b.getId());
    }
}
