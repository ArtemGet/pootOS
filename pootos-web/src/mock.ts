export type Zone = 'RED' | 'YELLOW' | 'GREEN';

export interface AttentionCard {
  id: string;
  title: string;
  kind: 'task' | 'artifact';
}

export interface AgentCard {
  id: string;
  name: string;
  template: string;
  modelRef: string;
  status: 'working' | 'idle' | 'stalled';
  heartbeat: string;
}

export interface ResourceEntry {
  id: string;
  name: string;
  leaseTtl: string;
  budget: string;
  cost: string;
  stalled: boolean;
}

export const attentionZones: Record<Zone, AttentionCard[]> = {
  RED: [
    { id: 'r1', title: 'Secret required: GitHub token', kind: 'task' },
    { id: 'r2', title: 'HumanQuestion L2 — sandbox escape review', kind: 'task' },
  ],
  YELLOW: [
    { id: 'y1', title: 'PR #40 awaiting review', kind: 'artifact' },
    { id: 'y2', title: 'Lesson: Cactoos primitives', kind: 'artifact' },
  ],
  GREEN: [
    { id: 'g1', title: 'Context graph persistence', kind: 'task' },
    { id: 'g2', title: 'Web UI scaffold', kind: 'artifact' },
  ],
};

export const agentCards: AgentCard[] = [
  {
    id: 'a1',
    name: 'reviewer',
    template: 'reviewer',
    modelRef: 'openai/gpt-x',
    status: 'working',
    heartbeat: '2s ago',
  },
  {
    id: 'a2',
    name: 'coder',
    template: 'coder',
    modelRef: 'anthropic/claude-x',
    status: 'idle',
    heartbeat: '1m ago',
  },
];

export const resources: ResourceEntry[] = [
  { id: 'res1', name: 'sandbox:reviewer-1', leaseTtl: '00:12:30', budget: '80%', cost: '$0.42', stalled: false },
  { id: 'res2', name: 'model:anthropic/claude-x', leaseTtl: '00:04:10', budget: '35%', cost: '$0.11', stalled: false },
  { id: 'res3', name: 'graph:sqlite', leaseTtl: '00:00:00', budget: '0%', cost: '$0.00', stalled: true },
];
