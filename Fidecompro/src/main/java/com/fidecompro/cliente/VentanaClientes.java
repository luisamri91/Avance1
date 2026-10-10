package com.fidecompro.cliente;

import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.TipoIdentificacion;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Gestión de clientes (HU 3, boceto P3): registrar, buscar, modificar y eliminar.
 */
public class VentanaClientes extends JFrame {

    private final Sesion sesion;
    private final JComboBox<TipoIdentificacion> cmbTipo = new JComboBox<>(TipoIdentificacion.values());
    private final JTextField txtIdentificacion = new JTextField(16);
    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtTelefono = new JTextField(16);
    private final JTextField txtCorreo = new JTextField(22);
    private final JTextField txtDireccion = new JTextField(22);
    private final JCheckBox chkActivo = new JCheckBox("(desmarcar para ocultarlo al facturar)", true);
    private final JTextField txtBuscar = new JTextField(24);
    private final JButton btnNuevo = Estilo.boton("Nuevo");
    private final JButton btnGuardar = Estilo.botonPrincipal("Guardar");
    private final JButton btnLimpiar = Estilo.boton("Limpiar");
    private final JButton btnEliminar = Estilo.botonPeligro("Eliminar");
    private final JButton btnActualizar = Estilo.boton("Actualizar lista");
    private final JButton btnBuscar = Estilo.boton("Buscar");
    private final DefaultTableModel modelo = Estilo.modeloTabla("Identificación", "Nombre", "Teléfono", "Correo", "Registro", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblCantidad = new JLabel();

    private List<Cliente> clientes;
    private Cliente seleccionado;

    public VentanaClientes(Sesion sesion) {
        super("Fidecompro · Gestión de clientes");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        construir();
        btnNuevo.addActionListener(e -> limpiar());
        btnLimpiar.addActionListener(e -> mostrar(seleccionado));
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla();
        });
        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                seleccionado = clientes.get(fila);
                mostrar(seleccionado);
            }
        });
        tabla.getColumnModel().getColumn(1).setPreferredWidth(230);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(190);
        // HU 3: para un VENDEDOR el botón Eliminar está deshabilitado
        btnEliminar.setEnabled(sesion.esAdministrador());
        setSize(940, 600);
        setLocationByPlatform(true);
        cargarTabla();
    }

    private void construir() {
        JPanel datos = Estilo.marco("Datos del cliente");
        Estilo.celda(datos, new JLabel("Tipo de identificación:"), 0, 0, 1, 0);
        Estilo.celda(datos, cmbTipo, 1, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Identificación:"), 2, 0, 1, 0);
        Estilo.celda(datos, txtIdentificacion, 3, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Nombre / Razón social:"), 0, 1, 1, 0);
        Estilo.celda(datos, txtNombre, 1, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Teléfono:"), 2, 1, 1, 0);
        Estilo.celda(datos, txtTelefono, 3, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Correo:"), 0, 2, 1, 0);
        Estilo.celda(datos, txtCorreo, 1, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Dirección:"), 2, 2, 1, 0);
        Estilo.celda(datos, txtDireccion, 3, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Activo:"), 0, 3, 1, 0);
        Estilo.celda(datos, chkActivo, 1, 3, 3, 1);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnLimpiar);
        botones.add(btnEliminar);
        botones.add(btnActualizar);
        Estilo.celda(datos, botones, 0, 4, 4, 1);

        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscar.add(new JLabel("Buscar:"));
        buscar.add(txtBuscar);
        buscar.add(btnBuscar);
        buscar.add(new JLabel("(parte del nombre o de la identificación)"));

        JPanel lista = new JPanel(new BorderLayout());
        lista.add(buscar, BorderLayout.NORTH);
        lista.add(Estilo.scroll(tabla), BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(datos, BorderLayout.NORTH);
        centro.add(lista, BorderLayout.CENTER);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(lblCantidad, new JLabel("Usuario: " + sesion.getUsuario().getNombre())), BorderLayout.SOUTH);
        setContentPane(raiz);
    }

    @SuppressWarnings("unchecked")
    private void cargarTabla() {
        Respuesta r = sesion.pedir(this, TipoOperacion.LISTAR_CLIENTES, txtBuscar.getText());
        if (r == null) {
            return;
        }
        clientes = (List<Cliente>) r.getDatos();
        modelo.setRowCount(0);
        for (Cliente c : clientes) {
            modelo.addRow(new Object[]{c.getIdentificacion(), c.getNombre(), c.getTelefono(), c.getCorreo(),
                c.getFechaRegistro().format(Estilo.FECHA), c.isActivo() ? "Activo" : "Inactivo"});
        }
        lblCantidad.setText(clientes.isEmpty() ? "No se encontraron clientes." : clientes.size() + " clientes encontrados");
    }

    private void guardar() {
        boolean nuevo = seleccionado == null;
        Cliente c = new Cliente(txtNombre.getText().trim(), txtTelefono.getText().trim(), txtCorreo.getText().trim(),
                (TipoIdentificacion) cmbTipo.getSelectedItem(), txtIdentificacion.getText().trim(), txtDireccion.getText().trim());
        c.setActivo(chkActivo.isSelected());
        if (!nuevo) {
            c.setId(seleccionado.getId());
            c.setFechaRegistro(seleccionado.getFechaRegistro());
        }
        Respuesta r = sesion.pedir(this, nuevo ? TipoOperacion.CREAR_CLIENTE : TipoOperacion.ACTUALIZAR_CLIENTE, c);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiar();
            cargarTabla();
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            Estilo.error(this, "Seleccione un cliente en la tabla.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Eliminar al cliente " + seleccionado.getNombre() + "?")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.ELIMINAR_CLIENTE, seleccionado);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiar();
            cargarTabla();
        }
    }

    private void mostrar(Cliente c) {
        if (c == null) {
            limpiar();
            return;
        }
        cmbTipo.setSelectedItem(c.getTipoIdentificacion());
        txtIdentificacion.setText(c.getIdentificacion());
        txtNombre.setText(c.getNombre());
        txtTelefono.setText(c.getTelefono());
        txtCorreo.setText(c.getCorreo());
        txtDireccion.setText(c.getDireccion());
        chkActivo.setSelected(c.isActivo());
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        cmbTipo.setSelectedIndex(0);
        txtIdentificacion.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        chkActivo.setSelected(true);
    }
}
