package com.fidecompro.red;

import java.io.Serializable;

/**
 * Datos de un ajuste de existencias (HU 6). La cantidad es positiva para entradas y negativa para salidas.
 */
public class AjusteStock implements Serializable {

    private int idProducto;
    private int cantidad;
    private String motivo;

    public AjusteStock(int idProducto, int cantidad, String motivo) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.motivo = motivo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getMotivo() {
        return motivo;
    }
}
