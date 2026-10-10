package com.fidecompro.servidor;

import com.fidecompro.datos.UsuarioDAO;
import com.fidecompro.excepciones.CredencialesInvalidasException;
import com.fidecompro.modelo.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * Inicio de sesión y administración de usuarios (HU 1 y HU 2).
 */
public class ServicioAutenticacion {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String nombreUsuario, String contrasena) throws SQLException, CredencialesInvalidasException {
        if (vacio(nombreUsuario) || vacio(contrasena)) {
            throw new CredencialesInvalidasException("Debe ingresar usuario y contraseña.");
        }
        Usuario u = usuarioDAO.buscarPorNombreUsuario(nombreUsuario.trim());
        // Se valida con la contraseña cifrada; el mensaje no dice qué dato falló
        if (u == null || !u.validarCredenciales(nombreUsuario.trim(), cifrarContrasena(contrasena))) {
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }
        u.setContrasena(null);
        return u;
    }

    public String registrarUsuario(Usuario u) throws SQLException {
        validar(u, true);
        u.setContrasena(cifrarContrasena(u.getContrasena()));
        usuarioDAO.insertar(u);
        return "Usuario creado correctamente.";
    }

    public String actualizarUsuario(Usuario u) throws SQLException {
        validar(u, false);
        if (!vacio(u.getContrasena())) {
            u.setContrasena(cifrarContrasena(u.getContrasena()));
        }
        usuarioDAO.actualizar(u);
        return "Usuario actualizado correctamente.";
    }

    public String eliminarUsuario(int id, Usuario quienElimina) throws SQLException {
        if (id == quienElimina.getId()) {
            throw new IllegalArgumentException("No puede eliminar su propio usuario.");
        }
        if (usuarioDAO.tieneFacturas(id)) {
            usuarioDAO.desactivar(id);
            return "El usuario tiene facturas registradas; se desactivará para conservar el historial.";
        }
        usuarioDAO.eliminar(id);
        return "Usuario eliminado correctamente.";
    }

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listar();
    }

    private void validar(Usuario u, boolean nuevo) throws SQLException {
        if (vacio(u.getNombre()) || vacio(u.getIdentificacion()) || vacio(u.getNombreUsuario()) || u.getRol() == null) {
            throw new IllegalArgumentException("Debe completar los campos obligatorios.");
        }
        // Al editar, la contraseña vacía significa "no cambiarla"
        if ((nuevo || !vacio(u.getContrasena())) && (u.getContrasena() == null || u.getContrasena().length() < 8)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres y coincidir con la confirmación.");
        }
        int idActual = nuevo ? 0 : u.getId();
        if (usuarioDAO.existeNombreUsuario(u.getNombreUsuario(), idActual)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }
        if (usuarioDAO.existeIdentificacion(u.getIdentificacion(), idActual)) {
            throw new IllegalArgumentException("Ya existe un usuario con esa identificación.");
        }
    }

    // SHA-256: en la base nunca se guarda la contraseña tal cual
    public static String cifrarContrasena(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
