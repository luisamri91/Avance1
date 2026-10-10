package com.fidecompro.cliente;

import com.fidecompro.modelo.Producto;
import com.fidecompro.red.AjusteStock;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Diálogo "Ajustar stock…" (HU 6): entradas de mercadería, mermas y correcciones.
 */
public class DialogoAjusteStock extends JDialog {

    // El motivo decide si la cantidad suma o resta; "Corrección" respeta el signo que se escriba
    private static final String[] MOTIVOS = {"(elija un motivo)", "Compra (entrada)", "Devolución de cliente (entrada)",
        "Merma (salida)", "Producto vencido (salida)", "Corrección de inventario (+/-)"};

    private final Sesion sesion;
    private final Producto producto;
    private final JComboBox<String> cmbMotivo = new JComboBox<>(MOTIVOS);
    private final JTextField txtCantidad = new JTextField(8);
    private final JLabel lblResultado = new JLabel(" ");
    private boolean aplicado;

    public DialogoAjusteStock(Frame padre, Sesion sesion, Producto producto) {
        super(padre, "Ajustar stock", true);
        this.sesion = sesion;
        this.producto = producto;
        JPanel form = Estilo.marco(producto.getCodigo() + " · " + producto.getNombre());
        Estilo.celda(form, new JLabel("Existencias actuales:"), 0, 0, 1, 0);
        JLabel actuales = new JLabel(producto.getExistencias() + " unidades (mínimo " + producto.getStockMinimo() + ")");
        actuales.setFont(actuales.getFont().deriveFont(Font.BOLD));
        Estilo.celda(form, actuales, 1, 0, 1, 1);
        Estilo.celda(form, new JLabel("Motivo:"), 0, 1, 1, 0);
        Estilo.celda(form, cmbMotivo, 1, 1, 1, 1);
        Estilo.celda(form, new JLabel("Cantidad:"), 0, 2, 1, 0);
        Estilo.celda(form, txtCantidad, 1, 2, 1, 0);
        lblResultado.setForeground(Estilo.GRIS);
        Estilo.celda(form, lblResultado, 0, 3, 2, 1);

        JButton btnAplicar = Estilo.botonPrincipal("Aplicar");
        JButton btnCancelar = Estilo.boton("Cancelar");
        btnAplicar.addActionListener(e -> aplicar());
        btnCancelar.addActionListener(e -> dispose());
        cmbMotivo.addActionListener(e -> previsualizar());
        txtCantidad.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                previsualizar();
            }

            public void removeUpdate(DocumentEvent e) {
                previsualizar();
            }

            public void changedUpdate(DocumentEvent e) {
                previsualizar();
            }
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnAplicar);
        JPanel raiz = new JPanel(new BorderLayout(0, 8));
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        raiz.add(form, BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);
        getRootPane().setDefaultButton(btnAplicar);
        pack();
        setLocationRelativeTo(padre);
    }

    // Cantidad con signo según el motivo: entradas suman y salidas restan
    private int cantidadConSigno() {
        int cantidad = Estilo.leerEntero(txtCantidad.getText(), "Cantidad");
        String motivo = (String) cmbMotivo.getSelectedItem();
        if (motivo.contains("(salida)")) {
            return -Math.abs(cantidad);
        } else if (motivo.contains("(entrada)")) {
            return Math.abs(cantidad);
        }
        return cantidad;
    }

    private void previsualizar() {
        try {
            int nuevas = producto.getExistencias() + cantidadConSigno();
            lblResultado.setText("Quedarán " + nuevas + " unidades.");
        } catch (IllegalArgumentException e) {
            lblResultado.setText(" ");
        }
    }

    private void aplicar() {
        String motivo = cmbMotivo.getSelectedIndex() == 0 ? "" : (String) cmbMotivo.getSelectedItem();
        int cantidad;
        try {
            cantidad = cantidadConSigno();
        } catch (IllegalArgumentException e) {
            Estilo.error(this, e.getMessage());
            return;
        }
        if (motivo.isEmpty()) {
            Estilo.error(this, "Debe indicar el motivo del ajuste.");
            return;
        }
        if (producto.getExistencias() + cantidad < 0) {
            Estilo.error(this, "El ajuste dejaría las existencias en negativo.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Aplicar el ajuste de " + (cantidad > 0 ? "+" : "") + cantidad + " unidades?")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.AJUSTAR_STOCK, new AjusteStock(producto.getId(), cantidad, motivo));
        if (r != null) {
            aplicado = true;
            Estilo.info(this, r.getMensaje());
            dispose();
        }
    }

    public boolean isAplicado() {
        return aplicado;
    }
}
