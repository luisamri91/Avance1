package com.fidecompro.datos;

import com.fidecompro.modelo.Abarrote;
import com.fidecompro.modelo.ArticuloHogar;
import com.fidecompro.modelo.Bebida;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.Producto;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a la tabla PRODUCTOS. Los tres tipos se guardan en la misma tabla con la columna "tipo";
 * las columnas que no le tocan a un tipo quedan en NULL.
 */
public class ProductoDAO {

    private static final String SELECT = "SELECT p.*, c.nombre AS nombre_categoria, c.descripcion AS descripcion_categoria"
            + " FROM productos p JOIN categorias c ON p.id_categoria = c.id";

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos (codigo, tipo, nombre, descripcion, valor_compra, valor_venta, existencias,"
                + " stock_minimo, id_categoria, fecha_vencimiento, peso_kg, canasta_basica, volumen_ml, unidades_paquete,"
                + " retornable, marca, presentacion, advertencias, activo)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            llenarColumnas(ps, p, 2);
            ps.setBoolean(19, p.isActivo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    p.setId(llaves.getInt(1));
                }
            }
        }
    }

    // Las existencias no se cambian aquí: solo con ajustes de stock (HU 6) o con facturas
    public void actualizar(Producto p) throws SQLException {
        String sql = "UPDATE productos SET tipo = ?, nombre = ?, descripcion = ?, valor_compra = ?, valor_venta = ?,"
                + " existencias = existencias, stock_minimo = ?, id_categoria = ?, fecha_vencimiento = ?, peso_kg = ?,"
                + " canasta_basica = ?, volumen_ml = ?, unidades_paquete = ?, retornable = ?, marca = ?, presentacion = ?,"
                + " advertencias = ?, codigo = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoBD(p));
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setDouble(4, p.getValorCompra());
            ps.setDouble(5, p.getValorVenta());
            ps.setInt(6, p.getStockMinimo());
            ps.setInt(7, p.getCategoria().getId());
            llenarColumnasTipo(ps, p, 8);
            ps.setString(17, p.getCodigo());
            ps.setBoolean(18, p.isActivo());
            ps.setInt(19, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM productos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("UPDATE productos SET activo = ? WHERE id = ?")) {
            ps.setBoolean(1, false);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean apareceEnFacturas(int id) throws SQLException {
        return Consultas.existe("SELECT 1 FROM detalle_factura WHERE id_producto = ?", id);
    }

    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        return Consultas.existe("SELECT 1 FROM productos WHERE LOWER(codigo) = LOWER(?) AND id <> ?", codigo, idExcluir);
    }

    public Producto buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            return buscarPorId(con, id);
        }
    }

    // Versión que usa una conexión abierta, para trabajar dentro de una transacción
    public Producto buscarPorId(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + " WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs, new HashMap<>()) : null;
            }
        }
    }

    public void actualizarExistencias(Connection con, int id, int existencias) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE productos SET existencias = ? WHERE id = ?")) {
            ps.setInt(1, existencias);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizarExistencias(int id, int existencias) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion()) {
            actualizarExistencias(con, id, existencias);
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        // Map: cada categoría se crea una sola vez y la comparten sus productos
        Map<Integer, Categoria> categorias = new HashMap<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(SELECT + " ORDER BY p.nombre")) {
            while (rs.next()) {
                lista.add(leer(rs, categorias));
            }
        }
        return lista;
    }

    private Producto leer(ResultSet rs, Map<Integer, Categoria> categorias) throws SQLException {
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        double compra = rs.getDouble("valor_compra");
        double venta = rs.getDouble("valor_venta");
        int existencias = rs.getInt("existencias");
        int minimo = rs.getInt("stock_minimo");
        Producto p;
        switch (rs.getString("tipo")) {
            case "ABARROTE":
                Date vence = rs.getDate("fecha_vencimiento");
                p = new Abarrote(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                        vence == null ? null : vence.toLocalDate(), rs.getDouble("peso_kg"), rs.getBoolean("canasta_basica"));
                break;
            case "BEBIDA":
                p = new Bebida(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                        rs.getInt("volumen_ml"), rs.getInt("unidades_paquete"), rs.getBoolean("retornable"));
                break;
            default:
                ArticuloHogar a = new ArticuloHogar(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                        rs.getString("marca"), rs.getString("presentacion"));
                String advertencias = rs.getString("advertencias");
                if (advertencias != null && !advertencias.isBlank()) {
                    for (String texto : advertencias.split(";")) {
                        a.agregarAdvertencia(texto.trim());
                    }
                }
                p = a;
        }
        p.setId(rs.getInt("id"));
        p.setActivo(rs.getBoolean("activo"));
        int idCategoria = rs.getInt("id_categoria");
        Categoria categoria = categorias.get(idCategoria);
        if (categoria == null) {
            categoria = new Categoria(rs.getString("nombre_categoria"), rs.getString("descripcion_categoria"));
            categoria.setId(idCategoria);
            categorias.put(idCategoria, categoria);
        }
        p.setCategoria(categoria);
        return p;
    }

    private String tipoBD(Producto p) {
        if (p instanceof Abarrote) {
            return "ABARROTE";
        } else if (p instanceof Bebida) {
            return "BEBIDA";
        }
        return "HOGAR";
    }

    // Columnas 2 a 18 del INSERT
    private void llenarColumnas(PreparedStatement ps, Producto p, int inicio) throws SQLException {
        ps.setString(inicio, tipoBD(p));
        ps.setString(inicio + 1, p.getNombre());
        ps.setString(inicio + 2, p.getDescripcion());
        ps.setDouble(inicio + 3, p.getValorCompra());
        ps.setDouble(inicio + 4, p.getValorVenta());
        ps.setInt(inicio + 5, p.getExistencias());
        ps.setInt(inicio + 6, p.getStockMinimo());
        ps.setInt(inicio + 7, p.getCategoria().getId());
        llenarColumnasTipo(ps, p, inicio + 8);
    }

    // Las 9 columnas propias de cada tipo, en el orden de la tabla
    private void llenarColumnasTipo(PreparedStatement ps, Producto p, int i) throws SQLException {
        for (int k = 0; k < 9; k++) {
            ps.setNull(i + k, Types.VARCHAR);
        }
        if (p instanceof Abarrote) {
            Abarrote a = (Abarrote) p;
            if (a.getFechaVencimiento() != null) {
                ps.setDate(i, Date.valueOf(a.getFechaVencimiento()));
            } else {
                ps.setNull(i, Types.DATE);
            }
            ps.setDouble(i + 1, a.getPesoKg());
            ps.setBoolean(i + 2, a.isCanastaBasica());
            ps.setNull(i + 3, Types.INTEGER);
            ps.setNull(i + 4, Types.INTEGER);
            ps.setNull(i + 5, Types.BOOLEAN);
        } else if (p instanceof Bebida) {
            Bebida b = (Bebida) p;
            ps.setNull(i, Types.DATE);
            ps.setNull(i + 1, Types.DECIMAL);
            ps.setNull(i + 2, Types.BOOLEAN);
            ps.setInt(i + 3, b.getVolumenMl());
            ps.setInt(i + 4, b.getUnidadesPorPaquete());
            ps.setBoolean(i + 5, b.isRetornable());
        } else {
            ArticuloHogar h = (ArticuloHogar) p;
            ps.setNull(i, Types.DATE);
            ps.setNull(i + 1, Types.DECIMAL);
            ps.setNull(i + 2, Types.BOOLEAN);
            ps.setNull(i + 3, Types.INTEGER);
            ps.setNull(i + 4, Types.INTEGER);
            ps.setNull(i + 5, Types.BOOLEAN);
            ps.setString(i + 6, h.getMarca());
            ps.setString(i + 7, h.getPresentacion());
            ps.setString(i + 8, String.join("; ", h.getAdvertencias()));
        }
    }
}
