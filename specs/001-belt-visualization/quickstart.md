# Quickstart: Sushi Belt Visualization

## Prerequisites

1. Decide which backend runtime you are using:
   - local Spring run: backend typically available at `http://localhost:8080`
   - Docker Compose run: backend available at `http://localhost:8088`
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. The repo now includes `proxy.conf.json` and `npm start` uses it by default.

### Suggested proxy configuration

The checked-in `proxy.conf.json` targets Docker Compose on `http://localhost:8088`.

If you are running Spring locally instead, temporarily change the target to `http://localhost:8080`:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

The Docker Compose default is:

```json
{
  "/api": {
    "target": "http://localhost:8088",
    "secure": false,
    "changeOrigin": true
  }
}
```

## Implementation Outline

1. Replace the Angular CLI placeholder shell in `frontend/src/app/app.ts`, `frontend/src/app/app.html`, and `frontend/src/app/app.css` with the read-only belt overview page.
2. Add a small `frontend/src/app/belt-visualization/` area for:
   - polling and freshness state,
   - DTO-to-view-model mapping,
   - pure motion helpers,
   - focused belt stage rendering.
3. Reuse `frontend/src/app/api/belts.api.ts` for `getAllBelts()`, `getBeltSnapshot()`, and `getSeatOverview()`.
4. Treat `frontend/src/app/api/generated/openapi.types.ts` as generated code and do not edit it.
5. Prefer relative `/api/...` requests in the frontend implementation and keep runtime backend host differences in the proxy configuration rather than in page-level code.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Confirm the page tracks the first returned belt from `GET /api/v1/belts` and does not expose any belt-selection UI in this slice.
3. Confirm the page renders a circular belt with stable slot identities and fixed seats around the outside ring.
4. Confirm occupied slots show returned plate data without invented redistribution.
5. Confirm the belt continues moving between refreshes when `beltSpeedSlotsPerTick > 0` and remains stationary when it is `0`.
6. Confirm a fresh snapshot with changed timing data is applied immediately, even if the visible position jumps.
7. Confirm that a temporary refresh failure keeps the last valid state visible and surfaces degraded freshness messaging.
8. Confirm reduced-motion mode disables continuous interpolation while preserving slot, seat, paused, and stale information.

## Automated Verification

1. Add Vitest unit tests for motion math in `frontend/src/app/belt-visualization/motion.spec.ts`.
2. Add view-model/state tests for loading, degraded freshness, empty belt list, paused belt, reduced-motion behavior, and refresh-after-write refresh behavior.
3. Update `frontend/src/app/app.spec.ts` or add focused component tests for the rendered page shell.
4. Run `npm test`.
5. Run `npm run build`.

## Suggested Fixture Coverage

1. Empty belt with zero occupied slots.
2. Moving belt with non-zero speed and complete timing fields.
3. Paused belt with `beltSpeedSlotsPerTick = 0`.
4. Snapshot jump caused by a changed base offset or speed.
5. Seat and belt responses arriving with different freshness.
6. Recoverable refresh failure after a prior successful render.
7. Immediate reread triggered by the store-level refresh-after-write hook.
