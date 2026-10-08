package com.fidecompro.modelo;

/**
 * Una línea de la factura. Solo existe dentro de su factura (composición).
 */
public class DetalleFactura implements Mostrable {

    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public DetalleFactura(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        // Se copia el precio para que la factura no cambie si luego cambia el precio del producto
        this.precioUnitario = producto.getValorVenta();
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public double calcularImpuesto() {
        return calcularSubtotal() * producto.obtenerPorcentajeImpuesto();
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
}
