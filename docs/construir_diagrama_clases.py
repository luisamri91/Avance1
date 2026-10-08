"""Genera un Word aparte con solo el diagrama de clases del Avance 1.

Toma la sección "Entidades y clases" del documento de diseño (hasta antes del
flujo de emisión de facturas), la renumera como un documento independiente y
le aplica el mismo formato que construir_documento.py.

Uso (desde docs/):  python3 construir_diagrama_clases.py
Para una versión nueva, subir VERSION en uno.
"""
import re
import subprocess
from pathlib import Path

from docx import Document

from construir_documento import FUENTE, dar_formato_general

VERSION = 3
SALIDA = Path(f"Avance1_Diagrama_de_Clases_v{VERSION}.docx")
TEMPORAL = Path("_diagrama_clases.md")

PORTADA = f"""---
title: "Proyecto Final – Avance 1: Diagrama de clases"
subtitle: "Sistema de Facturación e Inventario Fidecompro (Proyecto 1) · Versión {VERSION}"
author: "Estudiante: [Nombre completo] · Carné: [número]"
date: "Programación Cliente-Servidor Concurrente · Profesor: Mario Alberto Vargas Montes · Octubre 2026"
lang: es
---

Este documento presenta las entidades o clases identificadas para la solución, sus atributos, sus métodos y su relación con otras clases.

"""


def extraer_seccion():
    texto = FUENTE.read_text(encoding="utf-8")
    inicio = texto.index("# 4. Entidades y clases")
    fin = texto.index("## 4.5 ")
    seccion = texto[inicio:fin]
    # Quita el título de la sección: el documento entero trata de esto.
    seccion = seccion.split("\n", 1)[1]
    # Subsecciones 4.x pasan a ser capítulos x. y sus subtítulos suben un nivel.
    seccion = re.sub(r"^### ", "## ", seccion, flags=re.M)
    seccion = re.sub(r"^## 4\.(\d) ", r"# \1. ", seccion, flags=re.M)
    seccion = re.sub(r"sección 4\.(\d)", r"sección \1", seccion)
    return PORTADA + seccion


def main():
    TEMPORAL.write_text(extraer_seccion(), encoding="utf-8")
    try:
        subprocess.run(["pandoc", TEMPORAL.name, "-o", SALIDA.name, "--resource-path=."], check=True)
    finally:
        TEMPORAL.unlink()
    doc = Document(SALIDA.name)
    dar_formato_general(doc)
    doc.save(SALIDA.name)
    print("Generado", SALIDA.name)


if __name__ == "__main__":
    main()
