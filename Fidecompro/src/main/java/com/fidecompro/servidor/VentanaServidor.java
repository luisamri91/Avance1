package com.fidecompro.servidor;

import com.fidecompro.datos.ConexionBD;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana del servidor (HU 12, boceto P9): iniciar y detener, clientes conectados con su hilo
 * y bitácora de operaciones. La pestaña "Base de datos" muestra las tablas para comprobar
 * que todo quedó guardado.
 */
public class VentanaServidor extends JFrame {

    private static final String[] TABLAS = {"USUARIOS", "CLIENTES", "CATEGORIAS", "PRODUCTOS", "FACTURAS", "DETALLE_FACTURA"};

    private ServidorFacturacion servidor;

    private final JTextField txtPuerto = new JTextField(String.valueOf(ServidorFacturacion.PUERTO_POR_DEFECTO), 6);
    private final JComboBox<String> cmbBaseDatos = new JComboBox<>(new String[]{ConexionBD.DERBY, ConexionBD.MYSQL});
    private final JButton btnIniciar = Estilo.botonPrincipal("Iniciar");
    private final JButton btnDetener = Estilo.botonPeligro("Detener");
    private final JLabel lblEstado = new JLabel("● Detenido");
    private final JLabel lblClientes = new JLabel("Clientes conectados: 0");
    private final JLabel lblHilos = new JLabel("Hilos activos: 0 de " + ServidorFacturacion.MAX_CLIENTES);
    private final DefaultTableModel modeloClientes = Estilo.modeloTabla("Hilo", "Equipo (IP:puerto)", "Usuario", "Rol", "Conectado desde");
    private final DefaultTableModel modeloBitacora = Estilo.modeloTabla("Hora", "Hilo", "Operación", "Resultado");
    private final JTable tablaClientes = new JTable(modeloClientes);
    private final JTable tablaBitacora = new JTable(modeloBitacora);
    private final JComboBox<String> cmbTabla = new JComboBox<>(TABLAS);
    private final DefaultTableModel modeloDatos = Estilo.modeloTabla();
    private final JLabel lblBaseDerecha = new JLabel();
    private final JTable tablaDatos = new JTable(modeloDatos);

