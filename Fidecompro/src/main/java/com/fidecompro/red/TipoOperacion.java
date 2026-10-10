package com.fidecompro.red;

/**
 * Operaciones que un cliente puede pedirle al servidor.
 */
public enum TipoOperacion {
    LOGIN, LOGOUT, PING, RESUMEN_DIA,
    CREAR_CLIENTE, LISTAR_CLIENTES, ACTUALIZAR_CLIENTE, ELIMINAR_CLIENTE,
    CREAR_PRODUCTO, LISTAR_PRODUCTOS, ACTUALIZAR_PRODUCTO, ELIMINAR_PRODUCTO,
    CREAR_CATEGORIA, LISTAR_CATEGORIAS, ACTUALIZAR_CATEGORIA, ELIMINAR_CATEGORIA,
    AJUSTAR_STOCK,
    CREAR_FACTURA, LISTAR_FACTURAS, ANULAR_FACTURA,
    CREAR_USUARIO, LISTAR_USUARIOS, ACTUALIZAR_USUARIO, ELIMINAR_USUARIO;

    // Operaciones que solo puede hacer un ADMINISTRADOR (el servidor lo vuelve a revisar)
    public boolean esSoloAdministrador() {
        switch (this) {
            case ELIMINAR_CLIENTE:
            case CREAR_PRODUCTO:
            case ACTUALIZAR_PRODUCTO:
            case ELIMINAR_PRODUCTO:
            case CREAR_CATEGORIA:
            case ACTUALIZAR_CATEGORIA:
            case ELIMINAR_CATEGORIA:
            case AJUSTAR_STOCK:
            case ANULAR_FACTURA:
            case CREAR_USUARIO:
            case LISTAR_USUARIOS:
            case ACTUALIZAR_USUARIO:
            case ELIMINAR_USUARIO:
                return true;
            default:
                return false;
        }
    }
}
