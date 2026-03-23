# Implementation Plan: Design Polish

**Branch**: `[008-design-polish]` | **Date**: 2026-03-22 | **Spec**: `C:\se\sushi-train\frontend\specs\008-design-polish\spec.md`
**Input**: Feature specification from `C:\se\sushi-train\frontend\specs\008-design-polish\spec.md`

**Note**: This plan covers Phase 0 research and Phase 1 design artifacts for the final presentation-focused UI polish pass.

## Summary

Refresh the existing single-belt Angular experience into a more playful, funny, Japanese-inspired kawaii cafe without changing guest workflows, backend contracts, or core store behavior. The implementation should concentrate on the current app shell, belt stage, selected-seat detail, operator surface, and supporting feedback copy; introduce a cohesive visual system with stronger typography, texture, color, and decorative character cues; preserve or improve belt, slot, plate, and seat readability; and keep humor as a secondary layer that never replaces clear state communication.

## Technical Context

**Language/Version**: TypeScript 5.9, Angular 21 standalone  
**Primary Dependencies**: `@angular/core`, `@angular/common`, `@angular/router`, `@angular/common/http`, `rxjs`, existing generated DTOs from `src/app/api/generated/openapi.types.ts`  
**Storage**: N/A, frontend-only presentation changes over the existing in-memory Angular signal state and backend-authoritative REST data  
**Testing**: `npm test` via Angular builder with Vitest and Angular TestBed, plus manual responsive and reduced-motion verification  
**Target Platform**: Modern desktop and mobile browsers running the Angular SPA  
**Project Type**: Single-project web application frontend  
**Performance Goals**: Preserve the current stage responsiveness, avoid introducing heavier or more distracting animation than the existing experience, keep belt and seat comprehension immediate, and maintain smooth rendering under the current polling and realtime update behavior  
**Constraints**: Preserve all current seat, plate-picking, checkout, operator, and realtime-update behavior; avoid backend or API contract changes; keep belt, slot, plate, and seat layout at least as clear as the current version; keep humor secondary to clarity; preserve reduced-motion accessibility; and stay within the existing app surface without new routes or major architectural layers  
**Scale/Scope**: One Angular application, one main belt page, one cohesive presentation pass across the app shell plus `belt-visualization` surfaces, and one consistent humor and visual language system applied to existing states and feedback moments

## Constitution Check

_GATE: Must pass before Phase 0 research. Re-check after Phase 1 design._

### Pre-Research Gate

- **Standalone Angular First**: PASS. The feature remains inside the existing standalone Angular app and does not add routes or NgModules.
- **Strict, Simple, Maintainable Code**: PASS. The planned changes stay presentation-first, with only limited semantic view-model or template adjustments when needed for clearer styling or copy.
- **Testable Changes Are Required**: PASS. The redesign can be validated through existing component, store, and app-shell tests where semantic output changes, plus manual readability checks for responsive and reduced-motion behavior.
- **Accessible, Replaceable UI**: PASS. The feature intentionally improves the existing UI shell while keeping semantic structure, accessible state communication, and replaceable root-shell content.
- **Grow the Surface Area Conservatively**: PASS. Work stays within the current root app surface and `belt-visualization` files instead of introducing new feature routes or shared layers.

### Post-Design Gate

- **Standalone Angular First**: PASS. Phase 1 design keeps the standalone root shell and existing feature-local composition.
- **Strict, Simple, Maintainable Code**: PASS. Design emphasizes CSS, template, and copy refinements, with only small presentation metadata additions when necessary.
- **Testable Changes Are Required**: PASS. Design isolates observable presentation contracts for shell states, stage readability, humorous secondary copy, and reduced-motion behavior.
- **Accessible, Replaceable UI**: PASS. Design preserves semantic structure, explicit state labels, and reduced-motion parity while allowing the placeholder shell to evolve into the final authored experience.
- **Grow the Surface Area Conservatively**: PASS. No new routes, backend dependencies, or speculative abstractions are required.

## Project Structure

### Documentation (this feature)

```text
specs/008-design-polish/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── design-polish-ui-contract.md
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
    │   ├── belt-events.api.ts
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
        ├── selected-seat-detail.component.ts
        ├── selected-seat-detail.component.html
        └── selected-seat-detail.component.css
```

**Structure Decision**: Keep `008-design-polish` inside the existing Angular root shell and `belt-visualization` feature area. Concentrate changes in `app.html`, `app.css`, `styles.css`, and the existing belt-stage, selected-seat-detail, and operator-placement templates and styles. Allow only small view-model or store adjustments when they add semantic presentation data needed for styling or clear humorous secondary copy, not for new behavior.

## Phase 0 Research Summary

- Keep the redesign presentation-only and preserve the current API, store, and interaction flow as the source of truth.
- Apply the strongest polish work in the existing shell, stage, and secondary panel surfaces rather than introducing new page structure or routes.
- Preserve the current belt, slot, plate, and seat geometry semantics; only stretch or rebalance the layout if readability measurably improves.
- Treat humorous language as a secondary layer paired with plain primary state labels and accessible copy.
- Use the kitchen, chef, and ingredient motifs as recurring decorative character cues rather than new interactive features.
- Keep reduced-motion parity by relying on shape, color, hierarchy, and short transitions instead of adding more continuous animation.
- Add only limited presentation metadata when the template needs an explicit secondary label, decorative tone, or semantic style hook.

## Phase 1 Design Summary

- Introduce a cohesive presentation system spanning page-shell atmosphere, stage surfaces, feedback cards, seat detail, operator controls, and supporting status rail copy.
- Keep the belt stage dominant while strengthening decorative framing, typography, texture, and kitchen personality around it.
- Define a small presentation data model for shell tone, humorous secondary labels, feedback styling, responsive density, and decorative character cues.
- Preserve state clarity by requiring all important states to retain literal primary labels and non-color-only cues even when playful secondary language is added.
- Validate the redesign through a UI contract covering functional continuity, readability, humor layering, and reduced-motion parity before task planning.

## Complexity Tracking

No constitution violations or justified complexity exceptions are required for this plan.
