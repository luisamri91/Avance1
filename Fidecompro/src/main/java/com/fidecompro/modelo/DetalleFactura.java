package com.fidecompro.modelo;

import java.io.Serializable;

/**
 * Una línea de la factura. Solo existe dentro de su factura (composición).
 */
public class DetalleFactura implements Mostrable, Serializable {

    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double porcentajeIva;

    public DetalleFactura(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        // Se copian el precio y el IVA para que la factura no cambie si luego cambia el producto
        this.precioUnitario = producto.getValorVenta();
        this.porcentajeIva = producto.obtenerPorcentajeImpuesto();
    }

    // Para reconstruir una línea guardada en la base de datos, con los valores del momento de la venta
    public DetalleFactura(Producto producto, int cantidad, double precioUnitario, double porcentajeIva) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.porcentajeIva = porcentajeIva;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public double calcularImpuesto() {
        return calcularSubtotal() * porcentajeIva;
    }

    public double calcularTotal() {
        return calcularSubtotal() + calcularImpuesto();
    }

    @Override
    public String mostrarInformacion() {
        return String.format("%4d  %-30s %10s %12s", cantidad, recortar(producto.getNombre(), 30),
                Factura.formatearMonto(precioUnitario), Factura.formatearMonto(calcularSubtotal()));
    }

    private String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo);
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getPorcentajeIva() {
        return porcentajeIva;
    }
}
