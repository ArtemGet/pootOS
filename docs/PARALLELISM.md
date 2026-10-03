# Parallel development workspaces

How the dev factory runs several coding agents at once without them stepping on each other.
Companion to `AGENTS.md` §5–§6.

---

## 1. Model: one base clone + one git worktree per task

- **Base clone** (shared object store, stays on `main`, never edited):
  `C:\projects\pootOSForAgents\_base`
- **Per parallel task**: a `git worktree` at `C:\projects\pootOSForAgents\<issue>-<slug>`,
  on its own branch `<issue>-<slug>`, created from the freshest `origin/main`.

```
C:\projects\pootOSForAgents\
  _base              <- shared clone; `git fetch` happens here ONCE
  4-escapedtext      <- worktree, branch 4-escapedtext
  3-ci-workflow      <- worktree, branch 3-ci-workflow
  9-docs-i18n        <- worktree, branch 9-docs-i18n
```

Why worktrees, not separate clones: they **share one object database**, so there is no per-task
re-download and no per-task `git pull`. A single `git fetch` in `_base` makes the latest `main`
available to every new worktree. Each worktree is a fully independent working directory on its
own branch, outside the project repo, exactly as required.

## 2. Lifecycle of a task

1. Orchestrator runs `git -C C:\projects\pootOSForAgents\_base fetch origin --prune`.
2. Orchestrator creates the worktree: `git worktree add <path> -b <branch> origin/main`.
3. Coding agent works in the worktree, runs the gate, then pushes via **GitHub MCP**
   (`github_push_files` on `<branch>`), opens the PR, and never runs local `git push`
   (no credentials by design).
4. Review agent reviews via MCP and, on success, merges via MCP.
5. Orchestrator removes the worktree after merge: `git worktree remove <path>` and
   `git branch -D <branch>` in `_base`.

Because branches are distinct, `git worktree` never conflicts on branch checkout. `_base` stays
on `main`; worktrees never check out `main`.

## 3. What may run in parallel

Parallelism is decided by **file-set disjointness**, not by topic:

- Tasks whose file sets do **not** intersect run **in parallel** (Wave).
- Tasks that edit the **same file** must be **serialized** (later Wave). The usual hotspot is the
  root `pom.xml` (build/plugin changes) — never two pom editors at once.
- A task that **depends on** another's output waits for it (e.g. CI integration waits for the
  base CI workflow).

Orchestrator owns the wave plan (a tracking epic) and assigns each task its own worktree.

## 4. Resource caps

- Default **max 4 concurrent coding agents** (each may run a Maven build; more causes CPU and
  `~/.m2` contention).
- Builds use a shared local Maven repo; heavy parallel downloads are the main risk, so the
  first parallel wave should reuse cached dependencies.
- The safe-build protocol (`AGENTS.md` §3) applies in every worktree: background + log + tail
  polling + timeout.

## 5. Cleanup

After a PR is merged: `git -C _base worktree remove <path>`, `git -C _base branch -D <branch>`.
A periodic sweep removes worktrees whose PR is merged/closed.
