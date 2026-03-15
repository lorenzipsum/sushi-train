# Implementation Plan: Checkout Seat

**Branch**: `[004-checkout-seat]` | **Date**: 2026-03-15 | **Spec**: `C:\se\sushi-train\frontend\specs\004-checkout-seat\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\004-checkout-seat\spec.md`

## Summary

Add the checkout write flow so an anonymous guest can start from an occupied seat in the existing single-belt UI, complete `POST /api/v1/seats/{seatId}/checkout`, and immediately see the backend-authoritative checked-out order summary while the seat becomes visibly free again. The implementation stays inside the current Angular standalone app, reuses the existing seats API and store feedback patterns, preserves the successful `SeatOrderDto` in in-memory session state for confirmation and later in-session use, and reconciles visible seat truth with the normal refresh-after-write flow without inventing a second seat-session lifecycle.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory signal state for current-session checkout confirmation  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Keep checkout feedback within the current UI responsiveness expectations, preserve belt rendering readability, and reconcile seat availability within the existing immediate refresh-after-write plus 3 second polling cadence  
**Constraints**: Anonymous guests only, no login or guest profile, no multi-belt management, no new seat-session abstraction, backend totals/status/timestamps remain authoritative, final checkout summary persists only for the current in-app session, reduced-motion and degraded-state clarity must remain intact  
**Scale/Scope**: One checkout flow on the existing belt page, one active `OPEN` order per occupied seat, one checked-out summary retained in-session per completed checkout, current single-belt application surface only

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature stays inside the existing standalone app and extends the current store, stage, and app shell without routes or NgModules.
- **Strict, Simple, Maintainable Code**: PASS. The plan reuses the backend's order lifecycle and the already-generated checkout DTOs instead of adding a separate visit or receipt model.
- **Testable Changes Are Required**: PASS. Checkout success, stale conflict, not-found handling, and in-session summary retention all map cleanly to store and UI tests.
- **Accessible, Replaceable UI**: PASS. The design can reuse semantic buttons, `aria-live` feedback, and reduced-motion messaging on the existing page shell.
- **Grow the Surface Area Conservatively**: PASS. The work remains within the current app surface, existing API service, and current belt visualization files.

### Post-Design Gate

- **Standalone Angular First**: PASS. Design remains focused on `src/app/` standalone components and signal-based store state.
- **Strict, Simple, Maintainable Code**: PASS. Design uses `POST /checkout` as the authoritative final summary source and avoids redundant seat-detail reconciliation that would discard the checked-out order.
- **Testable Changes Are Required**: PASS. Design isolates checkout write state, success summary persistence, and failure reconciliation into deterministic store behavior and view-model changes.
- **Accessible, Replaceable UI**: PASS. Design keeps checkout initiation on the seat UI, uses explicit occupied-seat action labels, and reuses persistent `aria-live` feedback for success and failure states.
- **Grow the Surface Area Conservatively**: PASS. No new route, no speculative persistence layer, and no broader account/session system are introduced.

## Project Structure

### Documentation (this feature)

```text
frontend/specs/004-checkout-seat/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── checkout-seat-api-contract.md
├── checklists/
│   └── requirements.md
└── tasks.md
```

### Source Code (repository root)

```text
frontend/
├── src/
│   ├── app/
│   │   ├── api/
│   │   │   ├── belts.api.ts
│   │   │   ├── seats.api.ts
│   │   │   ├── types.ts
│   │   │   ├── generated/
│   │   │   │   └── openapi.types.ts
│   │   │   └── http/
│   │   │       ├── api-config.ts
│   │   │       └── problem-detail.ts
│   │   ├── app.html
│   │   ├── app.css
│   │   ├── app.spec.ts
│   │   └── belt-visualization/
│   │       ├── belt-stage.component.ts
│   │       ├── belt-stage.html
│   │       ├── belt-stage.css
│   │       ├── belt-view-model.ts
│   │       ├── belt-view-model.spec.ts
│   │       ├── belt-visualization.store.ts
│   │       ├── belt-visualization.store.spec.ts
│   │       ├── belt-layout.ts
│   │       └── belt-layout.spec.ts
│   └── main.ts
├── contracts/
│   └── openapi.json
└── package.json
```

**Structure Decision**: Keep `004-checkout-seat` inside the current frontend app surface. The feature will extend the existing `SeatsApi.checkout()` integration, the belt visualization store, the seat view model/stage interaction layer, and the app-shell feedback area. No new route, module, or shared library is required.

## Phase 0 Research Summary

- Use the successful checkout response itself as the authoritative final summary because later seat-detail reads intentionally drop checked-out orders.
- Preserve the final checked-out summary only in current-session frontend state; full browser-reload persistence remains out of scope for this feature.
- Reconcile visible seat truth after success or failure with the existing refresh-after-write path rather than relying on optimistic local occupancy changes.
- Treat `409 SEAT_NOT_OCCUPIED` as a stale, repeated, or concurrent-loss business outcome and communicate it explicitly.
- Reuse the existing seat-driven interaction surface and feedback-card pattern rather than introducing a new route or separate checkout screen.

## Phase 1 Design Summary

- Extend the seat stage/view model so occupied seats can start the checkout flow from the existing UI with clear labels and pending states.
- Add focused checkout signals and summary retention in the store while keeping backend-authoritative seat availability in the existing seat overview state.
- Use the `SeatOrderDto` success response to populate a receipt-style confirmation card with final status, timestamps, lines, and total from the backend.
- Refresh seat overview and snapshot state after every checkout attempt so the seat visibly returns to available status or reflects missing-seat outcomes.
- Add store and view-model tests for checkout success, `SEAT_NOT_OCCUPIED`, not-found handling, and in-session summary retention.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
