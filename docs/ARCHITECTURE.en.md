# pootOS — architecture

> Agent Operating System: a sandbox for agents, a kernel with a scheduler and resource
> leases, a context graph instead of per-task chat, and a System Agent that manages
> everything at runtime.
>
> This document fixes the architecture, technologies and functionality. The backend
> language is Java 25 (Elegant Objects), the frontend is React + TypeScript. The first goal
> is a secure application for Windows and macOS (Linux later) with minimal functionality:
> safely running agents and solving a simple task (EO PR review).
>
> **Documentation languages:** ru (the canon) · en (`ARCHITECTURE.en.md`) ·
> zh-CN (`ARCHITECTURE.zh-CN.md`). Translations are maintained by a separate agent, and
> freshness is periodically verified by an auditor agent (see §22).
>
> en (this file) · [ru](ARCHITECTURE.md) · [zh-CN](ARCHITECTURE.zh-CN.md)

---

## 0. Product Vision

Format from the EO process (actors, features, quality, constraints). **Thinking** phase: spec.

### 0.1 Actors

1. **Owner** — the human operator (ArtemGet).
2. **System Agent** — a meta-agent; the Owner's only interlocutor through the main chat.
3. **Worker Agent** — task executor (in the MVP — the review agent).
4. **External systems** — GitHub (webhooks/API), LLM providers, MCP servers.

### 0.2 Owner ↔ system interaction model

- **One main chat — forever.** It is not about a task; it is about managing the OS. Through
  it the Owner:
  - sets tasks ("what needs to be done") and hands them to the **System Agent**;
  - gives edits/corrections;
  - reads System Agent reports.
- **The System Agent accepts an instruction and distributes it onward without blocking its
  loop.** The System Agent is a coordinator: it creates `Task` nodes, selects
  agents/templates, grants resources, and tracks progress. While agents work, it stays
  responsive.
- **All the work is not in the chat but on the canvas** (tasks, artifacts, agents).
- The chat's functions are strictly limited: start, instructions/edits, reports. No
  "chat per task".

### 0.3 Canvas and attention zones

The canvas is the primary workspace. Three zones by the human's need for attention:

| Zone | Meaning | Example |
|---|---|---|
| 🔴 **RED** | A human decision is needed; the branch's work is blocked | request for access to a token/secret; mount request; ambiguous requirement; budget exhausted; suspected irreversible action |
| 🟡 **YELLOW** | It is desirable for the human to look, but there is no blocking | borderline result, several options, isolation degradation |
| 🟢 **GREEN** | Agents cope on their own, no intervention needed | normal execution |

- **Tasks move between zones** as state changes (the scheduler/agents move the card).
- The zones hold **cards**. From a card you can drill in: see what the agents are doing,
  their steps, artifacts, and **write directly into the card** (edit/clarification).

### 0.4 Card ≠ Agent

- A **card** is a task or an artifact. **Several agents** may work on one card (for example,
  executor + reviewer + repair).
- An **agent** is a separate entity with its own: skills, MCP access, shell access, a set of
  mounted folders, model(s), and template.
- Separate views: **"currently working"** and **"all agents"**.

### 0.5 Features by actor (MVP, limit of 3–4 each)

- **Owner:** set a task via chat; see zones/cards/agents; respond to RED/YELLOW; see resources
  and make decisions (optimize / stop / restart stalled agents).
- **System Agent:** create/distribute tasks; add a provider, tool, MCP, agent template;
  configure **itself and the pootOS application via agents**; change policies — all **without
  restarting the process**.
- **Worker Agent:** start in the sandbox; read/write the context graph; spawn subagents with a
  **different model**; request mounts; produce artifacts and lessons.
- **Review agent (MVP):** PR webhook → EO review → publish review and labels to GitHub.

### 0.6 Quality requirements (measurable)

- An agent **cannot** read/write on the host outside its sandbox root (negative test).
- No two agents hold one resource simultaneously (`gpu:0`, `port:N`, `llm:rate:*`).
- Webhook → review start < 30 s; review of a small PR < 10 min.
- An agent's crash does not block the others; the watchdog releases leases within the TTL.
- A configuration change (new provider) is applied without a process restart.
- One agent = one durable actor; hundreds of agents in parallel on virtual threads.

