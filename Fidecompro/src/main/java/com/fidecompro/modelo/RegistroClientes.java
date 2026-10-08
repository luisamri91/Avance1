package com.fidecompro.modelo;

import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.HashMap;
import java.util.Map;

/**
 * Administra la cartera de clientes.
 */
public class RegistroClientes {

    // Map: cada cliente se guarda con su id como clave (igual que la llave primaria en la BD),
    // así buscar, editar y eliminar por id no necesita recorrer todos los clientes.
    private Map<Integer, Cliente> clientes;

    public RegistroClientes() {
        this.clientes = new HashMap<>();
    }

    public void agregarCliente(Cliente cliente) {
        for (Cliente c : clientes.values()) {
            if (c.getIdentificacion().equals(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("Ya existe un cliente con la identificacion "
                        + cliente.getIdentificacion());
            }
        }
        clientes.put(cliente.getId(), cliente);
    }

    public void editarCliente(int id, Cliente clienteEditar) throws RegistroNoEncontradoException {
        buscarClientePorId(id);
        clientes.put(id, clienteEditar);
    }

    public void eliminarCliente(int id) throws RegistroNoEncontradoException {
        buscarClientePorId(id);
        clientes.remove(id);
    }

    public void desactivarCliente(int id) throws RegistroNoEncontradoException {
        buscarClientePorId(id).setActivo(false);
    }

    public Cliente buscarClientePorId(int id) throws RegistroNoEncontradoException {
        Cliente cliente = clientes.get(id);
        if (cliente == null) {
            throw new RegistroNoEncontradoException("No existe el cliente con id " + id);
        }
        return cliente;
    }

    public Cliente buscarCliente(String identificacion) throws RegistroNoEncontradoException {
        for (Cliente c : clientes.values()) {
            if (c.getIdentificacion().equals(identificacion) && c.isActivo()) {
                return c;
            }
        }
        throw new RegistroNoEncontradoException("No existe un cliente activo con identificacion " + identificacion);
    }

    public String mostrarClientes() {
        String texto = "";
        for (Cliente c : clientes.values()) {
            texto += c.mostrarInformacion() + "\n";
        }
        return texto;
    }

    public Map<Integer, Cliente> getClientes() {
        return clientes;
    }
}
