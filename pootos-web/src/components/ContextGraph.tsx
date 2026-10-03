import { Background, Controls, ReactFlow, type Edge, type Node } from '@xyflow/react';
import { fetchGraph, type ApiGraph } from '../api';
import { mockGraph } from '../mock';
import { useRemote } from '../useRemote';

const COLUMNS = 2;

function toNodes(graph: ApiGraph): Node[] {
  return graph.nodes.map((node, index) => ({
    id: node.id,
    position: { x: (index % COLUMNS) * 220, y: Math.floor(index / COLUMNS) * 110 },
    data: { label: `${node.kind} · ${node.id}` },
  }));
}

function toEdges(graph: ApiGraph): Edge[] {
  return graph.edges.map((edge, index) => ({
    id: `e${index}`,
    source: edge.from,
    target: edge.to,
    label: edge.relation,
  }));
}

export function ContextGraph() {
  const graph = useRemote<ApiGraph>(fetchGraph, mockGraph);
  return (
    <section className="panel graph">
      <h2>Context graph</h2>
      <div className="graph-canvas">
        <ReactFlow nodes={toNodes(graph)} edges={toEdges(graph)} fitView>
          <Background />
          <Controls showInteractive={false} />
        </ReactFlow>
      </div>
    </section>
  );
}
