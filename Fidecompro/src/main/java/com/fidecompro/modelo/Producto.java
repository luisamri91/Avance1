package com.fidecompro.modelo;

import com.fidecompro.excepciones.StockInsuficienteException;
import java.io.Serializable;

/**
 * Cualquier artículo que vende la cadena. Cada tipo de producto es una subclase
 * que define su propio porcentaje de impuesto.
 */
public abstract class Producto implements Mostrable, Comparable<Producto>, Serializable {

    public static final double IVA_GENERAL = 0.13;
    private static int idAutoIncremental = 1;

    protected int id;
    protected String codigo;
    protected String nombre;
    protected String descripcion;
    protected double valorCompra;
    protected double valorVenta;
    protected int existencias;
    protected int stockMinimo;
    protected Categoria categoria;
    // Un producto que ya está en facturas no se borra: se desactiva para no venderlo más
    protected boolean activo;

    public Producto(String codigo, String nombre, String descripcion,
            double valorCompra, double valorVenta, int existencias, int stockMinimo) {
        this.id = idAutoIncremental++;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.valorCompra = valorCompra;
        this.valorVenta = valorVenta;
        this.existencias = existencias;
        this.stockMinimo = stockMinimo;
        this.activo = true;
    }

    // Cada subclase responde distinto a este mensaje (polimorfismo)
    public abstract double obtenerPorcentajeImpuesto();

    // Nombre del tipo para las tablas: Abarrote, Bebida o Art. hogar
    public abstract String obtenerTipo();

    public void descontarExistencias(int cantidad) throws StockInsuficienteException {
        if (cantidad > existencias) {
            throw new StockInsuficienteException("Stock insuficiente de " + nombre
                    + ": hay " + existencias + " unidades.");
        }
        existencias -= cantidad;
    }

    public void aumentarExistencias(int cantidad) {
        existencias += cantidad;
    }

    public boolean necesitaReabastecer() {
        return existencias <= stockMinimo;
    }

    // Orden natural del catálogo: por nombre
    @Override
    public int compareTo(Producto otro) {
        return this.nombre.compareToIgnoreCase(otro.nombre);
    }

    @Override
    public String mostrarInformacion() {
        return String.format("[%s] %s | Venta: %,.2f | IVA: %.0f%% | Existencias: %d%s",
                codigo, nombre, valorVenta, obtenerPorcentajeImpuesto() * 100, existencias,
                necesitaReabastecer() ? " (BAJO MINIMO)" : "");
    }

    public int getId() {
        return id;
    }

    // La base de datos genera el id; el DAO lo coloca aquí
    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getValorCompra() {
        return valorCompra;
    }

    public void setValorCompra(double valorCompra) {
        this.valorCompra = valorCompra;
    }

    public double getValorVenta() {
        return valorVenta;
    }

    public void setValorVenta(double valorVenta) {
        this.valorVenta = valorVenta;
    }

    public int getExistencias() {
        return existencias;
    }

    public void setExistencias(int existencias) {
        this.existencias = existencias;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // En los combos de la factura se muestra código y nombre
    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
