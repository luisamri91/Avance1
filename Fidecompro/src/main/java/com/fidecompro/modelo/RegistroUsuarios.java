package com.fidecompro.modelo;

import com.fidecompro.excepciones.CredencialesInvalidasException;
import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.ArrayList;

/**
 * Administra los usuarios del sistema y el inicio de sesión.
 */
public class RegistroUsuarios {

    private ArrayList<Usuario> usuarios;

    public RegistroUsuarios() {
        this.usuarios = new ArrayList<>();
    }

    public void agregarUsuario(Usuario usuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario())) {
                throw new IllegalArgumentException("Ya existe el usuario " + usuario.getNombreUsuario());
            }
        }
        usuarios.add(usuario);
    }

    public void editarUsuario(int id, Usuario usuarioEditar) throws RegistroNoEncontradoException {
        Usuario actual = buscarUsuario(id);
        usuarios.set(usuarios.indexOf(actual), usuarioEditar);
    }

    public void desactivarUsuario(int id) throws RegistroNoEncontradoException {
        buscarUsuario(id).setActivo(false);
    }

    public Usuario buscarUsuario(int id) throws RegistroNoEncontradoException {
        for (Usuario u : usuarios) {
            if (u.getId() == id) {
                return u;
            }
        }
        throw new RegistroNoEncontradoException("No existe el usuario con id " + id);
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasena) throws CredencialesInvalidasException {
        for (Usuario u : usuarios) {
            if (u.validarCredenciales(nombreUsuario, contrasena)) {
                return u;
            }
        }
        // No se indica si falló el usuario o la contraseña
        throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }
}
