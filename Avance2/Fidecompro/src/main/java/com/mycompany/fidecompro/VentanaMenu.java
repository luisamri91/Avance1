package com.mycompany.fidecompro;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// menu principal con el resumen del dia y la alerta de stock bajo (HU 7)
public class VentanaMenu extends JFrame {

    private Usuario usuario;
    private VentanaFactura ventanaFactura;
    private JLabel lblFacturas;
    private JLabel lblTotal;
    private JLabel lblClientes;
    private JLabel lblAlerta;
    private JLabel lblProductosBajos;

    public VentanaMenu(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Fidecompro · Menú principal");
        setSize(1000, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        iniciarComponentes();
        cargarResumen();

        // cada vez que se vuelve al menu se actualiza el resumen
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarResumen();
            }
        });
    }

    private void iniciarComponentes() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem itemSalir = new JMenuItem("Cerrar sesión");
        itemSalir.addActionListener(e -> cerrarSesion());
        archivo.add(itemSalir);
        JMenu clientes = new JMenu("Clientes");
        JMenuItem itemClientes = new JMenuItem("Gestión de clientes");
        itemClientes.addActionListener(e -> new VentanaClientes(usuario).setVisible(true));
        clientes.add(itemClientes);
        JMenu productos = new JMenu("Productos");
        JMenuItem itemProductos = new JMenuItem("Productos e inventario");
        itemProductos.addActionListener(e -> new VentanaProductos(usuario).setVisible(true));
        productos.add(itemProductos);
        JMenu facturacion = new JMenu("Facturación");
        JMenuItem itemFactura = new JMenuItem("Nueva factura");
        itemFactura.addActionListener(e -> abrirFactura());
        facturacion.add(itemFactura);
        barra.add(archivo);
        barra.add(clientes);
        barra.add(productos);
        barra.add(facturacion);
        setJMenuBar(barra);

        add(crearMosaico("Clientes", "Registrar y editar", 20, 20));
        add(crearMosaico("Productos", "Catálogo e inventario", 257, 20));
        add(crearMosaico("Nueva factura", "Emitir a un cliente", 20, 255));
        add(crearMosaico("Cerrar sesión", "Volver al inicio", 257, 255));

        JPanel resumen = new JPanel(null);
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen del día"));
        resumen.setBounds(500, 15, 470, 455);
        add(resumen);

        resumen.add(etiqueta("Facturas emitidas:", 20, 30));
        lblFacturas = etiqueta("0", 220, 30);
        resumen.add(lblFacturas);
        resumen.add(etiqueta("Total vendido:", 20, 60));
        lblTotal = etiqueta("", 220, 60);
        resumen.add(lblTotal);
        resumen.add(etiqueta("Clientes registrados:", 20, 90));
        lblClientes = etiqueta("", 220, 90);
        resumen.add(lblClientes);

        lblAlerta = new JLabel("");
        lblAlerta.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblAlerta.setForeground(new Color(190, 40, 40));
        lblAlerta.setBounds(20, 130, 430, 25);
        resumen.add(lblAlerta);

        lblProductosBajos = new JLabel("");
        lblProductosBajos.setVerticalAlignment(JLabel.TOP);
        lblProductosBajos.setBounds(20, 160, 430, 200);
        resumen.add(lblProductosBajos);

        JLabel lblEstado = new JLabel(" Usuario: " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(225, 230, 240));
        lblEstado.setBounds(0, 485, 1000, 25);
        add(lblEstado);
    }

    private JLabel etiqueta(String texto, int x, int y) {
        JLabel l = new JLabel(texto);
        l.setBounds(x, y, 200, 25);
        return l;
    }

    private JButton crearMosaico(String titulo, String detalle, int x, int y) {
        JButton b = new JButton("<html><center><font size=5><b>" + titulo + "</b></font><br><br>" + detalle + "</center></html>");
        b.setBounds(x, y, 222, 215);
        b.setBackground(Color.WHITE);
        b.addActionListener(e -> {
            switch (titulo) {
                case "Clientes":
                    new VentanaClientes(usuario).setVisible(true);
                    break;
                case "Productos":
                    new VentanaProductos(usuario).setVisible(true);
                    break;
                case "Nueva factura":
                    abrirFactura();
                    break;
                default:
                    cerrarSesion();
            }
        });
        return b;
    }

    // solo se deja una ventana de factura abierta a la vez
    private void abrirFactura() {
        if (ventanaFactura == null || !ventanaFactura.isDisplayable()) {
            ventanaFactura = new VentanaFactura(usuario);
        }
        ventanaFactura.setVisible(true);
        ventanaFactura.toFront();
    }

    public void cargarResumen() {
        lblFacturas.setText(String.valueOf(Datos.facturas.cantidad()));
        lblTotal.setText(Factura.formatearMonto(Datos.facturas.totalVendido()));
        lblClientes.setText(String.valueOf(Datos.clientes.listar().size()));

        List<Producto> bajos = Datos.inventario.productosBajoMinimo();
        if (bajos.isEmpty()) {
            lblAlerta.setText("");
            lblProductosBajos.setText("");
        } else {
            lblAlerta.setText("⚠ " + bajos.size() + " productos bajo el stock mínimo");
            String nombres = "<html>";
            for (Producto p : bajos) {
                nombres += p.getNombre() + " (" + p.getExistencias() + ")<br>";
            }
            lblProductosBajos.setText(nombres + "</html>");
        }
    }

    private void cerrarSesion() {
        if (ventanaFactura != null && ventanaFactura.isDisplayable() && ventanaFactura.tieneLineasSinEmitir()) {
            int r = JOptionPane.showConfirmDialog(this, "Tiene una factura sin emitir. ¿Desea descartarla y cerrar sesión?",
                    "Cerrar sesión", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) {
                ventanaFactura.toFront();
                return;
            }
        }
        // se cierran todas las ventanas abiertas y se vuelve al login
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            w.dispose();
        }
        new VentanaLogin().setVisible(true);
    }
}
