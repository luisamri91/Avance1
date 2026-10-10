package com.fidecompro.servidor;

import com.fidecompro.datos.CategoriaDAO;
import com.fidecompro.datos.ProductoDAO;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.ComparadorPorExistencias;
import com.fidecompro.modelo.Producto;
import com.fidecompro.red.AjusteStock;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Categorías, productos y existencias (HU 4, 5, 6 y 7).
 */
public class ServicioInventario {

    // Candado único del inventario: los hilos que cambian existencias (ajustes, facturas y
    // anulaciones) entran de uno en uno, así nunca se vende más de lo que hay.
    public static final Object CANDADO = new Object();

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // ---------- Categorías ----------
    public String registrarCategoria(Categoria c) throws SQLException {
        validarCategoria(c, 0);
        categoriaDAO.insertar(c);
        return "Categoría creada correctamente.";
    }

    public String actualizarCategoria(Categoria c) throws SQLException {
        validarCategoria(c, c.getId());
        categoriaDAO.actualizar(c);
        return "Categoría actualizada correctamente.";
    }

    public String eliminarCategoria(int id) throws SQLException {
        if (categoriaDAO.tieneProductos(id)) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos.");
        }
        categoriaDAO.eliminar(id);
        return "Categoría eliminada correctamente.";
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    private void validarCategoria(Categoria c, int idActual) throws SQLException {
        if (ServicioAutenticacion.vacio(c.getNombre())) {
            throw new IllegalArgumentException("Debe escribir el nombre de la categoría.");
        }
        if (categoriaDAO.existeNombre(c.getNombre().trim(), idActual)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
    }

    // ---------- Productos ----------
    public String registrarProducto(Producto p) throws SQLException {
        validarProducto(p, 0);
        p.setActivo(true);
        productoDAO.insertar(p);
        return "Producto registrado correctamente.";
    }

    public String actualizarProducto(Producto p) throws SQLException {
        validarProducto(p, p.getId());
        productoDAO.actualizar(p);
        return "Producto actualizado correctamente.";
    }

    public String eliminarProducto(int id) throws SQLException {
        if (productoDAO.apareceEnFacturas(id)) {
            productoDAO.desactivar(id);
            return "El producto aparece en facturas; se desactivó para que no se pueda vender.";
        }
        productoDAO.eliminar(id);
        return "Producto eliminado correctamente.";
    }

    // Orden natural (Comparable): por nombre
    public List<Producto> listarProductos() throws SQLException {
        List<Producto> lista = productoDAO.listar();
        Collections.sort(lista);
        return lista;
    }

    // Orden alterno (Comparator): de menos a más existencias
    public List<Producto> productosBajoMinimo() throws SQLException {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productoDAO.listar()) {
            if (p.isActivo() && p.necesitaReabastecer()) {
                resultado.add(p);
            }
        }
        Collections.sort(resultado, new ComparadorPorExistencias());
        return resultado;
    }

    public String ajustarStock(AjusteStock ajuste) throws SQLException {
        if (ServicioAutenticacion.vacio(ajuste.getMotivo())) {
            throw new IllegalArgumentException("Debe indicar el motivo del ajuste.");
        }
        if (ajuste.getCantidad() == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste no puede ser 0.");
        }
        synchronized (CANDADO) {
            Producto p = productoDAO.buscarPorId(ajuste.getIdProducto());
            if (p == null) {
                throw new IllegalArgumentException("El producto ya no existe.");
            }
            int nuevas = p.getExistencias() + ajuste.getCantidad();
            if (nuevas < 0) {
                throw new IllegalArgumentException("El ajuste dejaría las existencias en negativo.");
            }
            productoDAO.actualizarExistencias(p.getId(), nuevas);
            return "Existencias actualizadas: " + nuevas + " unidades.";
        }
    }

    private void validarProducto(Producto p, int idActual) throws SQLException {
        if (ServicioAutenticacion.vacio(p.getCodigo()) || ServicioAutenticacion.vacio(p.getNombre()) || p.getCategoria() == null) {
            throw new IllegalArgumentException("Debe completar código, nombre y categoría.");
        }
        if (productoDAO.existeCodigo(p.getCodigo().trim(), idActual)) {
            throw new IllegalArgumentException("Ya existe un producto con ese código.");
        }
        if (p.getValorVenta() <= 0 || p.getValorVenta() < p.getValorCompra()) {
            throw new IllegalArgumentException("El valor de venta debe ser mayor que 0 y no menor que el valor de compra.");
        }
        if (p.getExistencias() < 0 || p.getStockMinimo() < 0) {
            throw new IllegalArgumentException("Las existencias y el stock mínimo no pueden ser negativos.");
        }
    }
}
