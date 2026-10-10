package com.fidecompro.servidor;

import com.fidecompro.datos.ClienteDAO;
import com.fidecompro.datos.FacturaDAO;
import com.fidecompro.datos.ProductoDAO;
import com.fidecompro.excepciones.StockInsuficienteException;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.DetalleFactura;
import com.fidecompro.modelo.EstadoFactura;
import com.fidecompro.modelo.Factura;
import com.fidecompro.modelo.Producto;
import com.fidecompro.modelo.Usuario;
import com.fidecompro.red.FiltroFacturas;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Emisión, consulta y anulación de facturas (HU 8, 9 y 11).
 */
public class ServicioFacturacion {

    private final FacturaDAO facturaDAO = new FacturaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Emite la factura que armó la caja. El vendedor es siempre el usuario de la sesión.
     * Varias cajas pueden llamar a este método al mismo tiempo (HU 9): el bloque synchronized
     * hace que revisen y descuenten existencias de una en una.
     */
    public Factura crearFactura(Factura recibida, Usuario vendedor) throws SQLException, StockInsuficienteException {
        if (recibida.getCliente() == null || recibida.estaVacia()) {
            throw new IllegalArgumentException("Debe seleccionar un cliente y agregar al menos un producto.");
        }
        if (recibida.getPorcentajeDescuento() < 0 || recibida.getPorcentajeDescuento() > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100.");
        }
        Cliente cliente = clienteDAO.buscarPorId(recibida.getCliente().getId());
        if (cliente == null || !cliente.isActivo()) {
            throw new IllegalArgumentException("El cliente no existe o está inactivo.");
        }
        Factura factura = new Factura(cliente, vendedor, recibida.getMetodoPago());
        factura.aplicarDescuento(recibida.getPorcentajeDescuento());
        // Cada línea se arma con el precio e IVA vigentes en la base, no con los que tenía la caja
        for (DetalleFactura d : recibida.getDetalles()) {
            Producto actual = productoDAO.buscarPorId(d.getProducto().getId());
            if (actual == null) {
                throw new IllegalArgumentException("El producto " + d.getProducto().getNombre() + " ya no existe.");
            }
            factura.cargarDetalle(new DetalleFactura(actual, d.getCantidad()));
        }
        synchronized (ServicioInventario.CANDADO) {
            facturaDAO.emitir(factura);
        }
        return factura;
    }

    public String anularFactura(int numero) throws SQLException {
        boolean anulada;
        synchronized (ServicioInventario.CANDADO) {
            anulada = facturaDAO.anular(numero);
        }
        if (!anulada) {
            throw new IllegalStateException("La factura ya estaba anulada.");
        }
        return "Factura anulada; las unidades volvieron al inventario.";
    }

    public List<Factura> listarFacturas(FiltroFacturas filtro) throws SQLException {
        if (filtro.getDesde() == null || filtro.getHasta() == null) {
            throw new IllegalArgumentException("Debe indicar la fecha inicial y la final.");
        }
        if (filtro.getDesde().isAfter(filtro.getHasta())) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        List<Factura> facturas = facturaDAO.listarPorFechas(filtro.getDesde(), filtro.getHasta(), filtro.getEstado());
        String texto = filtro.getTextoCliente();
        if (texto == null || texto.isBlank()) {
            return facturas;
        }
        String buscado = texto.trim().toLowerCase();
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas) {
            if (f.getCliente().getNombre().toLowerCase().contains(buscado)
                    || f.getCliente().getIdentificacion().contains(buscado)) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public static double totalEmitidas(List<Factura> facturas) {
        double total = 0;
        for (Factura f : facturas) {
            if (f.getEstado() == EstadoFactura.EMITIDA) {
                total += f.calcularTotal();
            }
        }
        return total;
    }
}
