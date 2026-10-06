# Fidecompro – Sistema de Facturación e Inventario

Proyecto final del curso **Programación Cliente-Servidor Concurrente** (Proyecto 1 del enunciado).
Aplicación de escritorio en Java Swing con arquitectura cliente-servidor por sockets y servidor multihilo.

## Avance 1 – Documento de diseño

| Entregable | Archivo |
|---|---|
| Documento de diseño (para entregar) | [`docs/Avance1_Documento_Diseno.pdf`](docs/Avance1_Documento_Diseno.pdf) · [`.docx`](docs/Avance1_Documento_Diseno.docx) |
| Fuente editable del documento | [`docs/Avance1_Documento_Diseno.md`](docs/Avance1_Documento_Diseno.md) |
| Diagramas (clases, arquitectura, secuencia, navegación) | [`docs/diagramas/`](docs/diagramas) (Mermaid) → [`docs/img/`](docs/img) |
| Prototipos de interfaz | [`docs/prototipos/html/`](docs/prototipos/html) → [`docs/img/P*.png`](docs/img) |

Contenido del documento:

1. **Entidades o clases**: modelo de dominio (Usuario, Cliente, CategoriaProducto, Producto, MovimientoInventario, Factura, DetalleFactura y enumeraciones) y clases cliente-servidor (servidor, hilos, servicios, DAO, ventanas Swing), con atributos, métodos y relaciones.
2. **Historias de usuario**: 15 historias (HU-01 a HU-15) con criterios de aceptación, prioridad y estimación.
3. **Prototipos**: 8 pantallas (inicio de sesión, menú, clientes, productos e inventario, nueva factura, factura física, historial y usuarios) más el mapa de navegación.

### Antes de entregar

Completar en la portada del documento: nombre del estudiante, carné y profesor(a).

### Regenerar imágenes y documento

Los diagramas se escriben en Mermaid (`docs/diagramas/*.mmd`) y los prototipos en HTML/CSS. Tras editarlos, se exportan a PNG (por ejemplo con [Mermaid Live](https://mermaid.live) o abriendo el HTML en el navegador) y el documento se genera con:

```bash
cd docs
pandoc Avance1_Documento_Diseno.md -o Avance1_Documento_Diseno.docx --resource-path=.
```
