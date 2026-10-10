package com.fidecompro.modelo;

/**
 * Refrescos, jugos y agua.
 */
public class Bebida extends Producto {

    private int volumenMl;
    private int unidadesPorPaquete;
    private boolean retornable;

    public Bebida(String codigo, String nombre, String descripcion,
            double valorCompra, double valorVenta, int existencias, int stockMinimo,
            int volumenMl, int unidadesPorPaquete, boolean retornable) {
        super(codigo, nombre, descripcion, valorCompra, valorVenta, existencias, stockMinimo);
        this.volumenMl = volumenMl;
        this.unidadesPorPaquete = unidadesPorPaquete;
        this.retornable = retornable;
    }

    @Override
    public double obtenerPorcentajeImpuesto() {
        return IVA_GENERAL;
    }

    @Override
    public String obtenerTipo() {
        return "Bebida";
    }

    @Override
    public String mostrarInformacion() {
        return super.mostrarInformacion() + " | Bebida " + volumenMl + " ml x "
                + unidadesPorPaquete + (retornable ? ", retornable" : "");
    }

    public int getVolumenMl() {
        return volumenMl;
    }

    public void setVolumenMl(int volumenMl) {
        this.volumenMl = volumenMl;
    }

    public int getUnidadesPorPaquete() {
        return unidadesPorPaquete;
    }

    public void setUnidadesPorPaquete(int unidadesPorPaquete) {
        this.unidadesPorPaquete = unidadesPorPaquete;
    }

    public boolean isRetornable() {
        return retornable;
    }

    public void setRetornable(boolean retornable) {
        this.retornable = retornable;
    }
}