### 0.7 Constraints

- Backend Java 25, Maven, EO style, gates Qulice 0.36 + jtcop + JaCoCo/PIT.
- Isolation — Docker + gVisor (`runsc`). Windows requires the WSL2 backend of Docker.
- Web UI — React + TS; desktop (Kotlin Multiplatform/Compose) — later.
- GitHub — a single account ⇒ the review verdict is expressed as a **label** (`approved`),
  not Approve.

---

## 1. Requirements (R1–R18)

| # | Requirement | Layer / solution | MVP |
|---|---|---|---|
| R1 | Real FS segmentation: the agent runs under a different root and cannot reach the data | Sandbox (Docker/gVisor), separate root, only explicit mounts | ✅ |
| R2 | System Agent adds providers/tools/MCP | System Agent + config as data | ✅ |
| R3 | Automatic agent configuration for skills/tools, different providers | Agent Templates + Provider SPI | ✅ |
| R4 | Monitoring in the UI | Canvas (zones/cards) + resource dashboard | ✅ |
| R5 | Dynamic control of the System Agent | Config graph + hot-reload | ✅ |
| R6 | Resources/timeouts; no deadlock of parallel agents | Resource Arbiter (leases) + Watchdog + Budgets | ✅ |
| R7 | Work graph from the start | Context Graph: `Task` + DAG edges | ✅ |
| R8 | Context is a graph, not a chat; an agent fixes another | Context Graph + repair-flow | ✅ basic |
| R9 | Human tasks by levels: storage, bypass, continuation | RED/YELLOW zones + `HumanQuestion` L0–L3 | ✅ |
| R10 | Memory compaction across sessions/projects; a log of mistakes | `Summary` + `Lesson` | ✅ |
| R11 | Context synchronization between machines | Append-only log + content-addressing | ⏳ design |
| R12 | Work NOT as chat and NOT as "one project — one directory" | Canvas + `Project` as a set of mounts | ✅ |
| R13 | Parallel agents without blocking the main loop | Virtual threads + scheduler; there is no main chat loop | ✅ |
| R14 | Subagents with models different from the parent's | `ModelRef` per-agent | ✅ |
| R15 | Isolated shell | PTY in the container | ✅ |
| R16 | Configuration without a process restart | Versioned config nodes + resolver | ✅ |
| R17 | A single main chat only for management | Owner ↔ System Agent; distribution without blocking | ✅ |
| R18 | Safe self-configuration: configs — yes, tokens — no | Config-write without secret-read; secret request → RED | ✅ |

---

## 2. Principles

1. **A graph, not a transcript.** The unit of state is a typed node with links.
2. **Isolation by default.** Access to the host/secrets is an explicit grant.
3. **Everything is a resource with a lease.** GPU, ports, egress, provider rate-limit.
4. **Durable by design.** An agent survives a restart (event-sourced).
5. **Configuration is data.** Changed at runtime, versioned, rollback-able.
6. **Secrets are inviolable.** An agent may write config, but not read tokens.
7. **Fail fast, recover once.**
8. **Java core in EO style.** `final`, `private final`, constructors only assign, no
   `null`/statics/`instanceof`, behavior through decorators. Gates Qulice + jtcop.

---

## 3. Overall architecture

