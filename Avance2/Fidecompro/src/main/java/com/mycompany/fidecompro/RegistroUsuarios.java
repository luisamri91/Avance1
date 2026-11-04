package com.mycompany.fidecompro;

import java.util.HashMap;
import java.util.Map;

public class RegistroUsuarios {

    // la llave es el nombre de usuario
    private Map<String, Usuario> usuarios;

    public RegistroUsuarios() {
        usuarios = new HashMap<>();
    }

    public void agregar(Usuario usuario) {
        usuarios.put(usuario.getNombreUsuario(), usuario);
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasena) throws CredencialesInvalidasException {
        Usuario u = usuarios.get(nombreUsuario);
        if (u == null || !u.validarCredenciales(nombreUsuario, contrasena)) {
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos.");
        }
        return u;
    }
}
