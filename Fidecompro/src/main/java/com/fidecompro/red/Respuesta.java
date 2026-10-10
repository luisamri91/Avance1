package com.fidecompro.red;

import java.io.Serializable;

/**
 * Lo que el servidor contesta: si la operación salió bien, un mensaje para el usuario y los datos.
 */
public class Respuesta implements Serializable {

    private boolean exito;
    private String mensaje;
    private Object datos;

    private Respuesta(boolean exito, String mensaje, Object datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public static Respuesta ok(Object datos) {
        return new Respuesta(true, "OK", datos);
    }

    public static Respuesta ok(String mensaje, Object datos) {
        return new Respuesta(true, mensaje, datos);
    }

    public static Respuesta error(String mensaje) {
        return new Respuesta(false, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Object getDatos() {
        return datos;
    }
}
