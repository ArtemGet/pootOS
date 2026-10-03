# syntax=docker/dockerfile:1
#
# pootOS agent image: a Node runtime with the opencode CLI (ADR-001, ADR-002).
#
# The image carries NO credentials. Provider auth is injected by the kernel at
# run time (ADR-006) and is never baked in here. Build with:
#
#   docker build --build-arg OPENCODE_VERSION=1.18.34 \
#     -t pootos/opencode:1.18.34 docker/agent
#
FROM node:22-bookworm-slim

ARG OPENCODE_VERSION=1.18.34

RUN npm install --global --no-fund --no-audit "opencode-ai@${OPENCODE_VERSION}" \
    && npm cache clean --force \
    && rm -rf /root/.npm

# The sandbox runs as 65534:65534 on a read-only rootfs, so opencode must not
# need a writable home; callers point HOME at the writable /tmp instead.
ENV HOME=/tmp

CMD ["opencode", "--version"]
