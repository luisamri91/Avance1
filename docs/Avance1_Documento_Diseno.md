---
title: "Proyecto Final – Avance 1: Documento de Diseño"
subtitle: "Sistema de Facturación e Inventario Fidecompro (Proyecto 1)"
author: "Estudiante: [Nombre completo] · Carné: [número]"
date: "Programación Cliente-Servidor Concurrente · Profesor(a): [nombre] · Octubre 2026"
lang: es
---

# 1. Introducción

La cadena de venta al por mayor **Fidecompro** necesita una aplicación de escritorio para llevar el inventario de sus productos y emitir facturas a sus clientes. Este documento presenta el diseño de la solución para el Avance 1 del proyecto final: las clases identificadas (con atributos, métodos y relaciones), las funcionalidades expresadas como historias de usuario y los prototipos de la interfaz gráfica.

## 1.1 Requerimientos del enunciado

| # | Requerimiento del enunciado | Historias de usuario que lo cubren |
|---|---|---|
| R1 | Creación de los registros de los clientes | HU-04, HU-05, HU-06 |
| R2 | Registro de los productos (varios tipos de productos) | HU-07, HU-08, HU-09, HU-10 |
| R3 | Crear facturas a los clientes | HU-11, HU-12, HU-14, HU-15 |
| R4 | Entregar la factura física (archivo con el desglose de pago) | HU-13 |
| R5 | Ingreso con usuario y contraseña | HU-01, HU-02, HU-03 |

## 1.2 Alcance y decisiones técnicas

* **Lenguaje y entorno:** Java 17, proyecto Maven en NetBeans IDE, interfaz gráfica con **Java Swing**. No se usa ningún framework; todo el código se escribe desde cero.
* **Base del diseño:** el modelo de clases (sección 2) aplica lo visto en las semanas 1 a 5: herencia, clases abstractas, interfaces, polimorfismo, colecciones y excepciones propias. Hilos, sockets, Swing y base de datos aún no se han visto en el curso; se incluyen como **arquitectura prevista** para cumplir el carácter cliente-servidor concurrente del proyecto y se detallarán en los siguientes avances.
* **Arquitectura cliente-servidor:** un **servidor** central atiende a varias cajas (clientes Swing) al mismo tiempo mediante **sockets TCP**. Cada conexión es atendida por su propio hilo (`ManejadorCliente`) dentro de un pool de hilos.
* **Concurrencia:** cuando dos vendedores facturan el mismo producto a la vez, el servidor valida y descuenta el stock dentro de un bloque `synchronized`, de modo que nunca se venda más inventario del que existe. El número de factura sale del contador `static Factura.ultimoNumero`, que se incrementa dentro de un método `synchronized` para que dos facturas nunca reciban el mismo número. En el cliente, las consultas largas se ejecutan con `SwingWorker` para no congelar la ventana.
* **Persistencia:** base de datos MySQL accedida con JDBC (API estándar de Java) a través de clases DAO.
* **Factura física:** el servidor genera un archivo `.txt` (y opcionalmente `.html`) con el desglose completo de la factura, que el usuario guarda o imprime.
* **Roles:** `ADMINISTRADOR` (gestiona usuarios, catálogo e inventario, puede anular facturas) y `VENDEDOR` (registra clientes y emite facturas).

![Arquitectura general de la solución](img/03_arquitectura.png)

# 2. Entidades y clases

El modelo de clases sigue la forma de trabajo vista en las semanas 1 a 5 del curso, en particular la práctica integradora MultiSports de la semana 5:

| Concepto del curso | Dónde se aplica en Fidecompro |
|---|---|
| Clase abstracta con subclases que sobrescriben un método (semanas 2 y 3) | `Producto` → `Abarrote`, `Bebida`, `ArticuloHogar`; `Persona` → `Usuario`, `Cliente` |
| Interface terminada en *-able* con `mostrarInformacion()` (semanas 3 y 5) | `Mostrable`, implementada por personas, productos, facturas y detalles |
| Polimorfismo: distintos objetos responden distinto al mismo mensaje (semana 3) | Cada tipo de producto responde `obtenerPorcentajeImpuesto()` a su manera |
| Atributos `static` e IDs autoincrementales (semanas 1 y 5) | `idAutoIncremental` en `Producto`, `Categoria`, `Cliente` y `Usuario`; `Factura.ultimoNumero` |
| Constantes `final` (semana 1, ejemplo del IVA) | `Producto.IVA_GENERAL = 0.13` y `Abarrote.IVA_CANASTA_BASICA = 0.01` |
| Composición y agregación con `ArrayList<T>` (semanas 1 y 5) | `Factura` *contiene* sus `DetalleFactura`; `Inventario` agrupa `Categoria` y `Categoria` agrupa `Producto` |
| Métodos `agregarX / editarX / eliminarX / mostrarX` por id (semana 5) | `Inventario`, `Categoria`, `RegistroClientes`, `RegistroUsuarios`, `RegistroFacturas` |
| Enumeraciones para catálogos cerrados (semana 1) | `Rol`, `TipoIdentificacion`, `MetodoPago`, `EstadoFactura` |
| `Comparable` y `Comparator` (semana 4) | `Producto implements Comparable<Producto>` (orden por nombre) y `ComparadorPorExistencias` |
| Excepciones propias y `try/catch/finally` (semana 4) | `StockInsuficienteException`, `CredencialesInvalidasException`, `RegistroNoEncontradoException`; `IOException` al escribir la factura |