    // salirAlCerrar = false cuando se abre desde el lanzador junto con ventanas cliente
    public VentanaServidor(boolean salirAlCerrar) {
        super("Servidor Fidecompro");
        setDefaultCloseOperation(salirAlCerrar ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (servidor != null) {
                    servidor.detener();
                }
            }
        });
        construir();
        btnDetener.setEnabled(false);
        btnIniciar.addActionListener(e -> iniciar());
        btnDetener.addActionListener(e -> detener());
        // Cada segundo se actualiza el contador de hilos activos
        new Timer(1000, e -> actualizarContadores()).start();
        setSize(980, 720);
        setLocationRelativeTo(null);
    }

    private void construir() {
        JPanel servidorPanel = Estilo.marco("Servidor");
        Estilo.celda(servidorPanel, new JLabel("Puerto:"), 0, 0, 1, 0);
        Estilo.celda(servidorPanel, txtPuerto, 1, 0, 1, 0);
        Estilo.celda(servidorPanel, new JLabel("Base de datos:"), 2, 0, 1, 0);
        Estilo.celda(servidorPanel, cmbBaseDatos, 3, 0, 1, 0);
        Estilo.celda(servidorPanel, btnIniciar, 4, 0, 1, 0);
        Estilo.celda(servidorPanel, btnDetener, 5, 0, 1, 0);
        Estilo.celda(servidorPanel, new JLabel(), 6, 0, 1, 1);
        Estilo.celda(servidorPanel, new JLabel("Estado:"), 0, 1, 1, 0);
        lblEstado.setForeground(Estilo.GRIS);
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 14f));
        Estilo.celda(servidorPanel, lblEstado, 1, 1, 2, 0);
        Estilo.celda(servidorPanel, lblClientes, 3, 1, 1, 0);
        Estilo.celda(servidorPanel, lblHilos, 4, 1, 3, 0);

        JPanel clientesPanel = Estilo.marco("Clientes conectados");
        clientesPanel.setLayout(new BorderLayout());
        clientesPanel.add(Estilo.scroll(tablaClientes));
        clientesPanel.setPreferredSize(new Dimension(100, 170));

        JPanel bitacoraPanel = Estilo.marco("Bitácora de operaciones");
        bitacoraPanel.setLayout(new BorderLayout());
        bitacoraPanel.add(Estilo.scroll(tablaBitacora));
        tablaBitacora.getColumnModel().getColumn(0).setMaxWidth(90);
        tablaBitacora.getColumnModel().getColumn(1).setMaxWidth(90);
        tablaBitacora.getColumnModel().getColumn(2).setPreferredWidth(420);
        tablaBitacora.getColumnModel().getColumn(3).setCellRenderer(new RenderResultado());

        JPanel monitor = new JPanel(new BorderLayout(0, 8));
        monitor.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        monitor.add(clientesPanel, BorderLayout.NORTH);
        monitor.add(bitacoraPanel, BorderLayout.CENTER);

        JPanel datos = new JPanel(new BorderLayout(0, 8));
        datos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnConsultar = Estilo.botonPrincipal("Consultar tabla");
        btnConsultar.addActionListener(e -> consultarTabla());
        cmbTabla.addActionListener(e -> consultarTabla());
        filtro.add(new JLabel("Tabla:"));
        filtro.add(cmbTabla);
        filtro.add(btnConsultar);
        filtro.add(new JLabel("  (los datos se leen directamente de la base de datos)"));
        datos.add(filtro, BorderLayout.NORTH);
        tablaDatos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        datos.add(Estilo.scroll(tablaDatos), BorderLayout.CENTER);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Monitor", monitor);
        pestanas.addTab("Base de datos", datos);
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedIndex() == 1) {
                consultarTabla();
            }
        });

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        arriba.add(servidorPanel);

        lblBaseDerecha.setText("Datos guardados en la base de datos");
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(pestanas, BorderLayout.CENTER);
        contenido.add(Estilo.barraEstado(new JLabel("Cada cliente conectado se atiende en su propio hilo"), lblBaseDerecha),
                BorderLayout.SOUTH);
        setContentPane(contenido);
    }

    private void iniciar() {
        int puerto;
        try {
            puerto = Integer.parseInt(txtPuerto.getText().trim());
        } catch (NumberFormatException e) {
            Estilo.error(this, "El puerto debe ser un número, por ejemplo 5000.");
            return;
        }
        ConexionBD.getInstancia().usarMotor((String) cmbBaseDatos.getSelectedItem());
        servidor = new ServidorFacturacion(puerto, ServidorFacturacion.MAX_CLIENTES);
        servidor.setVentana(this);
        try {
            servidor.iniciar();
        } catch (java.sql.SQLException e) {
            Estilo.error(this, "No se pudo conectar con la base de datos.\n" + e.getMessage());
            servidor = null;
            return;
        } catch (java.io.IOException e) {
            Estilo.error(this, "No se pudo abrir el puerto " + puerto + ": " + e.getMessage());
            servidor = null;
            return;
        }
        lblEstado.setText("● En ejecución desde " + LocalTime.now().format(Estilo.HORA));
        lblEstado.setForeground(Estilo.VERDE);
        btnIniciar.setEnabled(false);
        btnDetener.setEnabled(true);
        txtPuerto.setEnabled(false);
        cmbBaseDatos.setEnabled(false);
        lblBaseDerecha.setText(ConexionBD.getInstancia().esDerby()
                ? "Derby: basedatos/fidecompro · en NetBeans: jdbc:derby://localhost:1527/basedatos/fidecompro"
                : "MySQL: localhost:3306/fidecompro");
    }

    private void detener() {
        if (servidor == null) {
            return;
        }
        int conectados = servidor.getClientesConectados().size();
        if (conectados > 0 && !Estilo.confirmar(this, "Hay " + conectados + " clientes conectados. ¿Detener el servidor?")) {
            return;
        }
        servidor.detener();
        servidor = null;
        lblEstado.setText("● Detenido");
        lblEstado.setForeground(Estilo.GRIS);
        btnIniciar.setEnabled(true);
        btnDetener.setEnabled(false);
        txtPuerto.setEnabled(true);
        cmbBaseDatos.setEnabled(true);
    }

    // Lo llaman los hilos de los clientes: el cambio en la ventana se hace en el hilo de Swing
    public void refrescarClientes() {
        SwingUtilities.invokeLater(() -> {
            modeloClientes.setRowCount(0);
            if (servidor != null) {
                for (ManejadorCliente m : servidor.getClientesConectados()) {
                    modeloClientes.addRow(new Object[]{m.getNombreHilo(), m.getEquipo(),
                        m.getUsuarioSesion() == null ? "(sin sesión)" : m.getUsuarioSesion().getNombreUsuario(),
                        m.getUsuarioSesion() == null ? "" : m.getUsuarioSesion().getRol(),
                        m.getConectadoDesde().format(Estilo.HORA)});
                }
            }
            actualizarContadores();
        });
    }

    public void agregarBitacora(String hilo, String operacion, String resultado) {
        String hora = LocalTime.now().format(Estilo.HORA);
        SwingUtilities.invokeLater(() -> modeloBitacora.insertRow(0, new Object[]{hora, hilo, operacion, resultado}));
    }

    private void actualizarContadores() {
        int clientes = servidor == null ? 0 : servidor.getClientesConectados().size();
        int hilos = servidor == null ? 0 : servidor.getHilosActivos();
        lblClientes.setText("<html>Clientes conectados: <b>" + clientes + "</b></html>");
        lblHilos.setText("<html>Hilos activos: <b>" + hilos + "</b> de " + ServidorFacturacion.MAX_CLIENTES + "</html>");
    }

    private void consultarTabla() {
        if (servidor == null) {
            modeloDatos.setColumnIdentifiers(new Object[]{"Inicie el servidor para ver los datos"});
            modeloDatos.setRowCount(0);
            return;
        }
        String tabla = (String) cmbTabla.getSelectedItem();
        String orden = tabla.equals("FACTURAS") ? "numero" : tabla.equals("DETALLE_FACTURA") ? "numero_factura, linea" : "id";
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM " + tabla + " ORDER BY " + orden)) {
            ResultSetMetaData meta = rs.getMetaData();
            Vector<String> columnas = new Vector<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columnas.add(meta.getColumnName(i).toLowerCase());
            }
            Vector<Vector<Object>> filas = new Vector<>();
            while (rs.next()) {
                Vector<Object> fila = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    fila.add(rs.getObject(i));
                }
                filas.add(fila);
            }
            modeloDatos.setDataVector(filas, columnas);
            ajustarColumnas(tablaDatos);
        } catch (SQLException e) {
            Estilo.error(this, "No se pudo leer la tabla: " + e.getMessage());
        }
    }

    // Ancho de cada columna según su contenido, para leer las tablas completas
    private void ajustarColumnas(JTable tabla) {
        for (int c = 0; c < tabla.getColumnCount(); c++) {
            int ancho = tabla.getTableHeader().getFontMetrics(tabla.getTableHeader().getFont())
                    .stringWidth(tabla.getColumnName(c)) + 20;
            for (int f = 0; f < tabla.getRowCount(); f++) {
                Object v = tabla.getValueAt(f, c);
                ancho = Math.max(ancho, tabla.getFontMetrics(tabla.getFont()).stringWidth(String.valueOf(v)) + 16);
            }
            tabla.getColumnModel().getColumn(c).setPreferredWidth(Math.min(ancho, 320));
        }
    }

    // "OK" en negro y los errores en rojo
    private static class RenderResultado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            c.setForeground("OK".equals(valor) ? Color.BLACK : Estilo.ROJO);
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarLookAndFeel();
            new VentanaServidor(true).setVisible(true);
        });
    }
}
