package com.fidecompro.modelo;

import java.time.LocalDate;

/**
 * Comercio o persona a quien Fidecompro le vende.
 */
public class Cliente extends Persona {

    private static int idAutoIncremental = 1;
    private int id;
    private TipoIdentificacion tipoIdentificacion;
    private String identificacion;
    private String direccion;
    private LocalDate fechaRegistro;
    private boolean activo;

    public Cliente(String nombre, String telefono, String correo,
            TipoIdentificacion tipoIdentificacion, String identificacion, String direccion) {
        super(nombre, telefono, correo);
        this.id = idAutoIncremental++;
        this.tipoIdentificacion = tipoIdentificacion;
        this.identificacion = identificacion;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    public int getId() {
        return id;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String mostrarInformacion() {
        return "Cliente #" + id + " " + tipoIdentificacion + " " + identificacion + " - "
                + super.mostrarInformacion() + " | " + direccion + (activo ? "" : " [INACTIVO]");
    }
}
