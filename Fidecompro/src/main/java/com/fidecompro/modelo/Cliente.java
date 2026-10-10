package com.fidecompro.modelo;

import java.time.LocalDate;

/**
 * Comercio o persona a quien Fidecompro le vende.
 */
public class Cliente extends Persona {

    private static int idAutoIncremental = 1;
    private int id;
    private String direccion;
    private LocalDate fechaRegistro;
    private boolean activo;

    public Cliente(String nombre, String telefono, String correo,
            TipoIdentificacion tipoIdentificacion, String identificacion, String direccion) {
        super(nombre, telefono, correo, tipoIdentificacion, identificacion);
        this.id = idAutoIncremental++;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    // La base de datos genera el id; el DAO lo coloca aquí
    public void setId(int id) {
        this.id = id;
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

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String mostrarInformacion() {
        return "Cliente #" + id + " - "
                + super.mostrarInformacion() + " | " + direccion + (activo ? "" : " [INACTIVO]");
    }
}
