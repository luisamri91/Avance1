package com.fidecompro.cliente;

import com.fidecompro.excepciones.StockInsuficienteException;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.DetalleFactura;
import com.fidecompro.modelo.Factura;
import com.fidecompro.modelo.MetodoPago;
import com.fidecompro.modelo.Producto;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Nueva factura (HU 8 y 9, boceto P5). La factura se arma en la caja y el servidor la emite.
 */
public class VentanaFactura extends JFrame {

    private final Sesion sesion;
    // TreeMap por código / identificación: búsqueda directa y listas ordenadas para los diálogos
    private final Map<String, Producto> productos = new TreeMap<>();
    private final Map<String, Cliente> clientesActivos = new TreeMap<>();

    private final JTextField txtCliente = new JTextField(14);
    private final JLabel lblCliente = new JLabel("(sin cliente)");
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JLabel lblDisponible = new JLabel(" ");
    private final DefaultTableModel modelo = Estilo.modeloTabla("#", "Código", "Descripción", "Cant.", "Precio unit.", "IVA", "Total línea");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<MetodoPago> cmbPago = new JComboBox<>(MetodoPago.values());
    private final JTextField txtDescuento = new JTextField("0", 5);
    private final JLabel lblSubtotal = valor();
    private final JLabel lblDescuento = valor();
    private final JLabel lblIva = valor();
    private final JLabel lblTotal = valor();
    private final JLabel lblTituloDescuento = new JLabel("Descuento (0 %)");
    private final JButton btnEmitir = Estilo.botonPrincipal("Emitir factura");

    private Cliente cliente;
    private Factura factura;

    public VentanaFactura(Sesion sesion) {
        super("Fidecompro · Nueva factura");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        construir();
        cargarCatalogos();
        nuevaFactura();
        setSize(1000, 680);
        setLocationByPlatform(true);
    }

    private static JLabel valor() {
        JLabel l = new JLabel(Estilo.colones(0), SwingConstants.RIGHT);
        l.setPreferredSize(new Dimension(150, 22));
        return l;
    }

