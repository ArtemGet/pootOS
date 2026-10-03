# Roadmap

Legend: ✅ done · 🟡 partial · ⬜ planned. Targets: Windows + macOS (mobile UI later).

## 0.1.0 — first runnable (web-first; no CLI)

The first version that runs and can host **reviewer/coder** agents; dogfood target: pootOS reviews
its own PRs.

| Area | Status | Notes |
|---|---|---|
| Context graph (nodes/edges/slices) | ✅ | `pootos-context` |
| Lesson / Task / Decision nodes | ✅ | content-addressed |
| Cactoos primitives (no homegrown Text) | ✅ | PR #42 |
| Memory spine (`ContextGraph`/`Recall` SPI) | 🟡 | graph core done; recall planned (ADR-003) |
| Graph persistence (SQLite + event log) | ⬜ | critical path |
| Sandbox SPI | ✅ | `pootos-sandbox` |
| Docker + gVisor sandbox adapter | ⬜ | critical path (ADR-001) |
| `AgentExecutor` SPI (+ Echo) | ✅ | `pootos-agent` (ADR-002) |
| OpenCode executor adapter | ⬜ | depends on the sandbox adapter |
| Kernel resource leases | ✅ | `pootos-kernel` |
| Kernel actors + watchdog + budgets | ⬜ | |
| **System Agent: manage LLM providers** (add main providers at runtime) | ⬜ | **MVP requirement** |
| **MCP: connect & manage MCP servers at runtime** | ⬜ | **MVP requirement** |
| System Agent: agent templates + hot-reload | ⬜ | |
| Web UI: graph, agent cards, resource dashboard, zones | 🟡 | scaffold PR #41; wiring pending |
| Reviewer + coder agent templates | ⬜ | EO skill |
| Security gates (Qulice/jtcop/JaCoCo, gitleaks, osv, PII) | ✅ | CI |
| Web CI (build the UI in CI) | ⬜ | follow-up |

## 0.2.0

- Attention zones (RED/YELLOW/GREEN) fully wired to real data.
- Subagents with per-agent models; System-Agent-driven agent configuration.
- Lessons recall; compaction; more node kinds.
- Desktop UI (Kotlin Multiplatform / Compose).

## 0.3.0+

- Cross-machine context sync.
- Graph-DB adapter behind the `GraphStore` SPI.
- Mobile UI client (Kotlin MP / Swift) against a pootOS server.
