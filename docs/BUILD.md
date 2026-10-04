# Build & Maven environment

Why local `mvn` builds sometimes stall for minutes and how to avoid it.

## Root cause

1. **`com.jcabi:parent`** (our root parent) declares **legacy `oss.sonatype.org`** repositories and a
   pluginRepository (plus `parent.jcabi.com`, `github-packages`). On any uncached artifact/plugin Maven
   probes those hosts; the legacy Nexus is slow/redirecting → long stalls.
2. The machine's `~/.m2` cache was seeded from **corporate repos** (`splunk.jfrog.io` → 403,
   internal `bcvm983.dev.ts:8081` → unresolved), leaving **1800+ stale `.lastUpdated` markers** and
   tracking metadata. Maven re-checks them on resolve.

CI is fast because the runner has a clean cache and only Central.

## Fix

- A **`*` → Maven Central mirror** is configured:
  - machine-wide in `~/.m2/settings.xml`, and
  - shipped in the repo at **`.mvn/settings.xml`**.
- Run builds with the repo settings for a hermetic, fast resolve:

  ```bash
  mvn -s .mvn/settings.xml --errors --batch-mode --no-transfer-progress clean install -Pqulice -Pjtcop
  ```

- **Share the local repository** (default `~/.m2/repository`); do not isolate a repo per worktree — the
  mirror removes the extra probing, and sharing reuses already-downloaded artifacts.
- Never run a long build in the foreground: background + log file + poll the tail (see `AGENTS.md` §3).

## If a stall still happens

- Check for stale markers: `Get-ChildItem ~/.m2/repository -Recurse -Filter *.lastUpdated`.
- One-off bypass of tracking: `-Daether.enhancedLocalRepository.trackingFilename=...` or clean the stale
  `_remote.repositories`/`*.lastUpdated` files.
