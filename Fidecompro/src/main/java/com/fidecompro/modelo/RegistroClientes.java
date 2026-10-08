package com.fidecompro.modelo;

import com.fidecompro.excepciones.RegistroNoEncontradoException;
import java.util.ArrayList;

/**
 * Administra la cartera de clientes.
 */
public class RegistroClientes {

    private ArrayList<Cliente> clientes;

    public RegistroClientes() {
        this.clientes = new ArrayList<>();
    }

    public void agregarCliente(Cliente cliente) {
        for (Cliente c : clientes) {
            if (c.getIdentificacion().equals(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("Ya existe un cliente con la identificacion "
                        + cliente.getIdentificacion());
            }
        }
        clientes.add(cliente);
    }

    public void editarCliente(int id, Cliente clienteEditar) throws RegistroNoEncontradoException {
        Cliente actual = buscarClientePorId(id);
        clientes.set(clientes.indexOf(actual), clienteEditar);
    }

    public void desactivarCliente(int id) throws RegistroNoEncontradoException {
        buscarClientePorId(id).setActivo(false);
    }

    public Cliente buscarClientePorId(int id) throws RegistroNoEncontradoException {
        for (Cliente c : clientes) {
            if (c.getId() == id) {
                return c;
            }
        }
        throw new RegistroNoEncontradoException("No existe el cliente con id " + id);
    }

    public Cliente buscarCliente(String identificacion) throws RegistroNoEncontradoException {
        for (Cliente c : clientes) {
            if (c.getIdentificacion().equals(identificacion) && c.isActivo()) {
                return c;
            }
        }
        throw new RegistroNoEncontradoException("No existe un cliente activo con identificacion " + identificacion);
    }

    public String mostrarClientes() {
        String texto = "";
        for (Cliente c : clientes) {
            texto += c.mostrarInformacion() + "\n";
        }
        return texto;
    }

    public ArrayList<Cliente> getClientes() {
        return clientes;
    }
}
