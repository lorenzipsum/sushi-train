# Frontend

Angular 21 standalone frontend for the sushi-train demo application. The app renders the main belt experience, seat lifecycle, plate picking, restored seat orders, operator-side belt plate placement, and realtime belt refresh behavior inside the same UI.

## Current State

Shipped frontend features:

- `001-belt-visualization`: read-only belt baseline
- `002-belt-layout-redesign`: richer kitchen-centered belt-stage presentation
- `003-occupy-seat`: start dining from an available seat
- `004-checkout-seat`: checkout flow and summary feedback
- `005-pick-plates`: guest plate picking from the belt
- `006-hydrate-seat-orders`: reload-time order restoration and selected-seat continuity
- `007-add-plates-belt`: demo-mode operator flow for adding new plates to the belt
- `008-realtime-belt-updates`: server-sent-event belt updates with polling fallback, delivered as shared backend-plus-frontend work

Planned next work lives in `specs/ROADMAP.md`.

## Tech Stack

- Angular 21 standalone application
- TypeScript 5.9
- RxJS
- Vitest through the Angular test builder
- Generated OpenAPI TypeScript types from `contracts/openapi.json`

## Development

Install dependencies:

```bash
npm install
```

Start the frontend with the local API proxy:

```bash
npm start
```

The dev server runs on `http://localhost:4200/` and proxies `/api` requests to `http://localhost:8088` via `proxy.conf.json`.

## Scripts

Start the development server:

```bash
npm start
```

Build the production bundle:

```bash
npm run build
```

Run tests:

```bash
npm test
```

Regenerate API types after contract changes:

```bash
npm run api:types
```

## Project Structure

- `src/app/`: application shell, store, belt visualization, and API services
- `src/app/api/generated/`: generated OpenAPI types
- `contracts/openapi.json`: API contract source
- `specs/`: feature specs, plans, tasks, and roadmap
- `public/`: static assets

## Notes

- Browser-facing API calls should stay relative to `/api/...` so the proxy handles local backend routing.
- Belt refresh now prefers server-sent events and falls back to polling when realtime events are unavailable.
- The `008-realtime-belt-updates` planning artifacts were maintained above `frontend/`, so that feature does not have a dedicated `frontend/specs/008-realtime-belt-updates/` folder.
- Unit tests are Vitest-based; there is no e2e framework configured in this workspace.
- Production output is written to `dist/frontend/`.

## Azure Runtime And API Integration

The frontend is intentionally kept browser-simple:

- browser API calls remain relative to `/api/...`
- the production container serves the Angular app through Nginx
- Nginx proxies `/api/*` to the backend using runtime environment variables

Current runtime proxy variables:

- `API_UPSTREAM_SCHEME`
- `API_UPSTREAM`

Local Docker Compose defaults:

- `API_UPSTREAM_SCHEME=http`
- `API_UPSTREAM=backend:8080`

Azure-oriented example:

- `API_UPSTREAM_SCHEME=https`
- `API_UPSTREAM=<backend-container-app-hostname>`

Initial Azure Container App assumptions:

- one frontend container app
- one replica only
- target port `80`
- runtime proxy scheme `https`
- runtime proxy host derived from the backend Container App FQDN
- image pulled from Azure Container Registry
- public ingress for the first working browser-facing deployment path

Why this approach is useful:

- the Angular app does not need environment-specific API code
- the same production frontend image can be reused locally and in Azure
- HTTPS backend routing can be configured at runtime without rebuilding the frontend image

The Nginx proxy configuration now also forwards the upstream host correctly and enables TLS server name indication for HTTPS backend targets, which is important for Azure Container Apps host-based routing.
