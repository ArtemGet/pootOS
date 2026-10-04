# AGENTS.md — working contract for agents in pootOS

This file is the **contract** every coding/review agent follows. It encodes the project's
architecture, its Elegant Objects (EO) rules, the build gates, the security gates, and the
review process. Read `docs/ARCHITECTURE.md` for the system design, `docs/SECURITY.md` for the
build-time security policy, and `docs/BUILD.md` for the Maven environment.

> Language of this contract: English (tooling + code conventions). User-facing
> documentation is maintained in **ru / en / zh-CN** (see §8).

---

## 1. What pootOS is

An Agent Operating System: a sandboxed runtime for autonomous agents with a kernel (durable actors,
resource leases, watchdogs), a context **graph** instead of per-task chat, attention zones
(RED/YELLOW/GREEN), and a System Agent that configures everything at runtime. Backend: **Java 25
(latest LTS), Elegant Objects, Maven**. Frontend: React + TypeScript.

## 2. Non-negotiable EO rules

Every Java class in `pootos-*` modules MUST:

1. Be `final` (or `abstract`).
2. Have `private final` fields only — objects are immutable; mutators return a new object.
3. Have constructors that **only assign** (no code, no `new` of peers, no validation/I/O).
   Exactly one primary constructor, declared last; secondaries funnel via `this(...)`.
4. No `null` in any contract (accept, return, or signal). Use Null Object / empty / throw.
5. No `public static` (only `public static void main` and `private static final` constants).
6. No utility classes, no singletons, no DI containers — inject via constructors.
7. No getters/setters — expose behavior ("tell, don't ask").
8. No `instanceof` / casting / reflection (only the `equals(Object)` guard is allowed).
9. No implementation inheritance — compose and decorate.
10. No `-er` / job-title names (name what it *is*).
11. Every public method overrides an interface; keep interfaces short (≤3 methods).
12. Fail fast with checked exceptions; never swallow, never catch-and-log; recover once at top.
13. Javadoc on every public type/method (Qulice requires it); no inline narration.
14. **Never commit or log anything not needed by the ticket** — no secrets, tokens, API keys, passwords,
    private keys, `.env`, credentials, PII, build logs, or generated artifacts.
15. **Do not reinvent the standard library.** Prefer JDK and **Cactoos** (`org.cactoos.*`) over homegrown
    primitives (`Text`, hex, digests, string/collection helpers).

### Tests

- JUnit 5 + Hamcrest; **one `assertThat(...)` per test**.
- No `@Before`, no shared fields; **fakes over mocks**.
- Test class is `final`, named `*Test`, and constructs its subject (no fixtures).
- Every bug fix ships a test that **fails before** and passes after.

When you must deviate, state the reason in the ticket/PR. Never add a blanket `@SuppressWarnings`; a
narrow exclusion needs a ticket and a comment.

## 3. Build & gates

**Java 25 (latest LTS)** is required. In this environment the default `java` is 17 — always point
`JAVA_HOME` at the 25 install:

```powershell
$env:JAVA_HOME = "C:\Users\Артем\.jdks\temurin-25"
```

Full gate (must be green before review):

```bash
mvn -s .mvn/settings.xml --errors --batch-mode --no-transfer-progress clean install -Pqulice -Pjtcop
```

**Maven environment (avoid stalls — see `docs/BUILD.md`).** `com.jcabi:parent` declares legacy
`oss.sonatype.org` repositories, and the local `~/.m2` may be seeded from unreachable corporate repos,
so Maven probes dead hosts on every resolve (multi-minute stalls). A `*` → Maven Central mirror is
configured machine-wide in `~/.m2/settings.xml` and shipped at **`.mvn/settings.xml`** — pass
`-s .mvn/settings.xml` for a hermetic, fast resolve. **Share the local repository** (default `~/.m2`);
do not isolate a repo per worktree.

