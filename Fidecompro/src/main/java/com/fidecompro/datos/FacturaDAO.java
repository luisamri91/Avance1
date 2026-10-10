package com.fidecompro.datos;

import com.fidecompro.excepciones.StockInsuficienteException;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.DetalleFactura;
import com.fidecompro.modelo.EstadoFactura;
import com.fidecompro.modelo.Factura;
import com.fidecompro.modelo.MetodoPago;
import com.fidecompro.modelo.Producto;
import com.fidecompro.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a las tablas FACTURAS y DETALLE_FACTURA.
 * Emitir y anular se hacen en una transacción: o se guarda todo o no se guarda nada.
 */
public class FacturaDAO {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Guarda la factura, sus líneas y descuenta las existencias.
     * Quien la llama debe tener el candado del inventario (ver ServicioFacturacion).
     */
    public void emitir(Factura f) throws SQLException, StockInsuficienteException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                // 1. Revisar existencias con los datos actuales de la base
                for (DetalleFactura d : f.getDetalles()) {
                    Producto actual = productoDAO.buscarPorId(con, d.getProducto().getId());
                    if (actual == null || !actual.isActivo()) {
                        throw new StockInsuficienteException("El producto " + d.getProducto().getNombre()
                                + " ya no está disponible.");
                    }
                    if (d.getCantidad() > actual.getExistencias()) {
                        throw new StockInsuficienteException("Stock insuficiente de " + actual.getNombre()
                                + ": hay " + actual.getExistencias() + " unidades.");
                    }
                }
                // 2. Número consecutivo: se calcula aquí, así una venta rechazada no deja huecos
                f.setNumero(siguienteNumero(con));
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO facturas (numero, fecha, id_cliente,"
                        + " id_usuario, porcentaje_descuento, metodo_pago, estado, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, f.getNumero());
                    ps.setTimestamp(2, Timestamp.valueOf(f.getFecha()));
                    ps.setInt(3, f.getCliente().getId());
                    ps.setInt(4, f.getVendedor().getId());
                    ps.setDouble(5, f.getPorcentajeDescuento());
                    ps.setString(6, f.getMetodoPago().name());
                    ps.setString(7, f.getEstado().name());
                    ps.setDouble(8, Math.round(f.calcularTotal() * 100) / 100.0);
                    ps.executeUpdate();
                }
                // 3. Líneas y descuento de existencias
                int linea = 1;
                for (DetalleFactura d : f.getDetalles()) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO detalle_factura (numero_factura, linea,"
                            + " id_producto, cantidad, precio_unitario, porcentaje_iva) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, f.getNumero());
                        ps.setInt(2, linea++);
                        ps.setInt(3, d.getProducto().getId());
                        ps.setInt(4, d.getCantidad());
                        ps.setDouble(5, d.getPrecioUnitario());
                        ps.setDouble(6, d.getPorcentajeIva());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE productos SET existencias = existencias - ? WHERE id = ?")) {
                        ps.setInt(1, d.getCantidad());
                        ps.setInt(2, d.getProducto().getId());
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException | StockInsuficienteException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int siguienteNumero(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT MAX(numero) FROM facturas")) {
            return rs.next() ? rs.getInt(1) + 1 : 1;
        }
    }

    /**
     * Cambia el estado a ANULADA y devuelve las unidades al inventario. Devuelve false si ya estaba anulada.
     */
    public boolean anular(int numero) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                String estado = null;
                try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM facturas WHERE numero = ?")) {
                    ps.setInt(1, numero);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            estado = rs.getString(1);
                        }
                    }
                }
                if (estado == null) {
                    throw new SQLException("No existe la factura número " + numero);
                }
                if (EstadoFactura.ANULADA.name().equals(estado)) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE facturas SET estado = ? WHERE numero = ?")) {
                    ps.setString(1, EstadoFactura.ANULADA.name());
                    ps.setInt(2, numero);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE productos SET existencias = existencias"
                        + " + (SELECT SUM(d.cantidad) FROM detalle_factura d WHERE d.numero_factura = ? AND d.id_producto = productos.id)"
                        + " WHERE id IN (SELECT id_producto FROM detalle_factura WHERE numero_factura = ?)")) {
                    ps.setInt(1, numero);
                    ps.setInt(2, numero);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Facturas entre dos fechas (incluidas), con sus líneas. Estado null = todas.
     */
    public List<Factura> listarPorFechas(LocalDate desde, LocalDate hasta, EstadoFactura estado) throws SQLException {
        List<Factura> lista = new ArrayList<>();
        // Map por id: un mismo cliente, vendedor o producto se busca una sola vez
        Map<Integer, Cliente> clientes = new HashMap<>();
        Map<Integer, Usuario> usuarios = new HashMap<>();
        Map<Integer, Producto> productos = new HashMap<>();
        String sql = "SELECT * FROM facturas WHERE fecha >= ? AND fecha < ?"
                + (estado != null ? " AND estado = ?" : "") + " ORDER BY numero DESC";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            if (estado != null) {
                ps.setString(3, estado.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idCliente = rs.getInt("id_cliente");
                    int idUsuario = rs.getInt("id_usuario");
                    if (!clientes.containsKey(idCliente)) {
                        clientes.put(idCliente, clienteDAO.buscarPorId(idCliente));
                    }
                    if (!usuarios.containsKey(idUsuario)) {
                        usuarios.put(idUsuario, usuarioDAO.buscarPorId(idUsuario));
                    }
                    Factura f = new Factura(clientes.get(idCliente), usuarios.get(idUsuario),
                            MetodoPago.valueOf(rs.getString("metodo_pago")));
                    f.setNumero(rs.getInt("numero"));
                    f.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    f.aplicarDescuento(rs.getDouble("porcentaje_descuento"));
                    f.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
                    lista.add(f);
                }
            }
            for (Factura f : lista) {
                cargarDetalles(con, f, productos);
            }
        }
        return lista;
    }

    private void cargarDetalles(Connection con, Factura f, Map<Integer, Producto> productos) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM detalle_factura WHERE numero_factura = ? ORDER BY linea")) {
            ps.setInt(1, f.getNumero());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    if (!productos.containsKey(idProducto)) {
                        productos.put(idProducto, productoDAO.buscarPorId(con, idProducto));
                    }
                    // Precio e IVA del momento de la venta (HU 10)
                    f.cargarDetalle(new DetalleFactura(productos.get(idProducto), rs.getInt("cantidad"),
                            rs.getDouble("precio_unitario"), rs.getDouble("porcentaje_iva")));
                }
            }
        }
    }

    public int contarEmitidasDelDia(LocalDate dia) throws SQLException {
        return Consultas.contar("SELECT COUNT(*) FROM facturas WHERE estado = 'EMITIDA' AND fecha >= ? AND fecha < ?",
                Timestamp.valueOf(dia.atStartOfDay()), Timestamp.valueOf(dia.plusDays(1).atStartOfDay()));
    }

    public double totalEmitidasDelDia(LocalDate dia) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT SUM(total) FROM facturas WHERE estado = 'EMITIDA' AND fecha >= ? AND fecha < ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(dia.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(dia.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }
}
