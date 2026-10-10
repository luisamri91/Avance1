package com.fidecompro.cliente;

import com.fidecompro.modelo.Rol;
import com.fidecompro.modelo.TipoIdentificacion;
import com.fidecompro.modelo.Usuario;
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
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Gestión de usuarios (HU 2, boceto P8). Solo la abre un ADMINISTRADOR.
 */
public class VentanaUsuarios extends JFrame {

    private final Sesion sesion;
    private final JTextField txtNombre = new JTextField(22);
    private final JComboBox<TipoIdentificacion> cmbTipo = new JComboBox<>(TipoIdentificacion.values());
    private final JTextField txtIdentificacion = new JTextField(14);
    private final JTextField txtTelefono = new JTextField(14);
    private final JTextField txtCorreo = new JTextField(22);
    private final JTextField txtUsuario = new JTextField(14);
    private final JPasswordField txtContrasena = new JPasswordField(14);
    private final JPasswordField txtConfirmar = new JPasswordField(14);
    private final JComboBox<Rol> cmbRol = new JComboBox<>(Rol.values());
    private final JCheckBox chkActivo = new JCheckBox("Activo (puede iniciar sesión)", true);
    private final JLabel lblClave = new JLabel(" ");
    private final DefaultTableModel modelo = Estilo.modeloTabla("ID", "Usuario", "Nombre", "Identificación", "Teléfono", "Rol", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblCantidad = new JLabel();
    private List<Usuario> usuarios;
    private Usuario seleccionado;

    public VentanaUsuarios(Sesion sesion) {
        super("Fidecompro · Gestión de usuarios");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        construir();
        setSize(960, 600);
        setLocationByPlatform(true);
        cargarTabla();
    }

    private void construir() {
        JPanel datos = Estilo.marco("Datos del usuario");
        Estilo.celda(datos, new JLabel("Nombre completo:"), 0, 0, 1, 0);
        Estilo.celda(datos, txtNombre, 1, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Rol:"), 2, 0, 1, 0);
        Estilo.celda(datos, cmbRol, 3, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Tipo de cédula:"), 0, 1, 1, 0);
        Estilo.celda(datos, cmbTipo, 1, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Identificación:"), 2, 1, 1, 0);
        Estilo.celda(datos, txtIdentificacion, 3, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Teléfono:"), 0, 2, 1, 0);
        Estilo.celda(datos, txtTelefono, 1, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Correo:"), 2, 2, 1, 0);
        Estilo.celda(datos, txtCorreo, 3, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Nombre de usuario:"), 0, 3, 1, 0);
        Estilo.celda(datos, txtUsuario, 1, 3, 1, 1);
        Estilo.celda(datos, chkActivo, 2, 3, 2, 1);
        Estilo.celda(datos, new JLabel("Contraseña:"), 0, 4, 1, 0);
        Estilo.celda(datos, txtContrasena, 1, 4, 1, 1);
        Estilo.celda(datos, new JLabel("Confirmar:"), 2, 4, 1, 0);
        Estilo.celda(datos, txtConfirmar, 3, 4, 1, 1);
        lblClave.setForeground(Estilo.GRIS);
        Estilo.celda(datos, lblClave, 0, 5, 4, 1);
        JButton btnNuevo = Estilo.boton("Nuevo");
        JButton btnGuardar = Estilo.botonPrincipal("Guardar");
        JButton btnLimpiar = Estilo.boton("Limpiar");
        JButton btnEliminar = Estilo.botonPeligro("Eliminar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnLimpiar);
        botones.add(btnEliminar);
        Estilo.celda(datos, botones, 0, 6, 4, 1);
        btnNuevo.addActionListener(e -> limpiar());
        btnLimpiar.addActionListener(e -> mostrar(seleccionado));
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                seleccionado = usuarios.get(fila);
                mostrar(seleccionado);
            }
        });
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(datos, BorderLayout.NORTH);
        centro.add(Estilo.scroll(tabla), BorderLayout.CENTER);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(lblCantidad, new JLabel("Usuario: " + sesion.getUsuario().getNombre())), BorderLayout.SOUTH);
        setContentPane(raiz);
        limpiar();
    }

    @SuppressWarnings("unchecked")
    private void cargarTabla() {
        Respuesta r = sesion.pedir(this, TipoOperacion.LISTAR_USUARIOS, null);
        if (r == null) {
            return;
        }
        usuarios = (List<Usuario>) r.getDatos();
        modelo.setRowCount(0);
        for (Usuario u : usuarios) {
            modelo.addRow(new Object[]{u.getId(), u.getNombreUsuario(), u.getNombre(), u.getIdentificacion(),
                u.getTelefono(), u.getRol(), u.isActivo() ? "Activo" : "Inactivo"});
        }
        lblCantidad.setText(usuarios.size() + " usuarios");
    }

    private void guardar() {
        String clave = new String(txtContrasena.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());
        boolean nuevo = seleccionado == null;
        // La contraseña se valida aquí contra la confirmación; el servidor revisa el largo
        if ((nuevo || !clave.isEmpty()) && (clave.length() < 8 || !clave.equals(confirmar))) {
            Estilo.error(this, "La contraseña debe tener al menos 8 caracteres y coincidir con la confirmación.");
            return;
        }
        Usuario u = new Usuario(txtNombre.getText().trim(), txtTelefono.getText().trim(), txtCorreo.getText().trim(),
                (TipoIdentificacion) cmbTipo.getSelectedItem(), txtIdentificacion.getText().trim(),
                txtUsuario.getText().trim(), clave, (Rol) cmbRol.getSelectedItem());
        u.setActivo(chkActivo.isSelected());
        if (!nuevo) {
            u.setId(seleccionado.getId());
        }
        Respuesta r = sesion.pedir(this, nuevo ? TipoOperacion.CREAR_USUARIO : TipoOperacion.ACTUALIZAR_USUARIO, u);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiar();
            cargarTabla();
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            Estilo.error(this, "Seleccione un usuario en la tabla.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Eliminar al usuario " + seleccionado.getNombreUsuario() + "?")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.ELIMINAR_USUARIO, seleccionado);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiar();
            cargarTabla();
        }
    }

    private void mostrar(Usuario u) {
        if (u == null) {
            limpiar();
            return;
        }
        txtNombre.setText(u.getNombre());
        cmbTipo.setSelectedItem(u.getTipoIdentificacion());
        txtIdentificacion.setText(u.getIdentificacion());
        txtTelefono.setText(u.getTelefono());
        txtCorreo.setText(u.getCorreo());
        txtUsuario.setText(u.getNombreUsuario());
        cmbRol.setSelectedItem(u.getRol());
        chkActivo.setSelected(u.isActivo());
        txtContrasena.setText("");
        txtConfirmar.setText("");
        lblClave.setText("Deje la contraseña vacía para conservar la actual.");
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtNombre, txtIdentificacion, txtTelefono, txtCorreo, txtUsuario, txtContrasena, txtConfirmar}) {
            t.setText("");
        }
        cmbTipo.setSelectedIndex(0);
        cmbRol.setSelectedItem(Rol.VENDEDOR);
        chkActivo.setSelected(true);
        lblClave.setText("La contraseña debe tener al menos 8 caracteres.");
    }
}
