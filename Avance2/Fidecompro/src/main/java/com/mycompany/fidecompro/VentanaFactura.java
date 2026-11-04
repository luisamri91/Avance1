package com.mycompany.fidecompro;

import java.awt.Color;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

// crear una factura a un cliente (HU 8)
public class VentanaFactura extends JFrame {

    private Usuario usuario;
    private Factura factura;

    private JTextField txtCliente;
    private JLabel lblCliente;
    private JTextField txtCodigo;
    private JTextField txtCantidad;
    private DefaultTableModel modelo;
    private JTable tabla;
    private JComboBox<MetodoPago> cmbPago;
    private JTextField txtDescuento;
    private JLabel lblSubtotal, lblDescuentoTitulo, lblDescuento, lblIva, lblTotal;

    public VentanaFactura(Usuario usuario) {
        this.usuario = usuario;
        this.factura = new Factura(usuario);
        setTitle("Fidecompro · Nueva factura");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        JPanel pCliente = new JPanel(null);
        pCliente.setBorder(BorderFactory.createTitledBorder("Cliente"));
        pCliente.setBounds(12, 10, 975, 70);
        add(pCliente);
        pCliente.add(etiqueta("Identificación:", 15, 30, 100));
        txtCliente = new JTextField();
        txtCliente.setBounds(110, 30, 160, 25);
        pCliente.add(txtCliente);
        JButton btnCliente = new JButton("Buscar cliente...");
        btnCliente.setBounds(285, 30, 130, 25);
        pCliente.add(btnCliente);
        lblCliente = new JLabel("(sin cliente)");
        lblCliente.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCliente.setBounds(430, 30, 520, 25);
        pCliente.add(lblCliente);

        JPanel pProducto = new JPanel(null);
        pProducto.setBorder(BorderFactory.createTitledBorder("Agregar producto"));
        pProducto.setBounds(12, 90, 975, 70);
        add(pProducto);
        pProducto.add(etiqueta("Código:", 15, 30, 60));
        txtCodigo = new JTextField();
        txtCodigo.setBounds(70, 30, 140, 25);
        pProducto.add(txtCodigo);
        JButton btnProducto = new JButton("Buscar...");
        btnProducto.setBounds(225, 30, 90, 25);
        pProducto.add(btnProducto);
        pProducto.add(etiqueta("Cantidad:", 330, 30, 70));
        txtCantidad = new JTextField("1");
        txtCantidad.setBounds(395, 30, 60, 25);
        pProducto.add(txtCantidad);
        JButton btnAgregar = new JButton("Agregar →");
        btnAgregar.setBounds(855, 30, 100, 25);
        pProducto.add(btnAgregar);

        modelo = new DefaultTableModel(new String[]{"#", "Código", "Descripción", "Cant.", "Precio unit.", "IVA", "Total línea"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(30);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(12, 170, 975, 260);
        add(scroll);

        add(etiqueta("Método de pago:", 15, 480, 110));
        cmbPago = new JComboBox<>(MetodoPago.values());
        cmbPago.setBounds(125, 480, 175, 25);
        add(cmbPago);
        add(etiqueta("Descuento (%):", 15, 515, 110));
        txtDescuento = new JTextField("0");
        txtDescuento.setBounds(125, 515, 60, 25);
        add(txtDescuento);
        JButton btnQuitar = new JButton("Quitar línea");
        btnQuitar.setBounds(15, 552, 100, 25);
        add(btnQuitar);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(125, 552, 90, 25);
        add(btnCancelar);

        JPanel pTotales = new JPanel(null);
        pTotales.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pTotales.setBackground(Color.WHITE);
        pTotales.setBounds(718, 440, 268, 125);
        add(pTotales);
        pTotales.add(etiqueta("Subtotal", 10, 8, 120));
        lblSubtotal = monto(8, pTotales);
        lblDescuentoTitulo = etiqueta("Descuento (0 %)", 10, 36, 120);
        pTotales.add(lblDescuentoTitulo);
        lblDescuento = monto(36, pTotales);
        pTotales.add(etiqueta("Impuesto (IVA)", 10, 64, 120));
        lblIva = monto(64, pTotales);
        JLabel lblTotalTitulo = etiqueta("TOTAL", 10, 92, 120);
        lblTotalTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        pTotales.add(lblTotalTitulo);
        lblTotal = monto(92, pTotales);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 15));

        JButton btnEmitir = new JButton("Emitir factura");
        btnEmitir.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnEmitir.setBounds(770, 575, 216, 38);
        add(btnEmitir);

        JPanel barra = new JPanel(null);
        barra.setBackground(new Color(225, 230, 240));
        barra.setBounds(0, 635, 1000, 25);
        add(barra);
        barra.add(etiqueta(" Vendedor: " + usuario.getNombre(), 5, 0, 400));
        barra.add(etiqueta(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 880, 0, 120));

        btnCliente.addActionListener(e -> buscarCliente());
        txtCliente.addActionListener(e -> buscarCliente());
        btnProducto.addActionListener(e -> elegirProducto());
        btnAgregar.addActionListener(e -> agregarProducto());
        txtCantidad.addActionListener(e -> agregarProducto());
        btnQuitar.addActionListener(e -> quitarLinea());
        btnCancelar.addActionListener(e -> nuevaFactura());
        btnEmitir.addActionListener(e -> emitir());
        txtDescuento.addActionListener(e -> actualizarTotales());
        txtDescuento.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                actualizarTotales();
            }
        });
        actualizarTotales();
    }

    private JLabel etiqueta(String texto, int x, int y, int ancho) {
        JLabel l = new JLabel(texto);
        l.setBounds(x, y, ancho, 25);
        return l;
    }

    private JLabel monto(int y, JPanel panel) {
        JLabel l = new JLabel("", SwingConstants.RIGHT);
        l.setBounds(120, y, 138, 25);
        panel.add(l);
        return l;
    }

    private void buscarCliente() {
        try {
            Cliente c = Datos.clientes.buscarPorIdentificacion(txtCliente.getText().trim());
            if (!c.isActivo()) {
                JOptionPane.showMessageDialog(this, "El cliente está inactivo y no se le puede facturar.");
                return;
            }
            factura.setCliente(c);
            lblCliente.setText(c.getNombre() + "  ·  " + c.getTelefono());
        } catch (RegistroNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // muestra la lista de productos activos para escoger el codigo
    private void elegirProducto() {
        List<String> opciones = new ArrayList<>();
        for (Producto p : Datos.inventario.listarProductos()) {
            if (p.isActivo()) {
                opciones.add(p.getCodigo() + " - " + p.getNombre() + " (" + p.getExistencias() + ")");
            }
        }
        Object elegido = JOptionPane.showInputDialog(this, "Seleccione el producto:", "Buscar producto",
                JOptionPane.PLAIN_MESSAGE, null, opciones.toArray(), null);
        if (elegido != null) {
            txtCodigo.setText(elegido.toString().split(" - ")[0]);
            txtCantidad.requestFocus();
        }
    }

    private void agregarProducto() {
        try {
            Producto p = Datos.inventario.buscarProducto(txtCodigo.getText().trim());
            if (!p.isActivo()) {
                JOptionPane.showMessageDialog(this, "El producto está inactivo y no se puede vender.");
                return;
            }
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0.");
                return;
            }
            factura.agregarDetalle(p, cantidad);
            txtCodigo.setText("");
            txtCantidad.setText("1");
            cargarLineas();
        } catch (RegistroNoEncontradoException | StockInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fidecompro", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.");
        }
    }

    private void quitarLinea() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la línea que desea quitar.");
            return;
        }
        factura.quitarDetalle(fila);
        cargarLineas();
    }

    private void cargarLineas() {
        modelo.setRowCount(0);
        int i = 1;
        for (DetalleFactura d : factura.getDetalles()) {
            Producto p = d.getProducto();
            double totalLinea = d.calcularSubtotal() * (1 + p.obtenerPorcentajeImpuesto());
            modelo.addRow(new Object[]{i++, p.getCodigo(), p.getNombre(), d.getCantidad(),
                Factura.formatearMonto(d.getPrecioUnitario()), (int) Math.round(p.obtenerPorcentajeImpuesto() * 100) + "%",
                Factura.formatearMonto(totalLinea)});
        }
        actualizarTotales();
    }

    private void actualizarTotales() {
        try {
            factura.setPorcentajeDescuento(Double.parseDouble(txtDescuento.getText().trim()));
        } catch (NumberFormatException ex) {
            factura.setPorcentajeDescuento(0);
        }
        lblSubtotal.setText(Factura.formatearMonto(factura.calcularSubtotal()));
        lblDescuentoTitulo.setText("Descuento (" + txtDescuento.getText().trim() + " %)");
        lblDescuento.setText("-" + Factura.formatearMonto(factura.calcularDescuento()));
        lblIva.setText(Factura.formatearMonto(factura.calcularImpuesto()));
        lblTotal.setText(Factura.formatearMonto(factura.calcularTotal()));
    }

    private void emitir() {
        try {
            factura.setPorcentajeDescuento(Double.parseDouble(txtDescuento.getText().trim()));
            factura.setMetodoPago((MetodoPago) cmbPago.getSelectedItem());
            Datos.facturas.emitir(factura);
            Datos.guardarCambios();
            Factura emitida = factura;
            nuevaFactura();
            new VentanaVistaFactura(this, emitida).setVisible(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El descuento debe estar entre 0 y 100.");
        } catch (IllegalArgumentException | StockInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fidecompro", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void nuevaFactura() {
        factura = new Factura(usuario);
        txtCliente.setText("");
        lblCliente.setText("(sin cliente)");
        txtCodigo.setText("");
        txtCantidad.setText("1");
        txtDescuento.setText("0");
        cmbPago.setSelectedIndex(0);
        cargarLineas();
    }

    public boolean tieneLineasSinEmitir() {
        return !factura.getDetalles().isEmpty();
    }
}
