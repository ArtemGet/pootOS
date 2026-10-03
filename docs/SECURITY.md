# pootOS — build-time security policy

Companion to `AGENTS.md` §3 and `docs/ARCHITECTURE.md` §17. Every gate here is **binding**:
a violation turns the build red and blocks a merge. Enforced in CI and, where feasible, locally.

---

## 1. Why

pootOS runs autonomous agents and touches repositories, providers and (later) secrets. The
build itself must not become a leak channel. Three classes of risk are gated:

1. **Vulnerable dependencies** — a known-CVE library shipped into the runtime.
2. **Secrets/credentials** — tokens, keys, passwords committed (or logged) by mistake.
3. **PII** — personal data (emails, phones, names, IDs) of real people committed to the repo.

Plus the standard EO quality gates (Qulice, jtcop, JaCoCo/PIT) from `AGENTS.md` §3.

---

## 2. Gates

| Gate | Tool (proposed) | Failure threshold | Phase |
|---|---|---|---|
| Dependency vulnerabilities | OWASP Dependency-Check (Maven plugin) | any **high/critical** (CVSS ≥ 7) | `verify` |
| Secrets/credentials | `gitleaks` (`detect`, incl. `--no-git` sweep) | any finding | pre-commit + CI |
| PII | a configurable scanner (regex set for email/phone/IBAN/card + allowlist) | any finding outside the allowlist | CI |
| PII in logs | centralized redaction + log scan | any unredacted hit | CI |

- Gates run in **CI** on every push/PR, and SHOULD run locally before commit.
- A **narrow, ticket-linked** allowlist is allowed (e.g. a test fixture email); a blanket
  disable is not. Every suppression names a ticket in a comment.
- Tool choices are finalized in the security-gates ticket; this file is the policy of record.

### CI secrets (optional)

- **`NVD_API_KEY`** — **optional** repository secret consumed by the `dependencies` CI job
  (`org.owasp:dependency-check-maven`). When set, the NVD API key raises the feed download rate
  limit and shortens the scan; when **unset or empty** the job degrades to the anonymous scan
  (no key is passed) instead of failing — an empty secret is never exported as a broken key.
  Never hardcode the key; it is injected only as an environment variable in CI.

---

## 3. Rules for agents

- Never commit or log: tokens, API keys, passwords, private keys, `.env` files, credentials,
  build logs, or **any data unrelated to the ticket**.
- No PII of real people in tests, fixtures, docs, or commit messages — use synthetic data.
- Do not add a dependency without checking it has no known critical CVE.
- If a task genuinely needs a secret, do not embed it — raise a `SecretRequest` (card → 🔴 RED);
  the secret is injected at the sandbox boundary as a `SecretRef` and never enters the graph,
  prompts, or logs (`docs/ARCHITECTURE.md` §12).

---

## 4. Suppressing a finding

A suppression is a one-line, ticket-linked exception in the scanner config; it must state the
ticket and the reason. CI shows all active suppressions in its summary so they are reviewed.

---

## 5. Definition of done (security)

- [ ] No dependency with a high/critical CVE.
- [ ] Secret scanner clean (working tree + history).
- [ ] PII scanner clean (or allowlisted with a ticket).
- [ ] No secrets/logs/PII in the diff.
