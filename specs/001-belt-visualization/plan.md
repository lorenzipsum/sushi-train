# Implementation Plan: Sushi Belt Visualization

**Branch**: `[001-belt-visualization]` | **Date**: 2026-03-14 | **Spec**: `C:\se\sushi-train\specs\001-belt-visualization\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\specs\001-belt-visualization\spec.md`

## Summary

Replace the Angular CLI placeholder app with a single read-only sushi belt overview that fetches the one available belt, renders stable slots and seats from authoritative backend data, and derives on-screen belt motion from snapshot timing metadata between refreshes. The implementation stays inside the existing Angular 21 standalone app surface, keeps `app.config.ts` as the provider entry point, uses the handwritten `src/app/api` services plus generated OpenAPI types, prefers relative `/api/...` browser calls routed through a proxy layer, and adds only minimal feature-local view-model and motion helpers under `src/app`.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/router`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A, in-memory client state only  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Maintain visually smooth belt interpolation when motion is allowed, keep authoritative freshness within the spec's 5 second window, and render variable slot/seat counts without layout breakage  
**Constraints**: Read-only MVP, polling interval must stay between 2 and 5 seconds, slot order and seat positions remain stable in state, motion is derived from timing metadata instead of slot reordering, reduced-motion mode must disable continuous interpolation, generated OpenAPI output must not be edited, browser networking should prefer relative `/api/...` calls through a proxy layer, architecture must stay small with no new routes or speculative layers  
**Scale/Scope**: One sushi belt overview page in the existing app, one backend-provided belt at a time with no belt-selection UX, variable numbers of slots and seats from backend data, no mutation flows in this slice

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The plan keeps the feature inside the existing standalone application rooted at `src/app/app.ts` and does not add NgModules.
- **Strict, Simple, Maintainable Code**: PASS. Backend-authoritative DTOs remain in `src/app/api/generated/`, handwritten API access stays in `src/app/api`, and frontend motion is derived from timing fields rather than mutating slot order.
- **Testable Changes Are Required**: PASS. The plan includes Vitest coverage for motion derivation, page state transitions, and the root app rendering path.
- **Accessible, Replaceable UI**: PASS. The current placeholder `app.html` is intended to be replaced directly with semantic, accessible Kawaii UI and reduced-motion support.
- **Grow the Surface Area Conservatively**: PASS. The plan keeps the feature on the current app surface, avoids new routes, and limits new code to a small feature-local set of helpers/components if needed.

### Post-Design Gate

- **Standalone Angular First**: PASS. Design keeps `app.config.ts` as the provider shell and uses standalone components only.
- **Strict, Simple, Maintainable Code**: PASS. Design uses one feature-local state layer plus pure motion/view-model helpers instead of new cross-cutting abstractions.
- **Testable Changes Are Required**: PASS. Design isolates pure calculations for unit tests and keeps UI state observable through component tests.
- **Accessible, Replaceable UI**: PASS. Design includes semantic page structure, contrast-safe status cues, and `prefers-reduced-motion` handling.
- **Grow the Surface Area Conservatively**: PASS. No new route tree, no shared library extraction, and no generated code edits are required.

## Project Structure

### Documentation (this feature)

```text
specs/001-belt-visualization/
├── plan.md
├── research.md
├── data-model.md
├── design-tokens.md
├── quickstart.md
├── contracts/
│   └── belt-visualization-ui-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
frontend/
├── contracts/
│   └── openapi.json
├── src/
│   ├── app/
│   │   ├── api/
│   │   │   ├── belts.api.ts
│   │   │   ├── generated/
│   │   │   │   └── openapi.types.ts
│   │   │   └── http/
│   │   │       └── api-config.ts
│   │   ├── app.config.ts
│   │   ├── app.html
│   │   ├── app.css
│   │   ├── app.routes.ts
│   │   ├── app.spec.ts
│   │   ├── app.ts
│   │   └── belt-visualization/
│   │       ├── belt-stage.component.ts
│   │       ├── belt-stage.html
│   │       ├── belt-stage.css
│   │       ├── belt-visualization.store.ts
│   │       ├── belt-view-model.ts
│   │       ├── motion.ts
│   │       ├── motion.spec.ts
│   │       └── belt-view-model.spec.ts
│   ├── main.ts
│   └── styles.css
└── package.json
```

**Structure Decision**: Replace the placeholder root page by editing `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css` directly, keep provider setup in `src/app/app.config.ts`, continue using `src/app/api/belts.api.ts` and generated DTOs for backend reads, and add one feature-local folder under `src/app/belt-visualization/` for motion math, DTO-to-view-model mapping, and a focused stage component. `src/app/app.routes.ts` remains unchanged unless a future scope explicitly introduces routing.

## Phase 0 Research Summary

- Treat the backend response as a single-belt source for this feature and always track the first returned belt rather than modeling belt selection in the UI state.
- Poll belt snapshot and seat overview every 3 seconds, with a manual `refreshNow()` path reserved for future successful write actions.
- Expose a store-level refresh-after-write hook so future mutation flows can trigger an immediate authoritative reread without changing the read-only page structure.
- Derive visual rotation from `beltBaseRotationOffset`, `beltOffsetStartedAt`, `beltTickIntervalMs`, and `beltSpeedSlotsPerTick`, while preserving stable slot identities and allowing authoritative jumps on refresh.
- Route browser requests through a proxy layer so the frontend can use relative `/api/...` calls across local Spring and Docker-backed development.
- Use `prefers-reduced-motion` to disable interpolation while continuing to show current authoritative positions and freshness state.
- Keep the implementation in the root app shell plus a single feature-local state/model area instead of adding routes or broad service layers.

## Phase 1 Design Summary

- Model backend entities separately from derived view state so animation math stays pure and testable.
- Render seats from the seat overview endpoint as a fixed ring outside the moving belt stage.
- Preserve the last successful snapshot and seat response independently during recoverable failures, and surface a degraded freshness indicator when either refresh stream fails.
- Use the warm Kawaii design system from the spec in semantic HTML and CSS, with belt motion as the only continuous animation.

## Complexity Tracking

No constitution violations or justified complexity exceptions were required for this plan.