```
┌───────────────────────────────────────────────────────────────┐
│ UI (React + TS + React Flow)                                   │
│  Главный чат · Канвас (RED/YELLOW/GREEN) · Карточки · Агенты   │
│  Дашборд ресурсов · Human-очередь                              │
└───────────────────────────▲───────────────────────────────────┘
              REST + WebSocket │
┌───────────────────────────┴───────────────────────────────────┐
│ pootOS-kernel (Java 25)                                        │
│  Actors · Scheduler · Resource Arbiter · Watchdog · Budgets    │
│  ConfigResolver (hot) · Event Log · Repair-flow · Zones        │
├───────────────────────────────────────────────────────────────┤
│ pootOS-context   Context Graph (nodes/edges, lessons, slices)  │
│ pootOS-agent     Agent runtime · Templates · Skills · Tools    │
│ pootOS-providers Provider SPI (OpenAI-compat, Anthropic, local)│
│ pootOS-system    System Agent (meta operations)                │
│ pootOS-github    Webhook receiver · GitHub client · Review flow │
├───────────────────────────────────────────────────────────────┤
│ pootOS-sandbox (SPI)                                           │
│   ├─ docker+runsc (gVisor)   ← основной, Win/macOS/Linux       │
│   └─ process (dev-only, небезопасно, помечено)                 │
└───────────────────────────▲───────────────────────────────────┘
              Docker Engine API │ (kernel ↔ Docker; агенты — НЕТ)
```

**Security rule #1:** the Docker socket is available only to the kernel; an agent receives a
container, but not access to docker/host.

---

## 4. Modules (Maven multi-module)

| Module | Responsibility |
|---|---|
| `pootos-kernel` | Actors, scheduler, leases, watchdog, budgets, event log, hot-config, zones |
| `pootos-context` | Context graph: nodes, edges, content-addressing, slices, compaction, lessons |
| `pootos-sandbox` | `Sandbox` SPI |
| `pootos-sandbox-docker` | Docker + gVisor adapter |
| `pootos-sandbox-process` | Dev adapter (no isolation; local only) |
| `pootos-providers` | `ModelProvider` SPI + adapters |
| `pootos-agent` | Agent runtime: templates, steps, skills, tools, MCP client |
| `pootos-system` | System Agent (meta-operations over config/graph) |
| `pootos-github` | Webhook intake, GitHub client, review flow |
| `pootos-ui-api` | REST + WebSocket for the Web UI |
| `pootos-skills` | Built-in skills (resources), e.g. EO-review |
| `pootos-cli` | Start/stop, entry point |
| `pootos-web` | Frontend (separate Vite build, not Maven) |

The MVP starts with a subset: `kernel`, `context`, `sandbox`, `sandbox-docker`, `providers`,
`agent`, `github`, `ui-api`, `cli`.

---

## 5. Kernel

### 5.1 Execution model

- **Agent = durable actor.** State: `AgentRecord{id, template, modelRef, status,
  leases[], heartbeat, budget, currentCard, edgesToContext}`.
- Supervision: a root supervisor; an actor's crash does not bring down the kernel.
- Parallelism is **virtual threads (Loom)**: I/O (LLM, shell, GitHub) does not block the
  scheduler. There is no main chat loop — there is a task DAG and a graph.
- **Event log** — append-only (`SQLite` WAL + an events table); recovery is a replay.

### 5.2 Resource Arbiter (R6)

- A resource is named: `gpu:0`, `port:8080`, `disk:/x`, `net:egress`, `llm:openai:rpm`.
- **Lease** = `{resource, owner, ttl, heartbeatAt, mode(shared|exclusive)}`.
- `acquire`: free → instant; exclusive taken → the task goes to `queued-on-resource`
  (it does not hang), the scheduler will pick it up on release (priorities + FIFO).
- `renew` on heartbeat; TTL expiry → release. Deadlock is impossible by construction
  (acquisition order + TTL + forced release on crash).

### 5.3 Timeouts, watchdog, budgets, operator decisions (R6)

- The **Watchdog** catches stale leases and heartbeat silence → `stalled`, releases leases,
  escalates to repair-flow.
- **Budget** per task/graph: `wallclock`, `tokens`, `vram`, `cost`. Exceeded → kill,
  a `Failure` node, a `Lesson` candidate.
- The **resource dashboard** shows: available resources, who holds leases, TTL, stalled
  agents. The Owner/System Agent can **optimize / stop / restart** an agent.
- Long foreground builds are not launched blindly: timeout + log file + tail polling.

### 5.4 Zones (R9)

- The scheduler moves a card between `RED`/`YELLOW`/`GREEN` by rules and by agent events
  (access request, budget exhaustion, isolation degradation, etc.).

