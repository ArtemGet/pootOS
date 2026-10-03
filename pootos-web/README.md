# pootOS web UI

The pootOS web UI — the **0.1.0 web-first entry point** (see `docs/ARCHITECTURE.md` §14). A
standalone **Vite + React + TypeScript** app using **React Flow** (`@xyflow/react`) for the
context graph.

This app is intentionally **not** part of the Maven reactor — it is a separate frontend project.

## Status

opencode-style dark theme (CSS variables in `src/theme.css`) plus a typed API client
(`src/api.ts`). The layout mirrors the planned UI:

- top bar with the single **System Agent** chat placeholder and a backend health indicator;
- **attention zones** canvas (RED / YELLOW / GREEN, tinted left borders);
- **context graph** (React Flow), wired to `GET /api/graph`;
- **agent cards** panel, wired to `GET /api/agents`;
- **resource dashboard**, wired to `GET /api/resources`.

### API client

`src/api.ts` reads `VITE_API_URL` (default `http://localhost:8080`) and fetches the backend
contract (`/api/health`, `/api/graph`, `/api/agents`, `/api/resources`). On any error the UI
falls back to the mock data in `src/mock.ts`, so it renders with or without a backend.

## Requirements

- Node.js **20.19+** or **22.12+** (Vite 7) and npm.

## Run

```bash
cd pootos-web
npm ci           # reproducible install from the committed package-lock.json
npm run dev      # dev server at http://localhost:5173
npm run build    # type-check + production build into dist/
npm run preview  # serve the production build
```

Dependencies are pinned by the committed `package-lock.json`; CI uses `npm ci`.
`node_modules/` and `dist/` are gitignored.

## Mapping to architecture

| Region | Component | `ARCHITECTURE.md` §14 |
|---|---|---|
| System-Agent chat | `src/components/TopBar.tsx` | main chat |
| Attention zones | `src/components/AttentionZones.tsx` | canvas (zones) |
| Context graph | `src/components/ContextGraph.tsx` | canvas (graph) |
| Agent cards | `src/components/AgentCards.tsx` | agents |
| Resource dashboard | `src/components/ResourceDashboard.tsx` | resource dashboard |
