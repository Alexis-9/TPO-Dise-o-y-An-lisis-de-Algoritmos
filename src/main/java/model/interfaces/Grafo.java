package main.java.model.interfaces;

import java.util.Collection;
import java.util.List;

public interface Grafo<T> {

    Collection<T> getVertices();
    List<? extends Arista<T>> getAdyacentes(T vertice);
    boolean contiene(T vertice);

}
