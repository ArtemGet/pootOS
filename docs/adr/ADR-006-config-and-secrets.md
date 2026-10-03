# ADR-006: Configuration as files + filesystem watch; secrets via SecretForm

## Status
Accepted (Owner, 2026-10-03).

## Context
The System Agent must configure itself and the pootOS application at runtime **without restarting
the process** (requirements R2, R5, R16, R18). The earlier model stored configuration as `Config`
nodes inside the context graph. That couples mutable infrastructure config (providers, tools, MCP
servers, agent templates, policies) to the append-only, content-addressed memory graph, where every
write is an immutable node and every rollback is a new version. Config is operational data that
humans want to read, diff and edit directly.

Separately, secrets (API keys, tokens) must never be read by agents or by the executor that runs
their turns (`AGENTS.md` §7, ADR-002). A required secret raises a 🔴 RED decision for a human.

## Decision
- **Configuration is plain files.** Providers, tools, MCP servers, agent templates and policies live
  in a config directory (`ConfigDirectory`/`ConfigFile`), as ordinary data — **not** as values in the
  context graph.
- **Hot reload by filesystem watch.** A `ConfigWatcher` uses `java.nio.file.WatchService` to detect
  changes and triggers a reload through `ConfigResolver`; no process restart. The context graph keeps
  only `SecretRef` references, never secret values or mutable config.
- **The System Agent writes data, never secrets.** Its operations (`define_provider`, `define_tool`,
  `define_mcp`, `define_agent_template`, `define_skill`, `set_policy`, …) write config files.
- **Secrets are declared, not supplied, by the agent.** The System Agent presents a **`SecretForm`**:
  a **schema only** (`SecretField{name, label, provider, purpose}`), carrying **no values**.
- **A human supplies secret values.** The UI renders the `SecretForm`; the human enters the values,
  which are written to the **OS secret store** (Windows Credential Manager / macOS Keychain).
- **Agents and executors hold only `SecretRef`.** No agent and no `AgentExecutor` (e.g. opencode)
  can read secret values; only the kernel resolves a `SecretRef` at injection time, at the sandbox
  boundary, with centralized redaction.
- The first implementation is the `pootos-system` module (issue #68): `ConfigDirectory`,
  `ConfigFile`, `ConfigResolver`, `ConfigWatcher`, and `SecretField`/`SecretForm`/`SecretRef`.

## Consequences
- Config is versionable, diffable and editable as files; the context graph stays a pure,
  content-addressed memory log.
- Runtime changes need no restart and no graph mutation; the watch event is the change signal.
- No agent/executor path to secret values; a missing secret is a 🔴 RED decision for a human.
- Adds the `pootos-system` module and a config directory as runtime state; configuration drift is
  visible in the filesystem.
