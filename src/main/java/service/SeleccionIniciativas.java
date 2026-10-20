package service;

import model.Iniciativa;

import java.util.List;

/** Iniciativas elegidas para un presupuesto y sus totales. */
public record SeleccionIniciativas(int presupuesto, List<Iniciativa> seleccionadas,
                                   int costoTotal, int impactoTotal, int restante) { }
