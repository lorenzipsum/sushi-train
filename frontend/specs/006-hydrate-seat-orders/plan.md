# Implementation Plan: Hydrate Seat Orders After Refresh

**Branch**: `[006-hydrate-seat-orders]` | **Date**: 2026-03-22 | **Spec**: `C:\se\sushi-train\frontend\specs\006-hydrate-seat-orders\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\006-hydrate-seat-orders\spec.md`

## Summary

Restore backend-authoritative dining context after a full page reload so occupied seats do not fall into contradictory UI states. The implementation will eagerly hydrate all occupied seats after reload, preserve the previously selected seat when possible, keep visible reach and pickability cues behaviorally blocked during syncing, retry temporary hydration failures in the background, and keep both reselected open-order state and checkout summaries visible in the selected-seat detail area.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory signal state for seat overview, restored active orders, selection continuity, and checkout summaries  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Remove contradictory post-reload UI states, restore occupied-seat dining context within the existing polling and refresh cadence, preserve belt readability, and keep interaction feedback effectively immediate  
**Constraints**: Preserve the current belt-stage layout and selected-seat detail structure, keep any UI design or layout changes limited to additional status and summary information inside the existing selected-seat detail area, hydrate all occupied seats eagerly after reload, preserve the previously selected seat when it still exists, keep reach and pickability cues visible-but-blocked during syncing, retry temporary failures in the background, and maintain backend-authoritative order and checkout summaries  
**Scale/Scope**: One Angular app, one belt page, one selected-seat detail surface, one belt snapshot plus seat overview refresh loop, and all occupied seats on the active belt participating in reload-time hydration

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature stays inside the existing standalone Angular app and extends the current store, app shell, stage, and selected-seat detail flow without routes or NgModules.
- **Strict, Simple, Maintainable Code**: PASS. The work centers on clarifying existing signal state and restoration lifecycle rather than adding new architectural layers.
- **Testable Changes Are Required**: PASS. The behavior maps to deterministic store, view-model, and app-shell tests for reload hydration, retry, seat reselection, and checkout summary continuity.
- **Accessible, Replaceable UI**: PASS. The spec preserves the current UI surface while requiring consistent, honest status messaging during syncing and after checkout.
- **Grow the Surface Area Conservatively**: PASS. The implementation remains within the existing belt visualization surface and selected-seat detail component.

### Post-Design Gate

- **Standalone Angular First**: PASS. The design remains limited to standalone Angular components, signals, and the current application surface.
- **Strict, Simple, Maintainable Code**: PASS. The design uses explicit restoration-state modeling and backend-authoritative summaries instead of introducing speculative caches or new feature layers.
- **Testable Changes Are Required**: PASS. The design isolates reload hydration, retry behavior, selected-seat continuity, reselected open-order continuity, and checkout-summary persistence into testable transitions.
- **Accessible, Replaceable UI**: PASS. The design preserves existing layout while requiring status, feedback, and blocked-action messaging to remain mutually consistent and understandable.
- **Grow the Surface Area Conservatively**: PASS. No new routes, shared libraries, or large structural changes are required.

## Project Structure

### Documentation (this feature)

```text
specs/006-hydrate-seat-orders/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── hydrate-seat-orders-ui-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main.ts
├── styles.css
└── app/
    ├── app.ts
    ├── app.html
    ├── app.css
    ├── app.spec.ts
    ├── app.config.ts
    ├── app.routes.ts
    ├── api/
    │   ├── belts.api.ts
    │   ├── seats.api.ts
    │   ├── types.ts
    │   ├── generated/
    │   │   └── openapi.types.ts
    │   └── http/
    │       ├── api-config.ts
    │       └── problem-detail.ts
    └── belt-visualization/
        ├── belt-stage.component.ts
        ├── belt-stage.html
        ├── belt-stage.css
        ├── belt-layout.ts
        ├── belt-layout.spec.ts
        ├── belt-view-model.ts
        ├── belt-view-model.spec.ts
        ├── belt-visualization.store.ts
        ├── belt-visualization.store.spec.ts
        ├── selected-seat-detail.component.ts
        ├── selected-seat-detail.component.html
        └── selected-seat-detail.component.css
```

**Structure Decision**: Keep `006-hydrate-seat-orders` inside the existing Angular app surface. Extend the current store, view model, selected-seat detail surface, and app-shell behavior to model reload-time hydration, unresolved retry states, seat reselection continuity, and checkout-summary continuity without introducing new routes or architectural layers.

## Phase 0 Research Summary

- Hydrate all occupied seats eagerly after reload so no occupied seat is presented as fully ready based on seat overview alone.
- Preserve the previously selected seat when it still exists so reload returns the guest to the same dining context rather than shifting focus unexpectedly.
- Keep visible reach and pickability cues during syncing, but block actual picks with syncing-specific feedback to avoid contradicting the existing visual language.
- Retry temporary dining-context hydration failures automatically in the background while keeping affected seats visibly unresolved.
- Preserve selected-seat continuity across reselection so active open orders keep their prior lines and remain extensible when the guest comes back to that seat.
- Keep the checked-out seat selected long enough to show the final backend-authoritative checkout summary in the selected-seat area.

## Phase 1 Design Summary

- Introduce explicit restoration lifecycle states that separate raw occupied-seat overview from confirmed active-order readiness, unresolved retry, and reconciled no-order outcomes.
- Hydrate active dining context for all occupied seats after reload, but continue rendering the selected-seat area as the primary guest-visible status and action surface.
- Preserve the currently selected seat across reload when it still exists, and use the selected-seat detail area to show syncing, restored running order, unresolved retry, or final checkout summary states without changing the page layout beyond the additional selected-seat information required for this feature.
- Keep reachable-area and pickability cues visible during syncing while ensuring picks remain behaviorally blocked and explained as loading rather than as missing dining start.
- Preserve backend-authoritative open-order continuity when seats are reselected later in the same session, and preserve final backend-authoritative checkout summaries in the selected-seat area immediately after checkout.
- Validate the behavior with store, view-model, and shell tests covering reload hydration, retry, reselection continuity, blocked syncing picks, and checkout summary persistence.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
