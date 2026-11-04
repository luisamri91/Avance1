package com.mycompany.fidecompro;

import java.time.LocalDate;

public class Cliente extends Persona {

    private static int contador = 1;

    private int id;
    private String direccion;
    private boolean activo;
    private LocalDate fechaRegistro;

    public Cliente(TipoIdentificacion tipo, String identificacion, String nombre, String telefono, String correo, String direccion) {
        super(nombre, telefono, correo, tipo, identificacion);
        this.id = contador++;
        this.direccion = direccion;
        this.activo = true;
        this.fechaRegistro = LocalDate.now();
    }

    @Override
    public String mostrarInformacion() {
        return "Cliente: " + super.mostrarInformacion() + " Dirección: " + direccion;
    }

    public int getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
}
