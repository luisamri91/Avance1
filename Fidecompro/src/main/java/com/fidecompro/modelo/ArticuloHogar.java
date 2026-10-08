package com.fidecompro.modelo;

import java.util.HashSet;
import java.util.Set;

/**
 * Productos de limpieza e higiene.
 */
public class ArticuloHogar extends Producto {

    private String marca;
    private String presentacion;
    // Set: una advertencia no se repite aunque se agregue dos veces
    private Set<String> advertencias;

    public ArticuloHogar(String codigo, String nombre, String descripcion,
            double valorCompra, double valorVenta, int existencias, int stockMinimo,
            String marca, String presentacion) {
        super(codigo, nombre, descripcion, valorCompra, valorVenta, existencias, stockMinimo);
        this.marca = marca;
        this.presentacion = presentacion;
        this.advertencias = new HashSet<>();
    }

    public void agregarAdvertencia(String texto) {
        advertencias.add(texto);
    }

    public void eliminarAdvertencia(String texto) {
        advertencias.remove(texto);
    }

    @Override
    public double obtenerPorcentajeImpuesto() {
        return IVA_GENERAL;
    }

    @Override
    public String mostrarInformacion() {
        String info = super.mostrarInformacion() + " | " + marca + ", " + presentacion;
        if (!advertencias.isEmpty()) {
            info += " | Advertencias: " + String.join("; ", advertencias);
        }
        return info;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public Set<String> getAdvertencias() {
        return advertencias;
    }
}
