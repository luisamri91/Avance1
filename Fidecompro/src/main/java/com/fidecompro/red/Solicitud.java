package com.fidecompro.red;

import java.io.Serializable;

/**
 * Lo que el cliente envía por el socket: qué operación quiere y con qué datos.
 */
public class Solicitud implements Serializable {

    private TipoOperacion operacion;
    private Object datos;

    public Solicitud(TipoOperacion operacion, Object datos) {
        this.operacion = operacion;
        this.datos = datos;
    }

    public TipoOperacion getOperacion() {
        return operacion;
    }

    public Object getDatos() {
        return datos;
    }
}
