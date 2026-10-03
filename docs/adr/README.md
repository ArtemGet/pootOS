# Architecture Decision Records

Short, immutable records of significant decisions. Format: Status / Context / Decision / Consequences.

| ADR | Decision | Status |
|---|---|---|
| [ADR-001](ADR-001-sandbox-isolation.md) | Sandbox isolation = Docker + gVisor | Accepted |
| [ADR-002](ADR-002-agent-executor.md) | Agents run through an `AgentExecutor` SPI; first adapter wraps opencode | Accepted |
| [ADR-003](ADR-003-context-memory.md) | Context memory = our content-addressed graph as source of truth + external recall engine behind SPI | Proposed |
| [ADR-004](ADR-004-dependency-vulnerability-gate.md) | Dependency gate = `osv-scanner` (keyless), no NVD key | Accepted |
| [ADR-005](ADR-005-inbound-channels.md) | Untrusted inbound channels + dispatcher agent | Accepted |
| [ADR-006](ADR-006-config-and-secrets.md) | Config = files + filesystem watch; secrets via `SecretForm` | Accepted |
