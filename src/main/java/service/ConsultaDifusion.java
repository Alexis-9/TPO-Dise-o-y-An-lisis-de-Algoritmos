package service;

import model.Usuario;

import java.util.List;

/**
 * Respuesta a una consulta de difusión entre dos usuarios, sacada de las matrices de Floyd.
 *
 * @param cadena      usuarios del camino, de origen a destino (vacía si no es alcanzable)
 * @param tiempoTotal minutos del camino, o null si no es alcanzable
 */
public record ConsultaDifusion(Usuario origen, Usuario destino, boolean alcanzable,
                               List<Usuario> cadena, Integer tiempoTotal) { }
