package com.fidecompro.modelo;

import com.fidecompro.excepciones.RegistroNoEncontradoException;
import com.fidecompro.excepciones.StockInsuficienteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Guarda las facturas emitidas. Al emitir descuenta existencias y al anular las devuelve.
 */
public class RegistroFacturas {

    // TreeMap: la clave es el número de factura y las mantiene ordenadas por número,
    // así el historial sale en orden y firstKey()/lastKey() dan la primera y la última.
    private TreeMap<Integer, Factura> facturas;

    public RegistroFacturas() {
        this.facturas = new TreeMap<>();
    }

    // synchronized: en el servidor varias cajas podrán emitir a la vez sobre el mismo inventario
    public synchronized void agregarFactura(Factura factura) throws StockInsuficienteException {
        if (factura.estaVacia()) {
            throw new IllegalArgumentException("No se puede emitir una factura sin productos");
        }
        // Primero se valida todo, para no descontar a medias
        for (DetalleFactura d : factura.getDetalles()) {
            if (d.getCantidad() > d.getProducto().getExistencias()) {
                throw new StockInsuficienteException("Stock insuficiente de " + d.getProducto().getNombre()
                        + ": se pidieron " + d.getCantidad() + " y hay " + d.getProducto().getExistencias());
            }
        }
        for (DetalleFactura d : factura.getDetalles()) {
            d.getProducto().descontarExistencias(d.getCantidad());
        }
        factura.asignarNumero();
        facturas.put(factura.getNumero(), factura);
    }

    public Factura buscarFactura(int numero) throws RegistroNoEncontradoException {
        Factura factura = facturas.get(numero);
        if (factura == null) {
            throw new RegistroNoEncontradoException("No existe la factura numero " + numero);
        }
        return factura;
    }

    public synchronized void anularFactura(int numero) throws RegistroNoEncontradoException {
        Factura factura = buscarFactura(numero);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("La factura " + factura.getNumeroFormateado() + " ya estaba anulada");
        }
        factura.anular();
        for (DetalleFactura d : factura.getDetalles()) {
            d.getProducto().aumentarExistencias(d.getCantidad());
        }
    }

    public List<Factura> facturasPorFecha(LocalDate desde, LocalDate hasta) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas.values()) {
            LocalDate dia = f.getFecha().toLocalDate();
            if (!dia.isBefore(desde) && !dia.isAfter(hasta)) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public double totalVendido(LocalDate desde, LocalDate hasta) {
        double total = 0;
        for (Factura f : facturasPorFecha(desde, hasta)) {
            if (f.getEstado() == EstadoFactura.EMITIDA) {
                total += f.calcularTotal();
            }
        }
        return total;
    }

    // Se consultan antes de eliminar: un cliente o usuario con facturas solo se desactiva.
    public boolean tieneFacturasCliente(int idCliente) {
        for (Factura f : facturas.values()) {
            if (f.getCliente().getId() == idCliente) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneFacturasUsuario(int idUsuario) {
        for (Factura f : facturas.values()) {
            if (f.getVendedor().getId() == idUsuario) {
                return true;
            }
        }
        return false;
    }

    public TreeMap<Integer, Factura> getFacturas() {
        return facturas;
    }
}
