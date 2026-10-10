package com.fidecompro.cliente;

import com.fidecompro.modelo.Factura;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Vista previa de la factura (HU 10, boceto P6) con el botón para guardarla en un archivo .txt.
 */
public class DialogoVistaFactura extends JDialog {

    private final Factura factura;

    public DialogoVistaFactura(Window padre, Factura factura) {
        super(padre, "Factura " + factura.getNumeroFormateado(), ModalityType.APPLICATION_MODAL);
        this.factura = factura;
        JTextArea texto = new JTextArea(factura.mostrarInformacion());
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        texto.setEditable(false);
        texto.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JButton btnGuardar = Estilo.botonPrincipal("Guardar archivo (.txt)");
        JButton btnCerrar = Estilo.boton("Cerrar");
        btnGuardar.addActionListener(e -> guardar());
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnGuardar);
        botones.add(btnCerrar);
        JPanel raiz = new JPanel(new BorderLayout(0, 6));
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        raiz.add(new JScrollPane(texto), BorderLayout.CENTER);
        raiz.add(botones, BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(padre);
    }

    private void guardar() {
        JFileChooser selector = new JFileChooser(new File("facturas").getAbsoluteFile());
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setDialogTitle("Carpeta donde guardar la factura");
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        guardarEn(selector.getSelectedFile());
    }

    public void guardarEn(File carpeta) {
        String ruta = factura.generarArchivo(carpeta.getPath());
        if (ruta == null) {
            Estilo.error(this, "No se pudo guardar el archivo de la factura.");
        } else {
            Estilo.info(this, "Archivo guardado en " + new File(ruta).getAbsolutePath() + ".");
        }
    }
}