- **Quality gates (fail the build):** Qulice 0.36, jtcop, JaCoCo/PIT.
- **Security gates (fail the build):** dependency vulnerabilities (**osv-scanner**, keyless, fail on
  high/critical), secrets/credentials (gitleaks), PII (`tools/pii-scan.py`). See `docs/SECURITY.md`.

The repo also builds the web app in CI (`pootos-web`). Do NOT run a long foreground build: run it in
the background with a log file and poll the tail. On Windows the tree MUST be LF (`.gitattributes`).

## 4. Module layout

`pootos-kernel`, `pootos-context`, `pootos-sandbox` (+ adapters), `pootos-providers`, `pootos-mcp`,
`pootos-agent`, `pootos-system`, `pootos-ui-api`; web in `pootos-web`. See `docs/ARCHITECTURE.md` §4
and `docs/ROADMAP.md`.

## 5. Process (ticket-first)

1. **No work without a ticket.**
2. **When you take an issue, apply the `in-progress` label** (`github_issue_write` update).
3. Branch named after the issue, off `main`; never commit to `main` directly.
4. Start with a failing test that reproduces the problem.
5. Smallest change that makes it pass; one concern per PR; **target ≤ ~200 changed lines**.
6. Commit subject starts with `#<issue>`; no history rewrite.
7. PR body: `Closes #N`, `What`/`Why`/`How`/`Test plan`.
8. Address every review comment: nits here, larger → linked follow-up issue.

### How agents write to GitHub (GitHub MCP)

Local `git push` is unavailable. Use the GitHub MCP directly: coding agent —
`github_issue_write` (labels) → `github_create_branch` → `github_push_files` → `github_create_pull_request`;
review agent — `github_pull_request_read` → `github_pull_request_review_write` → `github_issue_write`
(labels) → `github_merge_pull_request`.

## 6. Review & merge

- Reviewer playbook: correctness, tests, EO style, process, gates.
- **Wait for CI before merging:** a PR merges only when all required checks (`build`, `secrets`,
  `dependencies`, `pii`, `web`) are complete and green; poll `get_check_runs` in bounded intervals; a red
  or missing required check = no merge.
- Triage: **nit** → fix here; **larger** → separate issue.
- In a single-account setup the **label is the verdict** (`approved`/`needs-review`).
- **The review agent merges itself** (`github_merge_pull_request`) only when the review passed and CI is
  green. Prefer a merge commit.
- **Docs PRs also get a short ARCHITECTURE review** (docs must match code/§4/§10).

### Smell checks (mandatory)

A review MUST flag the following, and the finding must become an **issue + a fix** (not just a comment):

- **Reinvented stdlib / Cactoos** — a homegrown type duplicating a JDK/Cactoos primitive.
- **Anemic wrapper** — a class whose only job is to hold one value / rename a type, with no behavior.
- **Signature-only type** — a class that exists only to satisfy a method signature.
- **Duplicate types / parallel hierarchies.**

## 7. Secrets & safety (agents)

- Agents may write configs (data, not secrets) but MUST NOT read tokens/keys. A needed secret raises a
  `SecretRequest` → 🔴 RED for a human.
- **Never commit/log secrets, logs, PII, or data unrelated to the ticket.**

## 8. Documentation

- Architecture in **ru / en / zh-CN** (`docs/ARCHITECTURE.md` canon, `.en.md`, `.zh-CN.md`); keep in sync;
  a docs-auditor checks freshness and consistency with code.
- Status is tracked in `docs/ROADMAP.md` (done/partial/planned).

## 9. Roles

- **Orchestrator** — architecture, decomposition, tracking, reporting (no code).
- **Coding subagent** — one subtask, tests, gates, opens its own PR.
- **Review subagent** — independent review, waits for green CI, merges on success.
- **Docs subagent** — keeps ru/en/zh-CN current.
- **Docs-auditor** — periodic freshness/consistency check.

## 10. Code & package structure

- One module per boundary; inside a module, group types by concern into sub-packages
  (e.g. `…context.node` / `.edge` / `.graph`). Do not grow a flat package.
- Public types carry Javadoc; `package-info` per package; EO naming.
