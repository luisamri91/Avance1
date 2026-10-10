package com.fidecompro.cliente;

import com.fidecompro.red.Respuesta;
import com.fidecompro.red.ResumenDia;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;

/**
 * Menú principal (boceto P2). Abre cada módulo en su propia ventana JFrame.
 */
public class VentanaPrincipal extends JFrame {

    private final Sesion sesion;
    // Map: una ventana por módulo; si ya está abierta solo se trae al frente
    private final Map<String, JFrame> ventanasAbiertas = new HashMap<>();
    private final JLabel lblFacturas = new JLabel("0");
    private final JLabel lblTotal = new JLabel(Estilo.colones(0));
    private final JLabel lblClientesNuevos = new JLabel("0");
    private final JLabel lblAlerta = new JLabel();
    private final JLabel lblDetalleAlerta = new JLabel();
    private final Timer verificador;

    public VentanaPrincipal(Sesion sesion) {
        super("Fidecompro · Menú principal");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarSesion();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                cargarResumen();
            }
        });
        setJMenuBar(crearMenu());
        construir();
        // Cada 3 segundos se revisa que el servidor siga en línea (HU 12: detener el servidor)
        verificador = new Timer(3000, e -> sesion.enviar(TipoOperacion.PING, null));
        verificador.start();
        setSize(1000, 560);
        setLocationRelativeTo(null);
        cargarResumen();
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        archivo.add(item("Cerrar sesión", this::cerrarSesion));
        JMenu clientes = new JMenu("Clientes");
        clientes.add(item("Gestión de clientes", () -> abrir("clientes", () -> new VentanaClientes(sesion))));
        JMenu productos = new JMenu("Productos");
        productos.add(item("Productos e inventario", () -> abrir("productos", () -> new VentanaProductos(sesion))));
        JMenu facturacion = new JMenu("Facturación");
        facturacion.add(item("Nueva factura", () -> abrir("factura", () -> new VentanaFactura(sesion))));
        facturacion.add(item("Historial de facturas", () -> abrir("historial", () -> new VentanaHistorial(sesion))));
        barra.add(archivo);
        barra.add(clientes);
        barra.add(productos);
        barra.add(facturacion);
        // HU 2: el vendedor no ve la opción Usuarios
        if (sesion.esAdministrador()) {
            JMenu usuarios = new JMenu("Usuarios");
            usuarios.add(item("Gestión de usuarios", () -> abrir("usuarios", () -> new VentanaUsuarios(sesion))));
            barra.add(usuarios);
        }
        JMenu ayuda = new JMenu("Ayuda");
        ayuda.add(item("Acerca de", () -> Estilo.info(this,
                "Fidecompro v6\nSistema de facturación e inventario cliente-servidor\nCurso Programación Cliente-Servidor Concurrente")));
        barra.add(ayuda);
        return barra;
    }

    private JMenuItem item(String texto, Runnable accion) {
        JMenuItem i = new JMenuItem(texto);
        i.addActionListener(e -> accion.run());
        return i;
    }

    private void construir() {
        JPanel mosaicos = new JPanel(new GridLayout(2, 3, 16, 16));
        mosaicos.setOpaque(false);
        mosaicos.add(mosaico("Clientes", "Registrar y editar", () -> abrir("clientes", () -> new VentanaClientes(sesion))));
        mosaicos.add(mosaico("Productos", "Catálogo e inventario", () -> abrir("productos", () -> new VentanaProductos(sesion))));
        mosaicos.add(mosaico("Nueva factura", "Emitir a un cliente", () -> abrir("factura", () -> new VentanaFactura(sesion))));
        mosaicos.add(mosaico("Historial", "Consultar facturas", () -> abrir("historial", () -> new VentanaHistorial(sesion))));
        if (sesion.esAdministrador()) {
            mosaicos.add(mosaico("Usuarios", "Solo administrador", () -> abrir("usuarios", () -> new VentanaUsuarios(sesion))));
        } else {
            JPanel vacio = new JPanel();
            vacio.setOpaque(false);
            mosaicos.add(vacio);
        }
        mosaicos.add(mosaico("Cerrar sesión", "Volver al inicio", this::cerrarSesion));

        JPanel resumen = Estilo.marco("Resumen del día");
        resumen.setPreferredSize(new Dimension(250, 100));
        Estilo.celda(resumen, new JLabel("Facturas emitidas:"), 0, 0, 1, 1);
        Estilo.celda(resumen, negrita(lblFacturas), 1, 0, 1, 0);
        Estilo.celda(resumen, new JLabel("Total vendido:"), 0, 1, 1, 1);
        Estilo.celda(resumen, negrita(lblTotal), 1, 1, 1, 0);
        Estilo.celda(resumen, new JLabel("Clientes nuevos:"), 0, 2, 1, 1);
        Estilo.celda(resumen, negrita(lblClientesNuevos), 1, 2, 1, 0);
        lblAlerta.setForeground(Estilo.ROJO);
        lblAlerta.setFont(lblAlerta.getFont().deriveFont(Font.BOLD, 14f));
        Estilo.celda(resumen, lblAlerta, 0, 3, 2, 1);
        lblDetalleAlerta.setForeground(Estilo.GRIS);
        Estilo.celda(resumen, lblDetalleAlerta, 0, 4, 2, 1);
        JPanel relleno = new JPanel();
        relleno.setOpaque(false);
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridy = 5;
        g.weighty = 1;
        resumen.add(relleno, g);

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        centro.add(mosaicos, BorderLayout.CENTER);
        centro.add(resumen, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(
                new JLabel("Usuario: " + sesion.getUsuario().getNombre() + " (" + sesion.getUsuario().getRol() + ")"),
                new JLabel("Servidor " + sesion.getConexion().getHost() + ":" + sesion.getConexion().getPuerto() + " · conectado")),
                BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    private JLabel negrita(JLabel l) {
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    private JPanel mosaico(String titulo, String subtitulo, Runnable accion) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xB8, 0xC4, 0xD4), 1, true),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)));
        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        t.setForeground(Estilo.AZUL_OSCURO);
        JLabel s = new JLabel(subtitulo, SwingConstants.CENTER);
        s.setForeground(Estilo.GRIS);
        p.add(t, BorderLayout.CENTER);
        p.add(s, BorderLayout.SOUTH);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accion.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(0xEA, 0xF1, 0xFA));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                p.setBackground(Color.WHITE);
            }
        });
        return p;
    }

    public JFrame abrir(String clave, Supplier<JFrame> crear) {
        JFrame ventana = ventanasAbiertas.get(clave);
        if (ventana == null || !ventana.isDisplayable()) {
            ventana = crear.get();
            ventanasAbiertas.put(clave, ventana);
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    ventanasAbiertas.remove(clave);
                    cargarResumen();
                }
            });
            ventana.setVisible(true);
        }
        ventana.toFront();
        return ventana;
    }

    public void cargarResumen() {
        Respuesta r = sesion.enviar(TipoOperacion.RESUMEN_DIA, null);
        if (r == null || !r.isExito()) {
            return;
        }
        ResumenDia resumen = (ResumenDia) r.getDatos();
        lblFacturas.setText(String.valueOf(resumen.getFacturasEmitidas()));
        lblTotal.setText(Estilo.colones(resumen.getTotalVendido()));
        lblClientesNuevos.setText(String.valueOf(resumen.getClientesNuevos()));
        int bajos = resumen.getProductosBajoMinimo().size();
        if (bajos == 0) {
            lblAlerta.setText("");
            lblDetalleAlerta.setText("");
        } else {
            // HU 7: aviso "N productos bajo el stock mínimo" con sus nombres
            lblAlerta.setText("<html>⚠ " + bajos + (bajos == 1 ? " producto" : " productos") + " bajo el stock mínimo</html>");
            lblDetalleAlerta.setText("<html>" + String.join(", ", resumen.getProductosBajoMinimo()) + "</html>");
        }
    }

    private void cerrarSesion() {
        // HU 1: si hay una factura sin emitir se pregunta antes de salir
        JFrame factura = ventanasAbiertas.get("factura");
        if (factura instanceof VentanaFactura && ((VentanaFactura) factura).tieneLineasSinEmitir()) {
            factura.toFront();
            if (!Estilo.confirmar(factura, "Tiene una factura sin emitir. ¿Desea descartarla y cerrar sesión?")) {
                return;
            }
        }
        sesion.cerrarSesion();
        cerrarTodo();
        new VentanaLogin(sesion.getConexion().getHost(), sesion.getConexion().getPuerto()).setVisible(true);
    }

    // Cierra el menú y todas las ventanas de módulos
    public void cerrarTodo() {
        verificador.stop();
        for (JFrame v : ventanasAbiertas.values().toArray(new JFrame[0])) {
            v.dispose();
        }
        ventanasAbiertas.clear();
        dispose();
    }

    public Map<String, JFrame> getVentanasAbiertas() {
        return ventanasAbiertas;
    }
}
