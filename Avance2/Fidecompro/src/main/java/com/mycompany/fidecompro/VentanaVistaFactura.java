package com.mycompany.fidecompro;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// muestra la factura emitida y permite guardarla en un archivo .txt (HU 10)
public class VentanaVistaFactura extends JDialog {

    private Factura factura;

    public VentanaVistaFactura(JFrame padre, Factura factura) {
        super(padre, "Factura " + factura.getNumeroFormateado(), true);
        this.factura = factura;
        setSize(600, 520);
        setLocationRelativeTo(padre);
        setLayout(null);
        setResizable(false);

        JTextArea area = new JTextArea(factura.mostrarInformacion());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBounds(10, 10, 565, 410);
        add(scroll);

        JButton btnGuardar = new JButton("Guardar archivo (.txt)");
        btnGuardar.setBounds(300, 435, 175, 30);
        add(btnGuardar);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(485, 435, 90, 30);
        add(btnCerrar);

        btnGuardar.addActionListener(e -> guardarArchivo());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void guardarArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Seleccione la carpeta donde guardar la factura");
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File carpeta = selector.getSelectedFile();
        try {
            String ruta = factura.generarArchivo(carpeta.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Archivo guardado en " + ruta + ".");
        } catch (IOException ex) {
            // la factura sigue emitida, se puede intentar en otra carpeta
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo de la factura.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
