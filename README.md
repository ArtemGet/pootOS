# pootOS

pootOS is an Agent Operating System — a sandboxed runtime for autonomous agents built around a
durable-actor kernel, a content-addressed **context graph** (instead of per-task chat), attention
zones, and a System Agent that reconfigures everything at runtime. This is a Java 21 Maven
multi-module project written in the Elegant Objects style; the backend lives in `pootos-*` modules
and the UI lives in `pootos-web`. This scaffold starts the `pootos-context` module with the first
domain primitives (`NodeId`, `Node`, `Edge`, `Relation`, `LessonNode`).

## Build

JDK 21 is required. Point `JAVA_HOME` at a 21 install and run the full gate:

```bash
mvn --errors --batch-mode clean install -Pqulice -Pjtcop
```
