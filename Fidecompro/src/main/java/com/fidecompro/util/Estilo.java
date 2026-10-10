package com.fidecompro.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Colores, formatos y pequeñas ayudas para que todas las ventanas se vean igual.
 */
public class Estilo {

    public static final Color AZUL = new Color(0x2F, 0x5D, 0x8F);
    public static final Color AZUL_OSCURO = new Color(0x2A, 0x45, 0x66);
    public static final Color ROJO = new Color(0xC0, 0x39, 0x2B);
    public static final Color VERDE = new Color(0x1E, 0x84, 0x49);
    public static final Color FONDO = new Color(0xF4, 0xF6, 0xFA);
    public static final Color BARRA = new Color(0xDD, 0xE4, 0xEE);
    public static final Color GRIS = new Color(0x5F, 0x6B, 0x7A);

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final DecimalFormat MONEDA;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator(' ');
        simbolos.setDecimalSeparator(',');
        MONEDA = new DecimalFormat("#,##0.00", simbolos);
    }

    public static void aplicarLookAndFeel() {
        // Botones de los diálogos (Sí, No, Aceptar…) y el selector de archivos en español
        java.util.Locale.setDefault(new java.util.Locale("es", "CR"));
        javax.swing.JComponent.setDefaultLocale(java.util.Locale.getDefault());
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo aplicar Nimbus: " + e.getMessage());
        }
        UIManager.put("control", FONDO);
        UIManager.put("nimbusBase", AZUL_OSCURO);
        UIManager.put("nimbusBlueGrey", new Color(0xC9, 0xD3, 0xE0));
        UIManager.put("nimbusSelectionBackground", new Color(0xC5, 0xD9, 0xF0));
        UIManager.put("nimbusFocus", new Color(0x7F, 0xA7, 0xD6));
        UIManager.put("Table.selectionForeground", Color.BLACK);
        Font base = new Font("SansSerif", Font.PLAIN, 13);
        UIManager.put("defaultFont", base);
        UIManager.put("Table.rowHeight", 24);
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
    }

    // ---------- Formatos ----------
    public static String colones(double monto) {
        return "₡" + MONEDA.format(monto);
    }

    public static double leerNumero(String texto, String campo) {
        try {
            return Double.parseDouble(texto.trim().replace(" ", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número.");
        }
    }

    public static int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero.");
        }
    }

    public static LocalDate leerFecha(String texto, String campo) {
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe tener el formato dd/mm/aaaa.");
        }
    }

    // ---------- Componentes ----------
    public static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    public static JButton botonPrincipal(String texto) {
        return botonDeColor(texto, AZUL);
    }

    public static JButton botonPeligro(String texto) {
        return botonDeColor(texto, ROJO);
    }

    // Texto blanco sobre color; si el botón se deshabilita se ve gris para que se lea
    private static JButton botonDeColor(String texto, Color color) {
        JButton b = boton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.addPropertyChangeListener("enabled", e -> {
            b.setBackground(b.isEnabled() ? color : BARRA);
            b.setForeground(b.isEnabled() ? Color.WHITE : GRIS);
        });
        return b;
    }

    public static JPanel marco(String titulo) {
        JPanel p = new JPanel(new java.awt.GridBagLayout());
        TitledBorder borde = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xB8, 0xC4, 0xD4), 1, true), titulo);
        borde.setTitleColor(AZUL_OSCURO);
        borde.setTitleFont(new Font("SansSerif", Font.BOLD, 14));
        p.setBorder(BorderFactory.createCompoundBorder(borde, BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        return p;
    }

    // Agrega un componente en la celda (x, y) de un GridBagLayout
    public static void celda(JPanel panel, Component c, int x, int y, int ancho, double peso) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = ancho;
        g.weightx = peso;
        g.fill = peso > 0 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 6, 4, 6);
        panel.add(c, g);
    }

    public static JPanel barraEstado(JLabel izquierda, JLabel derecha) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(BARRA);
        barra.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        izquierda.setForeground(GRIS);
        derecha.setForeground(GRIS);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    // Tabla de solo lectura
    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JScrollPane scroll(JTable tabla) {
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionForeground(Color.BLACK);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(tabla);
    }

    // ---------- Mensajes ----------
    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Fidecompro", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Fidecompro", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
