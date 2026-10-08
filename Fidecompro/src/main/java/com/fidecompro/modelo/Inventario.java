package com.fidecompro.modelo;

import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Catálogo de productos de una sede, organizado por categorías.
 */
public class Inventario {

    private String nombre;
    private String sede;
    private List<Categoria> categorias;

    public Inventario(String nombre, String sede) {
        this.nombre = nombre;
        this.sede = sede;
        this.categorias = new ArrayList<>();
    }

    public void agregarCategoria(Categoria categoria) {
        categorias.add(categoria);
    }

    public void editarCategoria(int idCategoria, Categoria categoriaEditar) throws RegistroNoEncontradoException {
        Categoria actual = buscarCategoria(idCategoria);
        actual.setNombre(categoriaEditar.getNombre());
        actual.setDescripcion(categoriaEditar.getDescripcion());
    }

    public void eliminarCategoria(int idCategoria) throws RegistroNoEncontradoException {
        Categoria categoria = buscarCategoria(idCategoria);
        if (!categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoria "
                    + categoria.getNombre() + " porque tiene productos");
        }
        categorias.remove(categoria);
    }

    public Categoria buscarCategoria(int idCategoria) throws RegistroNoEncontradoException {
        for (Categoria c : categorias) {
            if (c.getId() == idCategoria) {
                return c;
            }
        }
        throw new RegistroNoEncontradoException("No existe la categoria con id " + idCategoria);
    }

    public void agregarProductoCategoria(int idCategoria, Producto producto) throws RegistroNoEncontradoException {
        buscarCategoria(idCategoria).agregarProducto(producto);
    }

    public void editarProductoCategoria(int idCategoria, int idProducto, Producto producto) throws RegistroNoEncontradoException {
        buscarCategoria(idCategoria).editarProducto(idProducto, producto);
    }

    public void eliminarProductoCategoria(int idCategoria, int idProducto) throws RegistroNoEncontradoException {
        buscarCategoria(idCategoria).eliminarProducto(idProducto);
    }

    public String mostrarProductosCategoria(int idCategoria) throws RegistroNoEncontradoException {
        return buscarCategoria(idCategoria).mostrarProductos();
    }

    public Producto buscarProducto(String codigo) throws RegistroNoEncontradoException {
        for (Categoria c : categorias) {
            for (Producto p : c.getProductos()) {
                if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    return p;
                }
            }
        }
        throw new RegistroNoEncontradoException("No existe el producto con codigo " + codigo);
    }

    // Todo el catálogo en su orden natural (Comparable: por nombre)
    public List<Producto> listarProductos() {
        List<Producto> todos = new ArrayList<>();
        for (Categoria c : categorias) {
            todos.addAll(c.getProductos());
        }
        Collections.sort(todos);
        return todos;
    }

    // Productos por reabastecer, con orden alterno (Comparator: por existencias)
    public List<Producto> productosBajoMinimo() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : listarProductos()) {
            if (p.necesitaReabastecer()) {
                resultado.add(p);
            }
        }
        Collections.sort(resultado, new ComparadorPorExistencias());
        return resultado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSede() {
        return sede;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }
}