    private void construir() {
        JPanel panelCliente = Estilo.marco("Cliente");
        JButton btnBuscarCliente = Estilo.boton("Buscar cliente…");
        Estilo.celda(panelCliente, new JLabel("Identificación:"), 0, 0, 1, 0);
        Estilo.celda(panelCliente, txtCliente, 1, 0, 1, 0);
        Estilo.celda(panelCliente, btnBuscarCliente, 2, 0, 1, 0);
        lblCliente.setFont(lblCliente.getFont().deriveFont(Font.BOLD, 14f));
        Estilo.celda(panelCliente, lblCliente, 3, 0, 1, 1);
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtCliente.addActionListener(e -> buscarCliente());

        JPanel panelProducto = Estilo.marco("Agregar producto");
        JButton btnBuscarProducto = Estilo.boton("Buscar…");
        JButton btnAgregar = Estilo.botonPrincipal("Agregar →");
        Estilo.celda(panelProducto, new JLabel("Código:"), 0, 0, 1, 0);
        Estilo.celda(panelProducto, txtCodigo, 1, 0, 1, 0);
        Estilo.celda(panelProducto, btnBuscarProducto, 2, 0, 1, 0);
        Estilo.celda(panelProducto, new JLabel("Cantidad:"), 3, 0, 1, 0);
        Estilo.celda(panelProducto, txtCantidad, 4, 0, 1, 0);
        lblDisponible.setForeground(Estilo.GRIS);
        Estilo.celda(panelProducto, lblDisponible, 5, 0, 1, 1);
        Estilo.celda(panelProducto, btnAgregar, 6, 0, 1, 0);
        btnBuscarProducto.addActionListener(e -> buscarProducto());
        txtCodigo.addActionListener(e -> buscarProducto());
        btnAgregar.addActionListener(e -> agregarLinea());
        txtCantidad.addActionListener(e -> agregarLinea());

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.add(panelCliente, BorderLayout.NORTH);
        arriba.add(panelProducto, BorderLayout.SOUTH);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c = 3; c <= 6; c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
        }
        tabla.getColumnModel().getColumn(0).setMaxWidth(40);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);

        // Abajo a la izquierda: pago, descuento y botones
        JPanel opciones = new JPanel(new GridBagLayout());
        JButton btnQuitar = Estilo.boton("Quitar línea");
        JButton btnCancelar = Estilo.boton("Cancelar");
        Estilo.celda(opciones, new JLabel("Método de pago:"), 0, 0, 1, 0);
        cmbPago.setPreferredSize(new Dimension(180, 28));
        Estilo.celda(opciones, cmbPago, 1, 0, 2, 0);
        Estilo.celda(opciones, new JLabel("Descuento (%):"), 0, 1, 1, 0);
        Estilo.celda(opciones, txtDescuento, 1, 1, 1, 0);
        Estilo.celda(opciones, btnQuitar, 0, 2, 1, 0);
        Estilo.celda(opciones, btnCancelar, 1, 2, 1, 0);
        btnQuitar.addActionListener(e -> quitarLinea());
        btnCancelar.addActionListener(e -> {
            if (!tieneLineasSinEmitir() || Estilo.confirmar(this, "¿Descartar la factura actual?")) {
                nuevaFactura();
            }
        });
        txtDescuento.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                recalcular();
            }

            public void removeUpdate(DocumentEvent e) {
                recalcular();
            }

            public void changedUpdate(DocumentEvent e) {
                recalcular();
            }
        });

        // Abajo a la derecha: totales y botón Emitir
        JPanel totales = new JPanel(new GridBagLayout());
        totales.setBackground(java.awt.Color.WHITE);
        totales.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xB8, 0xC4, 0xD4)));
        Estilo.celda(totales, new JLabel("Subtotal"), 0, 0, 1, 1);
        Estilo.celda(totales, lblSubtotal, 1, 0, 1, 0);
        Estilo.celda(totales, lblTituloDescuento, 0, 1, 1, 1);
        Estilo.celda(totales, lblDescuento, 1, 1, 1, 0);
        Estilo.celda(totales, new JLabel("Impuesto (IVA)"), 0, 2, 1, 1);
        Estilo.celda(totales, lblIva, 1, 2, 1, 0);
        JLabel tituloTotal = new JLabel("TOTAL");
        tituloTotal.setFont(tituloTotal.getFont().deriveFont(Font.BOLD, 15f));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));
        Estilo.celda(totales, tituloTotal, 0, 3, 1, 1);
        Estilo.celda(totales, lblTotal, 1, 3, 1, 0);
        btnEmitir.setFont(btnEmitir.getFont().deriveFont(Font.BOLD, 16f));
        btnEmitir.setPreferredSize(new Dimension(220, 40));
        btnEmitir.addActionListener(e -> emitir());
        JPanel derecha2 = new JPanel(new BorderLayout(0, 10));
        derecha2.add(totales, BorderLayout.CENTER);
        JPanel emitir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        emitir.add(btnEmitir);
        derecha2.add(emitir, BorderLayout.SOUTH);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(opciones, BorderLayout.WEST);
        abajo.add(derecha2, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(arriba, BorderLayout.NORTH);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        centro.add(abajo, BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(new JLabel("Vendedor: " + sesion.getUsuario().getNombre()),
                new JLabel(LocalDateTime.now().format(Estilo.FECHA_HORA))), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    // Catálogo y clientes activos actuales (se vuelven a pedir después de cada venta)
    @SuppressWarnings("unchecked")
    private void cargarCatalogos() {
        Respuesta r = sesion.pedir(this, TipoOperacion.LISTAR_PRODUCTOS, null);
        if (r != null) {
            productos.clear();
            for (Producto p : (List<Producto>) r.getDatos()) {
                if (p.isActivo()) {
                    productos.put(p.getCodigo().toUpperCase(), p);
                }
            }
        }
        r = sesion.pedir(this, TipoOperacion.LISTAR_CLIENTES, null);
        if (r != null) {
            clientesActivos.clear();
            for (Cliente c : (List<Cliente>) r.getDatos()) {
                if (c.isActivo()) {
                    clientesActivos.put(c.getIdentificacion(), c);
                }
            }
        }
    }

    private void nuevaFactura() {
        cliente = null;
        factura = new Factura(null, sesion.getUsuario(), MetodoPago.EFECTIVO);
        txtCliente.setText("");
        lblCliente.setText("(sin cliente)");
        txtCodigo.setText("");
        txtCantidad.setText("1");
        lblDisponible.setText(" ");
        txtDescuento.setText("0");
        cmbPago.setSelectedItem(MetodoPago.EFECTIVO);
        refrescarTabla();
    }

    public boolean tieneLineasSinEmitir() {
        return factura != null && !factura.estaVacia();
    }

    // Si la identificación existe se elige directo; si no, se muestra la lista de clientes activos
    private void buscarCliente() {
        String texto = txtCliente.getText().trim();
        Cliente encontrado = clientesActivos.get(texto);
        if (encontrado == null) {
            List<Cliente> opciones = new ArrayList<>();
            for (Cliente c : clientesActivos.values()) {
                if (texto.isEmpty() || c.getNombre().toLowerCase().contains(texto.toLowerCase())
                        || c.getIdentificacion().contains(texto)) {
                    opciones.add(c);
                }
            }
            if (opciones.isEmpty()) {
                Estilo.error(this, "No se encontraron clientes activos con \"" + texto + "\".");
                return;
            }
            String[] nombres = new String[opciones.size()];
            for (int i = 0; i < nombres.length; i++) {
                nombres[i] = opciones.get(i).getIdentificacion() + " · " + opciones.get(i).getNombre();
            }
            Object elegido = JOptionPane.showInputDialog(this, "Seleccione el cliente:", "Buscar cliente",
                    JOptionPane.PLAIN_MESSAGE, null, nombres, nombres[0]);
            if (elegido == null) {
                return;
            }
            encontrado = opciones.get(java.util.Arrays.asList(nombres).indexOf(elegido));
        }
        seleccionarCliente(encontrado);
    }

    public void seleccionarCliente(Cliente c) {
        cliente = c;
        txtCliente.setText(c.getIdentificacion());
        lblCliente.setText(c.getNombre() + "  ·  " + c.getTelefono());
    }

    private void buscarProducto() {
        String codigo = txtCodigo.getText().trim().toUpperCase();
        Producto p = productos.get(codigo);
        if (p == null) {
            List<Producto> opciones = new ArrayList<>();
            for (Producto prod : productos.values()) {
                if (codigo.isEmpty() || prod.getCodigo().contains(codigo) || prod.getNombre().toUpperCase().contains(codigo)) {
                    opciones.add(prod);
                }
            }
            if (opciones.isEmpty()) {
                Estilo.error(this, "No existe un producto activo con \"" + txtCodigo.getText() + "\".");
                return;
            }
            Object elegido = JOptionPane.showInputDialog(this, "Seleccione el producto:", "Buscar producto",
                    JOptionPane.PLAIN_MESSAGE, null, opciones.toArray(), opciones.get(0));
            if (elegido == null) {
                return;
            }
            p = (Producto) elegido;
        }
        txtCodigo.setText(p.getCodigo());
        lblDisponible.setText("Disponible: " + p.getExistencias());
        txtCantidad.requestFocus();
        txtCantidad.selectAll();
    }

    private void agregarLinea() {
        Producto p = productos.get(txtCodigo.getText().trim().toUpperCase());
        if (p == null) {
            buscarProducto();
            return;
        }
        try {
            int cantidad = Estilo.leerEntero(txtCantidad.getText(), "Cantidad");
            // HU 8: si ya está en la factura se suma a la misma línea, sin pasar de las existencias
            factura.agregarDetalle(p, cantidad);
            txtCodigo.setText("");
            txtCantidad.setText("1");
            lblDisponible.setText(" ");
            refrescarTabla();
            txtCodigo.requestFocus();
        } catch (StockInsuficienteException | IllegalArgumentException e) {
            Estilo.error(this, e.getMessage());
        }
    }

    private void quitarLinea() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.error(this, "Seleccione la línea que desea quitar.");
            return;
        }
        factura.eliminarDetalle(fila);
        refrescarTabla();
    }

    private void refrescarTabla() {
        modelo.setRowCount(0);
        int n = 1;
        for (DetalleFactura d : factura.getDetalles()) {
            modelo.addRow(new Object[]{n++, d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                Estilo.colones(d.getPrecioUnitario()), Math.round(d.getPorcentajeIva() * 100) + "%",
                Estilo.colones(d.calcularTotal())});
        }
        recalcular();
    }

    private void recalcular() {
        double porcentaje;
        try {
            porcentaje = Estilo.leerNumero(txtDescuento.getText().isBlank() ? "0" : txtDescuento.getText(), "Descuento");
            factura.aplicarDescuento(porcentaje);
        } catch (IllegalArgumentException e) {
            porcentaje = factura.getPorcentajeDescuento();
        }
        lblTituloDescuento.setText("Descuento (" + (porcentaje == Math.floor(porcentaje) ? String.valueOf((int) porcentaje) : porcentaje) + " %)");
        lblSubtotal.setText(Estilo.colones(factura.calcularSubtotal()));
        lblDescuento.setText("-" + Estilo.colones(factura.calcularDescuento()));
        lblIva.setText(Estilo.colones(factura.calcularImpuesto()));
        lblTotal.setText(Estilo.colones(factura.calcularTotal()));
    }

    private void emitir() {
        if (cliente == null || factura.estaVacia()) {
            Estilo.error(this, "Debe seleccionar un cliente y agregar al menos un producto.");
            return;
        }
        double descuento;
        try {
            descuento = Estilo.leerNumero(txtDescuento.getText().isBlank() ? "0" : txtDescuento.getText(), "Descuento");
        } catch (IllegalArgumentException e) {
            Estilo.error(this, e.getMessage());
            return;
        }
        if (descuento < 0 || descuento > 100) {
            Estilo.error(this, "El descuento debe estar entre 0 y 100.");
            return;
        }
        Factura enviar = new Factura(cliente, sesion.getUsuario(), (MetodoPago) cmbPago.getSelectedItem());
        enviar.aplicarDescuento(descuento);
        for (DetalleFactura d : factura.getDetalles()) {
            enviar.cargarDetalle(d);
        }
        Respuesta r = sesion.enviar(TipoOperacion.CREAR_FACTURA, enviar);
        if (r == null) {
            return;
        }
        if (!r.isExito()) {
            // HU 9: la otra caja ganó las últimas unidades; la factura se conserva para corregirla
            Estilo.error(this, r.getMensaje());
            cargarCatalogos();
            return;
        }
        Factura emitida = (Factura) r.getDatos();
        nuevaFactura();
        cargarCatalogos();
        new DialogoVistaFactura(this, emitida).setVisible(true);
    }

    // Para la demostración automática: escribe código y cantidad y presiona "Agregar"
    public void agregar(String codigo, int cantidad) {
        txtCodigo.setText(codigo);
        txtCantidad.setText(String.valueOf(cantidad));
        agregarLinea();
    }

    public Map<String, Cliente> getClientesActivos() {
        return clientesActivos;
    }
}
