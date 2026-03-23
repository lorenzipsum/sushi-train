# Implementation Plan: Refine Belt Interactions

**Branch**: `[009-refine-belt-interactions]` | **Date**: 2026-03-23 | **Spec**: `C:\se\sushi-train\frontend\specs\009-refine-belt-interactions\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\009-refine-belt-interactions\spec.md`

**Note**: This plan covers Phase 0 research and Phase 1 design artifacts for the seat reach fix, stronger selection cues, ring-only plate visuals, and modal-based belt speed control.

## Summary

Refine the existing sushi-belt interaction model without changing the overall dining flow by enlarging top-row seat reach where it currently blocks valid picks, making the selected seat and its reach zone read more clearly, simplifying moving plates to ring-only visuals, and enabling the existing cogwheel control to open a modal that updates belt speed through the already defined belt-parameter PATCH API.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in the frontend; backend-authoritative belt and seat state over REST with in-memory Angular signal state for snapshot, seats, selected seat context, operator flow, and new speed-control state  
**Testing**: `npm test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA
**Project Type**: Single-project web application frontend  
**Performance Goals**: Preserve the existing smooth belt presentation and near-immediate interaction feedback, keep seat selection and pickability updates visually stable during motion, and apply speed changes quickly through an immediate authoritative refresh rather than waiting for the normal polling interval  
**Constraints**: Preserve the existing standalone Angular structure, avoid new routes or admin shells, keep semantic and accessible dialog behavior, keep the belt stage readable on smaller screens, keep backend-authoritative slot data intact, and respect the belt speed contract range of `0..5` slots per tick without editing tick interval as part of this feature  
**Scale/Scope**: One Angular application, one existing belt-stage flow, one selected-seat context surface, one belt-speed modal, one focused Belts API extension, one view-model reach adjustment, and targeted updates to the existing store, template, styles, and tests

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature stays inside the existing standalone application and extends the current component, store, and API wrapper seams without introducing NgModules or routes.
- **Strict, Simple, Maintainable Code**: PASS. The work fits the current store-plus-view-model pattern, adds one focused belt-parameter API method, and keeps modal state local to the existing app surface rather than introducing a broader settings framework.
- **Testable Changes Are Required**: PASS. The user-visible behavior maps cleanly to view-model, store, shell, and component tests for reach math, selected-state emphasis, ring-only plate rendering, speed modal state, and refresh-after-speed-change behavior.
- **Accessible, Replaceable UI**: PASS. The speed control can follow the existing dialog overlay pattern with semantic modal markup, while reach and selection clarity remain presentation changes inside the current stage.
- **Grow the Surface Area Conservatively**: PASS. The plan edits the existing app shell, stage, store, and API layer directly and does not add a new route, global settings page, or speculative shared layer.

### Post-Design Gate

- **Standalone Angular First**: PASS. Phase 1 keeps the feature in standalone components, signals, computed state, and provider-based application setup.
- **Strict, Simple, Maintainable Code**: PASS. The design keeps actual pickup behavior in the view-model math, not a decorative CSS-only override, and allows visual highlight and slot-lighting cues to remain presentation-specific without changing pickability semantics.
- **Testable Changes Are Required**: PASS. The design isolates deterministic behavior for top-row reach adjustments, clearer selected-state cues, ring-only plate rendering, modal open-close-submit state, and backend-refresh reconciliation after speed updates.
- **Accessible, Replaceable UI**: PASS. The dialog reuses the app’s existing overlay pattern, preserves keyboard and close behavior, and keeps decorative lighting cues secondary to semantic labels.
- **Grow the Surface Area Conservatively**: PASS. No new page shell or feature route is needed; the current stage and shell remain the only user-facing surfaces.

## Project Structure

### Documentation (this feature)

```text
specs/009-refine-belt-interactions/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── refine-belt-interactions-ui-contract.md
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
        ├── operator-plate-placement.component.ts
        ├── selected-seat-detail.component.ts
        └── selected-seat-detail.component.html
```

**Structure Decision**: Keep `009-refine-belt-interactions` inside the existing Angular app surface. Extend the current belt-stage output surface for settings activation, keep speed modal markup in the current app shell alongside the existing guide dialog pattern, extend `BeltsApi` with belt-parameter updates, keep selected-seat reach and pickability logic in `belt-view-model.ts`, and validate behavior through the existing app, view-model, and store test files.

## Phase 0 Research Summary

- Adjust top-row reach in the view-model reach calculation rather than only scaling the bubble in CSS so the actual pickability bug is fixed at the behavior layer.
- Allow lit-slot and highlight presentation to diverge slightly from the strict pick radius where that improves seat-ownership clarity without changing actual pickability.
- Strengthen selected-seat clarity with a light-based treatment applied across seat, reach-area, and within-reach slot cues instead of introducing new structural UI elements or heavy animation.
- Simplify moving plates to a ring-only presentation by removing the current inner center and food garnish layers while preserving tier color as the readable identity cue.
- Implement belt speed control through the already defined `PATCH /api/v1/belts/{id}` contract, editing only `speedSlotsPerTick` and leaving tick-interval control out of scope.
- Reuse the existing modal-overlay pattern from the app shell and the existing `refreshAfterWrite()` reconciliation path so speed changes stay consistent with other backend-authoritative write flows.

## Phase 1 Design Summary

- Add focused API support for belt parameter updates and expose generated request and response types needed for speed changes.
- Introduce speed-control state in the existing belt visualization store for modal visibility, current choice, pending submission, outcome feedback, and unchanged-state handling.
- Extend the belt stage with a settings output from the existing cogwheel button and wire the app shell to render a semantic speed dialog using the current overlay approach.
- Update the belt stage view-model and presentation styles so top-row reach becomes slightly larger where needed, selected-seat ownership is more obvious, and plates render as tier-colored rings only.
- Validate the feature with view-model tests for reach and ring-only plate behavior, store tests for speed change submission and refresh behavior, and app-shell or component tests for modal open-close-submit flow.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
