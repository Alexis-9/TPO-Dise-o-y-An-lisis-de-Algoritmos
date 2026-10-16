package service;

import algoritmos.resultado.PasoDijkstra;
import model.Usuario;

import java.util.List;

/**
 * Cadena de difusión más rápida de origen a destino.
 *
 * @param cadena      usuarios del camino, de origen a destino (vacía si no es alcanzable)
 * @param tiempoTotal minutos del camino, o null si no es alcanzable
 * @param pasos       pasos de Dijkstra en el orden en que se eligieron los vértices
 * @param decisiones  traza de texto completa de Dijkstra
 * @param alcanzados  usuarios a los que llega la difusión desde el origen, ordenados por id
 */
public record ResultadoDifusion(Usuario origen, Usuario destino, boolean alcanzable,
                                List<Usuario> cadena, Integer tiempoTotal,
                                List<PasoDijkstra<Usuario>> pasos, List<String> decisiones,
                                List<Usuario> alcanzados) { }
