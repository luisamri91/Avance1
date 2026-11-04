package com.mycompany.fidecompro;

public class Usuario extends Persona {

    private String nombreUsuario;
    private String contrasena;
    private Rol rol;
    private boolean activo;

    public Usuario(String nombre, String telefono, String correo, String identificacion,
            String nombreUsuario, String contrasena, Rol rol) {
        super(nombre, telefono, correo, TipoIdentificacion.FISICA, identificacion);
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = true;
    }

    public boolean validarCredenciales(String usuario, String clave) {
        return activo && nombreUsuario.equals(usuario) && contrasena.equals(clave);
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    @Override
    public String mostrarInformacion() {
        return "Usuario: " + nombreUsuario + " - " + nombre + " (" + rol + ")";
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
