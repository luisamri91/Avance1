package com.fidecompro.datos;

import com.fidecompro.modelo.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla CATEGORIAS.
 */
public class CategoriaDAO {

    public void insertar(Categoria c) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    c.setId(llaves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("UPDATE categorias SET nombre = ?, descripcion = ? WHERE id = ?")) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean tieneProductos(int id) throws SQLException {
        return Consultas.existe("SELECT 1 FROM productos WHERE id_categoria = ?", id);
    }

    public boolean existeNombre(String nombre, int idExcluir) throws SQLException {
        return Consultas.existe("SELECT 1 FROM categorias WHERE LOWER(nombre) = LOWER(?) AND id <> ?", nombre, idExcluir);
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM categorias ORDER BY nombre")) {
            while (rs.next()) {
                Categoria c = new Categoria(rs.getString("nombre"), rs.getString("descripcion"));
                c.setId(rs.getInt("id"));
                lista.add(c);
            }
        }
        return lista;
    }
}
