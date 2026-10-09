package com.fidecompro.modelo;

/**
 * Datos comunes de los usuarios del sistema y de los clientes.
 */
public abstract class Persona implements Mostrable {

    protected String nombre;
    protected String telefono;
    protected String correo;
    protected TipoIdentificacion tipoIdentificacion;
    protected String identificacion;

    public Persona(String nombre, String telefono, String correo,
            TipoIdentificacion tipoIdentificacion, String identificacion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.tipoIdentificacion = tipoIdentificacion;
        this.identificacion = identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public TipoIdentificacion getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    @Override
    public String mostrarInformacion() {
        return nombre + " | " + tipoIdentificacion + " " + identificacion + " | Tel: " + telefono + " | " + correo;
    }
}
