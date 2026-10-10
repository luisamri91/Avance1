package com.fidecompro.cliente;

import com.fidecompro.red.Respuesta;
import com.fidecompro.red.Solicitud;
import com.fidecompro.red.TipoOperacion;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Conexión de una caja con el servidor: envía una Solicitud y espera la Respuesta.
 */
public class ClienteSocket {

    private final String host;
    private final int puerto;
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    public ClienteSocket(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public void conectar() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, puerto), 3000);
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
    }

    // synchronized: si dos ventanas de la misma caja piden algo a la vez, las respuestas no se mezclan
    public synchronized Respuesta enviar(TipoOperacion operacion, Object datos) throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("No hay conexión con el servidor.");
        }
        try {
            salida.reset();
            salida.writeObject(new Solicitud(operacion, datos));
            salida.flush();
            return (Respuesta) entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Respuesta desconocida del servidor", e);
        }
    }

    public void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }
}