### 5.5 Hot config (R16, R18)

- Config consists of graph nodes (`Config`), versioned, content-addressed.
- `ConfigResolver` atomically swaps the version (a decorator); providers/tools/templates
  are re-read lazily, without a restart.
- The System Agent changes config by mutating nodes; the kernel reacts by subscription.
- **Separation of rights:** writing configs is allowed; **reading secrets is not** (see §12).

---

## 6. Sandbox (R1, R15)

### 6.1 SPI

```java
interface Sandbox {
    Shell shell();       // PTY
    Workspace fs();      // контролируемая ФС
    Exec exec();         // запуск процессов
    void close();
}
```

### 6.2 Docker + gVisor (primary)

- Agent container:
  - **non-root**; **read-only rootfs**; a separate writable layer (overlay);
  - **host FS is not mounted**; only explicitly approved project mounts;
  - `--runtime=runsc` (gVisor);
  - `--cap-drop=ALL`, `no-new-privileges`, seccomp/AppArmor;
  - limits `--memory`, `--pids-limit`, `--cpus`;
  - **network default-deny**: egress only through an allowlist proxy (GitHub API, providers).
- **The Docker socket is unavailable to the agent.**
- An isolated shell = a PTY in the container, started by the kernel.

### 6.3 Platforms

- **macOS/Linux:** Docker Engine in a Linux VM → `runsc`. Standard.
- **Windows:** Docker Desktop with the **WSL2 backend**; `runsc` — in the WSL2 distro.
- **Fallback:** no `runsc` → hardened Docker; the fact of degradation is **explicit** in the
  UI (not silent) and moves the affected card to 🟡 YELLOW.

### 6.4 Project and mounts (R12)

- **`Project` is a logical entity, not tied to one folder.** One project may include 1, 2 or
  more mounted directories of the real OS.
- **A mount is requested by the agent, the decision is the human's.** Flow:
  1. the agent writes a `MountRequest{path, mode(ro|rw), reason}` node;
  2. the request card moves to 🔴 RED;
  3. the human approves/rejects;
  4. on approval the kernel adds the mount to the containers of the relevant agents.
- By default a mount is `ro`; `rw` requires explicit confirmation.
- One agent may touch several projects; projects share lessons (see §7.5), but
  separate artifacts/mounts.

---

## 7. Context graph (R7, R8, R10, R12)

### 7.1 Nodes

`Task`, `Decision`, `Evidence`, `Artifact`, `Failure`, `Lesson`, `HumanQuestion`, `Summary`,
`Review`, `Finding`, `Config`, `MountRequest`.

### 7.2 Edges

`depends_on`, `derived_from`, `supersedes`, `invalidated_by`, `taught_by`, `addresses`,
`produces`.

### 7.3 Content-addressing and storage

- A node is addressed by the SHA-256 of its canonical JSON. Storage: **SQLite** (nodes, edges,
  events) + a blob-store for artifacts. Portable, serverless.
- Idempotent writes: the same node = the same address (the basis of sync, R11).

### 7.4 Slices and reading

- An agent reads a **slice**: `slice(taskId)` — the closure over the edges
  `derived_from`/`depends_on`/`addresses` to a given depth, prioritizing fresh and relevant
  items. The slice is the input of the agent's prompt.
- Write: `attach(node, edges[])`.

### 7.5 Compaction and lessons (R10)

- A `Summary` node references its sources (`derived_from`); the originals are kept/archived
  ⇒ compression **without loss of provenance**. Lessons survive a session/project change.
- **`Lesson`** (a log of mistakes, first-class):
  - `trigger` (a glob over task tags / error signatures), `advice`, `validity`,
    `sourceFailure`, `confidence`;
  - before a step the agent queries relevant lessons; matches are injected into the prompt
    as **constraints**;
  - success ("made a fix — it worked") is recorded as a `Lesson` and reused ⇒ this solves
    the pain "success is lost when the task changes".

### 7.6 Rescuing the context of a dead agent (R8)

