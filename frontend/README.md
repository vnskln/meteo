# Frontend

Single-page app for the Meteo project, built with React, TypeScript and Vite. So far it has routing,
the page layout, the list of series and a plain table of the last 24 hours. The chart, filters,
login and admin panel are still to come.

## Running locally

Requires Node.js 22.12+.

```bash
npm install
npm run dev      # http://localhost:5173
npm run lint     # oxlint, including accessibility rules; any warning fails
npm run build    # type check, then the production bundle in dist/
```

The dev server forwards `/api` to staging (see `vite.config.ts`), so no local backend is needed.

## Docker image

The `Dockerfile` builds the app with Node.js and copies `dist/` into a Caddy image, so the running
container has no Node.js. `Caddyfile` serves the files and returns `index.html` for paths like
`/admin`, which the router handles in the browser. HTTPS and security headers are set by the Caddy
in front of it (`deploy/edge/`).
