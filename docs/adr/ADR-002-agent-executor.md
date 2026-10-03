# ADR-002: Agents run through an AgentExecutor SPI; first adapter wraps opencode

## Status
Accepted (Owner, 2026-10-03).

## Context
opencode already provides an agent loop, providers, MCP, tools, subagents, skills, permissions,
and an SDK/server — a large part of "an agent". What it does not provide is exactly the **OS**
layer pootOS targets: filesystem/shell isolation, resource leases and arbitration, durable actors,
a context **graph** instead of chat, attention zones, a system agent, and cross-machine sync.
Building the agent loop from scratch would duplicate a solved, fast-moving problem.

## Decision
- pootOS owns the OS: kernel, sandbox, leases, context graph, zones, system agent.
- "How to run one agent turn" is a **swappable `AgentExecutor` SPI**. An agent is a durable actor
  whose turns are delegated to an executor.
- The first non-trivial adapter is **`OpenCodeExecutor`**: pootOS launches opencode (server/SDK)
  **inside the sandbox**, projects a **slice of the context graph** into the executor as its input,
  and records the executor's outputs back as graph nodes.
- Per-agent model/provider selection is expressed through the executor config but is **subject to
  pootOS leases and budgets** (a run cannot exceed its granted resources/time).
- A native Java/EO executor remains possible later behind the same SPI.

## Consequences
- MVP review agent (EO skill) lands much faster; we reuse the opencode ecosystem.
- The stack gains a **Node/TypeScript runtime** alongside Java (contained in the sandbox).
- A **graph↔transcript projection** layer is required at the boundary; opencode's own compaction
  must not become the system of record (our graph is).
- opencode's permissions/loop must be bound to our resource leases and budgets.
- Reversible: the executor is a driver, not a dependency of the kernel.