Las clases se organizan en paquetes de un proyecto Maven de NetBeans:

| Paquete | Contenido |
|---|---|
| `com.fidecompro.modelo` | Entidades, enumeraciones, interface `Mostrable` y clases que administran colecciones (`Inventario`, `Registro…`) |
| `com.fidecompro.excepciones` | Excepciones propias del negocio |
| `com.fidecompro.servidor` | Servidor de sockets, hilos de atención y servicios (arquitectura prevista, sección 2.3) |
| `com.fidecompro.datos` | Conexión a la base de datos y clases DAO (arquitectura prevista) |
| `com.fidecompro.cliente` | Conexión del cliente y ventanas Swing (arquitectura prevista) |

## 2.1 Diagramas de clases del modelo de dominio

El modelo se presenta en dos diagramas para que se lea con claridad. El primero cubre el inventario, los tipos de producto y las excepciones; el segundo, las personas y la facturación.

![Diagrama de clases: inventario, tipos de producto y excepciones](img/01a_modelo_inventario.png)

![Diagrama de clases: personas y facturación](img/01b_modelo_facturacion.png)

## 2.2 Descripción de las clases del dominio

Todas las clases tienen constructor con sus campos, *getters* y *setters*. En las tablas se listan solo los métodos con lógica propia.

### Mostrable (interface)
Contrato que obliga a cada objeto a describirse a sí mismo como texto. Se usa para listar en pantalla y para armar la factura física.

| Método | Descripción |
|---|---|
| mostrarInformacion(): String | Devuelve la información del objeto lista para mostrar o imprimir |

### Persona (abstracta)
Datos comunes de quien usa el sistema y de a quien se le vende. No se instancia directamente. Implementa `Mostrable`.

| Atributo | Tipo | Descripción |
|---|---|---|
| nombre | String | Nombre completo o razón social |
| telefono | String | Teléfono de contacto |
| correo | String | Correo electrónico |

**Relaciones:** superclase de `Usuario` y `Cliente` (herencia jerárquica).

### Usuario (extends Persona)
Persona que ingresa al sistema: vendedor o administrador.

| Atributo | Tipo | Descripción |
|---|---|---|
| idAutoIncremental | static int | Contador de la clase para asignar el siguiente id |
| id | int | Identificador asignado en el constructor |
| nombreUsuario | String | Nombre con el que inicia sesión (único) |
| contrasena | String | Contraseña de acceso |
| rol | Rol | `ADMINISTRADOR` o `VENDEDOR` |
| activo | boolean | Un usuario inactivo no puede ingresar |

| Método | Descripción |
|---|---|
| validarCredenciales(String, String): boolean | Compara usuario y contraseña con los suyos y revisa que esté activo |
| esAdministrador(): boolean | Indica si puede entrar a los módulos restringidos |
| mostrarInformacion(): String | Sobrescribe el de `Persona` agregando usuario y rol |

**Relaciones:** emite muchas `Factura` (1 a \*); lo administra `RegistroUsuarios`.

### Cliente (extends Persona)
Comercio o persona a quien Fidecompro le vende.

| Atributo | Tipo | Descripción |
|---|---|---|
| idAutoIncremental | static int | Contador de la clase |
| id | int | Identificador |
| tipoIdentificacion | TipoIdentificacion | Física, jurídica, DIMEX o pasaporte |
| identificacion | String | Número de cédula o documento (único) |
| direccion | String | Dirección de entrega |
| fechaRegistro | LocalDate | Fecha en que se creó el registro |
| activo | boolean | Los clientes inactivos no aparecen al facturar |

