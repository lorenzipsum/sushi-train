# Implementation Plan: Kaiten-Zushi Belt Redesign

**Branch**: `[002-belt-layout-redesign]` | **Date**: 2026-03-14 | **Spec**: `C:\se\sushi-train\frontend\specs\002-belt-layout-redesign\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\002-belt-layout-redesign\spec.md`

## Summary

Redesign the current single-belt page from a circular overview into a wide, squarer kaiten-zushi counter loop that better uses horizontal space, reduces plate crowding, makes seats and occupied places more recognizable, adds an interior kitchen/chef core, and presents larger plates with menu-item-aware dish visuals. The implementation stays inside the existing Angular 21 standalone app surface, preserves the current backend-authoritative motion and seat behavior from Feature 001, keeps the API/store layer intact, and focuses the new work in the root app shell plus a small extension of the existing `src/app/belt-visualization/` area for path-based stage layout, seat presentation, kitchen-core rendering, and menu-item visual mapping.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/router`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A, in-memory client state only  
**Testing**: `ng test` via Angular builder with Vitest and Angular TestBed  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Keep motion smooth when interpolation is allowed, maintain current freshness expectations within the existing 5 second window, and keep the redesigned belt readable at common desktop widths and narrow mobile widths  
**Constraints**: Read-only redesign only, slot and seat identity remain backend-authoritative, motion still derives from timing metadata instead of slot reordering, reduced-motion mode must preserve comprehension, browser calls stay relative to `/api/...`, the app should remain small with no new routes, and menu-item visuals must degrade gracefully for unknown items  
**Scale/Scope**: One redesigned sushi belt page in the existing app, one backend-provided belt at a time, variable slot and seat counts, one visual family system covering the currently seeded menu items plus future fallback behavior

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The redesign remains inside the existing standalone app rooted at `src/app/app.ts` with no NgModules or route expansion.
- **Strict, Simple, Maintainable Code**: PASS. The plan preserves the current API and store ownership boundaries, keeps backend-authoritative movement separate from frontend layout derivation, and adds only focused presentation helpers where needed.
- **Testable Changes Are Required**: PASS. The plan includes tests for path/layout derivation, menu-item visual mapping, and responsive/state-preserving page behavior.
- **Accessible, Replaceable UI**: PASS. The current app shell is intentionally replaceable, and the redesign keeps semantic structure, reduced motion, and non-color-only state communication as hard requirements.
- **Grow the Surface Area Conservatively**: PASS. The work stays on the current page surface, reuses the existing `belt-visualization` feature area, and avoids speculative routing or new service layers.

### Post-Design Gate

- **Standalone Angular First**: PASS. Design keeps the standalone root shell and focused standalone presentation components.
- **Strict, Simple, Maintainable Code**: PASS. Design favors pure layout and visual-mapping helpers over new architectural layers.
- **Testable Changes Are Required**: PASS. Design isolates geometry, visual registry mapping, and state-preservation behavior for targeted Vitest coverage.
- **Accessible, Replaceable UI**: PASS. Design preserves semantic page structure, state messaging, and reduced-motion handling while improving visual hierarchy.
- **Grow the Surface Area Conservatively**: PASS. No new routes, no backend contract change requirement, and no shared library extraction are necessary.

## Project Structure

### Documentation (this feature)

```text
frontend/specs/002-belt-layout-redesign/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── belt-layout-redesign-ui-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
frontend/
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
│   │   ├── app.spec.ts
│   │   ├── app.ts
│   │   └── belt-visualization/
│   │       ├── belt-stage.component.ts
│   │       ├── belt-stage.html
│   │       ├── belt-stage.css
│   │       ├── belt-visualization.store.ts
│   │       ├── belt-visualization.store.spec.ts
│   │       ├── belt-view-model.ts
│   │       ├── motion.ts
│   │       ├── belt-layout.ts
│   │       ├── menu-item-visuals.ts
│   │       ├── motion.spec.ts
│   │       ├── belt-view-model.spec.ts
│   │       ├── belt-layout.spec.ts
│   │       └── menu-item-visuals.spec.ts
│   ├── main.ts
│   └── styles.css
└── package.json
```

**Structure Decision**: Keep the redesign entirely inside the existing root page and `src/app/belt-visualization/` feature area. The implementation updates `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css` for the new belt-first hierarchy, extends `belt-layout.ts` for squarer loop geometry plus belt-facing seat placement, adds `menu-item-visuals.ts` for category-first dish rendering and fallback behavior, and preserves the current store and API services as the authoritative data flow.

## Phase 0 Research Summary

- Keep the current single-belt, first-returned-belt selection model and 3 second polling behavior from Feature 001 unchanged.
- Use a squarer rounded-rectangle loop with longer straights and softened corners so the belt reads more like a real conveyor counter, creates more usable lane length, and reduces plate crowding while remaining responsive, testable, and compatible with stable slot ordering.
- Replace the circular polar-position assumption in the stage view model with a path-based derived layout that still preserves stable `positionIndex` ordering from the backend.
- Keep seat placement independent from belt movement, but redesign seat presentation to resemble belt-facing stools or counter places with stronger occupied-state cues and more even spacing.
- Use the center of the belt as a kitchen-prep zone with chef-presence cues so the stage reads like a working sushi counter instead of an empty decorative void.
- Use a category-first menu-item visual registry with item-specific overrides for especially recognizable seeded dishes, plus a generic fallback for future items.
- Keep the redesign within the current Angular standalone shell and feature-local files instead of adding routes or broad shared abstractions.
- Preserve reduced-motion, paused, loading, and degraded-freshness clarity as trust-critical behavior during the visual redesign.

## Phase 1 Design Summary

- Introduce a derived layout model that can map stable slot indices and seat positions onto a wide squarer conveyor path rather than a circular ring.
- Separate visual food identity from backend data by defining a small menu-item visual family model that maps names into recognizable dish treatments.
- Move the page hierarchy toward a belt-first hero layout, with support panels stacked below the main stage.
- Treat the belt center as a kitchen-prep zone with chef visuals and counter detailing.
- Keep the current store and API workflow stable so the redesign is primarily a presentation/modeling change rather than a data-flow rewrite.
- Preserve testable pure helpers for motion, layout geometry, and visual mapping so the redesigned UI remains reviewable and robust.

## Complexity Tracking

No constitution violations or justified complexity exceptions were required for this plan.
