package com.fidecompro.datos;

import com.fidecompro.modelo.Rol;
import com.fidecompro.modelo.TipoIdentificacion;
import com.fidecompro.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla USUARIOS.
 */
public class UsuarioDAO {

    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (tipo_identificacion, identificacion, nombre, telefono, correo,"
                + " nombre_usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getTipoIdentificacion().name());
            ps.setString(2, u.getIdentificacion());
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getTelefono());
            ps.setString(5, u.getCorreo());
            ps.setString(6, u.getNombreUsuario());
            ps.setString(7, u.getContrasena());
            ps.setString(8, u.getRol().name());
            ps.setBoolean(9, u.isActivo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    u.setId(llaves.getInt(1));
                }
            }
        }
    }

    // Si la contraseña viene vacía se conserva la anterior
    public void actualizar(Usuario u) throws SQLException {
        boolean cambiaClave = u.getContrasena() != null && !u.getContrasena().isEmpty();
        String sql = "UPDATE usuarios SET tipo_identificacion = ?, identificacion = ?, nombre = ?, telefono = ?,"
                + " correo = ?, nombre_usuario = ?, rol = ?, activo = ?"
                + (cambiaClave ? ", contrasena = ?" : "") + " WHERE id = ?";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getTipoIdentificacion().name());
            ps.setString(2, u.getIdentificacion());
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getTelefono());
            ps.setString(5, u.getCorreo());
            ps.setString(6, u.getNombreUsuario());
            ps.setString(7, u.getRol().name());
            ps.setBoolean(8, u.isActivo());
            int i = 9;
            if (cambiaClave) {
                ps.setString(i++, u.getContrasena());
            }
            ps.setInt(i, u.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("UPDATE usuarios SET activo = ? WHERE id = ?")) {
            ps.setBoolean(1, false);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneFacturas(int id) throws SQLException {
        return Consultas.existe("SELECT 1 FROM facturas WHERE id_usuario = ?", id);
    }

    // Trae también la contraseña cifrada, porque se usa para validar el inicio de sesión
    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM usuarios WHERE nombre_usuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs, true) : null;
            }
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs, false) : null;
            }
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario, int idExcluir) throws SQLException {
        return Consultas.existe("SELECT 1 FROM usuarios WHERE LOWER(nombre_usuario) = LOWER(?) AND id <> ?",
                nombreUsuario, idExcluir);
    }

    public boolean existeIdentificacion(String identificacion, int idExcluir) throws SQLException {
        return Consultas.existe("SELECT 1 FROM usuarios WHERE identificacion = ? AND id <> ?",
                identificacion, idExcluir);
    }

    // La lista que viaja a los clientes no lleva contraseñas
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM usuarios ORDER BY nombre")) {
            while (rs.next()) {
                lista.add(leer(rs, false));
            }
        }
        return lista;
    }

    private Usuario leer(ResultSet rs, boolean conContrasena) throws SQLException {
        Usuario u = new Usuario(rs.getString("nombre"), rs.getString("telefono"), rs.getString("correo"),
                TipoIdentificacion.valueOf(rs.getString("tipo_identificacion")), rs.getString("identificacion"),
                rs.getString("nombre_usuario"), conContrasena ? rs.getString("contrasena") : null,
                Rol.valueOf(rs.getString("rol")));
        u.setId(rs.getInt("id"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
