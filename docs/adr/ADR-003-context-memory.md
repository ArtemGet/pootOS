# ADR-003: Context memory = own content-addressed graph as source of truth, external recall behind a SPI

## Status
Proposed (awaiting Owner confirmation). Owner asked to consider **TencentDB Agent Memory**.

## Context
pootOS needs a memory layer where:
- a **journal of mistakes / lessons** survives task and project switches (the Owner's main pain);
- context is a **graph**, not a chat transcript;
- context can sync across machines;
- a dead agent's useful context can be salvaged by a repair agent.

The Owner proposed looking at **TencentDB Agent Memory**. Research findings:

- It is an **OSS (MIT) TypeScript/Node** project (`TencentCloud/TencentDB-Agent-Memory`) plus an npm
  plugin; the newer "Memory Hub" server variant leans on **Tencent Cloud COS/VectorDB**.
- Model: **layered memory** L0 conversation → L1 atom → L2 scenario → L3 persona, plus "Skill"
  assets, a Wiki link graph and a CodeGraph; retrieval is **hybrid (BM25 + vector + RRF)**.
- Isolation by `team/agent/user/task` — maps well to per-agent/session boundaries.
- Gaps: **wrong runtime** for an EO-Java kernel (would be an external sidecar), learning extraction
  is **LLM-driven and non-deterministic** (no guaranteed lesson ledger), **cross-machine sync is a
  roadmap item**, cloud-tied in the Hub variant, API still churning.

## Decision (proposed)
- **Source of truth = pootOS's own content-addressed context graph** (`Task`/`Decision`/`Lesson` +
  typed edges). Determinism is required for the journal-of-mistakes guarantee; a probabilistic
  memory layer cannot provide it.
- Memory is an **SPI** (`ContextGraph` + `Recall`). Persistence starts on SQLite; retrieval is
  pluggable.
- **TencentDB Agent Memory is adopted as a DESIGN REFERENCE, not a runtime dependency**: borrow the
  layered-memory idea (conversation → atom → scenario → persona) and the skill-asset/ACL model.
- If an external recall/temporal engine is later wanted behind the SPI, evaluate **Graphiti/Zep**
  (bi-temporal knowledge graph — best fit for lessons surviving switches) before TencentDB.

## Consequences
- No foreign runtime in the kernel; the memory layer stays EO-Java.
- Lessons are deterministic and auditable (content-addressed nodes).
- Semantic recall is optional and swappable; adopting a third-party engine is a config/adapter
  change, not a rewrite.
- We consciously forgo TencentDB's ready-made UI/skills until/unless it proves a better ROI.
