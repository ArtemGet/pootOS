# ADR-005: Untrusted inbound channels + dispatcher agent

## Status
Accepted (Owner, 2026-10-03).

## Context
Agents must be **woken by external signals** — e.g. GitHub webhooks, so that a reviewer agent starts
when a PR is opened. Inbound payloads come from outside the trust boundary and must never be trusted
blindly. We need: configurable **incoming channels**, **per-project bindings**, a **dispatcher agent**
that parses and routes an event to the right agent/task, **fraud/validity checks**, and the ability to
attach a **custom prompt** to a channel for the dispatcher — all configurable manually **and** by the
System Agent at runtime.

## Decision
- **Inbound Channels** subsystem: a channel is a named external source (GitHub webhooks first) with a
  verification method (HMAC signature), an allowlist, and a binding to a project.
- A **dispatcher agent** consumes raw inbound events, treats them as **UNTRUSTED**, runs
  fraud/validity checks, normalizes them into `Task` nodes in the context graph, and routes them to
  the correct agent template for the task.
- A **custom prompt** can be attached to a channel/stream for the dispatcher agent.
- Configuration (channels, bindings, parser prompt, verification settings) is **data in the context
  graph**; it is editable **manually (UI)** and by the **System Agent** at runtime (hot-reload).
- **Security:** verify HMAC; allowlist repositories; the dispatcher runs in a sandbox; secrets are
  `SecretRef` only; the dispatcher MUST NOT take privileged actions based solely on payload content
  (it may only create tasks/routes); anything sensitive escalates to 🔴 RED.

## Consequences
- Adds an intake module + the GitHub webhook receiver (`pootos-github`) and a new agent role
  (dispatcher).
- Routing/config become first-class configurable data, not hardcoded.
- Fraud checks combine deterministic validation (signature, schema, allowlist) with the dispatcher
  agent's judgement.
