package com.fidecompro.red;

import java.io.Serializable;
import java.util.List;

/**
 * Datos del recuadro "Resumen del día" del menú principal y el aviso de stock bajo (HU 7).
 */
public class ResumenDia implements Serializable {

    private int facturasEmitidas;
    private double totalVendido;
    private int clientesNuevos;
    private List<String> productosBajoMinimo;

    public ResumenDia(int facturasEmitidas, double totalVendido, int clientesNuevos, List<String> productosBajoMinimo) {
        this.facturasEmitidas = facturasEmitidas;
        this.totalVendido = totalVendido;
        this.clientesNuevos = clientesNuevos;
        this.productosBajoMinimo = productosBajoMinimo;
    }

    public int getFacturasEmitidas() {
        return facturasEmitidas;
    }

    public double getTotalVendido() {
        return totalVendido;
    }

    public int getClientesNuevos() {
        return clientesNuevos;
    }

    public List<String> getProductosBajoMinimo() {
        return productosBajoMinimo;
    }
}
