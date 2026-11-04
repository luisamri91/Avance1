package com.mycompany.fidecompro;

import java.time.LocalDate;

public class Abarrote extends Producto {

    private LocalDate fechaVencimiento;
    private boolean canastaBasica;

    public Abarrote(String codigo, String nombre, Categoria categoria, double valorCompra, double valorVenta,
            int existencias, int stockMinimo, LocalDate fechaVencimiento, boolean canastaBasica) {
        super(codigo, nombre, categoria, valorCompra, valorVenta, existencias, stockMinimo);
        this.fechaVencimiento = fechaVencimiento;
        this.canastaBasica = canastaBasica;
    }

    // los de canasta basica pagan 1%, los demas 13%
    @Override
    public double obtenerPorcentajeImpuesto() {
        if (canastaBasica) {
            return 0.01;
        }
        return 0.13;
    }

    @Override
    public String obtenerTipo() {
        return "Abarrote";
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isCanastaBasica() {
        return canastaBasica;
    }
}
