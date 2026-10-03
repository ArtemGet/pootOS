# ADR-003: Context memory = own content-addressed graph as source of truth, external recall behind a SPI

## Status
Accepted (Owner, 2026-10-03).

## Context
pootOS needs a memory layer where:
- a **journal of mistakes / lessons** survives task and project switches (the Owner's main pain);
- context is a **graph**, not a chat transcript;
- context can sync across machines;
- a dead agent's useful context can be salvaged by a repair agent.

The Owner asked to consider **TencentDB Agent Memory**. Research findings: it is OSS (MIT,
`TencentCloud/TencentDB-Agent-Memory`) but **TypeScript/Node**; the Hub variant leans on **Tencent
Cloud COS/VectorDB**; learning extraction is **LLM-driven and non-deterministic**; **cross-machine
sync is unshipped**. It is a poor direct fit for an EO-Java, deterministic, sync-ready kernel.

## Decision
- **Source of truth = pootOS's own content-addressed context graph** (`Task`/`Decision`/`Lesson` +
  typed edges). Determinism is required for the journal-of-mistakes guarantee; a probabilistic memory
  layer cannot provide it.
- Memory is an **SPI** (`ContextGraph` + `Recall`). Persistence starts on SQLite (+ append-only event
  log); retrieval is pluggable.
- **TencentDB Agent Memory is adopted as a DESIGN REFERENCE, not a runtime dependency** (layered
  memory L0→L3, skill assets, ACLs).
- If an external recall/temporal engine is wanted later, evaluate **Graphiti/Zep** (bi-temporal graph)
  first, behind the `Recall` SPI.

## Consequences
- No foreign runtime in the kernel; the memory layer stays EO-Java and deterministic/auditable.
- Semantic recall is optional and swappable; adopting a third-party engine is an adapter change.
- Cross-machine sync is feasible later (append-only, content-addressed).
