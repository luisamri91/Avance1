package com.mycompany.fidecompro;

import java.io.IOException;
import java.time.LocalDate;
import javax.swing.JOptionPane;

// guarda en memoria todo lo del sistema mientras el programa esta abierto
public class Datos {

    public static RegistroUsuarios usuarios = new RegistroUsuarios();
    public static RegistroClientes clientes = new RegistroClientes();
    public static RegistroFacturas facturas = new RegistroFacturas();
    public static Inventario inventario = new Inventario();

    // se llama despues de cada cambio para que no se pierda nada al cerrar
    public static void guardarCambios() {
        try {
            ArchivoDatos.guardar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se pudieron guardar los datos en el archivo.");
        }
    }

    // datos de prueba para no empezar vacio
    public static void cargarDatosDePrueba() {
        usuarios.agregar(new Usuario("María Rodríguez", "8888-0001", "mrodriguez@fidecompro.cr", "1-0452-0981",
                "mrodriguez", "Admin2026", Rol.ADMINISTRADOR));
        usuarios.agregar(new Usuario("Carlos Jiménez", "8888-0002", "cjimenez@fidecompro.cr", "2-0518-0733",
                "cjimenez", "Venta2026", Rol.VENDEDOR));

        clientes.agregar(new Cliente(TipoIdentificacion.JURIDICA, "3-101-456789", "Abastecedor La Esquina S.A.",
                "2222-3344", "compras@laesquina.cr", "San José, Desamparados"));
        clientes.agregar(new Cliente(TipoIdentificacion.JURIDICA, "3-102-778899", "Minisúper El Ahorro Ltda.",
                "2560-7788", "admin@elahorro.cr", "Heredia centro"));
        clientes.agregar(new Cliente(TipoIdentificacion.FISICA, "1-1234-0567", "Pulpería Doña Ana",
                "8888-1020", "ana.pulperia@gmail.com", "Cartago, Paraíso"));

        Categoria abarrotes = new Categoria("Abarrotes");
        Categoria bebidas = new Categoria("Bebidas");
        Categoria limpieza = new Categoria("Limpieza");
        Categoria higiene = new Categoria("Higiene");
        inventario.agregarCategoria(abarrotes);
        inventario.agregarCategoria(bebidas);
        inventario.agregarCategoria(limpieza);
        inventario.agregarCategoria(higiene);

        LocalDate vence = LocalDate.now().plusMonths(6);
        inventario.agregarProducto(new Abarrote("ABR-0012", "Arroz 99% grano entero 2 kg", abarrotes, 1500, 1950, 18, 25, vence, true));
        inventario.agregarProducto(new Abarrote("ABR-0020", "Aceite vegetal 1 L", abarrotes, 1400, 1890, 12, 15, vence, true));
        inventario.agregarProducto(new Abarrote("ABR-0031", "Café molido 500 g", abarrotes, 2600, 3350, 40, 10, vence, false));
        inventario.agregarProducto(new Bebida("BEB-0110", "Agua 600 ml (caja 24)", bebidas, 5200, 6900, 50, 10, 600, false));
        inventario.agregarProducto(new Bebida("BEB-0101", "Refresco cola 3 L (caja 6)", bebidas, 7400, 9300, 64, 12, 3000, true));
        inventario.agregarProducto(new ArticuloHogar("LIM-0230", "Detergente en polvo 3 kg", limpieza, 4100, 5400, 9, 10, "Ariel", "Bolsa 3 kg"));
        inventario.agregarProducto(new ArticuloHogar("HIG-0045", "Papel higiénico 24 rollos", higiene, 6900, 8750, 120, 20, "Nevax", "Paquete 24"));
    }
}
