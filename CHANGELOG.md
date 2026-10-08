# Historial de versiones

Cada cambio al documento de diseño o al código queda registrado aquí, con su número de versión y el commit de git donde quedó, para poder recuperar exactamente esa versión (`git checkout <commit>`).

* **Numeración:** `v1`, `v2`, `v3`… Sube en uno con cada cambio, por separado para el documento y para el código.
* **Nombre de archivo:** la versión va en el nombre, por ejemplo `Avance1_Documento_Diseno_v3.docx` o `Fidecompro-codigo_v1.zip`.
* Las copias de cada versión del documento y del código quedan también en los archivos del proyecto, en `avance1/versiones/`.

## Documento de diseño

### Documento v4 · 2026-10-08
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

## Código

### Código v1 · 2026-10-08 · commit `442187a`
* Proyecto Maven `Fidecompro` (Java 17) con las clases del modelo de dominio del documento v2.
* `main` de consola que recorre las historias de usuario principales, incluida la venta simultánea desde dos cajas con hilos.
* Código de referencia para entender el diseño; no es un entregable.
