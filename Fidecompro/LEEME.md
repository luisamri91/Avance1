# Fidecompro v6 · sistema completo

Sistema cliente-servidor de facturación e inventario en Java Swing, con sockets, hilos y base de datos Apache Derby (o MySQL).

## Cómo correrlo en NetBeans

1. *File > Open Project* y elegir la carpeta `Fidecompro` (es un proyecto Maven; la primera vez NetBeans descarga Derby solo).
2. *Run* (F6). Se abre el lanzador **Fidecompro v6**.
3. Presionar **Abrir servidor** y en esa ventana **Iniciar**. La primera vez crea la base `basedatos/fidecompro` con datos de ejemplo.
4. Presionar **Abrir caja (cliente)** una vez por cada caja que se quiera mostrar (por ejemplo tres) e iniciar sesión en cada una.

| Usuario | Contraseña | Rol |
|---|---|---|
| `mrodriguez` | `Admin2026` | ADMINISTRADOR |
| `cjimenez` | `Venta2026` | VENDEDOR |
| `apineda` | `Venta2026` | VENDEDOR |

Fuera de NetBeans: `mvn package` arma `target/Fidecompro-6.jar` con todo adentro.

* `java -jar Fidecompro-6.jar` abre el lanzador.
* `java -jar Fidecompro-6.jar servidor` abre solo el servidor.
* `java -jar Fidecompro-6.jar cliente 192.168.1.10` abre una caja que se conecta a otro equipo.

## Ver la base de datos

* En la ventana del servidor, pestaña **Base de datos**: muestra cada tabla tal como está guardada.
* En NetBeans, mientras el servidor corre: *Services > Databases > New Connection*, driver *Java DB (Network)*, URL `jdbc:derby://localhost:1527/basedatos/fidecompro`, usuario y clave `fidecompro`.
* Para usar MySQL: elegir **MySQL · fidecompro** en la ventana del servidor. Las tablas se crean solas con `src/main/resources/sql/fidecompro_mysql.sql`; el usuario y la clave de MySQL están en `ConexionBD` (root / root).

## Paquetes

| Paquete | Qué tiene |
|---|---|
| `modelo` | Clases del diagrama de dominio (Producto, Abarrote, Bebida, ArticuloHogar, Cliente, Usuario, Factura…) |
| `red` | Lo que viaja por el socket: `Solicitud`, `Respuesta`, `TipoOperacion` y datos auxiliares |
| `servidor` | `ServidorFacturacion` (ServerSocket + pool de 10 hilos), `ManejadorCliente` (un hilo por caja), servicios y `VentanaServidor` |
| `datos` | `ConexionBD` (Singleton), DAO de cada tabla y `CreadorBaseDatos` |
| `cliente` | `ClienteSocket`, `VentanaLogin`, `VentanaPrincipal` y una ventana JFrame por módulo |
| `util` | `Estilo`: colores, formatos y mensajes comunes |

## Concurrencia

* Cada caja conectada se atiende en su propio hilo (`ExecutorService` de 10 hilos); la ventana del servidor muestra el hilo de cada una.
* La lista de clientes conectados es una `CopyOnWriteArrayList`.
* Ajustes de stock, facturas y anulaciones pasan por un bloque `synchronized` con un solo candado y una transacción JDBC, así dos cajas nunca venden la misma unidad (HU 9) y el número de factura no deja huecos.
* La caja pide la lista de productos con un `SwingWorker` (`HiloActualizacionStock`) y revisa cada 3 segundos que el servidor siga en línea.

`PruebaModeloConsola` es la prueba por consola del modelo de las versiones anteriores (sin servidor ni base de datos).
