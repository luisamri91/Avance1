package com.fidecompro.cliente;

import com.fidecompro.modelo.Usuario;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.Component;
import java.io.IOException;
import javax.swing.SwingUtilities;

/**
 * Lo que comparten las ventanas de una caja: la conexión, el usuario que inició sesión y el menú.
 */
public class Sesion {

    private final ClienteSocket conexion;
    private final Usuario usuario;
    private VentanaPrincipal principal;
    private boolean conexionPerdida;

    public Sesion(ClienteSocket conexion, Usuario usuario) {
        this.conexion = conexion;
        this.usuario = usuario;
    }

    /**
     * Envía una solicitud. Si se cae la conexión avisa "Se perdió la conexión con el servidor."
     * y vuelve al inicio de sesión; en ese caso devuelve null.
     */
    public Respuesta enviar(TipoOperacion operacion, Object datos) {
        try {
            return conexion.enviar(operacion, datos);
        } catch (IOException e) {
            perderConexion();
            return null;
        }
    }

    /**
     * Igual que enviar(), pero si el servidor contesta con error lo muestra al usuario y devuelve null.
     */
    public Respuesta pedir(Component padre, TipoOperacion operacion, Object datos) {
        Respuesta r = enviar(operacion, datos);
        if (r != null && !r.isExito()) {
            Estilo.error(padre, r.getMensaje());
            return null;
        }
        return r;
    }

    public synchronized void perderConexion() {
        if (conexionPerdida) {
            return;
        }
        conexionPerdida = true;
        conexion.desconectar();
        SwingUtilities.invokeLater(() -> {
            if (principal != null) {
                principal.cerrarTodo();
            }
            Estilo.error(null, "Se perdió la conexión con el servidor.");
            new VentanaLogin(conexion.getHost(), conexion.getPuerto()).setVisible(true);
        });
    }

    public void cerrarSesion() {
        enviar(TipoOperacion.LOGOUT, null);
        conexionPerdida = true;
        conexion.desconectar();
    }

    public boolean esAdministrador() {
        return usuario.esAdministrador();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public ClienteSocket getConexion() {
        return conexion;
    }

    public VentanaPrincipal getPrincipal() {
        return principal;
    }

    public void setPrincipal(VentanaPrincipal principal) {
        this.principal = principal;
    }
}