- Crash/stall → the subtree is `stalled`.
- **Repair-flow:** the repair agent inherits the upstream context along the edges (decisions,
  evidence, artifacts), not the raw dialogue, and continues from the break point.
- The repair agent may have a different model.

### 7.7 `pootos-context` packages

- `io.github.artemget.pootos.context.node` — `Node`, `NodeId`, `LessonNode`, `EscapedText`
  (a JSON-escaping `org.cactoos.Text`); text and digest primitives are `org.cactoos.*`;
- `io.github.artemget.pootos.context.edge` — `Edge`, `TypedEdge`, `Relation`;
- `io.github.artemget.pootos.context.graph` — `Graph`, `MemoryGraph`.

### 7.8 Memory and recall (ADR-003, *proposed*)

- **The source of truth is our own content-addressed graph** (`Task`/`Decision`/`Lesson` +
  typed edges). Determinism is mandatory for the guarantee of the log of mistakes; a
  probabilistic memory layer cannot provide it.
- Memory is an **SPI**: `ContextGraph` + `Recall`. Persistence is SQLite; semantic recall is
  pluggable (optional and replaceable).
- **TencentDB Agent Memory is accepted as a DESIGN REFERENCE, not a runtime dependency:** we
  borrow the idea of layered memory (conversation → atom → scenario → persona) and the
  skill-asset/ACL model.
- If an external recall/temporal engine behind the SPI is needed later, **Graphiti/Zep**
  (a bitemporal knowledge graph) is considered first, and only then TencentDB.
- Status — **proposed**; awaiting Owner confirmation. Details: `docs/adr/ADR-003-context-memory.md`.

---

## 8. Taxonomy of human-dependent tasks (R9)

- `HumanQuestion{level, requiredCapability, deadline, escalationPolicy}`:
  - **L0** auto · **L1** notify · **L2** ask · **L3** human only.
- A blocking task → a deferred queue; independent DAG branches **continue**.
- Display via zones: L2/L3 → 🔴; "desirable to look" → 🟡.

---

## 9. System Agent (R2, R5, R17, R18)

The same agent runtime, with a privileged set of operations:

- `define_provider`, `define_tool`, `define_mcp`, `define_agent_template`, `define_skill`,
  `set_policy`, `define_human_task_level`, `create_task`, `assign_agent`, `move_zone`.
- Each operation = writing `Config`/`Template`/`Task` nodes; the kernel applies hot-reload.
- **Accepts instructions from the main chat and distributes them without blocking its loop.**
- **Self-configuration:** the System Agent configures itself and the pootOS application **via
  agents** — but **does not read secrets** (see §12).

---

## 10. Agent runtime (R3, R14, R15)

- **Agent Template** — a declaration: system prompt, skills, allowed tools/MCP, sandbox type,
  default `ModelRef`, budgets, resource requirements, allowed mounts.
- **Skills** — loadable recipes (including EO-review, §13); authored via the
  `programming-philosophy-skill` pipeline.
- **Tools / MCP** — a Java MCP client; convenient wiring; tools are granted by template.
- **Subagents (R14):** `spawn(template, ModelRef)`; a subagent's `ModelRef` may differ from
  the parent's. The parent is not blocked: a subagent is a separate actor.
- Agent step: `read slice → query lessons → act → attach nodes → renew lease`. Each step is
  idempotent and logged.
- **AgentExecutor (ADR-002):** "how to execute one agent turn" is a replaceable SPI
  `AgentExecutor`; an agent is a durable actor whose turns are delegated to an executor. The
  first adapter is **`OpenCodeExecutor`**: the kernel launches **opencode** (server/SDK)
  **inside the sandbox**, projects a **context-graph slice** to the executor's input and
  writes its outputs back as graph nodes. The choice of model/provider is through the
  executor's config, but is **subordinate to pootOS leases and budgets**. The opencode
  Node/TS runtime is **isolated in the sandbox** and is not a kernel dependency. A native
  Java/EO executor behind the same SPI is possible later.
- Agent configuration is **by hand or via the System Agent**.

---

## 11. Providers (R3)

