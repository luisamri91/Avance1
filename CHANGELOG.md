# Historial de versiones

Cada cambio al documento de diseño o al código queda registrado aquí, con su número de versión y el commit de git donde quedó, para poder recuperar exactamente esa versión (`git checkout <commit>`).

* **Numeración:** `v1`, `v2`, `v3`… Sube en uno con cada cambio, por separado para el documento y para el código.
* **Nombre de archivo:** la versión va en el nombre, por ejemplo `Avance1_Documento_Diseno_v3.docx` o `Fidecompro-codigo_v1.zip`.
* Las copias de cada versión del documento y del código quedan también en los archivos del proyecto, en `avance1/versiones/`.

## Documento de diseño

### Documento v9 · 2026-10-08 · commit `5400cea`
* La tabla «Colecciones: cuál se usa y por qué» tiene una columna nueva con el ejemplo del profesor que respalda cada elección: `EjemploList`, `EjemploCollection`, `EjemploSet`, `EjemploMap` y `EjemploTreeMap`.
* El texto explica, con `EjemploColeccionGenerica` y `EjemploColeccionNoGenerica`, por qué todas las colecciones son genéricas (no hace falta cast).

### Documento v8 · 2026-10-08 · commit `626c373`
* El profesor explicó que un error común es usar listas para todo. Ahora cada colección se elige según su uso, como en sus ejemplos (`EjemploList`, `EjemploSet`, `EjemploMap`, `EjemploTreeMap`), y se declaran por su interface: `List<T> lista = new ArrayList<>()`.
* `List` donde importa el orden: detalles de la factura, productos de la categoría, categorías del inventario y los resultados de las consultas.
* `Set<String>` para las advertencias de `ArticuloHogar` (no se repiten).
* `Map<Integer, Usuario>` y `Map<Integer, Cliente>` (`HashMap`) en los registros: el CRUD por id llega directo sin recorrer la colección.
* `TreeMap<Integer, Factura>` en `RegistroFacturas`: búsqueda por número y facturas ordenadas por número.
* `CopyOnWriteArrayList` para los clientes conectados al servidor (segura para hilos); nota sobre `ConcurrentHashMap` en el servidor.
* Nueva tabla «Colecciones: cuál se usa y por qué» al final de la sección 4.2 y fila de colecciones en la tabla de conceptos del curso.

### Documento v7 · 2026-10-08 · commit `047b31c`
* El profesor indicó que el Avance 1 es conceptual y pide tres partes: historias de usuario, diagrama de clases y bocetos (login, menú y pantallas CRUD de las clases). Se agrega al inicio una tabla que indica dónde está cada una.
* La sección 6 abre con una tabla que relaciona cada pantalla con la clase que administra y sus operaciones CRUD.
* Nuevo boceto P10 de gestión de categorías (antes solo se mencionaba como diálogo).
* La HU 7 se llama ahora "Administrar categorías" en el resumen.
* El nombre del profesor (Mario Alberto Vargas Montes) queda en la portada.

### Documento v6 · 2026-10-08 · commit `99f55d4`
* Diseño alineado con lo que el profesor pidió para el proyecto final: sockets, hilos, pantallas JFrame, base de datos MySQL o Derby, CRUD completo desde pantallas, varios clientes a la vez y un video final.
* Nueva sección 1.2 con esos requisitos y dónde los cumple el diseño.
* CRUD completo: usuarios, clientes, categorías y productos ahora se pueden **eliminar**. Si un registro ya está en facturas, se desactiva para no perder el historial. HU 3, HU 6 y HU 8 actualizadas; botones *Eliminar* y *Actualizar lista* en P3, P4 y P8.
* Ventana del servidor (`VentanaServidor`): nueva historia HU 16, nuevo requerimiento funcional 11 y nuevo prototipo P9 con clientes conectados y bitácora.
* Nueva sección 4.6 con el modelo de la base de datos (seis tablas).
* Nueva sección 8 con la matriz del CRUD y el guion del video final.
* Requerimientos no funcionales de pantallas, sockets, concurrencia y base de datos ajustados.

