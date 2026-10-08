package com.fidecompro.modelo;

import java.util.Comparator;

/**
 * Orden alterno: de menos a más existencias, para ver primero lo que urge reabastecer.
 */
public class ComparadorPorExistencias implements Comparator<Producto> {

    @Override
    public int compare(Producto a, Producto b) {
        return Integer.compare(a.getExistencias(), b.getExistencias());
    }
}
