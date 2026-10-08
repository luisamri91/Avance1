package com.fidecompro.excepciones;

public class CredencialesInvalidasException extends Exception {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
