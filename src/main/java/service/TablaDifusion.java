package service;

import model.Usuario;

import java.util.List;

/**
 * Tiempos mínimos de difusión entre todos los pares de usuarios.
 *
 * @param usuarios usuarios ordenados por id
 * @param tiempos  tiempos[i][j] son los minutos de usuarios.get(i) a usuarios.get(j),
 *                 o null si no existe una cadena entre ellos
 */
public record TablaDifusion(List<Usuario> usuarios, Integer[][] tiempos) { }
