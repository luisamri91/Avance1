package com.fidecompro.red;

import com.fidecompro.modelo.EstadoFactura;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Filtros del historial de facturas (HU 11). Cliente y estado son opcionales (null = todos).
 */
public class FiltroFacturas implements Serializable {

    private LocalDate desde;
    private LocalDate hasta;
    private String textoCliente;
    private EstadoFactura estado;

    public FiltroFacturas(LocalDate desde, LocalDate hasta, String textoCliente, EstadoFactura estado) {
        this.desde = desde;
        this.hasta = hasta;
        this.textoCliente = textoCliente;
        this.estado = estado;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public String getTextoCliente() {
        return textoCliente;
    }

    public EstadoFactura getEstado() {
        return estado;
    }
}
