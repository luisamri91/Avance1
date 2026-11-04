package com.mycompany.fidecompro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// clase que se encarga de la serializacion de los datos del sistema
// guarda y lee los usuarios, clientes, inventario y facturas en fidecompro.dat
public class ArchivoDatos {

    // el archivo se crea en la carpeta donde se ejecuta el proyecto
    private static final String NOMBRE_ARCHIVO = "fidecompro.dat";

    // se escriben los cuatro objetos en el mismo orden en que se leen
    public static void guardar() throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            salida.writeObject(Datos.usuarios);
            salida.writeObject(Datos.clientes);
            salida.writeObject(Datos.inventario);
            salida.writeObject(Datos.facturas);
        }
    }

    // devuelve false si el archivo todavia no existe
    public static boolean cargar() throws IOException, ClassNotFoundException {
        File archivo = new File(NOMBRE_ARCHIVO);
        if (!archivo.exists()) {
            return false;
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            Datos.usuarios = (RegistroUsuarios) entrada.readObject();
            Datos.clientes = (RegistroClientes) entrada.readObject();
            Datos.inventario = (Inventario) entrada.readObject();
            Datos.facturas = (RegistroFacturas) entrada.readObject();
        }
        // para que los clientes nuevos no repitan id
        for (Cliente c : Datos.clientes.listar()) {
            Cliente.actualizarContador(c.getId());
        }
        return true;
    }
}