- SPI `ModelProvider { complete(request): response }` + a registry.
- MVP adapters: OpenAI-compatible, Anthropic, local (Ollama/other).
- Per-agent `ModelRef{provider, model, params}`; the provider rate-limit is a resource with a
  lease (`llm:openai:rpm`) against throttling with parallel agents.

---

## 12. Configuration and secret security (R18)

The key separation ("fill the config — yes, obtain the token — no"):

- **Config-write:** the System Agent/agents may create and edit configs (providers, tools,
  MCP, templates) — those are graph data, not secrets.
- **Secret-read:** access to tokens/keys is forbidden to agents. Storage — Windows
  Credential Manager / macOS Keychain.
- **Flow when a secret is needed:**
  1. the agent forms a `SecretRequest` — the card moves to 🔴 RED;
  2. the **human** enters/confirms the secret (never the agent);
  3. the secret is supplied **at the sandbox boundary** (secret file/env), **not** into the
     context graph, prompt or log (centralized redaction);
  4. the `Config` receives a **reference** to the secret (`SecretRef`), not the value.
- **GitHub token:** where possible it is passed **through the kernel** (the agent does not see
  the value); the alternative is a `SecretRef` at the sandbox boundary. The decision — ADR-005.

---

## 13. GitHub integration and the review agent (MVP task)

### 13.1 Flow

```
GitHub webhook (pull_request: opened|synchronize|reopened)
        │  HMAC-подпись проверяется ядром
        ▼
Card(Task) ──► lease(net:egress, llm:*) ──► зона GREEN
        │
        ▼
Ревью-агент в песочнице:
  1. resolve repo/PR; прочитать связанный issue (PR закрывает РОВНО его)
  2. получить diff (GitHub API через ядро; токен не в контейнере)
  3. EO-проверки (§13.2)
  4. при необходимости — фоновый `mvn -Pqulice -Pjtcop` в песочнице (таймаут + лог)
  5. опубликовать ревью + строчные комментарии; развести nit vs follow-up
  6. применить метку `approved`/`needs-review`
  7. записать узлы Review/Finding/Lesson
```

### 13.2 Review conventions (EO skill + `teleroute` practice)

- **Correctness** — does what the issue asks; edge-cases.
- **Tests** — a reproducing test (failed before), one `assertThat` per test, Hamcrest, a
  `final` class, no fields, name `*Test`.
- **Style/EO** — `final` classes, `private final` fields, no static/`null`/`instanceof`
  (except `equals`), no blind casts.
- **Process** — a branch per issue, commits with `#<issue>`, one concern, no history rewrite.
- **Gates** — Qulice 0.36 + jtcop green; read CI via GitHub MCP (do not run a long foreground
  build blindly).
- **Triage:** a nit → a fix in the same PR; a larger item → a separate issue `Follow-up: #NNN`;
  reply to every comment; resolve your own threads.
- **Single account:** `Approve` is impossible ⇒ **the label is the verdict**; no meta-comments.
- **Response format:** `verdict / checks / actions / remaining`.
- Sources: `references/11-reviewer-playbook.md`, `references/07-process.md` of the EO skill;
  PRs/issues of `ArtemGet/teleroute` (issue-first with a minimal repro, `Closes #N`,
  What/Why/How/Test plan, gate, PDD puzzles).

---

## 14. UI (R4, R12)

- **Main chat** — the only one; System Agent control, reports, edits.
- **Canvas** — RED/YELLOW/GREEN zones; cards (task/artifact) with drill-in and direct writing;
  moving cards between zones.
- **Agents** — "currently working" and "all"; an agent card: template, `ModelRef`, heartbeat,
  leases, steps, event stream, mounts.
- **Resource dashboard** — leases, TTL, budgets, cost, stalled; actions
  optimize/stop/restart.
- **Human queue** — `HumanQuestion` L0–L3, deadlines, escalations.
- Frontend: React + TS + Vite + React Flow; live updates over WebSocket. Later — a desktop on
  Kotlin Multiplatform/Compose on top of the same `pootos-ui-api`.

---

## 15. Synchronization between machines (R11) — design

