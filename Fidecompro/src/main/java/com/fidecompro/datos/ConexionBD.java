package com.fidecompro.datos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton con los datos de conexión a la base de datos. Por defecto usa Apache Derby
 * embebido (no hay que instalar nada); para usar MySQL basta con elegirlo en la ventana del servidor.
 */
public class ConexionBD {

    public static final String DERBY = "Derby · fidecompro";
    public static final String MYSQL = "MySQL · fidecompro";

    // Carpeta donde Derby guarda los archivos de la base (dentro de la carpeta del proyecto)
    public static final String CARPETA_DERBY = "basedatos";

    private static ConexionBD instancia;

    private String motor;
    private String url;
    private String usuario;
    private String clave;

    private ConexionBD() {
        usarMotor(DERBY);
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public final void usarMotor(String motor) {
        this.motor = motor;
        cargarDriver(MYSQL.equals(motor) ? "com.mysql.cj.jdbc.Driver" : "org.apache.derby.jdbc.EmbeddedDriver");
        if (MYSQL.equals(motor)) {
            url = "jdbc:mysql://localhost:3306/fidecompro?createDatabaseIfNotExist=true&serverTimezone=America/Costa_Rica";
            usuario = "root";
            clave = "root";
        } else {
            File carpeta = new File(CARPETA_DERBY);
            carpeta.mkdirs();
            System.setProperty("derby.stream.error.file", new File(carpeta, "derby.log").getPath());
            url = "jdbc:derby:" + new File(carpeta, "fidecompro").getPath() + ";create=true";
            usuario = "fidecompro";
            clave = "fidecompro";
        }
    }

    // Carga el driver JDBC (está en las librerías que Maven descarga)
    private void cargarDriver(String clase) {
        try {
            Class.forName(clase);
        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el driver " + clase);
        }
    }

    // Cada operación pide su propia conexión y la cierra al terminar (try-with-resources),
    // así cada hilo del servidor trabaja con su conexión sin estorbar a los demás.
    public Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public boolean esDerby() {
        return DERBY.equals(motor);
    }

    public String getMotor() {
        return motor;
    }

    public String getUrl() {
        return url;
    }
}