| Método | Descripción |
|---|---|
| mostrarInformacion(): String | Sobrescribe el de `Persona` agregando identificación y dirección |

**Relaciones:** tiene muchas `Factura` (1 a \*); lo administra `RegistroClientes`.

### Producto (abstracta)
Representa cualquier artículo que vende la cadena. Como el enunciado indica que Fidecompro maneja **varios tipos de productos**, cada tipo es una subclase, igual que `Pelota`, `Bate` y `EquipoDeAnotacion` en la práctica MultiSports. Implementa `Mostrable` y `Comparable<Producto>`.

| Atributo | Tipo | Descripción |
|---|---|---|
| IVA_GENERAL | static final double | Constante `0.13` (tarifa general del IVA) |
| idAutoIncremental | static int | Contador de la clase |
| id | int | Identificador |
| codigo | String | Código interno único (p. ej. `ABR-0012`) |
| nombre, descripcion | String | Datos descriptivos |
| valorCompra | double | Costo al que Fidecompro compra el producto |
| valorVenta | double | Precio de venta sin impuesto |
| existencias | int | Unidades disponibles |
| stockMinimo | int | Nivel a partir del cual se alerta reabastecer |
| categoria | Categoria | Categoría a la que pertenece |

| Método | Descripción |
|---|---|
| obtenerPorcentajeImpuesto(): double | **Abstracto.** Cada tipo de producto define su impuesto (polimorfismo) |
| descontarExistencias(int): void | Resta unidades; lanza `StockInsuficienteException` si no alcanzan |
| aumentarExistencias(int): void | Suma unidades (compras, ajustes o facturas anuladas) |
| necesitaReabastecer(): boolean | `existencias <= stockMinimo` |
| compareTo(Producto): int | Orden natural por nombre, para listar el catálogo con `Collections.sort` |
| mostrarInformacion(): String | Código, nombre, precio y existencias |

**Relaciones:** superclase de `Abarrote`, `Bebida` y `ArticuloHogar`; agregado en `Categoria` (\* a 1); referenciado por `DetalleFactura`.

### Abarrote, Bebida y ArticuloHogar (extends Producto)

| Subclase | Atributos propios | Impuesto (`obtenerPorcentajeImpuesto`) | Métodos propios |
|---|---|---|---|
| **Abarrote** (arroz, frijoles, aceite, café…) | `fechaVencimiento: LocalDate`, `pesoKg: double`, `canastaBasica: boolean`; constante `IVA_CANASTA_BASICA = 0.01` | 1 % si es canasta básica, si no `IVA_GENERAL` | `estaVencido(): boolean` |
| **Bebida** (refrescos, jugos, agua) | `volumenMl: int`, `unidadesPorPaquete: int`, `retornable: boolean` | `IVA_GENERAL` | — |
| **ArticuloHogar** (limpieza e higiene) | `marca: String`, `presentacion: String`, `advertencias: ArrayList<String>` | `IVA_GENERAL` | `agregarAdvertencia(String)`, `eliminarAdvertencia(String)` |

Las tres sobrescriben `mostrarInformacion()` llamando a `super.mostrarInformacion()` y agregando sus datos propios.

### Categoria
Agrupa productos para ordenar el catálogo (Abarrotes, Bebidas, Limpieza, Higiene…), igual que en MultiSports.

| Atributo | Tipo | Descripción |
|---|---|---|
| idAutoIncremental | static int | Contador de la clase |
| id | int | Identificador |
| nombre, descripcion | String | Datos de la categoría |
| productos | ArrayList&lt;Producto&gt; | Productos de la categoría |

| Método | Descripción |
|---|---|
| agregarProducto(Producto): void | Agrega un producto y le asigna esta categoría |
| editarProducto(int idProducto, Producto): void | Reemplaza los datos del producto con ese id |
| eliminarProducto(int idProducto): void | Quita el producto; lanza `RegistroNoEncontradoException` si no existe |
| buscarProducto(int idProducto): Producto | Busca por id |
| mostrarProductos(): String | Une el `mostrarInformacion()` de cada producto |

**Relaciones:** agregación con `Producto` (1 a \*); agregada en `Inventario`.

### Inventario
Punto central del catálogo de una sede.

| Atributo | Tipo | Descripción |
|---|---|---|
| nombre | String | Nombre del inventario |
| sede | String | Sucursal de Fidecompro |
| categorias | ArrayList&lt;Categoria&gt; | Categorías registradas |

