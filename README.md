# Fidecompro – Sistema de Facturación e Inventario

Proyecto final del curso **Programación Cliente-Servidor Concurrente** (Proyecto 1 del enunciado).
Aplicación de escritorio en Java Swing con arquitectura cliente-servidor por sockets y servidor multihilo.

## Versiones

Versión actual: **documento v7** y **código v2**. El detalle de cada cambio está en [`CHANGELOG.md`](CHANGELOG.md), con el commit de cada versión.

## Avance 1 – Documento de diseño

| Entregable | Archivo |
|---|---|
| Documento de diseño (para entregar) | [`docs/Avance1_Documento_Diseno_v7.pdf`](docs/Avance1_Documento_Diseno_v7.pdf) · [`.docx`](docs/Avance1_Documento_Diseno_v7.docx) |
| Fuente editable del documento | [`docs/Avance1_Documento_Diseno.md`](docs/Avance1_Documento_Diseno.md) |
| Diagramas (clases, arquitectura, secuencia, navegación) | [`docs/diagramas/`](docs/diagramas) (Mermaid) → [`docs/img/`](docs/img) |
| Prototipos de interfaz | [`docs/prototipos/html/`](docs/prototipos/html) → [`docs/img/P*.png`](docs/img) |

Contenido del documento:

1. **Entidades o clases**: modelo de dominio (Usuario, Cliente, CategoriaProducto, Producto, MovimientoInventario, Factura, DetalleFactura y enumeraciones) y clases cliente-servidor (servidor, hilos, servicios, DAO, ventanas Swing), con atributos, métodos y relaciones.
2. **Historias de usuario**: 15 historias (HU 1 a HU 15) en tabla con 57 escenarios de aceptación (Dado que / Cuando / Resultado esperado); se editan en `docs/historias_usuario.json`.
3. **Prototipos**: 8 pantallas (inicio de sesión, menú, clientes, productos e inventario, nueva factura, factura física, historial y usuarios) más el mapa de navegación.

## Código de prueba del modelo (`Fidecompro/`)

Proyecto Maven de NetBeans (Java 17) con las clases del diagrama de dominio: `Producto` abstracto con `Abarrote`, `Bebida` y `ArticuloHogar`, la interface `Mostrable`, `Inventario`/`Categoria`, `Factura`/`DetalleFactura`, los registros de usuarios, clientes y facturas, y las excepciones propias.

`com.fidecompro.Fidecompro` es un `main` de consola que recorre las historias de usuario principales: login, registro de clientes, catálogo por tipo de producto con su IVA, alertas de stock, factura con descuento, factura física en `facturas/FC-000001.txt`, dos cajas vendiendo el último producto al mismo tiempo (adelanto de hilos) y anulación.

Para correrlo: abrir la carpeta `Fidecompro` en NetBeans (File > Open Project) y dar *Run*.

### Antes de entregar

Completar en la portada del documento: nombre del estudiante, carné y profesor(a).

### Regenerar imágenes y documento

Los diagramas se escriben en Mermaid (`docs/diagramas/*.mmd`) y los prototipos en HTML/CSS. Tras editarlos, se exportan a PNG (por ejemplo con [Mermaid Live](https://mermaid.live) o abriendo el HTML en el navegador) y el documento se genera con:

```bash
cd docs
python3 construir_documento.py   # genera Avance1_Documento_Diseno_vN.docx y .pdf
```
