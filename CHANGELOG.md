# Historial de versiones

Cada cambio al documento de diseño o al código queda registrado aquí, con su número de versión y el commit de git donde quedó, para poder recuperar exactamente esa versión (`git checkout <commit>`).

* **Documento de diseño:** `vX.Y`. Sube `Y` con cada iteración; sube `X` con cada nuevo avance del proyecto.
* **Código:** `vX.Y.Z`. Sube `Z` con correcciones, `Y` con funcionalidad nueva y `X` con cada entrega mayor.
* Las copias de cada versión del documento y del código quedan también en los archivos del proyecto, en `avance1/versiones/`.

## Documento de diseño

### Documento v1.2 · 2026-10-08 · commit `442187a`
* Se agrega la tabla de control de versiones al inicio del documento y la versión en la portada.

### Documento v1.1 · 2026-10-08 · commit `07de778`
* Modelo de clases rehecho con el estilo de las semanas 1 a 5 del curso (práctica MultiSports): `Producto` abstracto con `Abarrote`, `Bebida` y `ArticuloHogar`; `Persona` abstracta con `Usuario` y `Cliente`; interface `Mostrable`; ids `static` autoincrementales; `Inventario` y `Categoria` con CRUD por id; `Registro…` con `ArrayList`; `Comparable`/`Comparator`; excepciones propias.
* Tabla que relaciona cada tema de cada semana con las clases donde se aplica.
* El diagrama de dominio se divide en dos (inventario y facturación) para que se lea mejor.
* Hilos, sockets, Swing y base de datos pasan a "arquitectura prevista", porque el curso aún no los cubre.
* Historias HU-07, HU-08, HU-09 y HU-15 y prototipo P4 ajustados al tipo de producto.
* Se elimina `MovimientoInventario` y `CategoriaProducto` (el impuesto lo define cada tipo de producto).

### Documento v1.0 · 2026-10-06 · commit `9be1af4`
* Primera versión del documento de diseño del Avance 1 (Proyecto 1, Fidecompro): clases con atributos, métodos y relaciones; 15 historias de usuario; 8 prototipos de pantalla; diagramas de arquitectura, secuencia y navegación.

## Código

### Código v0.1.0 · 2026-10-08 · commit `442187a`
* Proyecto Maven `Fidecompro` (Java 17) con las clases del modelo de dominio del documento v1.1.
* `main` de consola que recorre las historias de usuario principales, incluida la venta simultánea desde dos cajas con hilos.
* Código de referencia para entender el diseño; no es un entregable.
