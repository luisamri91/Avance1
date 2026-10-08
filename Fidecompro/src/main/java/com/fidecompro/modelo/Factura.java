package com.fidecompro.modelo;

import com.fidecompro.excepciones.StockInsuficienteException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Documento de venta emitido a un cliente.
 */
public class Factura implements Mostrable {

    private static int ultimoNumero = 0;

    private int numero;
    private LocalDateTime fecha;
    private Cliente cliente;
    private Usuario vendedor;
    private ArrayList<DetalleFactura> detalles;
    private double porcentajeDescuento;
    private MetodoPago metodoPago;
    private EstadoFactura estado;

    public Factura(Cliente cliente, Usuario vendedor, MetodoPago metodoPago) {
        this.numero = 0; // se asigna al emitirla, para no dejar huecos si una venta falla
        this.fecha = LocalDateTime.now();
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.metodoPago = metodoPago;
        this.detalles = new ArrayList<>();
        this.porcentajeDescuento = 0;
        this.estado = EstadoFactura.EMITIDA;
    }

    // synchronized: cuando haya varias cajas (hilos), dos facturas no reciben el mismo número
    private static synchronized int siguienteNumero() {
        return ++ultimoNumero;
    }

    // La llama RegistroFacturas cuando la factura se emite con éxito
    void asignarNumero() {
        if (numero == 0) {
            numero = siguienteNumero();
        }
    }

    public void agregarDetalle(Producto producto, int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        // Si el producto ya está en la factura se suma a la misma línea
        for (DetalleFactura d : detalles) {
            if (d.getProducto() == producto) {
                validarExistencias(producto, d.getCantidad() + cantidad);
                d.setCantidad(d.getCantidad() + cantidad);
                return;
            }
        }
        validarExistencias(producto, cantidad);
        detalles.add(new DetalleFactura(producto, cantidad));
    }

    private void validarExistencias(Producto producto, int cantidad) throws StockInsuficienteException {
        if (cantidad > producto.getExistencias()) {
            throw new StockInsuficienteException("Stock insuficiente de " + producto.getNombre()
                    + ": se pidieron " + cantidad + " y hay " + producto.getExistencias());
        }
    }

    public void eliminarDetalle(int indice) {
        detalles.remove(indice);
    }

    public void aplicarDescuento(double porcentaje) {
        if (porcentaje < 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.porcentajeDescuento = porcentaje;
    }

    public double calcularSubtotal() {
        double subtotal = 0;
        for (DetalleFactura d : detalles) {
            subtotal += d.calcularSubtotal();
        }
        return subtotal;
    }

    public double calcularDescuento() {
        return calcularSubtotal() * porcentajeDescuento / 100;
    }

    // El impuesto de cada línea depende del tipo de producto; se aplica sobre el monto con descuento
    public double calcularImpuesto() {
        double impuesto = 0;
        for (DetalleFactura d : detalles) {
            impuesto += d.calcularImpuesto();
        }
        return impuesto * (1 - porcentajeDescuento / 100);
    }

    public double calcularTotal() {
        return calcularSubtotal() - calcularDescuento() + calcularImpuesto();
    }

    public void anular() {
        estado = EstadoFactura.ANULADA;
    }

    public boolean estaVacia() {
        return detalles.isEmpty();
    }

    public String getNumeroFormateado() {
        return String.format("FC-%06d", numero);
    }

    public static String formatearMonto(double monto) {
        return String.format("%,.2f", monto);
    }

    @Override
    public String mostrarInformacion() {
        String linea = "=".repeat(62) + "\n";
        String guion = "-".repeat(62) + "\n";
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String texto = linea
                + "               FIDECOMPRO - VENTAS AL POR MAYOR\n"
                + linea
                + "Factura N.: " + getNumeroFormateado() + "        Fecha: " + fecha.format(formato) + "\n"
                + "Vendedor  : " + vendedor.getNombre() + "\n"
                + "Cliente   : " + cliente.getNombre() + "\n"
                + "Identific.: " + cliente.getIdentificacion() + "   Tel: " + cliente.getTelefono() + "\n"
                + guion
                + "Cant  Descripcion                        P.Unit        Total\n"
                + guion;
        for (DetalleFactura d : detalles) {
            texto += d.mostrarInformacion() + "\n";
        }
        texto += guion
                + String.format("%45s %14s%n", "Subtotal :", formatearMonto(calcularSubtotal()))
                + String.format("%45s %14s%n", "Descuento " + formatearMonto(porcentajeDescuento) + "% :",
                        "-" + formatearMonto(calcularDescuento()))
                + String.format("%45s %14s%n", "IVA :", formatearMonto(calcularImpuesto()))
                + String.format("%45s %14s%n", "TOTAL :", formatearMonto(calcularTotal()))
                + "Metodo de pago: " + metodoPago + "   Estado: " + estado + "\n"
                + linea;
        return texto;
    }

    /**
     * Factura física: escribe el desglose en un archivo de texto.
     * Devuelve la ruta del archivo, o null si no se pudo escribir.
     */
    public String generarArchivo(String carpeta) {
        File archivo = new File(carpeta, getNumeroFormateado() + ".txt");
        FileWriter escritor = null;
        try {
            archivo.getParentFile().mkdirs();
            escritor = new FileWriter(archivo);
            escritor.write(mostrarInformacion());
            return archivo.getPath();
        } catch (IOException e) {
            System.out.println("No se pudo generar el archivo de la factura: " + e.getMessage());
            return null;
        } finally {
            try {
                if (escritor != null) {
                    escritor.close();
                }
            } catch (IOException e) {
                System.out.println("No se pudo cerrar el archivo: " + e.getMessage());
            }
        }
    }

    public int getNumero() {
        return numero;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public ArrayList<DetalleFactura> getDetalles() {
        return detalles;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoFactura getEstado() {
        return estado;
    }
}
