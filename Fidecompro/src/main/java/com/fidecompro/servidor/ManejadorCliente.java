package com.fidecompro.servidor;

import com.fidecompro.datos.Consultas;
import com.fidecompro.datos.FacturaDAO;
import com.fidecompro.excepciones.CredencialesInvalidasException;
import com.fidecompro.excepciones.StockInsuficienteException;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.DetalleFactura;
import com.fidecompro.modelo.Factura;
import com.fidecompro.modelo.Producto;
import com.fidecompro.modelo.Usuario;
import com.fidecompro.red.AjusteStock;
import com.fidecompro.red.FiltroFacturas;
import com.fidecompro.red.ResumenDia;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.Solicitud;
import com.fidecompro.red.TipoOperacion;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Atiende a una caja conectada, en su propio hilo: lee cada Solicitud, llama al servicio
 * que corresponde y contesta con una Respuesta. Recuerda el usuario de la sesión para validar permisos.
 */
public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final ServidorFacturacion servidor;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private volatile Usuario usuarioSesion;
    private volatile String nombreHilo = "(en espera)";
    private final LocalTime conectadoDesde = LocalTime.now();

    private final ServicioAutenticacion autenticacion = new ServicioAutenticacion();
    private final ServicioClientes clientes = new ServicioClientes();
    private final ServicioInventario inventario = new ServicioInventario();
    private final ServicioFacturacion facturacion = new ServicioFacturacion();

    public ManejadorCliente(Socket socket, ServidorFacturacion servidor) {
        this.socket = socket;
        this.servidor = servidor;
    }

    @Override
    public void run() {
        nombreHilo = Thread.currentThread().getName();
        servidor.avisarVentana();
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());
            servidor.registrarOperacion(nombreHilo, "CONECTAR " + getEquipo(), "OK");
            boolean seguir = true;
            while (seguir) {
                Solicitud solicitud = (Solicitud) entrada.readObject();
                Respuesta respuesta = procesar(solicitud);
                // reset(): si se envía otra vez un objeto ya enviado, que viaje con sus datos nuevos
                salida.reset();
                salida.writeObject(respuesta);
                salida.flush();
                seguir = solicitud.getOperacion() != TipoOperacion.LOGOUT;
            }
        } catch (EOFException | SocketException e) {
            // La caja cerró su ventana o se detuvo el servidor
        } catch (IOException | ClassNotFoundException e) {
            servidor.registrarOperacion(nombreHilo, "COMUNICACIÓN", e.getMessage());
        } finally {
            cerrarConexion();
            servidor.registrarOperacion(nombreHilo, "DESCONECTAR " + getEquipo()
                    + (usuarioSesion != null ? " (" + usuarioSesion.getNombreUsuario() + ")" : ""), "OK");
            servidor.quitarCliente(this);
        }
    }

    private Respuesta procesar(Solicitud s) {
        TipoOperacion op = s.getOperacion();
        Respuesta r;
        try {
            if (op != TipoOperacion.LOGIN && op != TipoOperacion.PING && usuarioSesion == null) {
                r = Respuesta.error("Debe iniciar sesión.");
            } else if (op.esSoloAdministrador() && !usuarioSesion.esAdministrador()) {
                r = Respuesta.error("Solo un administrador puede hacer esta operación.");
            } else {
                r = ejecutar(op, s.getDatos());
            }
        } catch (SQLException e) {
            r = Respuesta.error("Error de base de datos: " + e.getMessage());
        } catch (CredencialesInvalidasException | StockInsuficienteException
                | IllegalArgumentException | IllegalStateException e) {
            r = Respuesta.error(e.getMessage());
        } catch (RuntimeException e) {
            r = Respuesta.error("Error inesperado: " + e);
        }
        if (op != TipoOperacion.PING && op != TipoOperacion.RESUMEN_DIA) {
            servidor.registrarOperacion(nombreHilo, describir(s, r), r.isExito() ? "OK" : r.getMensaje());
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    private Respuesta ejecutar(TipoOperacion op, Object datos) throws SQLException,
            CredencialesInvalidasException, StockInsuficienteException {
        switch (op) {
            case LOGIN:
                String[] credenciales = (String[]) datos;
                usuarioSesion = autenticacion.iniciarSesion(credenciales[0], credenciales[1]);
                servidor.avisarVentana();
                return Respuesta.ok("Bienvenido(a) " + usuarioSesion.getNombre(), usuarioSesion);
            case LOGOUT:
                usuarioSesion = null;
                return Respuesta.ok("Sesión cerrada.", null);
            case PING:
                return Respuesta.ok(null);
            case RESUMEN_DIA:
                return Respuesta.ok(resumenDelDia());
            // ---------- Clientes ----------
            case CREAR_CLIENTE:
                return Respuesta.ok(clientes.registrar((Cliente) datos), null);
            case LISTAR_CLIENTES:
                return Respuesta.ok(clientes.buscar((String) datos));
            case ACTUALIZAR_CLIENTE:
                return Respuesta.ok(clientes.actualizar((Cliente) datos), null);
            case ELIMINAR_CLIENTE:
                return Respuesta.ok(clientes.eliminar(((Cliente) datos).getId()), null);
            // ---------- Productos y categorías ----------
            case CREAR_PRODUCTO:
                return Respuesta.ok(inventario.registrarProducto((Producto) datos), null);
            case LISTAR_PRODUCTOS:
                return Respuesta.ok(inventario.listarProductos());
            case ACTUALIZAR_PRODUCTO:
                return Respuesta.ok(inventario.actualizarProducto((Producto) datos), null);
            case ELIMINAR_PRODUCTO:
                return Respuesta.ok(inventario.eliminarProducto(((Producto) datos).getId()), null);
            case CREAR_CATEGORIA:
                return Respuesta.ok(inventario.registrarCategoria((Categoria) datos), null);
            case LISTAR_CATEGORIAS:
                return Respuesta.ok(inventario.listarCategorias());
            case ACTUALIZAR_CATEGORIA:
                return Respuesta.ok(inventario.actualizarCategoria((Categoria) datos), null);
            case ELIMINAR_CATEGORIA:
                return Respuesta.ok(inventario.eliminarCategoria(((Categoria) datos).getId()), null);
            case AJUSTAR_STOCK:
                return Respuesta.ok(inventario.ajustarStock((AjusteStock) datos), null);
            // ---------- Facturas ----------
            case CREAR_FACTURA:
                Factura emitida = facturacion.crearFactura((Factura) datos, usuarioSesion);
                return Respuesta.ok("Factura " + emitida.getNumeroFormateado() + " emitida.", emitida);
            case LISTAR_FACTURAS:
                return Respuesta.ok(facturacion.listarFacturas((FiltroFacturas) datos));
            case ANULAR_FACTURA:
                return Respuesta.ok(facturacion.anularFactura((Integer) datos), null);
            // ---------- Usuarios ----------
            case CREAR_USUARIO:
                return Respuesta.ok(autenticacion.registrarUsuario((Usuario) datos), null);
            case LISTAR_USUARIOS:
                return Respuesta.ok(autenticacion.listar());
            case ACTUALIZAR_USUARIO:
                return Respuesta.ok(autenticacion.actualizarUsuario((Usuario) datos), null);
            case ELIMINAR_USUARIO:
                return Respuesta.ok(autenticacion.eliminarUsuario(((Usuario) datos).getId(), usuarioSesion), null);
            default:
                return Respuesta.error("Operación no soportada: " + op);
        }
    }

    private ResumenDia resumenDelDia() throws SQLException {
        LocalDate hoy = LocalDate.now();
        FacturaDAO facturaDAO = new FacturaDAO();
        int clientesNuevos = Consultas.contar("SELECT COUNT(*) FROM clientes WHERE fecha_registro = ?", Date.valueOf(hoy));
        List<String> bajoMinimo = new ArrayList<>();
        for (Producto p : inventario.productosBajoMinimo()) {
            bajoMinimo.add(p.getNombre());
        }
        return new ResumenDia(facturaDAO.contarEmitidasDelDia(hoy), facturaDAO.totalEmitidasDelDia(hoy),
                clientesNuevos, bajoMinimo);
    }

    // Texto corto para la bitácora de la ventana del servidor
    private String describir(Solicitud s, Respuesta r) {
        Object d = s.getDatos();
        String texto = s.getOperacion().name();
        if (d instanceof Cliente) {
            texto += " " + ((Cliente) d).getIdentificacion();
        } else if (d instanceof Producto) {
            texto += " " + ((Producto) d).getCodigo();
        } else if (d instanceof Categoria) {
            texto += " " + ((Categoria) d).getNombre();
        } else if (d instanceof Usuario) {
            texto += " " + ((Usuario) d).getNombreUsuario();
        } else if (d instanceof AjusteStock) {
            AjusteStock a = (AjusteStock) d;
            texto += " producto " + a.getIdProducto() + " (" + (a.getCantidad() > 0 ? "+" : "") + a.getCantidad() + ")";
        } else if (d instanceof Integer) {
            texto += String.format(" FC-%06d", (Integer) d);
        } else if (d instanceof String[]) {
            texto += " " + ((String[]) d)[0];
        } else if (d instanceof Factura) {
            Factura f = (Factura) d;
            if (r.isExito()) {
                texto += " " + ((Factura) r.getDatos()).getNumeroFormateado();
            } else {
                List<String> lineas = new ArrayList<>();
                for (DetalleFactura det : f.getDetalles()) {
                    lineas.add(det.getProducto().getCodigo() + " × " + det.getCantidad());
                }
                texto += " (" + String.join(", ", lineas) + ")";
            }
            if (f.getCliente() != null) {
                texto += " cliente " + f.getCliente().getIdentificacion();
            }
        }
        return texto;
    }

    public void cerrarConexion() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar el socket: " + e.getMessage());
        }
    }

    public String getNombreHilo() {
        return nombreHilo;
    }

    public String getEquipo() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public LocalTime getConectadoDesde() {
        return conectadoDesde;
    }
}