- The graph = an append-only log; nodes are content-addressed ⇒ merging = union of sets.
- Transport (to be chosen later): a git repository data-dir · Syncthing · a sync server.
- `HumanQuestion`/`Config` — conflict points; CRDT semantics (LWW/explicit resolution).
- "Server → server" transfer: log + blob-store; an agent is restored by replay.

---

## 16. Technology stack

| Layer | Choice | Why |
|---|---|---|
| Backend | Java 25 (Loom), Maven, EO | virtual threads = hundreds of agents cheaply; EO style + gates |
| Quality | Qulice 0.36, jtcop, JaCoCo/PIT | as in `teleroute`; fixed in the POM |
| Agent executor | SPI `AgentExecutor`; `OpenCodeExecutor` adapter (opencode) in the sandbox | reuse opencode; the Node/TS runtime is **isolated** in the sandbox (ADR-002) |
| Dependency gate | `google/osv-scanner` (keyless) | no secrets, self-contained CI; fails on high/critical (ADR-004) |
| Scheduler | own event-sourced kernel (MVP); an SPI for Temporal later | lightweight local launch without a server |
| Storage | SQLite (WAL) + blob-store | portable, without an external server |
| Sandbox | Docker + gVisor (`runsc`) | R1/R15; cross-platform |
| Providers | OpenAI-compat / Anthropic / local | R3, cheap start |
| MCP | Java MCP client | tools out of the box |
| Secrets | Win Credential Manager / macOS Keychain | do not write secrets into the graph |
| UI | React + TS + Vite + React Flow | fast start, graph visualization |
| Packaging | jpackage/bootstrap + Docker | an application for Win/macOS |

### 16.1 Working environment (verified)

- git 2.43 · Maven 3.9.9 · Docker 28.5 · **JDK 25 = `C:\Users\Артем\.jdks\temurin-25`**
  (by default the PATH has JDK 17 — for the build, set `JAVA_HOME` to 25).
- `gh` is not installed; **a local non-interactive `git push` is impossible** (no stored
  token) ⇒ remote writes and PR/merge go **via GitHub MCP**.
  Optional: issue a fine-grained PAT for a direct `git push` by subagents (see §22).

---

## 17. Security (what we guarantee)

- **FS:** an agent sees only approved mounts; the host outside is inaccessible.
- **Processes:** gVisor syscall isolation; no `--privileged`; cap-drop ALL.
- **Network:** default-deny; egress by allowlist through a proxy.
- **Docker:** the socket belongs only to the kernel.
- **Secrets:** keychain; **the agent does not read**; only `SecretRef`; log redaction.
- **GitHub:** webhook HMAC, allowlist, minimal rights (read + comment/label).
- **Dependencies:** the `google/osv-scanner` gate (keyless, OSV database) — the build fails on
  high/critical vulnerabilities; without an API key or external secrets (ADR-004).
- **Audit:** all actions are events (who, what, when, under which lease).

---

## 18. Observability

- Metrics: active agents, queue depth, resource occupancy, TTL expirations, tokens,
  cost, step latency, budget/watchdog triggers.
- An event stream in the UI; the journal is suitable for a post-mortem and generates `Lesson`s.

---

## 19. The development factory (how pootOS is built)

Roles (all are agents, except the Owner):

- **Orchestrator** (main agent): describes the architecture, decomposes into subtasks, keeps
  tracks, reports to the Owner; validates minimally. **Writes no code itself.**
- **Coding subagent**: implements one subtask in an isolated workspace; must leave test(s);
  follows EO + gates.
- **Review subagent**: reviews independently; **on success — merges** (see §19.2).
- **Docs subagent**: keeps `ru/en/zh-CN` in sync.
- **Docs-auditor agent**: periodically verifies documentation freshness.

### 19.1 Flow

```
Owner → главный чат → Orchestrator
Orchestrator → подзадача → Coding subagent (локальный клон, ветка)
        → build `mvn --errors --batch-mode clean install -Pqulice -Pjtcop` (JAVA_HOME=25)
        → PR (через GitHub MCP)
        → Review subagent (по EO-плейбуку)
        → если ОК: Review subagent мержит; иначе: замечания → новый цикл
Orchestrator → трек + отчёт Owner
```

