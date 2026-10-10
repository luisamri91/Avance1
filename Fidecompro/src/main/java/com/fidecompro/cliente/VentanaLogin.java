package com.fidecompro.cliente;

import com.fidecompro.modelo.Usuario;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/**
 * Pantalla de inicio de sesión de una caja (HU 1, boceto P1).
 */
public class VentanaLogin extends JFrame {

    private static final int MAX_INTENTOS = 3;

    private final JTextField txtServidor = new JTextField(14);
    private final JTextField txtPuerto = new JTextField(5);
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(16);
    private final JButton btnIngresar = Estilo.botonPrincipal("Ingresar");
    private final JLabel lblMensaje = new JLabel(" ");
    private final Border bordeNormal = txtUsuario.getBorder();

    private ClienteSocket conexion;
    private int intentosFallidos;

    public VentanaLogin(String host, int puerto) {
        super("Fidecompro · Inicio de sesión");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        txtServidor.setText(host);
        txtPuerto.setText(String.valueOf(puerto));
        construir();
        btnIngresar.addActionListener(e -> ingresar());
        txtContrasena.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void construir() {
        JLabel titulo = new JLabel("FIDECOMPRO", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(Estilo.AZUL_OSCURO);
        JLabel subtitulo = new JLabel("Facturación e inventario · ventas al por mayor", SwingConstants.CENTER);
        subtitulo.setForeground(Estilo.GRIS);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(titulo, BorderLayout.CENTER);
        encabezado.add(subtitulo, BorderLayout.SOUTH);

        JPanel form = Estilo.marco("Ingrese sus datos");
        Estilo.celda(form, new JLabel("Usuario:"), 0, 0, 1, 0);
        Estilo.celda(form, txtUsuario, 1, 0, 3, 1);
        Estilo.celda(form, new JLabel("Contraseña:"), 0, 1, 1, 0);
        Estilo.celda(form, txtContrasena, 1, 1, 3, 1);
        Estilo.celda(form, new JLabel("Servidor:"), 0, 2, 1, 0);
        Estilo.celda(form, txtServidor, 1, 2, 1, 1);
        Estilo.celda(form, new JLabel("Puerto:"), 2, 2, 1, 0);
        Estilo.celda(form, txtPuerto, 3, 2, 1, 0);
        lblMensaje.setForeground(Estilo.ROJO);
        Estilo.celda(form, lblMensaje, 0, 3, 4, 1);
        btnIngresar.setPreferredSize(new java.awt.Dimension(160, 34));
        Estilo.celda(form, btnIngresar, 1, 4, 3, 0);

        JPanel contenido = new JPanel(new BorderLayout(0, 14));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(form, BorderLayout.CENTER);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(new JLabel("Usuarios de prueba: mrodriguez / Admin2026 · cjimenez / Venta2026"),
                new JLabel("")), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private void ingresar() {
        txtUsuario.setBorder(bordeNormal);
        txtContrasena.setBorder(bordeNormal);
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtContrasena.getPassword());
        if (usuario.isEmpty() || clave.isEmpty()) {
            Border rojo = BorderFactory.createLineBorder(Estilo.ROJO, 2);
            if (usuario.isEmpty()) {
                txtUsuario.setBorder(rojo);
            }
            if (clave.isEmpty()) {
                txtContrasena.setBorder(rojo);
            }
            lblMensaje.setText("Debe ingresar usuario y contraseña.");
            return;
        }
        try {
            if (conexion == null) {
                conexion = new ClienteSocket(txtServidor.getText().trim(), Integer.parseInt(txtPuerto.getText().trim()));
                conexion.conectar();
            }
            Respuesta r = conexion.enviar(TipoOperacion.LOGIN, new String[]{usuario, clave});
            if (r.isExito()) {
                abrirMenu((Usuario) r.getDatos());
            } else {
                registrarFallo(r.getMensaje());
            }
        } catch (NumberFormatException e) {
            lblMensaje.setText("El puerto debe ser un número.");
            conexion = null;
        } catch (IOException e) {
            lblMensaje.setText("No se pudo conectar con el servidor " + txtServidor.getText() + ":" + txtPuerto.getText() + ".");
            if (conexion != null) {
                conexion.desconectar();
            }
            conexion = null;
        }
    }

    private void registrarFallo(String mensaje) {
        intentosFallidos++;
        txtContrasena.setText("");
        if (intentosFallidos >= MAX_INTENTOS) {
            lblMensaje.setText("Demasiados intentos fallidos. Intente de nuevo en 1 minuto.");
            btnIngresar.setEnabled(false);
            Timer espera = new Timer(60_000, e -> {
                btnIngresar.setEnabled(true);
                intentosFallidos = 0;
                lblMensaje.setText(" ");
            });
            espera.setRepeats(false);
            espera.start();
        } else {
            lblMensaje.setText(mensaje);
        }
    }

    private void abrirMenu(Usuario usuario) {
        Sesion sesion = new Sesion(conexion, usuario);
        VentanaPrincipal principal = new VentanaPrincipal(sesion);
        sesion.setPrincipal(principal);
        principal.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            String host = args.length > 0 ? args[0] : "localhost";
            new VentanaLogin(host, 5000).setVisible(true);
        });
    }
}
