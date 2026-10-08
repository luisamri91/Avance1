package com.fidecompro.modelo;

import com.fidecompro.excepciones.CredencialesInvalidasException;
import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.HashMap;
import java.util.Map;

/**
 * Administra los usuarios del sistema y el inicio de sesión.
 */
public class RegistroUsuarios {

    // Map: cada usuario se guarda con su id como clave, sin ids repetidos.
    private Map<Integer, Usuario> usuarios;

    public RegistroUsuarios() {
        this.usuarios = new HashMap<>();
    }

    public void agregarUsuario(Usuario usuario) {
        for (Usuario u : usuarios.values()) {
            if (u.getNombreUsuario().equalsIgnoreCase(usuario.getNombreUsuario())) {
                throw new IllegalArgumentException("Ya existe el usuario " + usuario.getNombreUsuario());
            }
        }
        usuarios.put(usuario.getId(), usuario);
    }

    public void editarUsuario(int id, Usuario usuarioEditar) throws RegistroNoEncontradoException {
        buscarUsuario(id);
        usuarios.put(id, usuarioEditar);
    }

    public void eliminarUsuario(int id) throws RegistroNoEncontradoException {
        buscarUsuario(id);
        usuarios.remove(id);
    }

    public void desactivarUsuario(int id) throws RegistroNoEncontradoException {
        buscarUsuario(id).setActivo(false);
    }

    public Usuario buscarUsuario(int id) throws RegistroNoEncontradoException {
        Usuario usuario = usuarios.get(id);
        if (usuario == null) {
            throw new RegistroNoEncontradoException("No existe el usuario con id " + id);
        }
        return usuario;
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasena) throws CredencialesInvalidasException {
        for (Usuario u : usuarios.values()) {
            if (u.validarCredenciales(nombreUsuario, contrasena)) {
                return u;
            }
        }
        // No se indica si falló el usuario o la contraseña
        throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
    }

    public Map<Integer, Usuario> getUsuarios() {
        return usuarios;
    }
}
