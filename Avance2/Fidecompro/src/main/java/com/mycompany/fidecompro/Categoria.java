package com.mycompany.fidecompro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Categoria implements Serializable {

    private String nombre;
    private List<Producto> productos;

    public Categoria(String nombre) {
        this.nombre = nombre;
        this.productos = new ArrayList<>();
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public void eliminarProducto(String codigo) {
        productos.removeIf(p -> p.getCodigo().equalsIgnoreCase(codigo));
    }

    public String getNombre() {
        return nombre;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    // para que el combo muestre el nombre
    @Override
    public String toString() {
        return nombre;
    }
}