| Método | Descripción |
|---|---|
| agregarCategoria / editarCategoria / eliminarCategoria | CRUD de categorías por id |
| agregarProductoCategoria(int idCategoria, Producto) | Agrega un producto en la categoría indicada |
| editarProductoCategoria / eliminarProductoCategoria | Edita o elimina un producto dentro de una categoría |
| mostrarProductosCategoria(int idCategoria): String | Lista los productos de una categoría |
| buscarProducto(String codigo): Producto | Busca en todas las categorías; se usa al facturar |
| productosBajoMinimo(): ArrayList&lt;Producto&gt; | Productos con `necesitaReabastecer()`, ordenados con `ComparadorPorExistencias` |

### ComparadorPorExistencias (implements Comparator&lt;Producto&gt;)
Orden alterno al natural: de menos a más existencias, para mostrar primero lo que urge reabastecer.

### Factura
Documento de venta emitido a un cliente. Implementa `Mostrable`.

| Atributo | Tipo | Descripción |
|---|---|---|
| ultimoNumero | static int | Último consecutivo usado (como `Factura.ultimoNumero` en el ejemplo de la semana 1) |
| numero | int | Consecutivo de esta factura (se muestra como `FC-000123`) |
| fecha | LocalDateTime | Fecha y hora de emisión |
| cliente | Cliente | A quién se factura |
| vendedor | Usuario | Quién la emite |
| detalles | ArrayList&lt;DetalleFactura&gt; | Líneas de la factura |
| porcentajeDescuento | double | Descuento aplicado al subtotal |
| metodoPago | MetodoPago | Efectivo, tarjeta o transferencia |
| estado | EstadoFactura | `EMITIDA` o `ANULADA` |

| Método | Descripción |
|---|---|
| agregarDetalle(Producto, int): void | Crea la línea; si el producto ya está, suma la cantidad. Lanza `StockInsuficienteException` |
| eliminarDetalle(int): void | Quita una línea |
| calcularSubtotal / calcularDescuento / calcularImpuesto / calcularTotal | Recorren `detalles` y suman; el impuesto de cada línea lo da su producto (polimorfismo) |
| anular(): void | Cambia el estado a `ANULADA` |
| mostrarInformacion(): String | Arma el desglose completo de la factura como texto |
| generarArchivo(String carpeta): void | Escribe `mostrarInformacion()` en `FC-000123.txt` con `FileWriter`, manejando `IOException` con `try/catch/finally` |

**Relaciones:** **composición** con `DetalleFactura` (1 a 1..\*): las líneas no existen sin su factura. Asociación con `Cliente` y `Usuario` (\* a 1). La administra `RegistroFacturas`.

### DetalleFactura
Línea de una factura. Implementa `Mostrable`.

| Atributo | Tipo | Descripción |
|---|---|---|
| producto | Producto | Producto vendido |
| cantidad | int | Unidades |
| precioUnitario | double | Copia del `valorVenta` al momento de la venta, para conservar el histórico |

| Método | Descripción |
|---|---|
| calcularSubtotal(): double | `cantidad × precioUnitario` |
| calcularImpuesto(): double | `subtotal × producto.obtenerPorcentajeImpuesto()` |
| calcularTotal(): double | Subtotal más impuesto |
| mostrarInformacion(): String | Una línea del desglose: cantidad, descripción, precio y total |

### RegistroUsuarios, RegistroClientes y RegistroFacturas
Clases que administran las colecciones, con el mismo patrón de `Inventario`.

| Clase | Atributo | Métodos |
|---|---|---|
| **RegistroUsuarios** | `usuarios: ArrayList<Usuario>` | `agregarUsuario`, `editarUsuario`, `desactivarUsuario`, `iniciarSesion(nombreUsuario, contrasena)` que devuelve el `Usuario` o lanza `CredencialesInvalidasException` |
| **RegistroClientes** | `clientes: ArrayList<Cliente>` | `agregarCliente` (rechaza identificaciones repetidas), `editarCliente`, `desactivarCliente`, `buscarCliente(identificacion)`, `mostrarClientes` |
| **RegistroFacturas** | `facturas: ArrayList<Factura>` | `agregarFactura`, `buscarFactura(numero)`, `anularFactura(numero)` (devuelve las existencias al inventario), `facturasPorFecha(desde, hasta)` |

### Excepciones propias (extends Exception)

| Excepción | Cuándo se lanza |
|---|---|
| StockInsuficienteException | Se intenta facturar o descontar más unidades de las que hay |
| CredencialesInvalidasException | Usuario o contraseña incorrectos, o usuario inactivo |
| RegistroNoEncontradoException | Se busca, edita o elimina un id o identificación que no existe |

