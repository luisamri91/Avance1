package com.mycompany.fidecompro;

public class ArticuloHogar extends Producto {

    private String marca;
    private String presentacion;

    public ArticuloHogar(String codigo, String nombre, Categoria categoria, double valorCompra, double valorVenta,
            int existencias, int stockMinimo, String marca, String presentacion) {
        super(codigo, nombre, categoria, valorCompra, valorVenta, existencias, stockMinimo);
        this.marca = marca;
        this.presentacion = presentacion;
    }

    @Override
    public double obtenerPorcentajeImpuesto() {
        return 0.13;
    }

    @Override
    public String obtenerTipo() {
        return "Art. hogar";
    }

    public String getMarca() {
        return marca;
    }

    public String getPresentacion() {
        return presentacion;
    }
}
