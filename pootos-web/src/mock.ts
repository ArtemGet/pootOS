import type { ApiAgents, ApiGraph, ApiResources } from './api';

export type Zone = 'RED' | 'YELLOW' | 'GREEN';

export interface AttentionCard {
  id: string;
  title: string;
  kind: 'task' | 'artifact';
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

export const mockGraph: ApiGraph = {
  nodes: [
    { id: 'n1', kind: 'Lesson', json: { title: 'EO style' } },
    { id: 'n2', kind: 'Task', json: { title: 'web scaffold' } },
    { id: 'n3', kind: 'Decision', json: { title: 'ADR-002' } },
    { id: 'n4', kind: 'Artifact', json: { title: 'PR #40' } },
  ],
  edges: [
    { from: 'n1', relation: 'informs', to: 'n2' },
    { from: 'n2', relation: 'produces', to: 'n4' },
    { from: 'n3', relation: 'informs', to: 'n4' },
  ],
};

export const mockAgents: ApiAgents = {
  agents: [
    { id: 'reviewer', template: 'reviewer', model: 'openai/gpt-x', status: 'working' },
    { id: 'coder', template: 'coder', model: 'anthropic/claude-x', status: 'idle' },
  ],
};

export const mockResources: ApiResources = {
  leases: [
    { resource: 'sandbox:reviewer-1', owner: 'reviewer', expires: '00:12:30' },
    { resource: 'model:anthropic/claude-x', owner: 'coder', expires: '00:04:10' },
    { resource: 'graph:sqlite', owner: 'system', expires: '00:00:00' },
  ],
};
