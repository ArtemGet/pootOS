# AGENTS.md — working contract for agents in pootOS

This file is the **contract** every coding/review agent follows. It encodes the project's
architecture, its Elegant Objects (EO) rules, the build gates, the security gates, and the
review process. Read `docs/ARCHITECTURE.md` for the system design and `docs/SECURITY.md`
for the build-time security policy.

> Language of this contract: English (tooling + code conventions). User-facing
> documentation is maintained in **ru / en / zh-CN** (see §8).

---

## 1. What pootOS is

An Agent Operating System: a sandboxed runtime for autonomous agents with a kernel
(durable actors, resource leases, watchdogs), a context **graph** instead of per-task chat,
attention zones (RED/YELLOW/GREEN), and a System Agent that configures everything at runtime.
Backend: **Java 21, Elegant Objects, Maven**. Frontend: React + TypeScript.

The MVP task is an **EO code-review agent** on GitHub PR webhooks.

---

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
9. No implementation inheritance — compose and decorate (`X implements Y` wrapping another `Y`).
10. No `-er` / job-title names (name what it *is*).
11. Every public method overrides an interface; keep interfaces short (≤3 methods).
12. Fail fast with checked exceptions; never swallow, never catch-and-log; recover once at top.
13. Javadoc on every public type/method (Qulice requires it); no inline narration.
14. **Never commit or log anything not needed by the ticket** — no secrets, tokens, API keys,
    passwords, private keys, `.env` files, credentials, personal data (PII), build logs, or
    generated artifacts. Secret/PII/vulnerability gates fail the build if any slip in (§3).

### Tests

- JUnit 5 + Hamcrest; **one `assertThat(...)` per test**.
- No `@Before`, no shared fields; **fakes over mocks**.
- Test class is `final`, named `*Test`, and constructs its subject (no fixtures).
- Every bug fix ships a test that **fails before** and passes after.

When you must deviate, state the reason in the ticket/PR. Never add a blanket
`@SuppressWarnings`; a narrow exclusion needs a ticket and a comment.

---

## 3. Build & gates

JDK 21 is required (virtual threads). In this environment the default `java` is 17 — always
point `JAVA_HOME` at the 21 install:

```powershell
$env:JAVA_HOME = "C:\Users\Артем\.jdks\corretto-21.0.6"
```

Full gate command (must be green before review):

```bash
mvn --errors --batch-mode clean install -Pqulice -Pjtcop
```

**Quality gates (mandatory, fail the build):**

- Qulice **0.36** — style/design/EO violations.
- jtcop — test conventions.
- JaCoCo/PIT — coverage/mutation gates in the POM.

**Security gates (mandatory, fail the build — see `docs/SECURITY.md`):**

- **Dependency vulnerabilities** — scan dependencies; the build **fails** on a
  high/critical vulnerability (CVSS ≥ 7).
- **Secrets/credentials** — scan the working tree and history; the build **fails** if any
  token/key/password is found.
- **PII** — scan tracked files; the build **fails** on personal data leaking into the repo.

Do **not** run a long foreground build blindly. If a check must run locally, run it with a
timeout and a log file, and poll the tail.

---

## 4. Module layout

`pootos-kernel`, `pootos-context`, `pootos-sandbox` (+ `-docker`, `-process`),
`pootos-providers`, `pootos-agent`, `pootos-system`, `pootos-github`, `pootos-ui-api`,
`pootos-skills`, `pootos-cli`; frontend in `pootos-web`. See `docs/ARCHITECTURE.md` §4.
Each module is a Maven module with its own `src/main/java` and `src/test/java`.

---

## 5. Process (ticket-first)

1. **No work without a ticket.** A ticket is a complaint with a minimal reproduction.
2. Branch named after the issue, off `main`; never commit to `main` directly.
3. Start with a failing test that reproduces the problem.
4. Smallest change that makes it pass; one concern per PR; small PRs.
5. Commit subject starts with `#<issue>`; no history rewrite (no force-push, no amend on
   shared branches).
6. PR body: `Closes #N`, `What` / `Why` / `How` / `Test plan` (the exact gate command).
7. Address **every** review comment: nits fixed in this PR, larger items become a linked
   `Follow-up: #NNN`.

### PR size (decompose!)

- **A PR must be small and reviewable.** Target **≤ ~200 changed lines** and a single concern.
  A ~1000-line PR is a defect of decomposition, not a feature.
- If a task cannot land in a small PR, **split it into multiple issues/subtasks** (and, if
  useful, a tracking epic) and open several PRs. Prefer a stack of small PRs over one big one.
- The Orchestrator decomposes work; the author may return a task as "too big, here is the split".

### PRs and merging in this environment

Local `git push` is **not** available non-interactively (no stored token). All remote
writes, PR creation and merging go through the **GitHub MCP** tools:

- `github_create_branch`, `github_push_files`, `github_create_pull_request`,
  `github_pull_request_read`, `github_pull_request_review_write`, `github_merge_pull_request`.

---

## 6. Review & merge

- Reviews follow the EO reviewer playbook: correctness, tests, EO style, process, gates.
- Read CI via the GitHub MCP (`pull_request_read` → `get_check_runs`); never call the GitHub
  API with a raw token.
- Triage each finding: **nit** → fix in this PR; **larger** → separate issue.
- In a single-account setup, GitHub forbids self-approve: the **label is the verdict**
  (`approved` / `needs-review`). Say nothing about the platform limit.
- **The reviewer merges only on success.** A red gate = no merge.
- Reviewer output shape:

  ```text
  verdict: APPROVED | CHANGES_REQUESTED
  checks:  <bullets>
  actions: <threads resolved; label applied; merged at <sha>>
  remaining: none | <concern>
  ```

---

## 7. Secrets & safety (agents)

- Agents may **write configs** (providers, tools, MCP, templates) — that is data, not secrets.
- Agents may **not read tokens/keys**. A needed secret raises a `SecretRequest` that moves the
  card to 🔴 RED for a human to supply; the secret is injected at the sandbox boundary as a
  `SecretRef` and never enters the context graph, prompts, or logs.
- Never commit secrets; never log them; redaction is centralized.
- **Never push logs, tokens, passwords, credentials, PII, or any data unrelated to the ticket.**
  The secret/PII gates in §3 enforce this at build time.

---

## 8. Documentation

- User-facing architecture is in **ru / en / zh-CN**: `docs/ARCHITECTURE.md` (ru canonical),
  `docs/ARCHITECTURE.en.md`, `docs/ARCHITECTURE.zh-CN.md`.
- Keep translations in sync; a docs-auditor agent periodically verifies freshness.
- Update `docs/ARCHITECTURE.md` whenever behavior/architecture changes.

---

## 9. Roles in the dev factory

- **Orchestrator** (main agent): architecture, task decomposition, tracking, reporting.
  Writes no code.
- **Coding subagent**: implements one subtask, leaves tests, keeps gates green.
- **Review subagent**: independent review; merges on success.
- **Docs subagent**: keeps ru/en/zh-CN current.
