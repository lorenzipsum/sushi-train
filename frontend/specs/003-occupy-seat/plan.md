# Implementation Plan: Occupy Seat

**Branch**: `[003-occupy-seat]` | **Date**: 2026-03-15 | **Spec**: `C:\se\sushi-train\frontend\specs\003-occupy-seat\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\003-occupy-seat\spec.md`

## Summary

Add the first seat write flow so an anonymous guest can click a free seat in the existing belt stage, create an occupied seat through the backend's existing `OPEN` order model, and receive clear success, conflict, and not-found feedback without introducing a parallel seat-session concept. The implementation stays within the current Angular standalone app and existing backend API surface, extending the frontend seat view model, stage interactions, store write flow, and API types only as needed to support `POST /api/v1/seats/{seatId}/occupy` plus a follow-up `GET /api/v1/seats/{seatId}` reconciliation that retains durable `orderId` context.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in frontend; backend-authoritative persistence remains behind existing REST APIs  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Keep occupy-seat feedback within the existing UI responsiveness expectations and preserve current snapshot readability within the 3 second polling cadence  
**Constraints**: Anonymous guests only, no login or guest profile, no new session concept beyond the existing open order, reduced-motion and degraded-state clarity must remain intact, write flow must coexist with current polling and refresh-after-write behavior, current single-belt page scope only  
**Scale/Scope**: One new seat-occupy write flow on the existing belt page, one active occupancy per seat, one durable `OPEN` order per occupied seat

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature stays inside the existing standalone app and extends current components/store behavior without NgModules or route changes.
- **Strict, Simple, Maintainable Code**: PASS. The design reuses the backend's existing open-order occupancy model and avoids introducing a duplicate seat-session abstraction.
- **Testable Changes Are Required**: PASS. The feature naturally supports store, view-model, and UI tests around occupy success, conflict, and state refresh behavior.
- **Accessible, Replaceable UI**: PASS. Seat interaction can be added to the existing stage while preserving semantic labels, non-color-only occupied cues, and trust-critical state messaging.
- **Grow the Surface Area Conservatively**: PASS. The work stays in the current app surface, current store, and existing API area.

### Post-Design Gate

- **Standalone Angular First**: PASS. Design remains within `src/app/` and focused standalone presentation/store files.
- **Strict, Simple, Maintainable Code**: PASS. Design uses the backend's `OPEN` order as the durable occupancy record instead of adding a second lifecycle concept.
- **Testable Changes Are Required**: PASS. Design isolates new behavior into API contract, store actions, stage interaction states, and view-model updates that can be validated independently.
- **Accessible, Replaceable UI**: PASS. Design requires clear clickability for free seats, clear non-clickability for occupied seats, and explicit failure messaging for conflicts.
- **Grow the Surface Area Conservatively**: PASS. No new routes, no speculative shared library, and no new auth/profile system are introduced.

## Project Structure

### Documentation (this feature)

```text
frontend/specs/003-occupy-seat/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── occupy-seat-api-contract.md
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
│   │   │       └── api-config.ts
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
└── package.json
```

**Structure Decision**: Keep `003-occupy-seat` entirely inside the existing frontend app surface. The feature will likely extend the current HTTP API wrapper, generated or handwritten seat DTO usage, the `belt-visualization` store and view model, and the stage template/styles for clickability and feedback, while preserving the current root app shell and single-page layout.

## Phase 0 Research Summary

- Use the backend's existing `OPEN` order as the seat occupancy source of truth and durable session identifier.
- Treat `orderId` and `createdAt` from seat-detail reconciliation after occupy as the minimum durable occupancy context needed to enable later checkout-seat and pick-plates features.
- Keep the occupy interaction seat-based in the UI and REST path-based in the backend, rather than introducing order-based URLs in this feature.
- Preserve the existing polling and refresh-after-write model for now, because realtime delivery is a later roadmap item.
- Require a clear, seat-specific `409 SEAT_ALREADY_OCCUPIED` response for both normal conflict checks and unique-constraint-backed conflicts so the UI can communicate failure consistently.

## Phase 1 Design Summary

- Make free seats actionable in the belt stage and keep occupied seats visibly distinct and non-primary for interaction.
- Add a focused occupy-seat write path to the frontend store, with optimistic interaction state only for request progress and backend-authoritative reconciliation after completion.
- Extend the seat-facing frontend contract to capture success, conflict, and not-found outcomes, using immediate seat-detail reconciliation when the write response itself omits order context.
- Preserve existing read-side rendering of occupied seats while preparing the frontend model to carry active-order identifiers and occupancy start time when available.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
