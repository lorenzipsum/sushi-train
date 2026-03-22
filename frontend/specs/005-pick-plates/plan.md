# Implementation Plan: Pick Plates

**Branch**: `[005-pick-plates]` | **Date**: 2026-03-16 | **Spec**: `C:\se\sushi-train\frontend\specs\005-pick-plates\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\005-pick-plates\spec.md`

## Summary

Add selected-seat ordering behavior without redesigning the existing sushi belt presentation. Seat clicks remain selection-only, explicit occupy and checkout actions live in a compact selected-seat detail area below the current belt UI, and a seat-anchored reachable area communicates which passing plates can be picked for the selected occupied seat. The implementation should preserve the current stage layout, make pickable plates easy to tap or click, use backend `pickPlate` responses as the immediate running-order source of truth, and provide brief visible reject feedback when an unpickable plate is attempted.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A in frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory signal state for selected seat, active open orders, and current-session checkout summaries  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Preserve current belt readability, keep seat selection and reachable-area updates effectively immediate, maintain easy plate picking during continuous belt motion, and remain compatible with the existing polling plus refresh-after-write cadence  
**Constraints**: Preserve the current belt-stage layout with minimal visual disruption, add new UI below the existing belt UI by default, keep the reachable area anchored to the seat rather than moving slots, keep reject feedback brief without breaking belt motion, maintain anonymous seat-ID backend semantics, and preserve reduced-motion and degraded-state clarity  
**Scale/Scope**: One existing Angular app, one belt page, one selected-seat detail area below the current stage, one selected occupied seat acting as the write target, and one seat-anchored reachable area governing plate picking

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature remains inside the existing standalone Angular app and extends the current app shell, stage, store, and API wrappers without routes or NgModules.
- **Strict, Simple, Maintainable Code**: PASS. The revised scope removes speculative layout redesign and centers the work on selection state, a compact detail surface, and seat-anchored reach visualization.
- **Testable Changes Are Required**: PASS. The feature maps to deterministic store, view-model, app-shell, and interaction feedback tests, including easy-pick hit area and reject feedback.
- **Accessible, Replaceable UI**: PASS. The updated spec emphasizes preserving the current stage UI while adding semantic controls and clear, brief feedback below the stage.
- **Grow the Surface Area Conservatively**: PASS. The work stays inside the existing app surface and avoids adding routes, libraries, or broader layout layers.

### Post-Design Gate

- **Standalone Angular First**: PASS. The design remains limited to the current standalone application structure and signal-based state patterns.
- **Strict, Simple, Maintainable Code**: PASS. The design prefers derived seat reach metadata, minimal shell additions, and backend-authoritative order updates over new architectural layers.
- **Testable Changes Are Required**: PASS. The design isolates reachable-area derivation, pick hit-area behavior, and unpickable reject feedback into testable UI and state transitions.
- **Accessible, Replaceable UI**: PASS. The design requires a clear seat-anchored reachable area, easy interaction targets, and reject feedback that remains understandable without disrupting motion semantics.
- **Grow the Surface Area Conservatively**: PASS. No new route, auth model, state framework, or speculative component hierarchy is introduced.

## Project Structure

### Documentation (this feature)

```text
specs/005-pick-plates/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── pick-plates-ui-contract.md
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
        └── belt-visualization.store.spec.ts
```

**Structure Decision**: Keep `005-pick-plates` inside the existing Angular app surface. Extend the current store, stage, app shell, and API typing layers to add selected-seat behavior, a compact selected-seat detail area below the stage, a seat-anchored reachable area, easy pick targets, and reject feedback without introducing routes or new architectural layers.

## Phase 0 Research Summary

- Preserve the current belt-stage presentation and treat any new selected-seat UI as a compact secondary surface below the belt by default.
- Keep seat clicks selection-only so browsing never performs writes and explicit actions remain outside the stage.
- Represent the pickable region as a seat-anchored reachable area that is visually read as belonging to the selected seat rather than to moving slot markers.
- Increase the practical interaction surface for pickable plates so picking remains easy without changing the authoritative belt motion model.
- Use brief whole-plate reject feedback for unpickable picks so the guest gets immediate confirmation while the plate continues along its path.
- Continue using backend `pickPlate` responses as the immediate running-order source of truth and the shared refresh-after-write path for reconciliation.

## Phase 1 Design Summary

- Extend the stage view model with seat selection, seat-anchored reachable-area metadata, and plate interaction-state metadata without requiring stage-layout compression.
- Add a compact selected-seat detail area below the current belt UI that shows selected-seat status, explicit start or checkout actions, running-order details, and relevant write feedback.
- Keep passing plates on the belt while expanding their interaction affordance so pickable plates are easy to activate without demanding precise targeting.
- Surface out-of-range or otherwise unpickable attempts with clear explanation plus brief visible reject feedback that does not imply the plate stopped or left the belt.
- Validate the updated UX with view-model, store, and shell tests focused on reach-area ownership, easy pick activation, reject feedback, and current-layout preservation.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
