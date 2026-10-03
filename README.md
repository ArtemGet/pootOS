# pootOS

An Agent Operating System — a sandboxed runtime for autonomous agents: a durable-actor kernel with
resource leases, a content-addressed **context graph** instead of per-task chat, attention zones,
and a System Agent that reconfigures everything at runtime. Backend: **Java 25 (Elegant Objects)**;
web UI: **React + TypeScript**.

> Languages: **en** (this file) · [ru](README.ru.md) · [zh-CN](README.zh-CN.md)

## Status

Early development (pre-0.1.0). **Nothing runnable yet** — the foundation is in place (context graph
primitives, sandbox SPI, executor SPI, kernel resource leases, CI security gates). See
[`docs/ROADMAP.md`](docs/ROADMAP.md).

## Quick start (build from source)

Requirements: **JDK 25** and **Maven** (Docker is required later, to run agents).

```bash
git clone https://github.com/ArtemGet/pootOS.git
cd pootOS
# Windows PowerShell:
$env:JAVA_HOME = "C:\Users\<you>\.jdks\temurin-25"
mvn --errors --batch-mode clean install -Pqulice -Pjtcop
```

A runnable `pootos` app is **not** available yet (target: 0.1.0, web-first).

## Documentation

- Architecture: [ru](docs/ARCHITECTURE.md) · [en](docs/ARCHITECTURE.en.md) · [zh-CN](docs/ARCHITECTURE.zh-CN.md)
- [Security policy](docs/SECURITY.md) · [Parallel workspaces](docs/PARALLELISM.md)
- [ADRs](docs/adr/) · [Roadmap](docs/ROADMAP.md) · [Contributor contract](AGENTS.md)

## Modules

`pootos-context` · `pootos-agent` · `pootos-sandbox` · `pootos-kernel` (more planned).

## License

MIT — see [LICENSE](LICENSE).
