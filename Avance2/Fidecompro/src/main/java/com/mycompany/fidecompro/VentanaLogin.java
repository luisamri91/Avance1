package com.mycompany.fidecompro;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

// ventana de inicio de sesion (HU 1)
public class VentanaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JLabel lblMensaje;
    private JButton btnIngresar;
    private int intentosFallidos = 0;

    public VentanaLogin() {
        setTitle("Fidecompro · Inicio de sesión");
        setSize(470, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("FIDECOMPRO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(40, 60, 90));
        lblTitulo.setBounds(0, 20, 470, 35);
        add(lblTitulo);

        JLabel lblSub = new JLabel("Facturación e inventario · ventas al por mayor", SwingConstants.CENTER);
        lblSub.setBounds(0, 55, 470, 20);
        add(lblSub);

        JPanel panel = new JPanel(null);
        panel.setBorder(BorderFactory.createTitledBorder("Ingrese sus datos"));
        panel.setBounds(25, 85, 415, 180);
        add(panel);

        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setBounds(20, 30, 90, 25);
        panel.add(lblUsuario);
        txtUsuario = new JTextField();
        txtUsuario.setBounds(110, 30, 280, 25);
        panel.add(txtUsuario);

        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setBounds(20, 65, 90, 25);
        panel.add(lblContrasena);
        txtContrasena = new JPasswordField();
        txtContrasena.setBounds(110, 65, 280, 25);
        panel.add(txtContrasena);

        lblMensaje = new JLabel("");
        lblMensaje.setForeground(Color.RED);
        lblMensaje.setBounds(20, 98, 380, 25);
        panel.add(lblMensaje);

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBounds(130, 130, 150, 32);
        panel.add(btnIngresar);

        JLabel lblPrueba = new JLabel(" Usuarios de prueba: mrodriguez / Admin2026 · cjimenez / Venta2026");
        lblPrueba.setBounds(0, 280, 470, 25);
        lblPrueba.setOpaque(true);
        lblPrueba.setBackground(new Color(225, 230, 240));
        add(lblPrueba);

        btnIngresar.addActionListener(e -> ingresar());
        txtContrasena.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtContrasena.getPassword());
        txtUsuario.setBackground(Color.WHITE);
        txtContrasena.setBackground(Color.WHITE);

        if (usuario.isEmpty() || clave.isEmpty()) {
            if (usuario.isEmpty()) {
                txtUsuario.setBackground(new Color(255, 220, 220));
            }
            if (clave.isEmpty()) {
                txtContrasena.setBackground(new Color(255, 220, 220));
            }
            lblMensaje.setText("Debe ingresar usuario y contraseña.");
            return;
        }

        try {
            Usuario u = Datos.usuarios.iniciarSesion(usuario, clave);
            intentosFallidos = 0;
            new VentanaMenu(u).setVisible(true);
            dispose();
        } catch (CredencialesInvalidasException ex) {
            intentosFallidos++;
            txtContrasena.setText("");
            if (intentosFallidos >= 3) {
                bloquear();
            } else {
                lblMensaje.setText(ex.getMessage());
            }
        }
    }

    // despues de 3 intentos se bloquea el boton por 1 minuto
    private void bloquear() {
        lblMensaje.setText("Demasiados intentos fallidos. Intente de nuevo en 1 minuto.");
        btnIngresar.setEnabled(false);
        Timer timer = new Timer(60000, e -> {
            btnIngresar.setEnabled(true);
            intentosFallidos = 0;
            lblMensaje.setText("");
        });
        timer.setRepeats(false);
        timer.start();
    }
}
