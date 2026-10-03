# Agent image (`docker/agent`)

The hardened container the OpenCode executor runs inside (ADR-001, ADR-002):
a minimal Node base with the `opencode` CLI installed at a pinned version.

## Build

```sh
docker build \
  --build-arg OPENCODE_VERSION=1.18.34 \
  --tag pootos/opencode:1.18.34 \
  docker/agent
```

The tag **must** match the pinned default in `OpenCodeImage`
(`pootos-agent`, package `io.github.artemget.pootos.agent.opencode`). Bump both
together.

## Run

`DockerSandbox` owns the isolation profile: read-only rootfs, non-root
(`65534:65534`), `cap-drop=ALL`, `no-new-privileges`, no network, and
memory/pids/cpu limits. A sanity check that mirrors that profile:

```sh
docker run --rm --read-only --tmpfs /tmp:rw,noexec,nosuid,size=64m \
  --user 65534:65534 --cap-drop ALL --security-opt no-new-privileges \
  --network none pootos/opencode:1.18.34 opencode --version
```

## Security

- **No credentials are baked in.** No API keys, tokens, `.env` files, or
  provider auth. Provider authentication is a kernel concern and requires a
  human (ADR-006).
- The rootfs is read-only at run time; only `/tmp` is writable.
- The build pins `opencode-ai@1.18.34`; bump it deliberately, under review.
