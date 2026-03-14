# Tasks: Kaiten-Zushi Belt Redesign

**Input**: Design documents from `frontend/specs/002-belt-layout-redesign/`
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`, `data-model.md`, `contracts/`

**Tests**: This feature changes user-visible behavior and layout, so task coverage includes Vitest updates for geometry derivation, menu-item visual mapping, and page/state rendering.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated as an independent increment.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the initial test scaffolds and task-specific file surface for the redesign.

- [x] T001 Create racetrack layout test scaffold in frontend/src/app/belt-visualization/belt-layout.spec.ts
- [x] T002 Create menu-item visual mapping test scaffold in frontend/src/app/belt-visualization/menu-item-visuals.spec.ts
- [x] T003 Update page-shell regression test scaffold in frontend/src/app/app.spec.ts

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the shared geometry and visual-mapping primitives that every story depends on.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [x] T004 Implement racetrack geometry helper in frontend/src/app/belt-visualization/belt-layout.ts
- [x] T005 Implement category-first menu-item visual registry with fallback behavior in frontend/src/app/belt-visualization/menu-item-visuals.ts
- [x] T006 Update frontend/src/app/belt-visualization/belt-view-model.ts to map stable slot and seat data onto racetrack-derived stage coordinates only
- [x] T007 [P] Add geometry coverage for slot progress, turns, and responsive spacing in frontend/src/app/belt-visualization/belt-layout.spec.ts
- [x] T008 [P] Add registry coverage for seeded dishes and generic fallback handling in frontend/src/app/belt-visualization/menu-item-visuals.spec.ts
- [x] T009 Update view-model coverage for racetrack coordinates, larger plate sizing, and seat-place metadata in frontend/src/app/belt-visualization/belt-view-model.spec.ts

**Checkpoint**: Geometry, menu-item mapping, and derived view-model primitives are ready for story work.

---

## Phase 3: User Story 1 - Feel The Conveyor As A Place (Priority: P1) 🎯 MVP

**Goal**: Turn the page into a belt-first kaiten-zushi counter with a wide hero stage and lower-priority support panels beneath it.

**Independent Test**: Load a belt snapshot and verify that the page shows a wide racetrack-style conveyor hero, places support panels below the stage, and preserves Feature 001 backend-authoritative movement behavior.

### Tests for User Story 1 ⚠️

- [x] T010 [US1] Add page hierarchy and hero-stage assertions in frontend/src/app/app.spec.ts

### Implementation for User Story 1

- [x] T011 [US1] Refactor the page shell hierarchy for a belt-first hero layout in frontend/src/app/app.html
- [x] T012 [US1] Implement the belt-first page layout, below-the-fold support panels, and full-width stage treatment in frontend/src/app/app.css
- [x] T013 [US1] Update stage component inputs to consume racetrack geometry and preserve continuous rotation behavior in frontend/src/app/belt-visualization/belt-stage.component.ts
- [x] T014 [US1] Replace circular stage markup with racetrack conveyor markup in frontend/src/app/belt-visualization/belt-stage.html
- [x] T015 [US1] Implement racetrack conveyor styling, counter surfaces, and stage sizing in frontend/src/app/belt-visualization/belt-stage.css

**Checkpoint**: User Story 1 should deliver a believable wide conveyor layout with the belt as the primary visual focus.

---

## Phase 4: User Story 2 - Recognize Seats And Food Quickly (Priority: P2)

**Goal**: Make seats read as real sushi-counter places and make visible dishes recognizably different across major menu-item families.

**Independent Test**: Load seeded seat occupancy and menu-item data, then verify that occupied seats are clearly recognizable and dish families render with distinct visual identities plus graceful fallback behavior.

### Tests for User Story 2 ⚠️

- [x] T016 [US2] Add seat-place, family-level dish distinction, and menu-visual fallback assertions in frontend/src/app/belt-visualization/belt-view-model.spec.ts and frontend/src/app/belt-visualization/menu-item-visuals.spec.ts

### Implementation for User Story 2

- [x] T017 [US2] Extend frontend/src/app/belt-visualization/belt-view-model.ts to attach menu-item visual metadata, seat presence cues, and plate presentation fields to the derived stage view model
- [x] T018 [US2] Render dish-family visuals, item overrides, and generic fallback treatments in frontend/src/app/belt-visualization/belt-stage.html
- [x] T019 [US2] Style larger plates, family-specific food treatments, and more obvious occupied seats in frontend/src/app/belt-visualization/belt-stage.css
- [x] T020 [US2] Update seat and plate support copy for recognizability in frontend/src/app/app.html and frontend/src/app/app.css

**Checkpoint**: User Story 2 should make seats and visible plate contents easier to recognize without changing the underlying read-only behavior.

---

## Phase 5: User Story 3 - Keep Trust During Real-World Use (Priority: P3)

**Goal**: Preserve loading, paused, degraded, reduced-motion, and responsive clarity in the redesigned layout.

**Independent Test**: Exercise paused state, degraded freshness, reduced motion, unknown menu items, and narrow viewports, then verify that the page remains understandable and belt-first.

### Tests for User Story 3 ⚠️

- [x] T021 [US3] Add reduced-motion, degraded-state, and responsive hierarchy assertions in frontend/src/app/app.spec.ts and frontend/src/app/belt-visualization/belt-visualization.store.spec.ts

### Implementation for User Story 3

- [x] T022 [US3] Remove duplicate titling and refine trust-critical state messaging in frontend/src/app/app.html
- [x] T023 [US3] Implement responsive stacking and non-color-only state treatments in frontend/src/app/app.css
- [x] T024 [US3] Tune stage behavior for paused, reduced-motion, and degraded readability in frontend/src/app/belt-visualization/belt-stage.component.ts and frontend/src/app/belt-visualization/belt-stage.css
- [x] T025 [US3] Preserve existing refresh-after-write and state-summary behavior under the redesigned hierarchy in frontend/src/app/belt-visualization/belt-visualization.store.ts

**Checkpoint**: User Story 3 should preserve Feature 001 trust guarantees in the redesigned page across real-world states and viewport sizes.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize cross-story quality, documentation alignment, and regression verification.

- [x] T026 [P] Align implementation notes with the delivered redesign in frontend/specs/002-belt-layout-redesign/quickstart.md and frontend/specs/002-belt-layout-redesign/contracts/belt-layout-redesign-ui-contract.md
- [x] T027 [P] Review contrast, semantics, and accessible labels across frontend/src/app/app.html and frontend/src/app/belt-visualization/belt-stage.html
- [x] T028 Run regression verification for the redesign using frontend/src/app/app.spec.ts, frontend/src/app/belt-visualization/\*.spec.ts, and frontend/package.json scripts
- [ ] T029 Conduct and document structured design and usability review for SC-001 through SC-004 in frontend/specs/002-belt-layout-redesign/quickstart.md and frontend/specs/002-belt-layout-redesign/checklists/requirements.md
- [x] T030 Refine Feature 002 documentation for squarer belt geometry, kitchen core, and belt-facing seat rhythm in frontend/specs/002-belt-layout-redesign/spec.md, frontend/specs/002-belt-layout-redesign/plan.md, frontend/specs/002-belt-layout-redesign/research.md, frontend/specs/002-belt-layout-redesign/data-model.md, frontend/specs/002-belt-layout-redesign/contracts/belt-layout-redesign-ui-contract.md, and frontend/specs/002-belt-layout-redesign/quickstart.md
- [x] T031 Refine frontend/src/app/belt-visualization/belt-layout.ts to use a squarer loop and belt-facing seat placement that reduces visible plate crowding
- [x] T032 Refine frontend/src/app/belt-visualization/belt-view-model.ts and frontend/src/app/belt-visualization/belt-stage.html to expose and render kitchen-core and belt-facing seat presentation metadata
- [x] T033 Refine frontend/src/app/belt-visualization/belt-stage.css and frontend/src/app/app.css to render the squarer belt, interior chef kitchen, and more even guest seating
- [x] T034 Re-run frontend/src/app/belt-visualization/\*.spec.ts, frontend/src/app/app.spec.ts, and frontend/package.json build verification after the squarer-loop refinement
- [x] T035 Refine Feature 002 documentation for four-sided seating, rounded-square belt geometry, and more deliberate plate presentation in frontend/specs/002-belt-layout-redesign/spec.md, frontend/specs/002-belt-layout-redesign/data-model.md, and frontend/specs/002-belt-layout-redesign/contracts/belt-layout-redesign-ui-contract.md
- [x] T036 Simplify frontend/src/app/app.html and frontend/src/app/app.css to a title-led shell with one compact info rail and no extra support boxes
- [x] T037 Refine frontend/src/app/belt-visualization/belt-layout.ts, frontend/src/app/belt-visualization/belt-view-model.ts, and frontend/src/app/belt-visualization/belt-stage.html for four-sided seating and a more square belt loop
- [x] T038 Refine frontend/src/app/belt-visualization/belt-stage.css for occupied-only guest silhouettes, a stronger chef/kitchen center, and more deliberate plate styling
- [x] T039 Refine frontend/src/app/belt-visualization/belt-layout.ts and frontend/src/app/belt-visualization/belt-stage.css for a stronger horizontal belt bias that fits the page without scrolling
- [x] T040 Re-tune frontend/src/app/belt-visualization/belt-view-model.ts and rerun frontend/src/app/belt-visualization/\*.spec.ts plus frontend/package.json build verification for the wider layout
- [x] T041 Rebalance frontend/src/app/belt-visualization/belt-layout.ts seat allocation so longer top and bottom sides receive more seats than the shorter left and right sides
- [x] T042 Add regression coverage for weighted side allocation in frontend/src/app/belt-visualization/belt-layout.spec.ts and rerun frontend/src/app/belt-visualization/\*.spec.ts plus frontend/package.json build verification
- [x] T043 Move the kitchen markup into frontend/src/app/belt-visualization/belt-stage.html so the chef and prep area live inside the inner lane instead of behind it
- [x] T044 Restyle frontend/src/app/belt-visualization/belt-stage.css to render a more expressive chef-focused kitchen scene inside the inner lane without changing belt alignment, then rerun frontend/package.json build verification

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all story work.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **User Story 2 (Phase 4)**: Depends on Foundational completion.
- **User Story 3 (Phase 5)**: Depends on Foundational completion.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First deliverable and MVP for the redesign.
- **User Story 2 (P2)**: Can start after Foundational completion and must remain independently testable, even if it reuses stage or layout work introduced earlier.
- **User Story 3 (P3)**: Can start after Foundational completion and must remain independently testable, even if it validates behavior that also appears in other stories.

### Within Each User Story

- Test tasks should be written first and should fail before implementation is considered complete.
- Pure layout/mapping helpers before component wiring.
- Foundational geometry/state mapping must land before story-specific visual enrichment in shared files such as `frontend/src/app/belt-visualization/belt-view-model.ts`.
- Template and style changes before final shell copy and polish.
- Story-level regression checks before moving to the next story.

### Parallel Opportunities

- `T007` and `T008` can run in parallel after `T004` and `T005` exist.
- `T026`, `T027`, and `T029` can run in parallel during Polish.
- If capacity allows, some test updates can be prepared in parallel with nearby implementation work, but file-sharing tasks should stay sequential.

---

## Parallel Example: User Story 1

```text
After foundational helpers are in place:
- T010 Add page hierarchy and hero-stage assertions in frontend/src/app/app.spec.ts
- T013 Update stage component inputs to consume racetrack geometry in frontend/src/app/belt-visualization/belt-stage.component.ts
```

## Parallel Example: Foundational Phase

```text
After T004 and T005 are implemented:
- T007 Add geometry coverage in frontend/src/app/belt-visualization/belt-layout.spec.ts
- T008 Add registry coverage in frontend/src/app/belt-visualization/menu-item-visuals.spec.ts
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup.
2. Complete Foundational work.
3. Complete User Story 1.
4. Validate the belt-first hero layout independently before expanding dish and seat richness.

### Incremental Delivery

1. Deliver the belt-first racetrack stage and below-the-fold hierarchy.
2. Independently add richer seat and food recognition on top of the same foundational geometry and mapping layer.
3. Independently harden reduced-motion, degraded-state, and responsive behavior.
4. Finish with documentation alignment and full regression verification.

### Parallel Team Strategy

1. One contributor handles geometry and view-model primitives.
2. One contributor handles root page hierarchy and responsive layout.
3. One contributor handles menu-item visual mapping and food/seat presentation.
4. Rejoin for trust-state hardening and regression verification.

---

## Notes

- `[P]` tasks operate on different files with no incomplete prerequisites.
- `[US1]`, `[US2]`, and `[US3]` labels map directly to the feature specification's user stories.
- Preserve Feature 001 trust guarantees while implementing every story in this redesign.
- Keep the redesign frontend-local and avoid route or backend contract expansion unless a later approved task explicitly requires it.