### 19.2 Rules

- One concern per PR; small PRs; a test for every behavior/bug.
- Gates `Qulice 0.36 + jtcop + JaCoCo` are mandatory; a green build is the condition to merge.
- The reviewer **merges only on success** (on a single account — the label as the verdict).
- No history rewrite; commits reference the issue (`#<issue>`).
- Code is written **only by subagents**; the Orchestrator — specs/decomposition/tracks/reports.

---

## 20. Roadmap / MVP slice

1. **Thinking** (now): this document. ✅
2. **Block 0 — scaffold:** parent POM (Java 25, Qulice 0.36, jtcop, JaCoCo),
   `.gitignore`, `LICENSE`, `README`, CI workflow; a green build with a trivial module.
3. **Block 1 — context:** nodes/edges/slices/content-addressing/compaction + `Lesson`.
4. **Block 2 — kernel:** actors on virtual threads + event log + leases + watchdog + budgets
   + zones.
5. **Block 3 — sandbox:** `Sandbox` SPI + `docker+runsc` (isolated shell, mounts).
6. **Block 4 — providers + agent runtime:** `ModelRef` per-agent, templates, skills, subagents.
7. **Block 5 — system agent:** config-write, hot-reload, secret request → RED.
8. **Block 6 — github + review agent:** webhook → EO review → label (dogfooding of subagent PRs).
9. **Block 7 — ui-api + web:** canvas (zones/cards/agents), resource dashboard, chat.
10. **Block 8 — docs:** en + zh-CN; auditor.

**Definition of Done for the MVP:** the isolation negative test is green; two parallel agents do
not share a resource; webhook → review conforms to the EO conventions; a config change without
a restart; the package installs and runs on Windows and macOS.

---

## 21. Risks and open questions

- **gVisor on Docker Desktop (Win/macOS):** verify `runsc` in WSL2/Linux VM; otherwise —
  explicit degradation to hardened Docker (§6.3).
- **LLM review quality:** mitigated by a strict EO skill + `Lesson`.
- **Review cost/latency:** budgets, allowlist, model differentiation by PR size.
- **Single-node durability:** event log + SQLite; later — move the log out.
- **Synchronization:** deferred; content-addressing is laid down from the start.
- **Local push:** no non-interactive token ⇒ MCP; optionally — a PAT (§22).

---

## 22. Open decisions for the Owner

1. **PAT for git:** issue a fine-grained PAT (contents: read/write) for a direct `git push` by
   subagents — or do we keep commits via GitHub MCP? (I recommend a PAT: greater autonomy of
   the development factory.)
2. **JDK 25:** confirm the use of `temurin-25` (the PATH currently has 17).
3. **MVP sandbox constraints:** whether an isolation negative test is needed in the DoD as
   blocking.
4. **Language canon:** keep `ru` as the canon (recommended), or make the canon `en`?

---

## 23. ADR (Architecture Decision Records)

The full list and index are in [`docs/adr/README.md`](adr/README.md).

| ADR | Decision | Status |
|---|---|---|
| [ADR-001](adr/ADR-001-sandbox-isolation.md) | Sandbox isolation = Docker + gVisor | Accepted |
| [ADR-002](adr/ADR-002-agent-executor.md) | Agents go through the SPI `AgentExecutor`; the first adapter is opencode | Accepted |
| [ADR-003](adr/ADR-003-context-memory.md) | Memory = our content-addressed graph as the source of truth + external recall behind the SPI | Proposed |
| [ADR-004](adr/ADR-004-dependency-vulnerability-gate.md) | Dependency gate = `osv-scanner` (keyless) | Accepted |

Open (not yet drafted): the genesis of the kernel (own vs Temporal/Restate), the choice of
storage (SQLite vs KV/LMDB), synchronization (git-dir vs Syncthing vs a sync server), the
GitHub token (through the kernel proxy vs `SecretRef` into the container).
