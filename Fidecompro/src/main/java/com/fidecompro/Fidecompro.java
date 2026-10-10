package com.fidecompro;

import com.fidecompro.cliente.VentanaLogin;
import com.fidecompro.servidor.VentanaServidor;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Punto de entrada. Sin argumentos abre un lanzador para abrir el servidor y varias cajas
 * desde un solo "Run" de NetBeans. Con el argumento "servidor" o "cliente" abre directamente esa ventana.
 */
public class Fidecompro {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            String modo = args.length > 0 ? args[0] : "";
            if (modo.equalsIgnoreCase("servidor")) {
                new VentanaServidor(true).setVisible(true);
            } else if (modo.equalsIgnoreCase("cliente")) {
                String host = args.length > 1 ? args[1] : "localhost";
                new VentanaLogin(host, 5000).setVisible(true);
            } else {
                mostrarLanzador();
            }
        });
    }

    private static void mostrarLanzador() {
        JFrame lanzador = new JFrame("Fidecompro v6");
        lanzador.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel titulo = new JLabel("FIDECOMPRO", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(Estilo.AZUL_OSCURO);
        JLabel ayuda = new JLabel("<html><center>1. Abra el servidor y presione <b>Iniciar</b>.<br>"
                + "2. Abra una o varias cajas e inicie sesión en cada una.</center></html>", SwingConstants.CENTER);
        JButton btnServidor = Estilo.botonPrincipal("Abrir servidor");
        JButton btnCliente = Estilo.boton("Abrir caja (cliente)");
        btnServidor.addActionListener(e -> new VentanaServidor(false).setVisible(true));
        btnCliente.addActionListener(e -> new VentanaLogin("localhost", 5000).setVisible(true));
        JPanel botones = new JPanel(new GridLayout(1, 2, 12, 0));
        botones.add(btnServidor);
        botones.add(btnCliente);
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(ayuda, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        lanzador.setContentPane(raiz);
        lanzador.pack();
        lanzador.setLocationRelativeTo(null);
        lanzador.setVisible(true);
    }
}
