package com.mycompany.fidecompro;

import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

// registrar, buscar, modificar y eliminar clientes (HU 3)
public class VentanaClientes extends JFrame {

    private Usuario usuario;
    private Cliente seleccionado;
    private List<Cliente> listaActual;

    private JComboBox<TipoIdentificacion> cmbTipo;
    private JTextField txtIdentificacion;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JCheckBox chkActivo;
    private JTextField txtBuscar;
    private DefaultTableModel modelo;
    private JTable tabla;
    private JLabel lblEstado;

    public VentanaClientes(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Fidecompro · Gestión de clientes");
        setSize(940, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        iniciarComponentes();
        cargarTabla(Datos.clientes.listar());
    }

    private void iniciarComponentes() {
        JPanel datos = new JPanel(null);
        datos.setBorder(BorderFactory.createTitledBorder("Datos del cliente"));
        datos.setBounds(12, 10, 910, 220);
        add(datos);

        datos.add(etiqueta("Tipo de identificación:", 20, 30));
        cmbTipo = new JComboBox<>(TipoIdentificacion.values());
        cmbTipo.setBounds(170, 30, 310, 25);
        datos.add(cmbTipo);
        datos.add(etiqueta("Identificación:", 495, 30));
        txtIdentificacion = campo(590, 30, datos);

        datos.add(etiqueta("Nombre / Razón social:", 20, 65));
        txtNombre = campo(170, 65, datos);
        datos.add(etiqueta("Teléfono:", 495, 65));
        txtTelefono = campo(590, 65, datos);

        datos.add(etiqueta("Correo:", 20, 100));
        txtCorreo = campo(170, 100, datos);
        datos.add(etiqueta("Dirección:", 495, 100));
        txtDireccion = campo(590, 100, datos);

        datos.add(etiqueta("Activo:", 20, 135));
        chkActivo = new JCheckBox("(desmarcar para ocultarlo al facturar)", true);
        chkActivo.setBounds(165, 135, 300, 25);
        datos.add(chkActivo);

        JButton btnNuevo = boton("Nuevo", 25, 175, datos);
        JButton btnGuardar = boton("Guardar", 135, 175, datos);
        JButton btnLimpiar = boton("Limpiar", 245, 175, datos);
        JButton btnEliminar = boton("Eliminar", 355, 175, datos);
        btnEliminar.setBackground(new Color(230, 80, 70));
        btnEliminar.setForeground(Color.WHITE);
        JButton btnActualizar = boton("Actualizar lista", 465, 175, datos);
        btnActualizar.setSize(140, 28);
        // solo el administrador puede eliminar
        btnEliminar.setEnabled(usuario.esAdministrador());

        add(etiqueta("Buscar:", 15, 240));
        txtBuscar = new JTextField();
        txtBuscar.setBounds(70, 240, 270, 25);
        add(txtBuscar);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBounds(350, 240, 90, 25);
        add(btnBuscar);
        JLabel ayuda = new JLabel("(parte del nombre o de la identificación)");
        ayuda.setBounds(450, 240, 300, 25);
        add(ayuda);

        modelo = new DefaultTableModel(new String[]{"Identificación", "Nombre", "Teléfono", "Correo", "Registro", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(12, 275, 910, 270);
        add(scroll);

        lblEstado = new JLabel();
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(225, 230, 240));
        lblEstado.setBounds(0, 555, 940, 25);
        add(lblEstado);

        btnNuevo.addActionListener(e -> limpiar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla(Datos.clientes.listar());
        });
        btnBuscar.addActionListener(e -> buscar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarFormulario(listaActual.get(tabla.getSelectedRow()));
            }
        });
    }

    private JLabel etiqueta(String texto, int x, int y) {
        JLabel l = new JLabel(texto);
        l.setBounds(x, y, 150, 25);
        return l;
    }

    private JTextField campo(int x, int y, JPanel panel) {
        JTextField t = new JTextField();
        t.setBounds(x, y, 300, 25);
        panel.add(t);
        return t;
    }

    private JButton boton(String texto, int x, int y, JPanel panel) {
        JButton b = new JButton(texto);
        b.setBounds(x, y, 100, 28);
        panel.add(b);
        return b;
    }

    private void cargarTabla(List<Cliente> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Cliente c : lista) {
            modelo.addRow(new Object[]{c.getIdentificacion(), c.getNombre(), c.getTelefono(), c.getCorreo(),
                c.getFechaRegistro().format(f), c.isActivo() ? "Activo" : "Inactivo"});
        }
        lblEstado.setText(" " + lista.size() + " clientes encontrados");
    }

    private void buscar() {
        List<Cliente> lista = Datos.clientes.buscar(txtBuscar.getText().trim());
        cargarTabla(lista);
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron clientes.");
        }
    }

    private void cargarFormulario(Cliente c) {
        seleccionado = c;
        cmbTipo.setSelectedItem(c.getTipoIdentificacion());
        txtIdentificacion.setText(c.getIdentificacion());
        txtNombre.setText(c.getNombre());
        txtTelefono.setText(c.getTelefono());
        txtCorreo.setText(c.getCorreo());
        txtDireccion.setText(c.getDireccion());
        chkActivo.setSelected(c.isActivo());
        // la identificacion no se cambia al editar
        cmbTipo.setEnabled(false);
        txtIdentificacion.setEditable(false);
    }

    private void limpiar() {
        seleccionado = null;
        cmbTipo.setEnabled(true);
        txtIdentificacion.setEditable(true);
        cmbTipo.setSelectedIndex(0);
        for (JTextField t : new JTextField[]{txtIdentificacion, txtNombre, txtTelefono, txtCorreo, txtDireccion}) {
            t.setText("");
            t.setBackground(Color.WHITE);
        }
        chkActivo.setSelected(true);
        tabla.clearSelection();
    }

    // valida los datos, si algo esta mal lanza la excepcion con el mensaje
    private void validar() {
        txtIdentificacion.setBackground(Color.WHITE);
        txtNombre.setBackground(Color.WHITE);
        txtCorreo.setBackground(Color.WHITE);
        Color rojo = new Color(255, 220, 220);

        if (txtIdentificacion.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty()) {
            if (txtIdentificacion.getText().trim().isEmpty()) {
                txtIdentificacion.setBackground(rojo);
            }
            if (txtNombre.getText().trim().isEmpty()) {
                txtNombre.setBackground(rojo);
            }
            throw new IllegalArgumentException("Debe completar los campos obligatorios.");
        }
        // fisica X-XXXX-XXXX y juridica X-XXX-XXXXXX
        String formato = cmbTipo.getSelectedItem() == TipoIdentificacion.FISICA ? "\\d-\\d{4}-\\d{4}" : "\\d-\\d{3}-\\d{6}";
        if (!txtIdentificacion.getText().trim().matches(formato)) {
            txtIdentificacion.setBackground(rojo);
            throw new IllegalArgumentException("Formato de identificación inválido (física X-XXXX-XXXX, jurídica X-XXX-XXXXXX).");
        }
        String correo = txtCorreo.getText().trim();
        if (!correo.isEmpty() && !correo.matches("[\\w.+-]+@[\\w-]+\\.[\\w.]+")) {
            txtCorreo.setBackground(rojo);
            throw new IllegalArgumentException("Formato de correo electrónico inválido.");
        }
    }

    private void guardar() {
        try {
            validar();
            if (seleccionado == null) {
                Cliente nuevo = new Cliente((TipoIdentificacion) cmbTipo.getSelectedItem(), txtIdentificacion.getText().trim(),
                        txtNombre.getText().trim(), txtTelefono.getText().trim(), txtCorreo.getText().trim(), txtDireccion.getText().trim());
                nuevo.setActivo(chkActivo.isSelected());
                Datos.clientes.agregar(nuevo);
                JOptionPane.showMessageDialog(this, "Cliente registrado correctamente.");
            } else {
                seleccionado.setNombre(txtNombre.getText().trim());
                seleccionado.setTelefono(txtTelefono.getText().trim());
                seleccionado.setCorreo(txtCorreo.getText().trim());
                seleccionado.setDireccion(txtDireccion.getText().trim());
                seleccionado.setActivo(chkActivo.isSelected());
                JOptionPane.showMessageDialog(this, "Cliente actualizado correctamente.");
            }
            Datos.guardarCambios();
            limpiar();
            cargarTabla(Datos.clientes.listar());
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().startsWith("Ya existe")) {
                txtIdentificacion.setBackground(new Color(255, 220, 220));
            }
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Desea eliminar a " + seleccionado.getNombre() + "?",
                "Eliminar cliente", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        // si ya tiene facturas no se borra, se desactiva para no perder el historial
        if (Datos.facturas.clienteTieneFacturas(seleccionado)) {
            seleccionado.setActivo(false);
            JOptionPane.showMessageDialog(this, "El cliente tiene facturas, se desactivó para conservar el historial.");
        } else {
            Datos.clientes.eliminar(seleccionado);
            JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
        }
        Datos.guardarCambios();
        limpiar();
        cargarTabla(Datos.clientes.listar());
    }
}
