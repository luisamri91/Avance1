package com.fidecompro.modelo;

import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa productos para ordenar el catálogo (Abarrotes, Bebidas, Limpieza...).
 */
public class Categoria {

    private static int idAutoIncremental = 1;
    private int id;
    private String nombre;
    private String descripcion;
    // List: los productos se muestran en orden y se ordenan con Comparable/Comparator
    private List<Producto> productos;

    public Categoria(String nombre, String descripcion) {
        this.id = idAutoIncremental++;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.productos = new ArrayList<>();
    }

    public void agregarProducto(Producto producto) {
        producto.setCategoria(this);
        productos.add(producto);
    }

    public void editarProducto(int idProducto, Producto productoEditar) throws RegistroNoEncontradoException {
        Producto actual = buscarProducto(idProducto);
        productoEditar.setCategoria(this);
        productos.set(productos.indexOf(actual), productoEditar);
    }

    public void eliminarProducto(int idProducto) throws RegistroNoEncontradoException {
        productos.remove(buscarProducto(idProducto));
    }

    public Producto buscarProducto(int idProducto) throws RegistroNoEncontradoException {
        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                return p;
            }
        }
        throw new RegistroNoEncontradoException("No existe el producto con id " + idProducto
                + " en la categoria " + nombre);
    }

    public String mostrarProductos() {
        String texto = "== " + nombre + " ==\n";
        for (Producto p : productos) {
            texto += "  " + p.mostrarInformacion() + "\n";
        }
        return texto;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProductos() {
        return productos;
    }
}
