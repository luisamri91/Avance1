package com.fidecompro.cliente;

import com.fidecompro.modelo.Abarrote;
import com.fidecompro.modelo.ArticuloHogar;
import com.fidecompro.modelo.Bebida;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.ComparadorPorExistencias;
import com.fidecompro.modelo.Producto;
import com.fidecompro.red.Respuesta;
import com.fidecompro.red.TipoOperacion;
import com.fidecompro.util.Estilo;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Productos e inventario (HU 4, 5, 6 y 7, bocetos P4 y P10): pestañas Productos, Categorías y Bajo el mínimo.
 */
public class VentanaProductos extends JFrame {

    private static final String ABARROTE = "Abarrote";
    private static final String BEBIDA = "Bebida";
    private static final String HOGAR = "Artículo del hogar";

    private final Sesion sesion;
    private final JTabbedPane pestanas = new JTabbedPane();

    // ---------- Pestaña Productos ----------
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{ABARROTE, BEBIDA, HOGAR});
    private final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(22);
    private final JTextField txtDescripcion = new JTextField(22);
    private final JTextField txtCompra = new JTextField(12);
    private final JTextField txtVenta = new JTextField(12);
    private final JTextField txtExistencias = new JTextField(6);
    private final JTextField txtMinimo = new JTextField(6);
    private final JLabel lblIva = new JLabel();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel panelTipo = new JPanel(tarjetas);
    private final JTextField txtVence = new JTextField(10);
    private final JTextField txtPeso = new JTextField(6);
    private final JCheckBox chkCanasta = new JCheckBox("Canasta básica (IVA 1 %)");
    private final JTextField txtVolumen = new JTextField(6);
    private final JTextField txtUnidades = new JTextField(6);
    private final JCheckBox chkRetornable = new JCheckBox("Retornable");
    private final JTextField txtMarca = new JTextField(12);
    private final JTextField txtPresentacion = new JTextField(12);
    private final JTextField txtAdvertencias = new JTextField(30);
    private final JButton btnNuevo = Estilo.boton("Nuevo");
    private final JButton btnGuardar = Estilo.botonPrincipal("Guardar");
    private final JButton btnAjustar = Estilo.boton("Ajustar stock…");
    private final JButton btnEliminar = Estilo.botonPeligro("Eliminar");
    private final JButton btnActualizar = Estilo.boton("Actualizar lista");
    private final JComboBox<Object> cmbFiltro = new JComboBox<>();
    private final JTextField txtFiltro = new JTextField(18);
    private final DefaultTableModel modeloProductos = Estilo.modeloTabla("Código", "Nombre", "Tipo", "Categoría", "Precio", "IVA", "Stock", "Estado");
    private final JTable tablaProductos = new JTable(modeloProductos);
    private final List<Producto> productosVisibles = new ArrayList<>();
    private List<Producto> todos = new ArrayList<>();
    private Producto seleccionado;

    // ---------- Pestaña Categorías ----------
    private final JTextField txtCatNombre = new JTextField(20);
    private final JTextField txtCatDescripcion = new JTextField(30);
    private final DefaultTableModel modeloCategorias = Estilo.modeloTabla("ID", "Nombre", "Descripción");
    private final JTable tablaCategorias = new JTable(modeloCategorias);
    private List<Categoria> categorias = new ArrayList<>();
    private Categoria categoriaSeleccionada;

    // ---------- Pestaña Bajo el mínimo ----------
    private final DefaultTableModel modeloBajos = Estilo.modeloTabla("Código", "Nombre", "Categoría", "Stock", "Mínimo", "Faltan");
    private final JTable tablaBajos = new JTable(modeloBajos);
    private final JLabel lblEstado = new JLabel(" ");

    public VentanaProductos(Sesion sesion) {
        super("Fidecompro · Productos e inventario");
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pestanas.addTab("Productos", construirProductos());
        pestanas.addTab("Categorías", construirCategorias());
        pestanas.addTab("Bajo el mínimo", construirBajos());
        JPanel raiz = new JPanel(new BorderLayout());
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centro.add(pestanas);
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(Estilo.barraEstado(lblEstado, new JLabel("Usuario: " + sesion.getUsuario().getNombre())), BorderLayout.SOUTH);
        setContentPane(raiz);
        // HU 5: solo el administrador registra, modifica, ajusta o elimina
        boolean admin = sesion.esAdministrador();
        btnNuevo.setEnabled(admin);
        btnGuardar.setEnabled(admin);
        btnAjustar.setEnabled(admin);
        btnEliminar.setEnabled(admin);
        if (!admin) {
            lblEstado.setText("Modo consulta: solo el administrador puede modificar el catálogo");
        }
        setSize(1060, 760);
        setLocationByPlatform(true);
        cargarCategorias();
        limpiarProducto();
        cargarProductos();
    }

    // ===================== Productos =====================
    private JPanel construirProductos() {
        JPanel datos = Estilo.marco("Datos del producto");
        Estilo.celda(datos, new JLabel("Tipo de producto:"), 0, 0, 1, 0);
        Estilo.celda(datos, cmbTipo, 1, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Categoría:"), 2, 0, 1, 0);
        Estilo.celda(datos, cmbCategoria, 3, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Código:"), 0, 1, 1, 0);
        Estilo.celda(datos, txtCodigo, 1, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Nombre:"), 2, 1, 1, 0);
        Estilo.celda(datos, txtNombre, 3, 1, 1, 1);
        Estilo.celda(datos, new JLabel("Valor compra (₡):"), 0, 2, 1, 0);
        Estilo.celda(datos, txtCompra, 1, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Valor venta (₡):"), 2, 2, 1, 0);
        Estilo.celda(datos, txtVenta, 3, 2, 1, 1);
        Estilo.celda(datos, new JLabel("Existencias:"), 0, 3, 1, 0);
        Estilo.celda(datos, txtExistencias, 1, 3, 1, 0);
        Estilo.celda(datos, new JLabel("Stock mínimo:"), 2, 3, 1, 0);
        Estilo.celda(datos, txtMinimo, 3, 3, 1, 0);
        Estilo.celda(datos, new JLabel("Descripción:"), 0, 4, 1, 0);
        Estilo.celda(datos, txtDescripcion, 1, 4, 1, 1);
        lblIva.setForeground(Estilo.AZUL_OSCURO);
        Estilo.celda(datos, lblIva, 2, 4, 2, 1);

        JPanel abarrote = Estilo.marco("Datos de abarrote");
        Estilo.celda(abarrote, new JLabel("Fecha vencimiento:"), 0, 0, 1, 0);
        Estilo.celda(abarrote, txtVence, 1, 0, 1, 0);
        Estilo.celda(abarrote, new JLabel("Peso (kg):"), 2, 0, 1, 0);
        Estilo.celda(abarrote, txtPeso, 3, 0, 1, 0);
        Estilo.celda(abarrote, chkCanasta, 4, 0, 1, 1);
        JPanel bebida = Estilo.marco("Datos de bebida");
        Estilo.celda(bebida, new JLabel("Volumen (ml):"), 0, 0, 1, 0);
        Estilo.celda(bebida, txtVolumen, 1, 0, 1, 0);
        Estilo.celda(bebida, new JLabel("Unidades por paquete:"), 2, 0, 1, 0);
        Estilo.celda(bebida, txtUnidades, 3, 0, 1, 0);
        Estilo.celda(bebida, chkRetornable, 4, 0, 1, 1);
        JPanel hogar = Estilo.marco("Datos de artículo del hogar");
        Estilo.celda(hogar, new JLabel("Marca:"), 0, 0, 1, 0);
        Estilo.celda(hogar, txtMarca, 1, 0, 1, 0);
        Estilo.celda(hogar, new JLabel("Presentación:"), 2, 0, 1, 0);
        Estilo.celda(hogar, txtPresentacion, 3, 0, 1, 0);
        Estilo.celda(hogar, new JLabel("Advertencias (separadas por ;):"), 0, 1, 2, 0);
        Estilo.celda(hogar, txtAdvertencias, 2, 1, 3, 1);
        panelTipo.add(abarrote, ABARROTE);
        panelTipo.add(bebida, BEBIDA);
        panelTipo.add(hogar, HOGAR);
        panelTipo.setOpaque(false);
        Estilo.celda(datos, panelTipo, 0, 5, 4, 1);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnAjustar);
        botones.add(btnEliminar);
        botones.add(btnActualizar);
        Estilo.celda(datos, botones, 0, 6, 4, 1);

        cmbTipo.addActionListener(e -> cambiarTipo());
        chkCanasta.addActionListener(e -> actualizarIva());
        btnNuevo.addActionListener(e -> limpiarProducto());
        btnGuardar.addActionListener(e -> guardarProducto());
        btnAjustar.addActionListener(e -> ajustarStock());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnActualizar.addActionListener(e -> cargarProductos());

        JButton btnBuscar = Estilo.boton("Buscar");
        btnBuscar.addActionListener(e -> filtrar());
        txtFiltro.addActionListener(e -> filtrar());
        cmbFiltro.addActionListener(e -> filtrar());
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro.add(new JLabel("Filtrar por categoría:"));
        filtro.add(cmbFiltro);
        filtro.add(new JLabel("Código o nombre:"));
        filtro.add(txtFiltro);
        filtro.add(btnBuscar);

        tablaProductos.getColumnModel().getColumn(7).setCellRenderer(new RenderEstado());
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(240);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                seleccionado = productosVisibles.get(fila);
                mostrarProducto(seleccionado);
            }
        });

        JPanel lista = new JPanel(new BorderLayout());
        lista.add(filtro, BorderLayout.NORTH);
        lista.add(Estilo.scroll(tablaProductos), BorderLayout.CENTER);
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        p.add(datos, BorderLayout.NORTH);
        p.add(lista, BorderLayout.CENTER);
        return p;
    }

    private void cambiarTipo() {
        tarjetas.show(panelTipo, (String) cmbTipo.getSelectedItem());
        actualizarIva();
    }

    // HU 5: el formulario indica qué IVA pagará el producto
    private void actualizarIva() {
        boolean canasta = ABARROTE.equals(cmbTipo.getSelectedItem()) && chkCanasta.isSelected();
        lblIva.setText(canasta ? "Este producto pagará IVA de 1 % (canasta básica)" : "Este producto pagará IVA de 13 %");
    }

    // Se usa el SwingWorker para no congelar la ventana mientras responde el servidor
    private void cargarProductos() {
        lblEstado.setText("Actualizando lista…");
        new HiloActualizacionStock(sesion, lista -> {
            todos = lista;
            filtrar();
            cargarBajos();
            lblEstado.setText(todos.size() + " productos · ordenados por nombre (Comparable) · \"Bajo el mínimo\" ordena por existencias (Comparator)");
        }).execute();
    }

    private void filtrar() {
        Object categoria = cmbFiltro.getSelectedItem();
        String texto = txtFiltro.getText().trim().toLowerCase();
        productosVisibles.clear();
        for (Producto p : todos) {
            boolean coincideCategoria = !(categoria instanceof Categoria) || p.getCategoria().getId() == ((Categoria) categoria).getId();
            boolean coincideTexto = texto.isEmpty() || p.getCodigo().toLowerCase().contains(texto)
                    || p.getNombre().toLowerCase().contains(texto);
            if (coincideCategoria && coincideTexto) {
                productosVisibles.add(p);
            }
        }
        modeloProductos.setRowCount(0);
        for (Producto p : productosVisibles) {
            String estado = !p.isActivo() ? "Inactivo" : p.necesitaReabastecer() ? "Bajo mínimo" : "Disponible";
            modeloProductos.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.obtenerTipo(), p.getCategoria().getNombre(),
                Estilo.colones(p.getValorVenta()), Math.round(p.obtenerPorcentajeImpuesto() * 100) + " %",
                p.getExistencias(), estado});
        }
    }

    private void mostrarProducto(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtCompra.setText(String.valueOf(p.getValorCompra()));
        txtVenta.setText(String.valueOf(p.getValorVenta()));
        txtExistencias.setText(String.valueOf(p.getExistencias()));
        txtExistencias.setEnabled(false);
        txtMinimo.setText(String.valueOf(p.getStockMinimo()));
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getId() == p.getCategoria().getId()) {
                cmbCategoria.setSelectedIndex(i);
            }
        }
        if (p instanceof Abarrote) {
            Abarrote a = (Abarrote) p;
            cmbTipo.setSelectedItem(ABARROTE);
            txtVence.setText(a.getFechaVencimiento() == null ? "" : a.getFechaVencimiento().format(Estilo.FECHA));
            txtPeso.setText(String.valueOf(a.getPesoKg()));
            chkCanasta.setSelected(a.isCanastaBasica());
        } else if (p instanceof Bebida) {
            Bebida b = (Bebida) p;
            cmbTipo.setSelectedItem(BEBIDA);
            txtVolumen.setText(String.valueOf(b.getVolumenMl()));
            txtUnidades.setText(String.valueOf(b.getUnidadesPorPaquete()));
            chkRetornable.setSelected(b.isRetornable());
        } else {
            ArticuloHogar h = (ArticuloHogar) p;
            cmbTipo.setSelectedItem(HOGAR);
            txtMarca.setText(h.getMarca());
            txtPresentacion.setText(h.getPresentacion());
            txtAdvertencias.setText(String.join("; ", h.getAdvertencias()));
        }
        cmbTipo.setEnabled(false);
        actualizarIva();
    }

    private void limpiarProducto() {
        seleccionado = null;
        tablaProductos.clearSelection();
        for (JTextField t : new JTextField[]{txtCodigo, txtNombre, txtDescripcion, txtCompra, txtVenta, txtMinimo,
            txtPeso, txtVolumen, txtUnidades, txtMarca, txtPresentacion, txtAdvertencias}) {
            t.setText("");
        }
        txtExistencias.setText("0");
        txtExistencias.setEnabled(true);
        txtVence.setText(LocalDate.now().plusMonths(6).format(Estilo.FECHA));
        chkCanasta.setSelected(false);
        chkRetornable.setSelected(false);
        cmbTipo.setEnabled(true);
        cmbTipo.setSelectedItem(ABARROTE);
        cambiarTipo();
    }

    // Arma el objeto de la subclase que corresponde al tipo elegido (polimorfismo)
    private Producto leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        double compra = Estilo.leerNumero(txtCompra.getText(), "Valor compra");
        double venta = Estilo.leerNumero(txtVenta.getText(), "Valor venta");
        int existencias = Estilo.leerEntero(txtExistencias.getText(), "Existencias");
        int minimo = Estilo.leerEntero(txtMinimo.getText(), "Stock mínimo");
        Producto p;
        String tipo = (String) cmbTipo.getSelectedItem();
        if (ABARROTE.equals(tipo)) {
            p = new Abarrote(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                    Estilo.leerFecha(txtVence.getText(), "Fecha vencimiento"),
                    Estilo.leerNumero(txtPeso.getText(), "Peso"), chkCanasta.isSelected());
        } else if (BEBIDA.equals(tipo)) {
            p = new Bebida(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                    Estilo.leerEntero(txtVolumen.getText(), "Volumen"),
                    Estilo.leerEntero(txtUnidades.getText(), "Unidades por paquete"), chkRetornable.isSelected());
        } else {
            ArticuloHogar h = new ArticuloHogar(codigo, nombre, descripcion, compra, venta, existencias, minimo,
                    txtMarca.getText().trim(), txtPresentacion.getText().trim());
            for (String a : txtAdvertencias.getText().split(";")) {
                if (!a.isBlank()) {
                    h.agregarAdvertencia(a.trim());
                }
            }
            p = h;
        }
        p.setCategoria((Categoria) cmbCategoria.getSelectedItem());
        return p;
    }

    private void guardarProducto() {
        Producto p;
        try {
            p = leerFormulario();
        } catch (IllegalArgumentException e) {
            Estilo.error(this, e.getMessage());
            return;
        }
        boolean nuevo = seleccionado == null;
        if (!nuevo) {
            p.setId(seleccionado.getId());
            p.setActivo(seleccionado.isActivo());
        }
        Respuesta r = sesion.pedir(this, nuevo ? TipoOperacion.CREAR_PRODUCTO : TipoOperacion.ACTUALIZAR_PRODUCTO, p);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiarProducto();
            cargarProductos();
        }
    }

    private void ajustarStock() {
        if (seleccionado == null) {
            Estilo.error(this, "Seleccione un producto en la tabla.");
            return;
        }
        DialogoAjusteStock dialogo = new DialogoAjusteStock(this, sesion, seleccionado);
        dialogo.setVisible(true);
        if (dialogo.isAplicado()) {
            limpiarProducto();
            cargarProductos();
        }
    }

    private void eliminarProducto() {
        if (seleccionado == null) {
            Estilo.error(this, "Seleccione un producto en la tabla.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Eliminar el producto " + seleccionado.getNombre() + "?")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.ELIMINAR_PRODUCTO, seleccionado);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiarProducto();
            cargarProductos();
        }
    }

    // ===================== Categorías =====================
    private JPanel construirCategorias() {
        JPanel datos = Estilo.marco("Datos de la categoría");
        Estilo.celda(datos, new JLabel("Nombre:"), 0, 0, 1, 0);
        Estilo.celda(datos, txtCatNombre, 1, 0, 1, 1);
        Estilo.celda(datos, new JLabel("Descripción:"), 0, 1, 1, 0);
        Estilo.celda(datos, txtCatDescripcion, 1, 1, 1, 1);
        JButton btnNueva = Estilo.boton("Nueva");
        JButton btnGuardarCat = Estilo.botonPrincipal("Guardar");
        JButton btnEliminarCat = Estilo.botonPeligro("Eliminar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnNueva);
        botones.add(btnGuardarCat);
        botones.add(btnEliminarCat);
        Estilo.celda(datos, botones, 0, 2, 2, 1);
        boolean admin = sesion.esAdministrador();
        btnNueva.setEnabled(admin);
        btnGuardarCat.setEnabled(admin);
        btnEliminarCat.setEnabled(admin);
        btnNueva.addActionListener(e -> limpiarCategoria());
        btnGuardarCat.addActionListener(e -> guardarCategoria());
        btnEliminarCat.addActionListener(e -> eliminarCategoria());
        tablaCategorias.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaCategorias.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaCategorias.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                categoriaSeleccionada = categorias.get(fila);
                txtCatNombre.setText(categoriaSeleccionada.getNombre());
                txtCatDescripcion.setText(categoriaSeleccionada.getDescripcion());
            }
        });
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        p.add(datos, BorderLayout.NORTH);
        p.add(Estilo.scroll(tablaCategorias), BorderLayout.CENTER);
        return p;
    }

    @SuppressWarnings("unchecked")
    private void cargarCategorias() {
        Respuesta r = sesion.pedir(this, TipoOperacion.LISTAR_CATEGORIAS, null);
        if (r == null) {
            return;
        }
        categorias = (List<Categoria>) r.getDatos();
        modeloCategorias.setRowCount(0);
        cmbCategoria.removeAllItems();
        Object filtroActual = cmbFiltro.getSelectedItem();
        cmbFiltro.removeAllItems();
        cmbFiltro.addItem("Todas");
        for (Categoria c : categorias) {
            modeloCategorias.addRow(new Object[]{c.getId(), c.getNombre(), c.getDescripcion()});
            cmbCategoria.addItem(c);
            cmbFiltro.addItem(c);
        }
        if (filtroActual instanceof Categoria) {
            for (int i = 1; i < cmbFiltro.getItemCount(); i++) {
                if (((Categoria) cmbFiltro.getItemAt(i)).getId() == ((Categoria) filtroActual).getId()) {
                    cmbFiltro.setSelectedIndex(i);
                }
            }
        }
    }

    private void guardarCategoria() {
        Categoria c = new Categoria(txtCatNombre.getText().trim(), txtCatDescripcion.getText().trim());
        boolean nueva = categoriaSeleccionada == null;
        if (!nueva) {
            c.setId(categoriaSeleccionada.getId());
        }
        Respuesta r = sesion.pedir(this, nueva ? TipoOperacion.CREAR_CATEGORIA : TipoOperacion.ACTUALIZAR_CATEGORIA, c);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiarCategoria();
            cargarCategorias();
            cargarProductos();
        }
    }

    private void eliminarCategoria() {
        if (categoriaSeleccionada == null) {
            Estilo.error(this, "Seleccione una categoría en la lista.");
            return;
        }
        if (!Estilo.confirmar(this, "¿Eliminar la categoría " + categoriaSeleccionada.getNombre() + "?")) {
            return;
        }
        Respuesta r = sesion.pedir(this, TipoOperacion.ELIMINAR_CATEGORIA, categoriaSeleccionada);
        if (r != null) {
            Estilo.info(this, r.getMensaje());
            limpiarCategoria();
            cargarCategorias();
        }
    }

    private void limpiarCategoria() {
        categoriaSeleccionada = null;
        tablaCategorias.clearSelection();
        txtCatNombre.setText("");
        txtCatDescripcion.setText("");
    }

    // ===================== Bajo el mínimo =====================
    private JPanel construirBajos() {
        tablaBajos.setDefaultRenderer(Object.class, new RenderRojo());
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        p.add(new JLabel("Productos con existencias menores o iguales a su stock mínimo, de menos a más existencias."),
                BorderLayout.NORTH);
        p.add(Estilo.scroll(tablaBajos), BorderLayout.CENTER);
        return p;
    }

    private void cargarBajos() {
        List<Producto> bajos = new ArrayList<>();
        for (Producto p : todos) {
            if (p.isActivo() && p.necesitaReabastecer()) {
                bajos.add(p);
            }
        }
        Collections.sort(bajos, new ComparadorPorExistencias());
        modeloBajos.setRowCount(0);
        for (Producto p : bajos) {
            modeloBajos.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getCategoria().getNombre(),
                p.getExistencias(), p.getStockMinimo(), p.getStockMinimo() - p.getExistencias()});
        }
        pestanas.setTitleAt(2, "Bajo el mínimo (" + bajos.size() + ")");
    }

    private static class RenderEstado extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foco, fila, col);
            c.setForeground("Bajo mínimo".equals(v) ? Estilo.ROJO : "Inactivo".equals(v) ? Estilo.GRIS : Color.BLACK);
            return c;
        }
    }

    private static class RenderRojo extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foco, fila, col);
            c.setForeground(Estilo.ROJO);
            return c;
        }
    }
}
