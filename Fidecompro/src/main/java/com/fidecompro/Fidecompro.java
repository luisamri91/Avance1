package com.fidecompro;

import com.fidecompro.excepciones.CredencialesInvalidasException;
import com.fidecompro.excepciones.RegistroNoEncontradoException;
import com.fidecompro.excepciones.StockInsuficienteException;
import com.fidecompro.modelo.Abarrote;
import com.fidecompro.modelo.ArticuloHogar;
import com.fidecompro.modelo.Bebida;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.Factura;
import com.fidecompro.modelo.Inventario;
import com.fidecompro.modelo.MetodoPago;
import com.fidecompro.modelo.Producto;
import com.fidecompro.modelo.RegistroClientes;
import com.fidecompro.modelo.RegistroFacturas;
import com.fidecompro.modelo.RegistroUsuarios;
import com.fidecompro.modelo.Rol;
import com.fidecompro.modelo.TipoIdentificacion;
import com.fidecompro.modelo.Usuario;
import java.time.LocalDate;

/**
 * Prueba por consola del modelo de Fidecompro: recorre las historias de usuario
 * principales (login, clientes, productos, facturas, factura física y anulación).
 */
public class Fidecompro {

    public static void main(String[] args) {
        RegistroUsuarios usuarios = new RegistroUsuarios();
        RegistroClientes clientes = new RegistroClientes();
        RegistroFacturas facturas = new RegistroFacturas();
        Inventario inventario = new Inventario("Inventario central", "Heredia");

        // ---------- HU-03: usuarios ----------
        usuarios.agregarUsuario(new Usuario("Maria Rodriguez", "8888-0001", "maria@fidecompro.cr",
                "mrodriguez", "admin123", Rol.ADMINISTRADOR));
        usuarios.agregarUsuario(new Usuario("Carlos Jimenez", "8888-0002", "carlos@fidecompro.cr",
                "cjimenez", "venta123", Rol.VENDEDOR));

        // ---------- HU-01: inicio de sesión ----------
        titulo("HU-01 Inicio de sesion");
        try {
            usuarios.iniciarSesion("cjimenez", "clave-mala");
        } catch (CredencialesInvalidasException e) {
            System.out.println("Intento fallido: " + e.getMessage());
        }
        Usuario vendedor;
        try {
            vendedor = usuarios.iniciarSesion("cjimenez", "venta123");
            System.out.println("Bienvenido " + vendedor.getNombre() + " (" + vendedor.getRol() + ")");
        } catch (CredencialesInvalidasException e) {
            System.out.println(e.getMessage());
            return;
        }

        // ---------- HU-04: clientes ----------
        titulo("HU-04 Registro de clientes");
        clientes.agregarCliente(new Cliente("Abastecedor La Esquina S.A.", "2222-3344", "compras@laesquina.cr",
                TipoIdentificacion.JURIDICA, "3-101-456789", "Heredia, San Pablo"));
        clientes.agregarCliente(new Cliente("Pulperia Dona Ana", "8888-1020", "ana.pulperia@gmail.com",
                TipoIdentificacion.FISICA, "1-1234-0567", "Alajuela centro"));
        try {
            clientes.agregarCliente(new Cliente("Copia", "0000-0000", "x@x.com",
                    TipoIdentificacion.FISICA, "1-1234-0567", "-"));
        } catch (IllegalArgumentException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
        System.out.print(clientes.mostrarClientes());

        // ---------- HU-07 y HU-08: categorías y productos ----------
        titulo("HU-07 / HU-08 Categorias y productos (polimorfismo en el IVA)");
        Categoria abarrotes = new Categoria("Abarrotes", "Granos, aceites y basicos");
        Categoria bebidas = new Categoria("Bebidas", "Refrescos, jugos y agua");
        Categoria limpieza = new Categoria("Limpieza", "Limpieza e higiene del hogar");
        inventario.agregarCategoria(abarrotes);
        inventario.agregarCategoria(bebidas);
        inventario.agregarCategoria(limpieza);

        Abarrote arroz = new Abarrote("ABR-0012", "Arroz 99% grano entero 2 kg", "Arroz blanco",
                1520, 1950, 18, 25, LocalDate.of(2027, 3, 15), 2.0, true);
        Abarrote galletas = new Abarrote("ABR-0300", "Galletas surtidas caja 24", "Snack",
                3100, 4200, 40, 10, LocalDate.of(2027, 1, 10), 1.2, false);
        Bebida refresco = new Bebida("BEB-0101", "Refresco cola 3 L (caja 6)", "Gaseosa",
                7200, 9300, 64, 15, 3000, 6, false);
        ArticuloHogar papel = new ArticuloHogar("HIG-0045", "Papel higienico 24 rollos", "Doble hoja",
                6900, 8750, 120, 30, "Suave", "Paquete 24 unidades");
        ArticuloHogar detergente = new ArticuloHogar("LIM-0230", "Detergente en polvo 3 kg", "Ropa",
                4100, 5400, 9, 12, "Limpiex", "Bolsa 3 kg");
        detergente.agregarAdvertencia("Mantener fuera del alcance de los ninos");

        try {
            inventario.agregarProductoCategoria(abarrotes.getId(), arroz);
            inventario.agregarProductoCategoria(abarrotes.getId(), galletas);
            inventario.agregarProductoCategoria(bebidas.getId(), refresco);
            inventario.agregarProductoCategoria(limpieza.getId(), papel);
            inventario.agregarProductoCategoria(limpieza.getId(), detergente);
            for (Categoria c : inventario.getCategorias()) {
                System.out.print(inventario.mostrarProductosCategoria(c.getId()));
            }
        } catch (RegistroNoEncontradoException e) {
            System.out.println(e.getMessage());
        }

        // ---------- HU-10: alertas de stock (Comparator) ----------
        titulo("HU-10 Productos bajo el minimo (ordenados por existencias)");
        for (Producto p : inventario.productosBajoMinimo()) {
            System.out.println(p.getExistencias() + " uds. - " + p.getNombre());
        }

        // ---------- HU-11: crear factura ----------
        titulo("HU-11 Nueva factura");
        Factura factura = null;
        try {
            Cliente cliente = clientes.buscarCliente("3-101-456789");
            factura = new Factura(cliente, vendedor, MetodoPago.TRANSFERENCIA);
            factura.agregarDetalle(inventario.buscarProducto("ABR-0012"), 10);
            factura.agregarDetalle(inventario.buscarProducto("HIG-0045"), 4);
            factura.agregarDetalle(inventario.buscarProducto("BEB-0101"), 5);
            factura.aplicarDescuento(2);
            try {
                factura.agregarDetalle(inventario.buscarProducto("ABR-0012"), 50);
            } catch (StockInsuficienteException e) {
                System.out.println("No se agrego la linea: " + e.getMessage());
            }
            facturas.agregarFactura(factura);
            System.out.print(factura.mostrarInformacion());
            System.out.println("Existencias de arroz despues de facturar: " + arroz.getExistencias());
        } catch (RegistroNoEncontradoException | StockInsuficienteException e) {
            System.out.println("No se pudo emitir la factura: " + e.getMessage());
        }

        // ---------- HU-13: factura física ----------
        titulo("HU-13 Factura fisica");
        if (factura != null) {
            String ruta = factura.generarArchivo("facturas");
            System.out.println(ruta != null ? "Archivo generado: " + ruta : "No se genero el archivo");
        }

        // ---------- HU-12: dos cajas a la vez (adelanto de hilos) ----------
        titulo("HU-12 Dos cajas venden el ultimo detergente al mismo tiempo");
        venderEnParalelo(clientes, facturas, vendedor, detergente);
        System.out.println("Existencias finales de detergente: " + detergente.getExistencias());

        // ---------- HU-14 y HU-15: historial y anulación ----------
        titulo("HU-14 / HU-15 Historial y anulacion");
        LocalDate hoy = LocalDate.now();
        System.out.println("Facturas de hoy: " + facturas.facturasPorFecha(hoy, hoy).size()
                + " | Total vendido: " + Factura.formatearMonto(facturas.totalVendido(hoy, hoy)));
        if (factura != null) {
            try {
                facturas.anularFactura(factura.getNumero());
                System.out.println("Factura " + factura.getNumeroFormateado() + " anulada. Arroz vuelve a "
                        + arroz.getExistencias() + " unidades.");
                facturas.anularFactura(factura.getNumero());
            } catch (RegistroNoEncontradoException | IllegalStateException e) {
                System.out.println("Rechazado: " + e.getMessage());
            }
        }
        System.out.println("Total vendido despues de anular: "
                + Factura.formatearMonto(facturas.totalVendido(hoy, hoy)));
    }

    /**
     * Dos hilos (cajas) intentan facturar las 9 unidades de detergente a la vez.
     * Como agregarFactura es synchronized, solo una venta pasa y la otra recibe
     * StockInsuficienteException en lugar de dejar las existencias en negativo.
     */
    private static void venderEnParalelo(RegistroClientes clientes, RegistroFacturas facturas,
            Usuario vendedor, Producto producto) {
        Runnable caja = () -> {
            String nombreCaja = Thread.currentThread().getName();
            try {
                Factura f = new Factura(clientes.buscarCliente("1-1234-0567"), vendedor, MetodoPago.EFECTIVO);
                f.agregarDetalle(producto, 9);
                facturas.agregarFactura(f);
                System.out.println(nombreCaja + ": emitio la factura " + f.getNumeroFormateado());
            } catch (StockInsuficienteException | RegistroNoEncontradoException e) {
                System.out.println(nombreCaja + ": " + e.getMessage());
            }
        };
        Thread caja1 = new Thread(caja, "Caja 1");
        Thread caja2 = new Thread(caja, "Caja 2");
        caja1.start();
        caja2.start();
        try {
            caja1.join();
            caja2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void titulo(String texto) {
        System.out.println("\n### " + texto);
    }
}
