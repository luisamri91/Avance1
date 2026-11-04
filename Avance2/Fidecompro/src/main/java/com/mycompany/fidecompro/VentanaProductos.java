package com.mycompany.fidecompro;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// registro de productos de los tres tipos (HU 5) y pestaña de los que estan bajo el minimo (HU 7)
public class VentanaProductos extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Usuario usuario;
    private Producto seleccionado;
    private List<Producto> listaActual;

    private JTabbedPane pestanas;
    private JComboBox<String> cmbTipo;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtCodigo, txtNombre, txtCompra, txtVenta, txtExistencias, txtMinimo;
    private JLabel lblIva;
    private JPanel panelEspecifico;
    // abarrote
    private JTextField txtVencimiento;
    private JCheckBox chkCanasta;
    // bebida
    private JTextField txtVolumen;
    private JCheckBox chkRetornable;
    // articulo de hogar
    private JTextField txtMarca, txtPresentacion;

    private JComboBox<Object> cmbFiltro;
    private JTextField txtBuscar;
    private DefaultTableModel modelo;
    private JTable tabla;
    private DefaultTableModel modeloBajos;
    private JLabel lblEstado;

    public VentanaProductos(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Fidecompro · Productos e inventario");
        setSize(1060, 770);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        iniciarComponentes();
        cambiarTipo();
        cargarTabla();
    }

    private void iniciarComponentes() {
        pestanas = new JTabbedPane();
        pestanas.setBounds(10, 10, 1035, 695);
        add(pestanas);

        JPanel pProductos = new JPanel(null);
        pestanas.addTab("Productos", pProductos);
        JPanel pBajos = new JPanel(null);
        pestanas.addTab("Bajo el mínimo", pBajos);

        // ---- formulario
        JPanel datos = new JPanel(null);
        datos.setBorder(BorderFactory.createTitledBorder("Datos del producto"));
        datos.setBounds(5, 5, 1015, 345);
        pProductos.add(datos);

        datos.add(etiqueta("Tipo de producto:", 15, 25));
        cmbTipo = new JComboBox<>(new String[]{"Abarrote", "Bebida", "Artículo de hogar"});
        cmbTipo.setBounds(130, 25, 380, 25);
        datos.add(cmbTipo);
        datos.add(etiqueta("Categoría:", 525, 25));
        cmbCategoria = new JComboBox<>();
        for (Categoria c : Datos.inventario.getCategorias()) {
            cmbCategoria.addItem(c);
        }
        cmbCategoria.setBounds(630, 25, 370, 25);
        datos.add(cmbCategoria);

        datos.add(etiqueta("Código:", 15, 60));
        txtCodigo = campo(130, 60, 380, datos);
        datos.add(etiqueta("Nombre:", 525, 60));
        txtNombre = campo(630, 60, 370, datos);

        datos.add(etiqueta("Valor compra (₡):", 15, 95));
        txtCompra = campo(130, 95, 380, datos);
        datos.add(etiqueta("Valor venta (₡):", 525, 95));
        txtVenta = campo(630, 95, 370, datos);

        datos.add(etiqueta("Existencias:", 15, 130));
        txtExistencias = campo(130, 130, 75, datos);
        datos.add(etiqueta("Stock mínimo:", 525, 130));
        txtMinimo = campo(630, 130, 75, datos);

        lblIva = new JLabel();
        lblIva.setForeground(new Color(40, 80, 140));
        lblIva.setBounds(525, 165, 400, 25);
        datos.add(lblIva);

        panelEspecifico = new JPanel(null);
        panelEspecifico.setBounds(20, 195, 975, 85);
        datos.add(panelEspecifico);

        txtVencimiento = new JTextField(LocalDate.now().plusMonths(6).format(FORMATO_FECHA));
        chkCanasta = new JCheckBox("Canasta básica (IVA 1 %)");
        txtVolumen = new JTextField();
        chkRetornable = new JCheckBox("Retornable");
        txtMarca = new JTextField();
        txtPresentacion = new JTextField();
        chkCanasta.addActionListener(e -> actualizarIva());

        JButton btnNuevo = boton("Nuevo", 25, 295, datos);
        JButton btnGuardar = boton("Guardar", 135, 295, datos);
        JButton btnEliminar = boton("Eliminar", 245, 295, datos);
        btnEliminar.setBackground(new Color(230, 80, 70));
        btnEliminar.setForeground(Color.WHITE);
        JButton btnActualizar = boton("Actualizar lista", 355, 295, datos);
        btnActualizar.setSize(140, 28);
        // registrar y eliminar productos es del administrador
        btnGuardar.setEnabled(usuario.esAdministrador());
        btnEliminar.setEnabled(usuario.esAdministrador());

        // ---- filtro y tabla
        pProductos.add(etiqueta("Filtrar por categoría:", 10, 360));
        cmbFiltro = new JComboBox<>();
        cmbFiltro.addItem("Todas");
        for (Categoria c : Datos.inventario.getCategorias()) {
            cmbFiltro.addItem(c);
        }
        cmbFiltro.setBounds(145, 360, 120, 25);
        pProductos.add(cmbFiltro);
        pProductos.add(etiqueta("Código o nombre:", 280, 360));
        txtBuscar = campo(395, 360, 200, pProductos);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBounds(605, 360, 90, 25);
        pProductos.add(btnBuscar);

        modelo = new DefaultTableModel(new String[]{"Código", "Nombre", "Tipo", "Categoría", "Precio", "IVA", "Stock", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(7).setCellRenderer(new RenderEstado());
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(5, 395, 1015, 265);
        pProductos.add(scroll);

        // ---- pestaña bajo el minimo
        JLabel lblBajos = new JLabel("Productos con existencias menores o iguales al mínimo, de menos a más existencias.");
        lblBajos.setBounds(10, 10, 700, 25);
        pBajos.add(lblBajos);
        modeloBajos = new DefaultTableModel(new String[]{"Código", "Nombre", "Categoría", "Stock", "Mínimo"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        JTable tablaBajos = new JTable(modeloBajos);
        tablaBajos.setForeground(new Color(190, 40, 40));
        JScrollPane scrollBajos = new JScrollPane(tablaBajos);
        scrollBajos.setBounds(5, 40, 1015, 615);
        pBajos.add(scrollBajos);

        lblEstado = new JLabel();
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(225, 230, 240));
        lblEstado.setBounds(0, 712, 1060, 25);
        add(lblEstado);

        cmbTipo.addActionListener(e -> cambiarTipo());
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> {
            cmbFiltro.setSelectedIndex(0);
            txtBuscar.setText("");
            cargarTabla();
        });
        btnBuscar.addActionListener(e -> cargarTabla());
        cmbFiltro.addActionListener(e -> cargarTabla());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarFormulario(listaActual.get(tabla.getSelectedRow()));
            }
        });
    }

    private JLabel etiqueta(String texto, int x, int y) {
        JLabel l = new JLabel(texto);
        l.setBounds(x, y, 140, 25);
        return l;
    }

    private JTextField campo(int x, int y, int ancho, JPanel panel) {
        JTextField t = new JTextField();
        t.setBounds(x, y, ancho, 25);
        panel.add(t);
        return t;
    }

    private JButton boton(String texto, int x, int y, JPanel panel) {
        JButton b = new JButton(texto);
        b.setBounds(x, y, 100, 28);
        panel.add(b);
        return b;
    }

    // cambia el recuadro de datos segun el tipo elegido
    private void cambiarTipo() {
        panelEspecifico.removeAll();
        String tipo = (String) cmbTipo.getSelectedItem();
        panelEspecifico.setBorder(BorderFactory.createTitledBorder("Datos de " + tipo.toLowerCase()));
        if (tipo.equals("Abarrote")) {
            agregarEspecifico(etiqueta("Fecha vencimiento:", 15, 35), null, 0);
            agregarEspecifico(txtVencimiento, 140, 110);
            agregarEspecifico(chkCanasta, 280, 220);
        } else if (tipo.equals("Bebida")) {
            agregarEspecifico(etiqueta("Volumen (ml):", 15, 35), null, 0);
            agregarEspecifico(txtVolumen, 140, 110);
            agregarEspecifico(chkRetornable, 280, 150);
        } else {
            agregarEspecifico(etiqueta("Marca:", 15, 35), null, 0);
            agregarEspecifico(txtMarca, 140, 200);
            agregarEspecifico(etiqueta("Presentación:", 370, 35), null, 0);
            agregarEspecifico(txtPresentacion, 470, 200);
        }
        actualizarIva();
        panelEspecifico.revalidate();
        panelEspecifico.repaint();
    }

    private void agregarEspecifico(Component c, Integer x, int ancho) {
        if (x != null) {
            c.setBounds(x, 35, ancho, 25);
        }
        panelEspecifico.add(c);
    }

    private void actualizarIva() {
        boolean uno = cmbTipo.getSelectedItem().equals("Abarrote") && chkCanasta.isSelected();
        lblIva.setText("Este producto pagará IVA de " + (uno ? "1" : "13") + " %");
    }

    private void cargarTabla() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        Object filtro = cmbFiltro.getSelectedItem();
        listaActual = new ArrayList<>();
        modelo.setRowCount(0);
        for (Producto p : Datos.inventario.listarProductos()) {
            boolean categoriaOk = !(filtro instanceof Categoria) || p.getCategoria() == filtro;
            boolean textoOk = p.getCodigo().toLowerCase().contains(texto) || p.getNombre().toLowerCase().contains(texto);
            if (categoriaOk && textoOk) {
                listaActual.add(p);
                String estado = !p.isActivo() ? "Inactivo" : (p.necesitaReabastecer() ? "Bajo mínimo" : "Disponible");
                modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.obtenerTipo(), p.getCategoria().getNombre(),
                    Factura.formatearMonto(p.getValorVenta()), (int) Math.round(p.obtenerPorcentajeImpuesto() * 100) + " %",
                    p.getExistencias(), estado});
            }
        }
        List<Producto> bajos = Datos.inventario.productosBajoMinimo();
        modeloBajos.setRowCount(0);
        for (Producto p : bajos) {
            modeloBajos.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria().getNombre(),
                p.getExistencias(), p.getStockMinimo()});
        }
        pestanas.setTitleAt(1, "Bajo el mínimo (" + bajos.size() + ")");
        lblEstado.setText(" " + listaActual.size() + " productos · ordenados por nombre · \"Bajo el mínimo\" ordena por existencias");
    }

    private void cargarFormulario(Producto p) {
        seleccionado = p;
        cmbTipo.setSelectedItem(p instanceof ArticuloHogar ? "Artículo de hogar" : p.obtenerTipo());
        cmbCategoria.setSelectedItem(p.getCategoria());
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtCompra.setText(String.valueOf(p.getValorCompra()));
        txtVenta.setText(String.valueOf(p.getValorVenta()));
        txtExistencias.setText(String.valueOf(p.getExistencias()));
        txtMinimo.setText(String.valueOf(p.getStockMinimo()));
        if (p instanceof Abarrote) {
            Abarrote a = (Abarrote) p;
            txtVencimiento.setText(a.getFechaVencimiento().format(FORMATO_FECHA));
            chkCanasta.setSelected(a.isCanastaBasica());
        } else if (p instanceof Bebida) {
            Bebida b = (Bebida) p;
            txtVolumen.setText(String.valueOf(b.getVolumenMl()));
            chkRetornable.setSelected(b.isRetornable());
        } else {
            ArticuloHogar h = (ArticuloHogar) p;
            txtMarca.setText(h.getMarca());
            txtPresentacion.setText(h.getPresentacion());
        }
        actualizarIva();
        // al editar no se cambia el tipo ni el codigo
        cmbTipo.setEnabled(false);
        txtCodigo.setEditable(false);
    }

    private void limpiar() {
        seleccionado = null;
        cmbTipo.setEnabled(true);
        txtCodigo.setEditable(true);
        for (JTextField t : new JTextField[]{txtCodigo, txtNombre, txtCompra, txtVenta, txtMinimo, txtVolumen, txtMarca, txtPresentacion}) {
            t.setText("");
            t.setBackground(Color.WHITE);
        }
        txtExistencias.setText("0");
        txtVencimiento.setText(LocalDate.now().plusMonths(6).format(FORMATO_FECHA));
        chkCanasta.setSelected(false);
        chkRetornable.setSelected(false);
        tabla.clearSelection();
        actualizarIva();
    }

    private void guardar() {
        Color rojo = new Color(255, 220, 220);
        txtCodigo.setBackground(Color.WHITE);
        txtVenta.setBackground(Color.WHITE);
        txtExistencias.setBackground(Color.WHITE);
        try {
            String codigo = txtCodigo.getText().trim().toUpperCase();
            String nombre = txtNombre.getText().trim();
            if (codigo.isEmpty() || nombre.isEmpty()) {
                throw new IllegalArgumentException("Debe completar el código y el nombre.");
            }
            double compra = Double.parseDouble(txtCompra.getText().trim());
            double venta = Double.parseDouble(txtVenta.getText().trim());
            int existencias = Integer.parseInt(txtExistencias.getText().trim());
            int minimo = Integer.parseInt(txtMinimo.getText().trim());
            Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();

            if (seleccionado == null && Datos.inventario.existeCodigo(codigo)) {
                txtCodigo.setBackground(rojo);
                throw new IllegalArgumentException("Ya existe un producto con ese código.");
            }
            if (venta <= 0 || venta < compra) {
                txtVenta.setBackground(rojo);
                throw new IllegalArgumentException("El valor de venta debe ser mayor que 0 y no menor que el valor de compra.");
            }
            if (existencias < 0) {
                txtExistencias.setBackground(rojo);
                throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
            }

            if (seleccionado == null) {
                Producto nuevo;
                String tipo = (String) cmbTipo.getSelectedItem();
                if (tipo.equals("Abarrote")) {
                    LocalDate vence = LocalDate.parse(txtVencimiento.getText().trim(), FORMATO_FECHA);
                    nuevo = new Abarrote(codigo, nombre, categoria, compra, venta, existencias, minimo, vence, chkCanasta.isSelected());
                } else if (tipo.equals("Bebida")) {
                    int volumen = Integer.parseInt(txtVolumen.getText().trim());
                    nuevo = new Bebida(codigo, nombre, categoria, compra, venta, existencias, minimo, volumen, chkRetornable.isSelected());
                } else {
                    nuevo = new ArticuloHogar(codigo, nombre, categoria, compra, venta, existencias, minimo,
                            txtMarca.getText().trim(), txtPresentacion.getText().trim());
                }
                Datos.inventario.agregarProducto(nuevo);
                JOptionPane.showMessageDialog(this, "Producto registrado correctamente.");
            } else {
                // si cambia de categoria se pasa a la lista de la nueva
                if (seleccionado.getCategoria() != categoria) {
                    Datos.inventario.eliminarProducto(seleccionado);
                    seleccionado.setCategoria(categoria);
                    Datos.inventario.agregarProducto(seleccionado);
                }
                seleccionado.setNombre(nombre);
                seleccionado.setValorCompra(compra);
                seleccionado.setValorVenta(venta);
                seleccionado.setExistencias(existencias);
                seleccionado.setStockMinimo(minimo);
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            }
            Datos.guardarCambios();
            limpiar();
            cargarTabla();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Revise que los valores numéricos estén bien escritos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha de vencimiento debe tener el formato dd/mm/aaaa.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "¿Desea eliminar " + seleccionado.getNombre() + "?",
                "Eliminar producto", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        if (Datos.facturas.productoEnFacturas(seleccionado)) {
            seleccionado.setActivo(false);
            JOptionPane.showMessageDialog(this, "El producto ya aparece en facturas, se desactivó para que no se pueda vender.");
        } else {
            Datos.inventario.eliminarProducto(seleccionado);
            JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
        }
        Datos.guardarCambios();
        limpiar();
        cargarTabla();
    }

    // pinta en rojo los que estan bajo el minimo
    private class RenderEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            if (!sel) {
                c.setForeground("Bajo mínimo".equals(valor) ? new Color(200, 40, 40) : Color.BLACK);
            }
            return c;
        }
    }
}
