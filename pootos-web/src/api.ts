export const API_URL: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export interface Health {
  status: string;
}

export interface ApiGraphNode {
  id: string;
  kind: string;
  json: unknown;
}

export interface ApiGraphEdge {
  from: string;
  relation: string;
  to: string;
}

export interface ApiGraph {
  nodes: ApiGraphNode[];
  edges: ApiGraphEdge[];
}

export interface ApiAgent {
  id: string;
  template: string;
  model: string;
  status: string;
}

export interface ApiAgents {
  agents: ApiAgent[];
}

export interface ApiLease {
  resource: string;
  owner: string;
  expires: string;
}

export interface ApiResources {
  leases: ApiLease[];
}

async function get<T>(path: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    headers: { accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`GET ${path} -> ${response.status}`);
  }
  return (await response.json()) as T;
}

export const fetchHealth = (): Promise<Health> => get<Health>('/api/health');
export const fetchGraph = (): Promise<ApiGraph> => get<ApiGraph>('/api/graph');
export const fetchAgents = (): Promise<ApiAgents> => get<ApiAgents>('/api/agents');
export const fetchResources = (): Promise<ApiResources> => get<ApiResources>('/api/resources');
