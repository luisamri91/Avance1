package com.fidecompro.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Pequeñas consultas que usan varios DAO.
 */
public class Consultas {

    // true si la consulta devuelve al menos una fila
    public static boolean existe(String sql, Object... parametros) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int contar(String sql, Object... parametros) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
