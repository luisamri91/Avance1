"""Convierte los diagramas de clases de Mermaid a un archivo de draw.io editable.

Cada diagrama (.mmd) pasa a ser una página del archivo .drawio, con las clases
como figuras UML de draw.io (nombre, atributos y métodos) y las relaciones como
flechas UML: herencia, implementación, composición, agregación, asociación y
dependencia. Las clases y las flechas se acomodan con ELK (acomodar_drawio.js)
para que las flechas no pasen por encima de las clases.

Uso (desde docs/):
    python3 exportar_drawio.py SALIDA.drawio diagramas/01a_modelo_inventario.mmd [otro.mmd ...]
Necesita Node.js y el paquete elkjs (npm install elkjs; si está en otra carpeta, NODE_PATH=<carpeta>/node_modules).
"""
import json
import re
import subprocess
import sys
import tempfile
from html import escape
from pathlib import Path

ALTO_TITULO = 26
ALTO_TITULO_ESTEREOTIPO = 40
ALTO_FILA = 20
ALTO_SEPARADOR = 8
ANCHO_MINUSCULA = 6.1  # ancho aproximado de un carácter en Arial 11
ANCHO_MAYUSCULA = 7.6
ACOMODAR = Path(__file__).with_name("acomodar_drawio.js")

NOMBRES_PAGINA = {
    "00_clases_simplificado": "Diagrama de clases",
    "01a_modelo_inventario": "Inventario y excepciones",
    "01b_modelo_facturacion": "Personas y facturación",
    "02_clases_cliente_servidor": "Cliente-servidor",
}

# Mermaid -> (estilo de la flecha en draw.io, si la flecha va del lado derecho al izquierdo).
# Para acomodar, cada relación se ordena de arriba hacia abajo: la superclase, la interface
# y el todo de una composición o agregación quedan arriba.
FLECHAS = {
    "<|--": ("endArrow=block;endFill=0;endSize=12;", True),
    "<|..": ("endArrow=block;endFill=0;endSize=12;dashed=1;", True),
    "*--": ("startArrow=diamondThin;startFill=1;startSize=14;endArrow=none;", False),
    "o--": ("startArrow=diamondThin;startFill=0;startSize=14;endArrow=none;", False),
    "-->": ("endArrow=open;endFill=0;endSize=10;", False),
    "..>": ("endArrow=open;endFill=0;endSize=10;dashed=1;", False),
    "--": ("endArrow=none;", False),
}
RELACION = re.compile(
    r'^\s*(\w+)(?:\s+"([^"]*)")?\s+(<\|--|<\|\.\.|\*--|o--|-->|\.\.>|--)\s+(?:"([^"]*)"\s+)?(\w+)(?:\s*:\s*(.+))?\s*$'
)


def leer_mermaid(ruta):
    clases, relaciones = {}, []
    actual = None
    for linea in Path(ruta).read_text(encoding="utf-8").splitlines():
        texto = linea.strip()
        if actual is not None:
            if texto == "}":
                actual = None
            elif texto.startswith("<<"):
                clases[actual]["estereotipo"] = texto.strip("<>")
            elif texto:
                clases[actual]["miembros"].append(texto)
            continue
        inicio = re.match(r"class (\w+)\s*(\{)?", texto)
        if inicio:
            clases.setdefault(inicio.group(1), {"estereotipo": None, "miembros": []})
            if inicio.group(2):
                actual = inicio.group(1)
            continue
        relacion = RELACION.match(texto)
        if relacion:
            a, mult_a, flecha, mult_b, b, etiqueta = relacion.groups()
            for nombre in (a, b):
                clases.setdefault(nombre, {"estereotipo": None, "miembros": []})
            relaciones.append((a, mult_a, flecha, mult_b, b, etiqueta))
    return clases, relaciones


def es_metodo(miembro):
    return re.search(r"\w\(", miembro) is not None


