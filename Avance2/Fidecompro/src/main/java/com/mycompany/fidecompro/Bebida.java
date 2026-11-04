package com.mycompany.fidecompro;

public class Bebida extends Producto {

    private int volumenMl;
    private boolean retornable;

    public Bebida(String codigo, String nombre, Categoria categoria, double valorCompra, double valorVenta,
            int existencias, int stockMinimo, int volumenMl, boolean retornable) {
        super(codigo, nombre, categoria, valorCompra, valorVenta, existencias, stockMinimo);
        this.volumenMl = volumenMl;
        this.retornable = retornable;
    }

    @Override
    public double obtenerPorcentajeImpuesto() {
        return 0.13;
    }

    @Override
    public String obtenerTipo() {
        return "Bebida";
    }

    public int getVolumenMl() {
        return volumenMl;
    }

    public boolean isRetornable() {
        return retornable;
    }
}
