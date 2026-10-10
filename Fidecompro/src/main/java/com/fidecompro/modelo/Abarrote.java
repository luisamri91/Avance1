package com.fidecompro.modelo;

import java.time.LocalDate;

/**
 * Arroz, frijoles, aceite, café... Si es de canasta básica paga 1% de IVA.
 */
public class Abarrote extends Producto {

    public static final double IVA_CANASTA_BASICA = 0.01;

    private LocalDate fechaVencimiento;
    private double pesoKg;
    private boolean canastaBasica;

    public Abarrote(String codigo, String nombre, String descripcion,
            double valorCompra, double valorVenta, int existencias, int stockMinimo,
            LocalDate fechaVencimiento, double pesoKg, boolean canastaBasica) {
        super(codigo, nombre, descripcion, valorCompra, valorVenta, existencias, stockMinimo);
        this.fechaVencimiento = fechaVencimiento;
        this.pesoKg = pesoKg;
        this.canastaBasica = canastaBasica;
    }

    @Override
    public double obtenerPorcentajeImpuesto() {
        return canastaBasica ? IVA_CANASTA_BASICA : IVA_GENERAL;
    }

    public boolean estaVencido() {
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    @Override
    public String obtenerTipo() {
        return "Abarrote";
    }

    @Override
    public String mostrarInformacion() {
        return super.mostrarInformacion() + " | Abarrote " + pesoKg + " kg, vence "
                + fechaVencimiento + (canastaBasica ? ", canasta basica" : "");
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public boolean isCanastaBasica() {
        return canastaBasica;
    }

    public void setCanastaBasica(boolean canastaBasica) {
        this.canastaBasica = canastaBasica;
    }
}
