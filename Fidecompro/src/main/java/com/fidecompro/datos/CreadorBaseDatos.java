package com.fidecompro.datos;

import com.fidecompro.modelo.Abarrote;
import com.fidecompro.modelo.ArticuloHogar;
import com.fidecompro.modelo.Bebida;
import com.fidecompro.modelo.Categoria;
import com.fidecompro.modelo.Cliente;
import com.fidecompro.modelo.Producto;
import com.fidecompro.modelo.Rol;
import com.fidecompro.modelo.TipoIdentificacion;
import com.fidecompro.modelo.Usuario;
import com.fidecompro.servidor.ServicioAutenticacion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * La primera vez que arranca el servidor crea las tablas (script sql/fidecompro_derby.sql
 * o sql/fidecompro_mysql.sql) y carga datos de ejemplo para poder entrar al sistema.
 */
public class CreadorBaseDatos {

    public static void prepararBase() throws SQLException {
        if (!existenTablas()) {
            crearTablas();
        }
        if (Consultas.contar("SELECT COUNT(*) FROM usuarios") == 0) {
            cargarDatosIniciales();
        }
    }

    private static boolean existenTablas() {
        try {
            Consultas.contar("SELECT COUNT(*) FROM usuarios");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void crearTablas() throws SQLException {
        String archivo = ConexionBD.getInstancia().esDerby() ? "/sql/fidecompro_derby.sql" : "/sql/fidecompro_mysql.sql";
        String script = leerRecurso(archivo);
        try (Connection con = ConexionBD.getInstancia().obtenerConexion();
                Statement st = con.createStatement()) {
            for (String sentencia : script.split(";")) {
                String limpia = quitarComentarios(sentencia).trim();
                // La base de MySQL ya la crea la URL de conexión
                if (limpia.isEmpty() || limpia.startsWith("CREATE DATABASE") || limpia.startsWith("USE ")) {
                    continue;
                }
                st.executeUpdate(limpia);
            }
        }
    }

    private static String quitarComentarios(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String linea : texto.split("\n")) {
            if (!linea.trim().startsWith("--")) {
                sb.append(linea).append("\n");
            }
        }
        return sb.toString();
    }

    private static String leerRecurso(String ruta) throws SQLException {
        try (InputStream in = CreadorBaseDatos.class.getResourceAsStream(ruta)) {
            if (in == null) {
                throw new SQLException("No se encontró el script " + ruta);
            }
            BufferedReader lector = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) != null) {
                sb.append(linea).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el script " + ruta, e);
        }
    }

    private static void cargarDatosIniciales() throws SQLException {
        UsuarioDAO usuarios = new UsuarioDAO();
        usuarios.insertar(new Usuario("María Rodríguez", "8888-0001", "maria@fidecompro.cr", TipoIdentificacion.FISICA,
                "1-0987-0654", "mrodriguez", ServicioAutenticacion.cifrarContrasena("Admin2026"), Rol.ADMINISTRADOR));
        usuarios.insertar(new Usuario("Carlos Jiménez", "8888-0002", "carlos@fidecompro.cr", TipoIdentificacion.FISICA,
                "2-0456-0789", "cjimenez", ServicioAutenticacion.cifrarContrasena("Venta2026"), Rol.VENDEDOR));
        usuarios.insertar(new Usuario("Ana Pineda", "8888-0003", "ana@fidecompro.cr", TipoIdentificacion.FISICA,
                "4-0222-0333", "apineda", ServicioAutenticacion.cifrarContrasena("Venta2026"), Rol.VENDEDOR));

        CategoriaDAO categorias = new CategoriaDAO();
        Categoria abarrotes = new Categoria("Abarrotes", "Granos, aceites, café y otros alimentos");
        Categoria bebidas = new Categoria("Bebidas", "Refrescos, jugos y agua");
        Categoria limpieza = new Categoria("Limpieza", "Detergentes y desinfectantes");
        Categoria higiene = new Categoria("Higiene", "Papel higiénico, jabón y cuidado personal");
        categorias.insertar(abarrotes);
        categorias.insertar(bebidas);
        categorias.insertar(limpieza);
        categorias.insertar(higiene);

        ProductoDAO productos = new ProductoDAO();
        LocalDate enUnAnio = LocalDate.now().plusYears(1);
        guardar(productos, new Abarrote("ABR-0012", "Arroz 99% grano entero 2 kg", "Saco de 2 kg", 1520, 1950, 18, 25,
                enUnAnio, 2.0, true), abarrotes);
        guardar(productos, new Abarrote("ABR-0020", "Aceite vegetal 1 L", "Botella de 1 litro", 1450, 1890, 12, 15,
                enUnAnio, 0.92, true), abarrotes);
        guardar(productos, new Abarrote("ABR-0031", "Café molido 500 g", "Bolsa de 500 g", 2600, 3350, 40, 20,
                enUnAnio, 0.5, false), abarrotes);
        guardar(productos, new Bebida("BEB-0101", "Refresco cola 3 L (caja 6)", "Caja de 6 botellas", 7400, 9300, 64, 20,
                3000, 6, false), bebidas);
        guardar(productos, new Bebida("BEB-0110", "Agua 600 ml (caja 24)", "Caja de 24 botellas", 5200, 6900, 50, 10,
                600, 24, false), bebidas);
        ArticuloHogar detergente = new ArticuloHogar("LIM-0230", "Detergente en polvo 3 kg", "Bolsa de 3 kg", 4100, 5400,
                9, 10, "Limpiamax", "Bolsa 3 kg");
        detergente.agregarAdvertencia("Mantener fuera del alcance de los niños");
        guardar(productos, detergente, limpieza);
        guardar(productos, new ArticuloHogar("HIG-0045", "Papel higiénico 24 rollos", "Paquete de 24 rollos", 6900, 8750,
                120, 30, "Suave", "Paquete 24"), higiene);

        ClienteDAO clientes = new ClienteDAO();
        clientes.insertar(new Cliente("Abastecedor La Esquina S.A.", "2222-3344", "compras@laesquina.cr",
                TipoIdentificacion.JURIDICA, "3-101-456789", "Heredia, San Pablo, 200 m norte"));
        clientes.insertar(new Cliente("Pulpería Doña Ana", "8888-1020", "ana.pulperia@gmail.com",
                TipoIdentificacion.FISICA, "1-1234-0567", "Alajuela, Grecia centro"));
        clientes.insertar(new Cliente("Minisúper El Ahorro Ltda.", "2560-7788", "admin@elahorro.cr",
                TipoIdentificacion.JURIDICA, "3-102-778899", "San José, Desamparados"));
    }

    private static void guardar(ProductoDAO dao, Producto p, Categoria c) throws SQLException {
        p.setCategoria(c);
        dao.insertar(p);
    }
}
