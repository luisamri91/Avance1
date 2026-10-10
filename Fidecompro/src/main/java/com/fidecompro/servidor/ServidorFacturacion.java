package com.fidecompro.servidor;

import com.fidecompro.datos.ConexionBD;
import com.fidecompro.datos.CreadorBaseDatos;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.derby.drda.NetworkServerControl;

/**
 * Servidor de Fidecompro: abre un ServerSocket, acepta las cajas que se conectan y
 * atiende a cada una en un hilo del pool (ManejadorCliente).
 */
public class ServidorFacturacion {

    public static final int PUERTO_POR_DEFECTO = 5000;
    public static final int MAX_CLIENTES = 10;
    public static final int PUERTO_DERBY_RED = 1527;

    private int puerto;
    private int maxClientes;
    private ServerSocket serverSocket;
    private ExecutorService poolHilos;
    // CopyOnWriteArrayList: el hilo que acepta conexiones, los hilos de cada cliente y la
    // ventana la recorren y la cambian al mismo tiempo sin errores de concurrencia
    private final List<ManejadorCliente> clientesConectados = new CopyOnWriteArrayList<>();
    private VentanaServidor ventana;
    private volatile boolean activo;
    private NetworkServerControl derbyRed;

    public ServidorFacturacion(int puerto, int maxClientes) {
        this.puerto = puerto;
        this.maxClientes = maxClientes;
    }

    public void setVentana(VentanaServidor ventana) {
        this.ventana = ventana;
    }

    /**
     * Prepara la base de datos, abre el puerto y empieza a aceptar clientes en otro hilo.
     */
    public void iniciar() throws SQLException, IOException {
        CreadorBaseDatos.prepararBase();
        serverSocket = new ServerSocket(puerto);
        AtomicInteger contador = new AtomicInteger();
        poolHilos = Executors.newFixedThreadPool(maxClientes,
                tarea -> new Thread(tarea, "Hilo-" + contador.incrementAndGet()));
        activo = true;
        iniciarDerbyEnRed();
        Thread aceptador = new Thread(this::aceptarConexiones, "Aceptador");
        aceptador.setDaemon(true);
        aceptador.start();
        registrarOperacion("Servidor", "INICIAR puerto " + puerto + " · " + ConexionBD.getInstancia().getMotor(), "OK");
    }

    private void aceptarConexiones() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                ManejadorCliente manejador = new ManejadorCliente(socket, this);
                clientesConectados.add(manejador);
                poolHilos.execute(manejador);
                avisarVentana();
            } catch (SocketException e) {
                // El serverSocket se cerró porque se detuvo el servidor
            } catch (IOException e) {
                registrarOperacion("Servidor", "ACEPTAR conexión", e.getMessage());
            }
        }
    }

    public void detener() {
        activo = false;
        for (ManejadorCliente m : clientesConectados) {
            m.cerrarConexion();
        }
        clientesConectados.clear();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar el puerto: " + e.getMessage());
        }
        if (poolHilos != null) {
            poolHilos.shutdownNow();
        }
        detenerDerbyEnRed();
        registrarOperacion("Servidor", "DETENER", "OK");
        avisarVentana();
    }

    // Con Derby se abre además su servidor de red, para ver las tablas desde NetBeans mientras corre
    private void iniciarDerbyEnRed() {
        if (!ConexionBD.getInstancia().esDerby()) {
            return;
        }
        try {
            derbyRed = new NetworkServerControl(InetAddress.getByName("localhost"), PUERTO_DERBY_RED);
            derbyRed.start(null);
        } catch (Exception e) {
            derbyRed = null;
            System.out.println("No se pudo abrir Derby en red: " + e.getMessage());
        }
    }

    private void detenerDerbyEnRed() {
        try {
            if (derbyRed != null) {
                derbyRed.shutdown();
            }
        } catch (Exception e) {
            System.out.println("Derby en red ya estaba detenido: " + e.getMessage());
        }
        derbyRed = null;
    }

    public void quitarCliente(ManejadorCliente manejador) {
        clientesConectados.remove(manejador);
        avisarVentana();
    }

    public void avisarVentana() {
        if (ventana != null) {
            ventana.refrescarClientes();
        }
    }

    public void registrarOperacion(String hilo, String operacion, String resultado) {
        if (ventana != null) {
            ventana.agregarBitacora(hilo, operacion, resultado);
        } else {
            System.out.println(hilo + " | " + operacion + " | " + resultado);
        }
    }

    public int getHilosActivos() {
        return poolHilos == null ? 0 : ((ThreadPoolExecutor) poolHilos).getActiveCount();
    }

    public List<ManejadorCliente> getClientesConectados() {
        return clientesConectados;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getPuerto() {
        return puerto;
    }

    public int getMaxClientes() {
        return maxClientes;
    }

    // Permite correr el servidor sin ventana: java ... ServidorFacturacion [puerto]
    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_POR_DEFECTO;
        new ServidorFacturacion(puerto, MAX_CLIENTES).iniciar();
        System.out.println("Servidor Fidecompro escuchando en el puerto " + puerto);
        Thread.currentThread().join();
    }
}
