# Implementation Plan: Add Plates To Belt

**Branch**: `[007-add-plates-belt]` | **Date**: 2026-03-22 | **Spec**: `C:\se\sushi-train\frontend\specs\007-add-plates-belt\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\007-add-plates-belt\spec.md`

**Note**: This plan covers Phase 0 research and Phase 1 design artifacts for the demo-mode operator plate-placement flow.

## Summary

Add a compact operator-only plate-placement workflow to the existing single-belt Angular experience without changing the guest dining model. The implementation should anchor the entry point near the kitchen area when space allows, fall back to a compact secondary surface when it does not, load the full menu list for limited-space search, submit plate-creation requests against the already selected primary belt, show kitchen-themed success or clear corrective failure notices, and trigger the existing immediate-refresh plus polling reconciliation path while preserving selected-seat guest context.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in the frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory Angular signal state for belt snapshot, seats, selected seat detail, and new operator placement draft or feedback state  
**Testing**: `npm test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA
**Project Type**: Single-project web application frontend  
**Performance Goals**: Keep the existing belt scene responsive and visually stable during operator interactions, keep menu search and operator feedback effectively immediate for a typical menu list fetched within current API limits, and preserve the current immediate refresh plus background polling cadence after placement  
**Constraints**: Preserve the current guest-facing belt layout and selected-seat detail experience, keep the kitchen and chef area visually central, avoid introducing an admin-dashboard shell, support only the primary belt currently shown on screen, treat spacing as backend-managed best effort, and keep changes within the existing app surface and standalone Angular structure  
**Scale/Scope**: One Angular application, one visible primary belt, one compact operator surface, one menu-item dataset loaded from the existing paginated menu endpoint, one plate-placement workflow, and one new write path layered onto the current belt visualization store

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature remains inside the existing standalone Angular app, extending the current app shell, belt stage, and API wrappers without adding routes or NgModules.
- **Strict, Simple, Maintainable Code**: PASS. The work fits the existing service-plus-store-plus-view-model pattern and adds only narrowly scoped operator state rather than a separate admin subsystem.
- **Testable Changes Are Required**: PASS. The feature maps cleanly to deterministic API-wrapper, store, shell, and component tests covering menu search, defaults, validation, feedback, and refresh-after-write behavior.
- **Accessible, Replaceable UI**: PASS. The operator flow can be delivered as semantic controls in or near the existing belt scene, with a compact fallback surface when inline density becomes unsafe.
- **Grow the Surface Area Conservatively**: PASS. The design stays inside the current app surface and avoids new routes, libraries, or speculative shared layers.

### Post-Design Gate

- **Standalone Angular First**: PASS. The design continues to use standalone components, signals, and the existing provider-based app configuration.
- **Strict, Simple, Maintainable Code**: PASS. The design adds one focused menu-items API wrapper, one focused placement flow in the existing store, and a compact operator UI without broader architectural churn.
- **Testable Changes Are Required**: PASS. Phase 1 isolates testable state transitions for menu loading, filtered selection, placement submission, success or failure notices, and belt refresh behavior.
- **Accessible, Replaceable UI**: PASS. The operator surface remains compact, explicit, keyboard-accessible, and secondary to the guest-facing stage.
- **Grow the Surface Area Conservatively**: PASS. No new route, no separate admin page, and no speculative state framework are required.

## Project Structure

### Documentation (this feature)

```text
specs/007-add-plates-belt/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── add-plates-to-belt-ui-contract.md
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
    │   ├── menu-items.api.ts
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
        ├── menu-item-visuals.ts
        ├── menu-item-visuals.spec.ts
        ├── operator-plate-placement.component.ts
        ├── operator-plate-placement.component.html
        ├── operator-plate-placement.component.css
        ├── operator-plate-placement.component.spec.ts
        ├── selected-seat-detail.component.ts
        ├── selected-seat-detail.component.html
        └── selected-seat-detail.component.css
```

**Structure Decision**: Keep `007-add-plates-belt` inside the existing Angular app surface. Extend the current API layer to cover menu retrieval and belt plate creation, extend the existing belt visualization store to own operator menu loading, search, draft, submission, and feedback state, and add the compact operator surface inside the current belt-stage or app-shell composition without introducing routes or a second application surface.

## Phase 0 Research Summary

- Anchor the operator entry point to the kitchen or chef zone when space permits, but preserve a compact fallback surface if inline controls would crowd the belt scene.
- Load the full menu list through the existing paginated menu endpoint and perform narrowing within the operator flow so limited-space search works without a server-side filter API.
- Keep operator placement state separate from selected-seat guest state so a successful or failed placement never repurposes the selected-seat detail area.
- Reuse the current refresh-after-write pattern in the belt visualization store so successful placement triggers an immediate authoritative belt refresh and then falls back to normal polling.
- Normalize known backend placement failures into operator-facing corrective messages, treat placement as all-or-nothing in the frontend, and explicitly avoid any separate frontend spacing-failure state because spacing remains backend-managed best effort.

## Phase 1 Design Summary

- Add a focused menu-items API wrapper for paginated menu retrieval and extend the existing belts API wrapper with plate-placement support using generated request and response types.
- Introduce operator placement state in the existing belt visualization store for menu-item loading, search text, filtered results, draft defaults, manual overrides, pending submission, and operator notices.
- Extend the belt-stage or adjacent shell composition with a compact operator surface near the kitchen presentation and define a compact secondary-surface fallback that preserves the current guest layout if inline density becomes unsafe.
- Preserve the current selected-seat detail component as a guest-only surface while routing operator feedback and retry affordances through the new operator placement surface.
- Validate the feature with API-wrapper, store, shell, and stage tests focused on menu search, default derivation, valid overrides, corrective failures, guest-context preservation, and immediate refresh-after-success behavior.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