### Enumeraciones
* `Rol`: ADMINISTRADOR, VENDEDOR.
* `TipoIdentificacion`: FISICA, JURIDICA, DIMEX, PASAPORTE.
* `MetodoPago`: EFECTIVO, TARJETA, TRANSFERENCIA.
* `EstadoFactura`: EMITIDA, ANULADA.

## 2.3 Diagrama de clases cliente-servidor

Este diagrama es la **arquitectura prevista** para los siguientes avances, cuando el curso cubra hilos, sockets, Swing y bases de datos. Muestra las clases que harán funcionar la aplicación en red y de forma concurrente sobre el modelo de la sección 2.1: las ventanas Swing, la conexión por sockets, el servidor con sus hilos, los servicios y el acceso a datos.

![Diagrama de clases cliente-servidor](img/02_clases_cliente_servidor.png)

## 2.4 Descripción de las clases cliente-servidor

| Clase | Responsabilidad | Métodos principales | Relaciones |
|---|---|---|---|
| **ServidorFacturacion** | Abre un `ServerSocket` en el puerto 5000 y acepta conexiones; cada conexión se entrega a un pool de hilos (`ExecutorService`) | `iniciar()`, `detener()`, `aceptarConexiones()`, `main()` | Crea muchos `ManejadorCliente` (1 a \*) |
| **ManejadorCliente** (implementa `Runnable`) | Atiende a un cliente conectado en su propio hilo: lee `Solicitud`, llama al servicio correspondiente y responde con `Respuesta`. Guarda el usuario de la sesión para validar permisos | `run()`, `procesar(Solicitud)`, `cerrarConexion()` | Usa los cuatro servicios |
| **Solicitud** / **Respuesta** | Objetos serializables que viajan por el socket. La solicitud indica la operación (`TipoOperacion`) y los datos; la respuesta indica éxito, mensaje y datos | `getOperacion()`, `ok()`, `error()` | `Solicitud` usa `TipoOperacion` |
| **ServicioAutenticacion** | Valida credenciales, bloquea usuarios inactivos y registra usuarios nuevos | `iniciarSesion()`, `registrarUsuario()`, `cifrarContrasena()` | Usa `UsuarioDAO` |
| **ServicioClientes** | Reglas de negocio de clientes (identificación única, campos obligatorios) | `registrar()`, `actualizar()`, `buscar()`, `listar()` | Usa `ClienteDAO` |
| **ServicioInventario** | Catálogo y stock. **Sección crítica:** `reservarStock()` y `ajustarStock()` se sincronizan sobre un mismo candado para que dos hilos no descuenten el mismo producto a la vez | `registrarProducto()`, `ajustarStock()`, `reservarStock()`, `productosBajoMinimo()` | Usa `Inventario` y `ProductoDAO` |
| **ServicioFacturacion** | Arma y guarda la factura, reserva el stock, asigna el consecutivo de `Factura` en un método `synchronized` y genera el archivo | `crearFactura()`, `anularFactura()`, `listarFacturas()` | Usa `ServicioInventario`, `FacturaDAO`, `GeneradorArchivoFactura` |
| **GeneradorArchivoFactura** | Escribe la factura física con su desglose en `.txt` o `.html` usando `FileWriter`/`PrintWriter` | `generarTXT()`, `generarHTML()` | Usado por `ServicioFacturacion` |
| **ConexionBD** (Singleton) | Centraliza la URL y credenciales de MySQL y entrega conexiones JDBC | `getInstancia()`, `obtenerConexion()` | Usada por todos los DAO |
| **UsuarioDAO, ClienteDAO, ProductoDAO, FacturaDAO** | Ejecutan las sentencias SQL (`PreparedStatement`) de cada entidad | `insertar()`, `actualizar()`, `listar()`, `buscarPorId()`… | Dependen de `ConexionBD` |
| **ClienteSocket** | Del lado del cliente: abre el socket hacia el servidor y envía/recibe objetos | `conectar()`, `enviar(Solicitud)`, `desconectar()` | Usado por todas las ventanas |
| **VentanaLogin** (`JFrame`) | Pide usuario y contraseña; si son válidos abre la ventana principal | `btnIngresarActionPerformed()` | Abre `VentanaPrincipal` |
| **VentanaPrincipal** (`JFrame`) | Menú y pestañas de los módulos; oculta los módulos de administrador a los vendedores | `mostrarModulo()` | Contiene los paneles |
| **PanelClientes, PanelProductos, PanelFacturacion** (`JPanel`) | Pantallas de cada módulo | `cargarTabla()`, `guardar…()`, `emitirFactura()` | Usan `ClienteSocket` |
| **HiloActualizacionStock** (`SwingWorker`) | Refresca la tabla de inventario en segundo plano sin congelar la interfaz | `doInBackground()`, `done()` | Usado por `PanelProductos` |