def formatear_miembro(miembro):
    """Devuelve el texto en notación UML y el estilo de letra (subrayado = static, cursiva = abstracto)."""
    estilo = 0
    if "$" in miembro:
        estilo |= 4
        miembro = miembro.replace("$", "")
    if ")*" in miembro:
        estilo |= 2
        miembro = miembro.replace(")*", ")")
    miembro = re.sub(r"~(\w+(?:\[\])?)~", r"<\1>", miembro)
    visibilidad = miembro[0] if miembro[0] in "+-#~" else ""
    cuerpo = miembro[len(visibilidad):]
    if es_metodo(cuerpo):
        firma, _, retorno = cuerpo.rpartition(")")
        texto = firma + ")" + (" : " + retorno.strip() if retorno.strip() else "")
    elif " " in cuerpo:
        nota = ""
        con_nota = re.match(r"(.*?)\s+(\(.*\))$", cuerpo)
        if con_nota:
            cuerpo, nota = con_nota.group(1), " " + con_nota.group(2)
        valor = ""
        if " = " in cuerpo:
            cuerpo, valor = cuerpo.split(" = ", 1)
            valor = " = " + valor
        tipo, nombre = cuerpo.rsplit(" ", 1)
        texto = f"{nombre} : {tipo}{valor}{nota}"
    else:
        texto = cuerpo
    return (visibilidad + " " + texto).strip(), estilo


def ancho_texto(texto):
    return sum(ANCHO_MAYUSCULA if c.isupper() else ANCHO_MINUSCULA for c in texto)


def medir(nombre, datos):
    estereotipo = datos["estereotipo"]
    atributos = [m for m in datos["miembros"] if not es_metodo(m)]
    metodos = [m for m in datos["miembros"] if es_metodo(m)]
    alto_titulo = ALTO_TITULO_ESTEREOTIPO if estereotipo else ALTO_TITULO
    alto = alto_titulo + ALTO_FILA * (len(atributos) + len(metodos))
    if atributos and metodos:
        alto += ALTO_SEPARADOR
    textos = [formatear_miembro(m)[0] for m in datos["miembros"]] + [nombre + "    ", f"«{estereotipo}»"]
    ancho = max(140, max(ancho_texto(t) for t in textos) + 16)
    return atributos, metodos, alto_titulo, ancho, alto


def acomodar(clases, medidas, relaciones):
    grafo = {
        "id": "raiz",
        "children": [{"id": n, "width": medidas[n][3], "height": medidas[n][4]} for n in clases],
        "edges": [{"id": f"e{i}", "sources": [r[0]], "targets": [r[4]]} for i, r in enumerate(relaciones)],
    }
    with tempfile.TemporaryDirectory() as carpeta:
        entrada, salida = Path(carpeta, "entrada.json"), Path(carpeta, "salida.json")
        entrada.write_text(json.dumps(grafo), encoding="utf-8")
        subprocess.run(["node", str(ACOMODAR), str(entrada), str(salida)], check=True)
        resultado = json.loads(salida.read_text(encoding="utf-8"))
    nodos = {n["id"]: n for n in resultado["children"]}
    rutas = {}
    for e in resultado["edges"]:
        seccion = e["sections"][0]
        rutas[e["id"]] = [seccion["startPoint"], *seccion.get("bendPoints", []), seccion["endPoint"]]
    return nodos, rutas


def celda(id_, valor, estilo, padre, x, y, w, h):
    return (f'<mxCell id="{id_}" value="{escape(valor, quote=True)}" style="{estilo}" vertex="1" parent="{padre}">'
            f'<mxGeometry x="{x:.0f}" y="{y:.0f}" width="{w:.0f}" height="{h:.0f}" as="geometry"/></mxCell>')


def anclaje(punto, nodo, prefijo):
    """Punto donde la flecha toca la clase, como fracción del ancho y del alto (lo que usa draw.io)."""
    x = min(max((punto["x"] - nodo["x"]) / nodo["width"], 0), 1)
    y = min(max((punto["y"] - nodo["y"]) / nodo["height"], 0), 1)
    return f"{prefijo}X={x:.3f};{prefijo}Y={y:.3f};{prefijo}Dx=0;{prefijo}Dy=0;"


