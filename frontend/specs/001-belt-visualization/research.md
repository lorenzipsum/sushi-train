# Phase 0 Research: Sushi Belt Visualization

## Decision 1: Target the first available belt for the MVP

- **Decision**: On initial load, call `BeltsApi.getAllBelts()`, select the first returned belt ID, and use that ID for snapshot and seat polling.
- **Rationale**: The feature spec defines a single read-only overview page, and the current frontend has no route or selector surface. Choosing the first belt keeps the slice small and aligns with the constitution's conservative growth rule.
- **Alternatives considered**:
  - Hardcode a belt ID: rejected because it would couple the UI to seeded data and fail across environments.
  - Add a route parameter or belt picker now: rejected because multi-belt navigation is outside this slice and would add surface area without user value for MVP.

## Decision 2: Use a 3 second polling cadence with a manual refresh-after-write hook

- **Decision**: Poll both `getBeltSnapshot(beltId)` and `getSeatOverview(beltId)` every 3 seconds after the first successful belt selection, and expose a feature-local `refreshAfterWrite()` trigger for future write success flows.
- **Rationale**: Three seconds sits safely inside the spec's 2 to 5 second requirement, reduces backend load compared with a 2 second loop, and still keeps freshness comfortably inside the 5 second success criterion.
- **Alternatives considered**:
  - 2 second polling: rejected because it adds unnecessary request volume for the first read-only slice.
  - 5 second polling: rejected because it leaves too little margin for the success criterion and makes degraded freshness feel slower to recover.
  - Separate cadences for belt and seats: rejected because the added timing complexity does not create product value in the first slice.

## Decision 3: Derive motion from timing metadata, never from reordered slots

- **Decision**: Keep snapshot slots in their authoritative `positionIndex` order and compute visual rotation as a derived fractional slot offset using the latest snapshot timing fields.
- **Rationale**: The backend contract and constitution both state that physical slot identity is stable and frontend motion must be derived from timing metadata. Pure derivation also makes the behavior unit-testable.
- **Alternatives considered**:
  - Reorder the slot array on each animation tick: rejected because it breaks stable slot identity and future reachability logic.
  - Animate the entire belt with a blind CSS rotation: rejected because seats must stay fixed and the app must trust new backend offsets even when they cause jumps.

### Derived motion formula

Use the latest snapshot fields as follows:

`derivedOffsetSlots = beltBaseRotationOffset + ((now - beltOffsetStartedAt) / beltTickIntervalMs) * beltSpeedSlotsPerTick`

The rendered angle for a slot becomes `(slot.positionIndex - derivedOffsetSlots) / beltSlotCount * 360` degrees, normalized with modulo arithmetic. If speed is `0`, if timing fields are incomplete, or if reduced motion is active, use only the latest authoritative offset without continuous interpolation.

## Decision 4: Honor reduced motion in JavaScript state, not only in CSS

- **Decision**: Detect `window.matchMedia('(prefers-reduced-motion: reduce)')` and disable requestAnimationFrame-driven interpolation while keeping the same snapshot, seat, and freshness data visible.
- **Rationale**: The main motion in this feature is derived in JavaScript from timing metadata. CSS-only handling would still leave the model updating continuously, which does not satisfy the spec's reduced-motion requirement.
- **Alternatives considered**:
  - Use only CSS `@media (prefers-reduced-motion)` rules: rejected because the belt position itself is not a purely CSS animation.
  - Remove all transitions and decorative feedback: rejected because the spec still requires a friendly Kawaii tone in reduced-motion mode.

## Decision 5: Replace the root app shell directly and keep new code feature-local

- **Decision**: Rework `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css` into the overview page, keep providers in `src/app/app.config.ts`, and add a small `src/app/belt-visualization/` area for state, mapping, and stage rendering helpers.
- **Rationale**: The repo instructions explicitly allow replacing the Angular placeholder app surface, and the constitution discourages speculative routing or shared-library extraction in this small app.
- **Alternatives considered**:
  - Build the whole feature inside `App` with no supporting files: rejected because motion math and freshness handling would become hard to test.
  - Add a route-driven feature module or broader facade layer: rejected because it adds complexity without current product need.

## Decision 6: Preserve last-known-good belt and seat data independently on failures

- **Decision**: Treat belt snapshot data and seat overview data as separate authoritative streams, preserve each stream's last valid response independently, and surface a single degraded freshness state when either refresh fails.
- **Rationale**: The spec explicitly calls out that the two responses can arrive at different times. Independent preservation avoids implying that seats move with the belt or that all data is stale when only one stream missed a refresh.
- **Alternatives considered**:
  - Collapse both responses into an all-or-nothing fetch state: rejected because one failing endpoint would unnecessarily blank valid data from the other.
  - Freeze the entire page on any failure without degraded messaging: rejected because it hides trust-critical state.

## Decision 7: Prefer relative `/api/...` browser calls with environment-specific proxy routing

- **Decision**: Keep frontend HTTP calls relative to `/api/...` and route them through an Angular dev proxy or equivalent edge proxy for local development, instead of having browser code depend on a hardcoded backend origin.
- **Rationale**: The backend is exposed differently depending on how it is run: `http://localhost:8088` through Docker Compose and typically `http://localhost:8080` through local Spring execution. A proxy keeps browser code stable, avoids CORS dependence, and prevents invalid browser-only assumptions such as calling `http://backend:8080` from outside Docker networking.
- **Alternatives considered**:
  - Hardcode `http://localhost:8088` in frontend browser code: rejected because it couples the app to one local runtime mode and conflicts with direct Spring runs.
  - Hardcode `http://localhost:8080` in frontend browser code: rejected because it fails when the backend is reached through Docker Compose on port `8088`.
  - Call `http://backend:8080` from browser code in containerized setups: rejected because `backend` is a container-network hostname, not a browser-resolvable public origin.

### Development routing notes

- Local Angular + local Spring backend: proxy `/api` to `http://localhost:8080`.
- Local Angular + Docker Compose backend: proxy `/api` to `http://localhost:8088`.
- Container-served frontend: proxy `/api` at the web server or edge layer rather than exposing container-only hostnames to the browser.

## Open Questions Resolved In Planning

- **How should the page pick a belt without new routing?** Select the first available belt.
- **How should future write flows trigger refresh without being in scope now?** Expose a store-level `refreshAfterWrite()` entry point for later reuse.
- **How should reduced motion change behavior?** Keep authoritative rendering and freshness cues, but disable continuous interpolation.
- **How should seat stability be preserved?** Render seat angles from `SeatStateDto.positionIndex` only; never couple them to belt rotation state.
