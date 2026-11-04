package com.mycompany.fidecompro;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class RegistroFacturas implements Serializable {

    // TreeMap para que queden ordenadas por numero
    private TreeMap<Integer, Factura> facturas;

    public RegistroFacturas() {
        facturas = new TreeMap<>();
    }

    // revisa todo, descuenta el stock y le pone el siguiente numero
    public void emitir(Factura factura) throws StockInsuficienteException {
        if (factura.getCliente() == null || factura.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un cliente y agregar al menos un producto.");
        }
        if (factura.getPorcentajeDescuento() < 0 || factura.getPorcentajeDescuento() > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100.");
        }
        // primero se revisa que alcance todo, asi no queda nada a medias
        for (DetalleFactura d : factura.getDetalles()) {
            if (d.getCantidad() > d.getProducto().getExistencias()) {
                throw new StockInsuficienteException("Stock insuficiente de " + d.getProducto().getNombre()
                        + ": hay " + d.getProducto().getExistencias() + " unidades.");
            }
        }
        for (DetalleFactura d : factura.getDetalles()) {
            d.getProducto().descontarExistencias(d.getCantidad());
        }
        int siguiente = facturas.isEmpty() ? 1 : facturas.lastKey() + 1;
        factura.setNumero(siguiente);
        factura.setFecha(LocalDateTime.now());
        factura.setEstado(EstadoFactura.EMITIDA);
        facturas.put(siguiente, factura);
    }

    public boolean clienteTieneFacturas(Cliente cliente) {
        for (Factura f : facturas.values()) {
            if (f.getCliente().getId() == cliente.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean productoEnFacturas(Producto producto) {
        for (Factura f : facturas.values()) {
            for (DetalleFactura d : f.getDetalles()) {
                if (d.getProducto().getCodigo().equals(producto.getCodigo())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int cantidad() {
        return facturas.size();
    }

    public double totalVendido() {
        double total = 0;
        for (Factura f : facturas.values()) {
            if (f.getEstado() == EstadoFactura.EMITIDA) {
                total += f.calcularTotal();
            }
        }
        return total;
    }
}
