package com.fidecompro.datos;

import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.TipoIdentificacion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla CLIENTES.
 */
public class ClienteDAO {

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (tipo_identificacion, identificacion, nombre, telefono, correo,"
                + " direccion, fecha_registro, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getTipoIdentificacion().name());
            ps.setString(2, c.getIdentificacion());
            ps.setString(3, c.getNombre());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getDireccion());
            ps.setDate(7, Date.valueOf(c.getFechaRegistro()));
            ps.setBoolean(8, c.isActivo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    c.setId(llaves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET tipo_identificacion = ?, identificacion = ?, nombre = ?, telefono = ?,"
                + " correo = ?, direccion = ?, activo = ? WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getTipoIdentificacion().name());
            ps.setString(2, c.getIdentificacion());
            ps.setString(3, c.getNombre());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setString(6, c.getDireccion());
            ps.setBoolean(7, c.isActivo());
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("UPDATE clientes SET activo = ? WHERE id = ?")) {
            ps.setBoolean(1, false);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneFacturas(int id) throws SQLException {
        return Consultas.existe("SELECT 1 FROM facturas WHERE id_cliente = ?", id);
    }

    public boolean existeIdentificacion(String identificacion, int idExcluir) throws SQLException {
        return Consultas.existe("SELECT 1 FROM clientes WHERE identificacion = ? AND id <> ?", identificacion, idExcluir);
    }

    public Cliente buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM clientes ORDER BY nombre")) {
            while (rs.next()) {
                lista.add(leer(rs));
            }
        }
        return lista;
    }

    private Cliente leer(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(rs.getString("nombre"), rs.getString("telefono"), rs.getString("correo"),
                TipoIdentificacion.valueOf(rs.getString("tipo_identificacion")), rs.getString("identificacion"),
                rs.getString("direccion"));
        c.setId(rs.getInt("id"));
        c.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
