package com.mycompany.fidecompro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroClientes implements Serializable {

    // la llave es el id del cliente
    private Map<Integer, Cliente> clientes;

    public RegistroClientes() {
        clientes = new HashMap<>();
    }

    public void agregar(Cliente cliente) {
        if (existeIdentificacion(cliente.getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un cliente con la identificación indicada.");
        }
        clientes.put(cliente.getId(), cliente);
    }

    public void eliminar(Cliente cliente) {
        clientes.remove(cliente.getId());
    }

    public boolean existeIdentificacion(String identificacion) {
        for (Cliente c : clientes.values()) {
            if (c.getIdentificacion().equals(identificacion)) {
                return true;
            }
        }
        return false;
    }

    public Cliente buscarPorIdentificacion(String identificacion) throws RegistroNoEncontradoException {
        for (Cliente c : clientes.values()) {
            if (c.getIdentificacion().equals(identificacion)) {
                return c;
            }
        }
        throw new RegistroNoEncontradoException("No existe un cliente con la identificación " + identificacion + ".");
    }

    // busca por parte del nombre o de la identificacion, sin importar mayusculas
    public List<Cliente> buscar(String texto) {
        List<Cliente> resultado = new ArrayList<>();
        String t = texto.toLowerCase();
        for (Cliente c : clientes.values()) {
            if (c.getNombre().toLowerCase().contains(t) || c.getIdentificacion().contains(t)) {
                resultado.add(c);
            }
        }
        resultado.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        return resultado;
    }

    public List<Cliente> listar() {
        return buscar("");
    }
}