## 2.5 Flujo concurrente de emisión de una factura

El siguiente diagrama de secuencia muestra cómo se emite una factura y dónde se controla la concurrencia.

![Diagrama de secuencia: emitir factura](img/04_secuencia_factura.png)

# 3. Historias de usuario

Prioridad según MoSCoW (**Alta** = imprescindible para la entrega final). La estimación está en puntos de historia (1 = muy simple, 8 = compleja).

## Módulo de acceso

**HU-01 · Iniciar sesión**
*Como* vendedor o administrador, *quiero* ingresar a la aplicación con mi usuario y contraseña *para* que solo el personal autorizado pueda facturar y modificar el inventario.
Prioridad: Alta · Estimación: 3 · Pantalla: P1

* Si el usuario y la contraseña son correctos y el usuario está activo, se abre el menú principal mostrando el nombre y el rol.
* Si son incorrectos, se muestra "Usuario o contraseña incorrectos" sin indicar cuál de los dos falló.
* Tras 3 intentos fallidos seguidos, el botón *Ingresar* se bloquea durante 1 minuto.
* La contraseña se escribe en un campo oculto y viaja/se guarda cifrada.

**HU-02 · Cerrar sesión**
*Como* usuario, *quiero* cerrar mi sesión *para* que otra persona no use mi cuenta en la misma caja.
Prioridad: Alta · Estimación: 1 · Pantalla: P2

* Al cerrar sesión se libera la conexión con el servidor y se regresa a la pantalla de inicio de sesión.

**HU-03 · Administrar usuarios**
*Como* administrador, *quiero* crear, editar y desactivar usuarios con rol de vendedor o administrador *para* controlar quién tiene acceso al sistema.
Prioridad: Alta · Estimación: 5 · Pantalla: P8

* El nombre de usuario no se puede repetir.
* La contraseña debe tener al menos 8 caracteres y confirmarse.
* Un usuario desactivado no puede iniciar sesión, pero sus facturas se conservan.
* El módulo no es visible para usuarios con rol `VENDEDOR`.

## Módulo de clientes

**HU-04 · Registrar cliente**
*Como* vendedor, *quiero* registrar un cliente con tipo e identificación, nombre, teléfono, correo y dirección *para* poder emitirle facturas.
Prioridad: Alta · Estimación: 3 · Pantalla: P3

* Identificación, tipo de identificación y nombre son obligatorios.
* No se permite registrar dos clientes con la misma identificación.
* El correo se valida con formato `algo@dominio`.
* Al guardar, el cliente aparece de inmediato en la tabla.

**HU-05 · Buscar y editar cliente**
*Como* vendedor, *quiero* buscar un cliente por identificación o nombre y actualizar sus datos de contacto *para* mantener la información al día.
Prioridad: Alta · Estimación: 2 · Pantalla: P3

* La búsqueda acepta coincidencias parciales y no distingue mayúsculas.
* Al seleccionar una fila de la tabla, sus datos se cargan en el formulario.

**HU-06 · Desactivar cliente**
*Como* administrador, *quiero* desactivar un cliente *para* que no aparezca al facturar sin perder sus facturas anteriores.
Prioridad: Media · Estimación: 1 · Pantalla: P3

* Se pide confirmación antes de desactivar.

## Módulo de productos e inventario

**HU-07 · Administrar tipos de producto**
*Como* administrador, *quiero* crear, editar y eliminar categorías de producto (abarrotes, bebidas, limpieza, higiene…) *para* mantener el catálogo ordenado.
Prioridad: Alta · Estimación: 2 · Pantalla: P4 (pestaña Categorías)

* El nombre de la categoría es único.
* No se puede eliminar una categoría que todavía tiene productos.

**HU-08 · Registrar producto**
*Como* administrador, *quiero* registrar un producto indicando su tipo (abarrote, bebida o artículo del hogar), código, nombre, categoría, valor de compra, valor de venta, existencias y stock mínimo *para* poder venderlo y controlar sus existencias.
Prioridad: Alta · Estimación: 3 · Pantalla: P4