def pagina(ruta_mmd, numero):
    clases, relaciones = leer_mermaid(ruta_mmd)
    medidas = {n: medir(n, d) for n, d in clases.items()}
    nodos, rutas = acomodar(clases, medidas, relaciones)
    celdas = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    ids = {}
    for indice, (nombre, datos) in enumerate(clases.items()):
        atributos, metodos, alto_titulo, ancho, alto = medidas[nombre]
        nodo = nodos[nombre]
        id_clase = f"p{numero}c{indice}"
        ids[nombre] = id_clase
        estereotipo = datos["estereotipo"]
        titulo = f"«{estereotipo}»<br><b>{escape(nombre)}</b>" if estereotipo else f"<b>{escape(nombre)}</b>"
        cursiva = "fontStyle=2;" if estereotipo in ("abstract", "interface") else "fontStyle=0;"
        estilo_clase = ("swimlane;html=1;" + cursiva + "align=center;verticalAlign=top;childLayout=stackLayout;"
                        f"horizontal=1;startSize={alto_titulo};horizontalStack=0;resizeParent=1;"
                        "resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;whiteSpace=wrap;"
                        "fillColor=#dae8fc;strokeColor=#6c8ebf;fontColor=#000000;fontSize=12;")
        celdas.append(celda(id_clase, titulo, estilo_clase, "1", nodo["x"] + 20, nodo["y"] + 20, ancho, alto))
        y = alto_titulo
        hijo = 0
        for grupo, separador in ((atributos, bool(metodos)), (metodos, False)):
            for miembro in grupo:
                texto, estilo_letra = formatear_miembro(miembro)
                estilo = ("text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=4;"
                          "spacingRight=4;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];"
                          "portConstraint=eastwest;whiteSpace=wrap;html=1;fontColor=#000000;fontSize=11;"
                          f"fontStyle={estilo_letra};")
                celdas.append(celda(f"{id_clase}m{hijo}", escape(texto), estilo, id_clase, 0, y, ancho, ALTO_FILA))
                hijo += 1
                y += ALTO_FILA
            if grupo and separador:
                estilo = ("line;strokeWidth=1;fillColor=none;align=left;verticalAlign=middle;spacingTop=-1;"
                          "spacingLeft=3;spacingRight=3;rotatable=0;labelPosition=right;points=[];"
                          "portConstraint=eastwest;strokeColor=inherit;")
                celdas.append(celda(f"{id_clase}m{hijo}", "", estilo, id_clase, 0, y, ancho, ALTO_SEPARADOR))
                hijo += 1
                y += ALTO_SEPARADOR
    for indice, (a, mult_a, flecha, mult_b, b, etiqueta) in enumerate(relaciones):
        estilo, invertir = FLECHAS[flecha]
        ruta = rutas[f"e{indice}"]
        origen, destino, mult_origen, mult_destino = a, b, mult_a, mult_b
        if invertir:
            ruta = ruta[::-1]
            origen, destino, mult_origen, mult_destino = b, a, mult_b, mult_a
        estilo = ("edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;fontSize=11;fontColor=#000000;"
                  "strokeColor=#000000;" + anclaje(ruta[0], nodos[origen], "exit")
                  + anclaje(ruta[-1], nodos[destino], "entry") + estilo)
        puntos = "".join(f'<mxPoint x="{p["x"] + 20:.0f}" y="{p["y"] + 20:.0f}"/>' for p in ruta[1:-1])
        id_flecha = f"p{numero}r{indice}"
        celdas.append(f'<mxCell id="{id_flecha}" value="{escape(escape(etiqueta or ""), quote=True)}" '
                      f'style="{estilo}" edge="1" parent="1" source="{ids[origen]}" target="{ids[destino]}">'
                      f'<mxGeometry relative="1" as="geometry"><Array as="points">{puntos}</Array></mxGeometry>'
                      '</mxCell>')
        for sub, (texto, posicion) in enumerate(((mult_origen, -1), (mult_destino, 1))):
            if texto:
                celdas.append(f'<mxCell id="{id_flecha}t{sub}" value="{escape(texto)}" '
                              'style="edgeLabel;html=1;align=left;verticalAlign=bottom;fontSize=11;'
                              f'fontColor=#000000;" vertex="1" connectable="0" parent="{id_flecha}">'
                              f'<mxGeometry x="{posicion * 0.85}" relative="1" as="geometry">'
                              '<mxPoint x="6" as="offset"/></mxGeometry></mxCell>')
    nombre_pagina = NOMBRES_PAGINA.get(Path(ruta_mmd).stem, Path(ruta_mmd).stem)
    return (f'<diagram id="pagina{numero}" name="{escape(nombre_pagina)}"><mxGraphModel grid="1" gridSize="10" '
            'guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" math="0" shadow="0">'
            "<root>" + "".join(celdas) + "</root></mxGraphModel></diagram>")


def main():
    salida, *diagramas = sys.argv[1:]
    paginas = [pagina(ruta, numero) for numero, ruta in enumerate(diagramas, start=1)]
    Path(salida).write_text('<mxfile host="Electron" type="device">' + "".join(paginas) + "</mxfile>\n",
                            encoding="utf-8")
    print("Generado", salida)


if __name__ == "__main__":
    main()
