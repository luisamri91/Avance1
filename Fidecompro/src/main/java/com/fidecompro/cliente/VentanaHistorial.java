package com.fidecompro.cliente;

import com.fidecompro.modelo.EstadoFactura;
import com.fidecompro.modelo.Factura;
import com.fidecompro.red.FiltroFacturas;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Historial de facturas (HU 10 y 11, boceto P7): consulta con filtros, detalle, archivo y anulación.
 */
public class VentanaHistorial extends JFrame {

    private final Sesion sesion;
    private final JTextField txtDesde = new JTextField(9);
    private final JTextField txtHasta = new JTextField(9);
    private final JTextField txtCliente = new JTextField(16);
    private final JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Todas", "EMITIDA", "ANULADA"});
    private final DefaultTableModel modelo = Estilo.modeloTabla("Número", "Fecha", "Cliente", "Vendedor", "Total", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblCantidad = new JLabel(" ");
    private final JButton btnAnular = Estilo.botonPeligro("Anular factura");
    private List<Factura> facturas = new ArrayList<>();

    public VentanaHistorial(Sesion sesion) {
        super("Fidecompro · Historial de facturas");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        LocalDate hoy = LocalDate.now();
        txtDesde.setText(hoy.withDayOfMonth(1).format(Estilo.FECHA));
        txtHasta.setText(hoy.format(Estilo.FECHA));
        construir();
        setSize(980, 560);
        setLocationByPlatform(true);
        consultar();
    }

    private void construir() {
        JPanel filtros = Estilo.marco("Filtros");
        JButton btnConsultar = Estilo.botonPrincipal("Consultar");
        Estilo.celda(filtros, new JLabel("Desde:"), 0, 0, 1, 0);
        Estilo.celda(filtros, txtDesde, 1, 0, 1, 0);
        Estilo.celda(filtros, new JLabel("Hasta:"), 2, 0, 1, 0);
        Estilo.celda(filtros, txtHasta, 3, 0, 1, 0);
        Estilo.celda(filtros, new JLabel("Cliente:"), 4, 0, 1, 0);
        Estilo.celda(filtros, txtCliente, 5, 0, 1, 1);
        Estilo.celda(filtros, new JLabel("Estado:"), 6, 0, 1, 0);
        Estilo.celda(filtros, cmbEstado, 7, 0, 1, 0);
        Estilo.celda(filtros, btnConsultar, 8, 0, 1, 0);
        btnConsultar.addActionListener(e -> consultar());

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(4).setCellRenderer(derecha);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                comp.setForeground("ANULADA".equals(v) ? Estilo.ROJO : Color.BLACK);
                return comp;
            }
        });
        tabla.getColumnModel().getColumn(2).setPreferredWidth(240);

        JButton btnDetalle = Estilo.boton("Ver detalle");
        JButton btnArchivo = Estilo.boton("Regenerar archivo");
        btnDetalle.addActionListener(e -> verDetalle());
        btnArchivo.addActionListener(e -> regenerarArchivo());
        btnAnular.addActionListener(e -> anular());
        // HU 11: para un VENDEDOR el botón "Anular factura" no aparece
        btnAnular.setVisible(sesion.esAdministrador());
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnDetalle);
        botones.add(btnArchivo);
        botones.add(btnAnular);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(lblTotal, BorderLayout.WEST);
        abajo.add(botones, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        centro.add(abajo, BorderLayout.SOUTH);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(lblCantidad, new JLabel("Usuario: " + sesion.getUsuario().getNombre())), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    @SuppressWarnings("unchecked")
    public void consultar() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = Estilo.leerFecha(txtDesde.getText(), "Desde");
            hasta = Estilo.leerFecha(txtHasta.getText(), "Hasta");
        } catch (IllegalArgumentException e) {
            Estilo.error(this, e.getMessage());
            return;
        }
        EstadoFactura estado = cmbEstado.getSelectedIndex() == 0 ? null : EstadoFactura.valueOf((String) cmbEstado.getSelectedItem());
        Respuesta r = sesion.pedir(this, TipoOperacion.LISTAR_FACTURAS, new FiltroFacturas(desde, hasta, txtCliente.getText(), estado));
        if (r == null) {
            return;
        }
        facturas = (List<Factura>) r.getDatos();
        modelo.setRowCount(0);
        double total = 0;
        for (Factura f : facturas) {
            modelo.addRow(new Object[]{f.getNumeroFormateado(), f.getFecha().format(Estilo.FECHA_HORA), f.getCliente().getNombre(),
                f.getVendedor().getNombre(), Estilo.colones(f.calcularTotal()), f.getEstado()});
            if (f.getEstado() == EstadoFactura.EMITIDA) {
                total += f.calcularTotal();
            }
        }
        lblTotal.setText("Total del periodo (emitidas): " + Estilo.colones(total));
        lblCantidad.setText(facturas.isEmpty() ? "No hay facturas para los filtros seleccionados." : facturas.size() + " facturas");
    }

    private Factura seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.error(this, "Seleccione una factura en la tabla.");
            return null;
        }
        return facturas.get(fila);
    }

    private void verDetalle() {
        Factura f = seleccionada();
        if (f != null) {
            new DialogoVistaFactura(this, f).setVisible(true);
        }
    }

    // HU 10: conserva los precios del momento de la venta (vienen de detalle_factura)
    private void regenerarArchivo() {
        Factura f = seleccionada();
        if (f == null) {
            return;
        }
        JFileChooser selector = new JFileChooser(new File("facturas").getAbsoluteFile());
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = f.generarArchivo(selector.getSelectedFile().getPath());
            if (ruta == null) {
                Estilo.error(this, "No se pudo guardar el archivo de la factura.");
            } else {
                Estilo.info(this, "Archivo guardado en " + new File(ruta).getAbsolutePath() + ".");
            }
        }
    }

    private void anular() {
        Factura f = seleccionada();
        if (f == null) {
            return;
        }
        if (f.getEstado() == EstadoFactura.ANULADA) {
            Estilo.error(this, "La factura ya estaba anulada.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Anular la factura " + f.getNumeroFormateado() + "? Las unidades vuelven al inventario.")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.ANULAR_FACTURA, f.getNumero());
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            consultar();
        }
    }
}