* Según el tipo elegido se piden sus datos propios: fecha de vencimiento, peso y si es canasta básica (abarrote); volumen y unidades por paquete (bebida); marca y presentación (artículo del hogar).
* El impuesto lo define el tipo: 1 % para abarrotes de canasta básica y 13 % para el resto.
* El código es único; el valor de venta debe ser mayor que 0 y mayor o igual al de compra; las existencias no pueden ser negativas.
* Se puede filtrar la lista por categoría y buscar por código o nombre.

**HU-09 · Ajustar inventario**
*Como* administrador, *quiero* registrar entradas de mercadería y ajustes de inventario indicando el motivo *para* que el stock del sistema coincida con la bodega.
Prioridad: Alta · Estimación: 3 · Pantalla: P4 (diálogo Ajustar stock)

* El ajuste pide el motivo (compra, merma, conteo físico) y se confirma antes de aplicarlo.
* Un ajuste no puede dejar el stock en negativo.

**HU-10 · Alertas de stock bajo**
*Como* administrador, *quiero* ver qué productos están por debajo del stock mínimo *para* reabastecerlos a tiempo.
Prioridad: Media · Estimación: 2 · Pantallas: P2 y P4

* El menú principal muestra cuántos productos están bajo el mínimo y cuáles.
* En la tabla de productos esos productos se marcan en rojo.
* La tabla se refresca en segundo plano cada 30 segundos sin bloquear la ventana.

## Módulo de facturación

**HU-11 · Crear factura**
*Como* vendedor, *quiero* seleccionar un cliente, agregar productos con su cantidad y elegir el método de pago *para* emitirle una factura.
Prioridad: Alta · Estimación: 8 · Pantalla: P5

* Al agregar un producto se muestran sus existencias disponibles; no se puede agregar una cantidad mayor.
* Si el producto ya está en la factura, se suma la cantidad en la misma línea.
* Subtotal, descuento, impuesto (según el tipo de cada producto) y total se recalculan en cada cambio.
* No se puede emitir una factura sin cliente o sin líneas.
* Al emitir, se asigna un número consecutivo único (`FC-000123`) y se descuenta el inventario.

**HU-12 · Facturación simultánea sin vender de más**
*Como* administrador, *quiero* que varias cajas puedan facturar al mismo tiempo sin que se venda más producto del que hay *para* que el inventario sea confiable.
Prioridad: Alta · Estimación: 5 · Pantalla: P5

* El servidor atiende a cada caja en un hilo distinto.
* Si dos cajas venden el último stock de un producto al mismo tiempo, solo una factura se emite; la otra recibe el mensaje "Stock insuficiente de &lt;producto&gt;" y puede corregir la cantidad.
* Dos facturas nunca reciben el mismo número.

**HU-13 · Generar factura física**
*Como* vendedor, *quiero* obtener la factura en un archivo con el desglose de pago *para* entregarla o imprimirla al cliente.
Prioridad: Alta · Estimación: 3 · Pantalla: P6

* El archivo incluye: datos de Fidecompro, número y fecha, vendedor, datos del cliente, cada línea (cantidad, descripción, precio unitario, total), subtotal, descuento, IVA, total y método de pago.
* Se guarda como `FC-000123.txt` (u `.html`) en la carpeta que elija el usuario.
* Se muestra una vista previa antes de guardar.

**HU-14 · Consultar historial de facturas**
*Como* vendedor o administrador, *quiero* consultar las facturas por rango de fechas, cliente y estado *para* dar seguimiento a las ventas.
Prioridad: Media · Estimación: 3 · Pantalla: P7

* Se muestra el total vendido del periodo (solo facturas emitidas).
* Desde el historial se puede ver el detalle y volver a generar el archivo.

**HU-15 · Anular factura**
*Como* administrador, *quiero* anular una factura emitida por error *para* corregir las ventas y devolver el producto al inventario.
Prioridad: Media · Estimación: 3 · Pantalla: P7

* Solo el rol administrador puede anular.
* Al anular, el estado pasa a `ANULADA` y las existencias de cada línea se devuelven al inventario.
* Una factura anulada no se puede volver a anular.

## Resumen

