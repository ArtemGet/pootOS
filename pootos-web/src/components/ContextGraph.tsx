import { Background, Controls, ReactFlow, type Edge, type Node } from '@xyflow/react';

const nodes: Node[] = [
  { id: '1', position: { x: 0, y: 40 }, data: { label: 'Lesson: EO style' } },
  { id: '2', position: { x: 200, y: 0 }, data: { label: 'Task: web scaffold' } },
  { id: '3', position: { x: 200, y: 120 }, data: { label: 'Decision: ADR-002' } },
  { id: '4', position: { x: 400, y: 60 }, data: { label: 'Artifact: PR #40' } },
];

const edges: Edge[] = [
  { id: 'e1-2', source: '1', target: '2' },
  { id: 'e2-4', source: '2', target: '4' },
  { id: 'e3-4', source: '3', target: '4' },
];

export function ContextGraph() {
  return (
    <section className="panel graph">
      <h2>Context graph</h2>
      <div className="graph-canvas">
        <ReactFlow nodes={nodes} edges={edges} fitView>
          <Background />
          <Controls showInteractive={false} />
        </ReactFlow>
      </div>
    </section>
  );
}
