package com.fidecompro.servidor;

import com.fidecompro.datos.ClienteDAO;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.TipoIdentificacion;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro, búsqueda, edición y eliminación de clientes (HU 3).
 */
public class ServicioClientes {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public String registrar(Cliente c) throws SQLException {
        validar(c, 0);
        c.setFechaRegistro(LocalDate.now());
        c.setActivo(true);
        clienteDAO.insertar(c);
        return "Cliente registrado correctamente.";
    }

    public String actualizar(Cliente c) throws SQLException {
        validar(c, c.getId());
        clienteDAO.actualizar(c);
        return "Cliente actualizado correctamente.";
    }

    // Si tiene facturas no se borra: se desactiva para conservar el historial
    public String eliminar(int id) throws SQLException {
        if (clienteDAO.tieneFacturas(id)) {
            clienteDAO.desactivar(id);
            return "El cliente tiene facturas; se desactivó para conservar el historial.";
        }
        clienteDAO.eliminar(id);
        return "Cliente eliminado correctamente.";
    }

    // Busca por parte del nombre o de la identificación, sin distinguir mayúsculas
    public List<Cliente> buscar(String texto) throws SQLException {
        List<Cliente> todos = clienteDAO.listar();
        if (texto == null || texto.isBlank()) {
            return todos;
        }
        String buscado = texto.trim().toLowerCase();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : todos) {
            if (c.getNombre().toLowerCase().contains(buscado) || c.getIdentificacion().toLowerCase().contains(buscado)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    private void validar(Cliente c, int idActual) throws SQLException {
        if (c.getTipoIdentificacion() == null || ServicioAutenticacion.vacio(c.getIdentificacion())
                || ServicioAutenticacion.vacio(c.getNombre())) {
            throw new IllegalArgumentException("Debe completar los campos obligatorios.");
        }
        if (c.getTipoIdentificacion() == TipoIdentificacion.FISICA && !c.getIdentificacion().matches("\\d-\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException("La cédula física debe tener el formato X-XXXX-XXXX.");
        }
        if (c.getTipoIdentificacion() == TipoIdentificacion.JURIDICA && !c.getIdentificacion().matches("\\d-\\d{3}-\\d{6}")) {
            throw new IllegalArgumentException("La cédula jurídica debe tener el formato X-XXX-XXXXXX.");
        }
        if (!ServicioAutenticacion.vacio(c.getCorreo()) && !c.getCorreo().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido.");
        }
        if (clienteDAO.existeIdentificacion(c.getIdentificacion(), idActual)) {
            throw new IllegalArgumentException("Ya existe un cliente con la identificación indicada.");
        }
    }
}