| ID | Historia | Rol | Prioridad | Puntos | Pantalla |
|---|---|---|---|---|---|
| HU-01 | Iniciar sesión | Todos | Alta | 3 | P1 |
| HU-02 | Cerrar sesión | Todos | Alta | 1 | P2 |
| HU-03 | Administrar usuarios | Administrador | Alta | 5 | P8 |
| HU-04 | Registrar cliente | Vendedor | Alta | 3 | P3 |
| HU-05 | Buscar y editar cliente | Vendedor | Alta | 2 | P3 |
| HU-06 | Desactivar cliente | Administrador | Media | 1 | P3 |
| HU-07 | Administrar tipos de producto | Administrador | Alta | 2 | P4 |
| HU-08 | Registrar producto | Administrador | Alta | 3 | P4 |
| HU-09 | Ajustar inventario | Administrador | Alta | 3 | P4 |
| HU-10 | Alertas de stock bajo | Administrador | Media | 2 | P2, P4 |
| HU-11 | Crear factura | Vendedor | Alta | 8 | P5 |
| HU-12 | Facturación simultánea | Administrador | Alta | 5 | P5 |
| HU-13 | Generar factura física | Vendedor | Alta | 3 | P6 |
| HU-14 | Historial de facturas | Todos | Media | 3 | P7 |
| HU-15 | Anular factura | Administrador | Media | 3 | P7 |

# 4. Prototipos de interfaz gráfica

Los prototipos representan las ventanas Swing de la aplicación final (look and feel Nimbus). Se elaboraron como maquetas HTML/CSS, incluidas en `docs/prototipos/html/`, y se exportaron a imagen. Los datos mostrados son de ejemplo.

## 4.1 Mapa de navegación

![Mapa de navegación entre pantallas](img/05_navegacion.png)

## 4.2 P1 · Inicio de sesión (HU-01)

Campos de usuario y contraseña (oculta), dirección del servidor y mensaje de error ante credenciales inválidas.

![P1 Inicio de sesión](img/P1_inicio_sesion.png)

## 4.3 P2 · Menú principal (HU-02, HU-10)

Acceso a todos los módulos mediante menú y botones, resumen del día y alerta de productos bajo el stock mínimo. La barra de estado muestra el usuario, su rol y el estado de la conexión. El botón *Usuarios* solo aparece para administradores.

![P2 Menú principal](img/P2_menu_principal.png)

## 4.4 P3 · Gestión de clientes (HU-04, HU-05, HU-06)

Formulario de registro y edición, búsqueda y tabla de clientes.

![P3 Gestión de clientes](img/P3_clientes.png)

## 4.5 P4 · Productos e inventario (HU-07, HU-08, HU-09, HU-10)

Pestañas para productos, categorías y productos bajo el mínimo. El formulario cambia según el tipo de producto elegido (abarrote, bebida o artículo del hogar). Los productos bajo el mínimo se resaltan en rojo.

![P4 Productos e inventario](img/P4_productos_inventario.png)

## 4.6 P5 · Nueva factura (HU-11, HU-12)

Selección de cliente, agregado de productos con validación de existencias, tabla de líneas con el IVA de cada tipo de producto, método de pago, descuento y totales calculados.

![P5 Nueva factura](img/P5_nueva_factura.png)

## 4.7 P6 · Vista previa de la factura física (HU-13)

Contenido exacto del archivo que se genera, con el desglose de pago, y botones para guardarlo como `.txt` o `.html`.

![P6 Vista previa de factura](img/P6_vista_previa_factura.png)

## 4.8 P7 · Historial de facturas (HU-14, HU-15)

Filtros por fecha, cliente y estado; total del periodo y acciones para ver el detalle, regenerar el archivo o anular.

![P7 Historial de facturas](img/P7_historial_facturas.png)

## 4.9 P8 · Gestión de usuarios (HU-03)

Exclusiva del administrador: alta, edición, cambio de rol, restablecimiento de contraseña y desactivación de usuarios.

![P8 Gestión de usuarios](img/P8_usuarios.png)

# 5. Trazabilidad historias – clases – pantallas

| Historia | Clases principales | Pantalla |
|---|---|---|
| HU-01, HU-02 | Usuario, RegistroUsuarios, CredencialesInvalidasException, VentanaLogin | P1, P2 |
| HU-03 | Usuario, Rol, RegistroUsuarios | P8 |
| HU-04 a HU-06 | Persona, Cliente, RegistroClientes, RegistroNoEncontradoException | P3 |
| HU-07, HU-08 | Inventario, Categoria, Producto, Abarrote, Bebida, ArticuloHogar | P4 |
| HU-09, HU-10 | Producto, Inventario, ComparadorPorExistencias | P2, P4 |
| HU-11, HU-12 | Factura, DetalleFactura, Inventario, StockInsuficienteException (y en la arquitectura prevista: ServicioFacturacion, ManejadorCliente) | P5 |
| HU-13 | Factura, DetalleFactura, Mostrable | P6 |
| HU-14, HU-15 | Factura, EstadoFactura, RegistroFacturas | P7 |
