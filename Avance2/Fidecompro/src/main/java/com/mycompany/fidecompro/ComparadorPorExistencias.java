package com.mycompany.fidecompro;

import java.util.Comparator;

// ordena de menos a mas existencias
public class ComparadorPorExistencias implements Comparator<Producto> {

    @Override
    public int compare(Producto a, Producto b) {
        return Integer.compare(a.getExistencias(), b.getExistencias());
    }
}
