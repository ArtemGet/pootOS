# ADR-001: Sandbox isolation = Docker + gVisor

## Status
Accepted (Owner, 2026-10-03).

## Context
Agents must not reach the host filesystem or shell. The requirement is a real OS-level boundary:
the agent lives under a different root and cannot read the user's data.

## Decision
- Each agent runs in its own container with a **read-only rootfs**, a writable overlay, non-root
  user, `cap-drop=ALL`, `no-new-privileges`, seccomp/AppArmor, and memory/pids/cpu limits.
- Host directories are **not** mounted by default; only explicitly approved mounts appear
  (mount-on-request, human-approved).
- Runtime isolation uses **gVisor (`runsc`)**. On macOS/Linux it runs inside the Docker Linux VM;
  on Windows via the Docker Desktop **WSL2** backend.
- If `runsc` is unavailable, fall back to hardened Docker **and surface the degradation visibly**
  (the affected card moves to the YELLOW zone). Never silently degrade.
- The **Docker socket is available only to the pootOS kernel**, never to an agent.
- Egress is default-deny; allowed endpoints go through an allowlist proxy.

## Consequences
- Strong FS/process separation out of the box (R1/R15).
- Windows requires WSL2; gVisor availability must be verified per host.
- The sandbox is a swappable `Sandbox` SPI (Docker/gVisor today; microVM later).
