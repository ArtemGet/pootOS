# pootOS web UI

The pootOS web UI — the **0.1.0 web-first entry point** (see `docs/ARCHITECTURE.md` §14). A
standalone **Vite + React + TypeScript** app using **React Flow** (`@xyflow/react`) for the
context graph.

This app is intentionally **not** part of the Maven reactor — it is a separate frontend project.

## Status

Skeleton only: local mock data, no backend integration, no real logic yet. The layout mirrors the
planned UI:

- top bar with the single **System Agent** chat placeholder;
- **attention zones** canvas (RED / YELLOW / GREEN);
- **context graph** placeholder (React Flow);
- **agent cards** panel;
- **resource dashboard** placeholder.

## Requirements

- Node.js 20+ and npm.

## Run

```bash
cd pootos-web
npm install
npm run dev      # dev server at http://localhost:5173
npm run build    # type-check + production build into dist/
npm run preview  # serve the production build
```

`node_modules/` and `dist/` are gitignored.

## Mapping to architecture

| Region | Component | `ARCHITECTURE.md` §14 |
|---|---|---|
| System-Agent chat | `src/components/TopBar.tsx` | main chat |
| Attention zones | `src/components/AttentionZones.tsx` | canvas (zones) |
| Context graph | `src/components/ContextGraph.tsx` | canvas (graph) |
| Agent cards | `src/components/AgentCards.tsx` | agents |
| Resource dashboard | `src/components/ResourceDashboard.tsx` | resource dashboard |