### Documento v5 · 2026-10-08 · commit `9a7a9a7`
* Se agregan dos capítulos nuevos con el formato pedido (tabla con #, Requerimiento, Descripción y Prioridad): **Lista de requerimientos funcionales** (10) y **Lista de requerimientos no funcionales** (8).
* Cada requerimiento funcional indica qué historias de usuario lo cubren.
* Las secciones siguientes se renumeran: Entidades y clases pasa a 4, Historias de usuario a 5, Prototipos a 6 y Trazabilidad a 7.
* Los requerimientos se editan en `docs/requerimientos.json`.

### Documento v4 · 2026-10-08 · commit `b3e7477`
* Las 15 historias de usuario pasan al formato de tabla pedido: ID, rol, característica, razón y escenarios de aceptación con número, título, contexto (Dado que), evento (Cuando) y resultado esperado. Son 57 escenarios en total.
* La sección 3 va en páginas horizontales para que la tabla se lea bien.
* Los identificadores pasan de HU-01 a HU 1 en todo el documento.
* El documento se genera con `docs/construir_documento.py` y las historias se editan en `docs/historias_usuario.json`.

### Documento v3 · 2026-10-08 · commit `bf66b07`
* Se agrega la tabla de control de versiones al inicio del documento y la versión en la portada.
* La versión va en el nombre del archivo y la numeración pasa a v1, v2, v3…

### Documento v2 · 2026-10-08 · commit `07de778`
* Modelo de clases rehecho con el estilo de las semanas 1 a 5 del curso (práctica MultiSports): `Producto` abstracto con `Abarrote`, `Bebida` y `ArticuloHogar`; `Persona` abstracta con `Usuario` y `Cliente`; interface `Mostrable`; ids `static` autoincrementales; `Inventario` y `Categoria` con CRUD por id; `Registro…` con `ArrayList`; `Comparable`/`Comparator`; excepciones propias.
* Tabla que relaciona cada tema de cada semana con las clases donde se aplica.
* El diagrama de dominio se divide en dos (inventario y facturación) para que se lea mejor.
* Hilos, sockets, Swing y base de datos pasan a "arquitectura prevista", porque el curso aún no los cubre.
* Historias HU-07, HU-08, HU-09 y HU-15 y prototipo P4 ajustados al tipo de producto.
* Se elimina `MovimientoInventario` y `CategoriaProducto` (el impuesto lo define cada tipo de producto).

### Documento v1 · 2026-10-06 · commit `9be1af4`
* Primera versión del documento de diseño del Avance 1 (Proyecto 1, Fidecompro): clases con atributos, métodos y relaciones; 15 historias de usuario; 8 prototipos de pantalla; diagramas de arquitectura, secuencia y navegación.

## Diagrama de clases (Word aparte)

### Diagrama de clases v3 · 2026-10-08 · commit `5400cea`
* Word aparte con la tabla de colecciones del documento v9 (cita los ejemplos del profesor). El `.drawio` sigue en v2 porque los diagramas no cambiaron.

### Diagrama de clases v2 · 2026-10-08 · commit `626c373`
* Mismos cambios de colecciones del documento v8: `List`, `Set`, `Map`, `TreeMap` y `CopyOnWriteArrayList` en los diagramas y en las tablas de clases, más la tabla «Colecciones: cuál se usa y por qué».
* `Avance1_Diagrama_de_Clases_v2.docx` y `Avance1_Diagrama_de_Clases_v2.drawio` (tres páginas, igual que v1).

### Diagrama de clases v1 · 2026-10-08 · commit `7900a76`
* Word aparte con solo la parte de clases del Avance 1: entidades o clases identificadas, sus atributos, métodos y relaciones (diagramas y descripción de cada clase, del dominio y cliente-servidor).
* Se genera con `docs/construir_diagrama_clases.py` a partir de la sección "Entidades y clases" del documento de diseño v7, así que siempre coincide con él.
* Versión editable en draw.io (`Avance1_Diagrama_de_Clases_v1.drawio`, commit `e137e8c`) con tres páginas: inventario y excepciones, personas y facturación, y cliente-servidor. Se genera con `docs/exportar_drawio.py` desde los diagramas de Mermaid.

### Diagrama de clases simplificado v4 · 2026-10-08 · commit `626c373`
* Colecciones declaradas por su interface: `List<Producto>`, `List<Categoria>` y el atributo `detalles: List<DetalleFactura>` en `Factura`.
* La simbología agrega el apartado **Colecciones**: qué es `List<Tipo>` y que el diagrama completo también usa `Set` y `Map`/`TreeMap`.

### Diagrama de clases simplificado v3 · 2026-10-08 · commit `4438bd1`
* Formato vertical para poner la imagen en una página completa de Word: la simbología pasa debajo del diagrama, en dos columnas, y la imagen queda con la proporción de una página carta con márgenes de 2 cm (ancho/alto = 0,8).
* Los métodos muestran solo los tipos de sus parámetros, por ejemplo `agregarDetalle(Producto, int)`, para que las clases sean más angostas.

### Diagrama de clases simplificado v2 · 2026-10-08 · commit `75f343e`
* Se agrega al lado del diagrama un cuadro de **simbología** que explica cada símbolo: partes de la clase, visibilidad (+, -, #), subrayado (static), cursiva (abstracto), «abstract» e «interface», multiplicidad (1, *, 1..*) con un ejemplo, y cada tipo de flecha (herencia, implementación, composición, agregación y asociación).

### Diagrama de clases simplificado v1 · 2026-10-08 · commit `6942679`
* Un solo diagrama, en una página, con las 12 clases principales del dominio (personas, productos, inventario y facturación), sus atributos y métodos más importantes y sus relaciones. Deja fuera los registros, las excepciones, las enumeraciones y las clases cliente-servidor.
* Archivo editable `Avance1_Diagrama_de_Clases_Simplificado_v1.drawio` e imagen `.png`. Fuente: `docs/diagramas/00_clases_simplificado.mmd`.

## Código

### Código v3 · 2026-10-08 · commit `626c373`
* `RegistroUsuarios` y `RegistroClientes` usan `Map<Integer, …> = new HashMap<>()` con el id como clave; `RegistroFacturas` usa `TreeMap<Integer, Factura>` con el número como clave.
* `ArticuloHogar.advertencias` es un `Set<String> = new HashSet<>()`; las demás colecciones se declaran como `List<T>`.
* El `main` muestra que el `Set` no repite una advertencia y que el `TreeMap` da la primera y la última factura.

### Código v2 · 2026-10-08 · commit `99f55d4`
* `eliminarCliente` y `eliminarUsuario` en los registros; `tieneFacturasCliente` y `tieneFacturasUsuario` en `RegistroFacturas`.
* El `main` muestra la regla: un cliente con facturas se desactiva y uno sin facturas se elimina.

### Código v1 · 2026-10-08 · commit `442187a`
* Proyecto Maven `Fidecompro` (Java 17) con las clases del modelo de dominio del documento v2.
* `main` de consola que recorre las historias de usuario principales, incluida la venta simultánea desde dos cajas con hilos.
* Código de referencia para entender el diseño; no es un entregable.
