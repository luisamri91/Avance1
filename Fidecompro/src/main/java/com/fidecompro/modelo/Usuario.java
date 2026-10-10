package com.fidecompro.modelo;

/**
 * Persona que ingresa al sistema: vendedor o administrador.
 */
public class Usuario extends Persona {

    private static int idAutoIncremental = 1;
    private int id;
    private String nombreUsuario;
    private String contrasena;
    private Rol rol;
    private boolean activo;

    public Usuario(String nombre, String telefono, String correo,
            TipoIdentificacion tipoIdentificacion, String identificacion,
            String nombreUsuario, String contrasena, Rol rol) {
        super(nombre, telefono, correo, tipoIdentificacion, identificacion);
        this.id = idAutoIncremental++;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.activo = true;
    }

    public boolean validarCredenciales(String nombreUsuario, String contrasena) {
        return activo
                && this.nombreUsuario.equals(nombreUsuario)
                && this.contrasena.equals(contrasena);
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public int getId() {
        return id;
    }

    // La base de datos genera el id; el DAO lo coloca aquí
    public void setId(int id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String mostrarInformacion() {
        return "Usuario #" + id + " " + nombreUsuario + " (" + rol + ") - "
                + super.mostrarInformacion() + (activo ? "" : " [INACTIVO]");
    }
}
