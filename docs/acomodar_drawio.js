// Acomoda las clases y las flechas de un diagrama con ELK (algoritmo por capas, flechas ortogonales).
// Lo usa exportar_drawio.py. Uso: node acomodar_drawio.js entrada.json salida.json
// Necesita el paquete elkjs (npm install elkjs); si está en otra carpeta, indicarla con NODE_PATH.
const fs = require('fs');
const ELK = require('elkjs/lib/elk.bundled.js');

const grafo = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
grafo.layoutOptions = {
  'elk.algorithm': 'layered',
  'elk.direction': 'DOWN',
  'elk.edgeRouting': 'ORTHOGONAL',
  'elk.spacing.nodeNode': '50',
  'elk.layered.spacing.nodeNodeBetweenLayers': '70',
  'elk.spacing.edgeNode': '25',
  'elk.spacing.edgeEdge': '15',
  'elk.layered.spacing.edgeNodeBetweenLayers': '25',
  'elk.layered.nodePlacement.strategy': 'NETWORK_SIMPLEX',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
};
new ELK().layout(grafo).then(r => fs.writeFileSync(process.argv[3], JSON.stringify(r)));
