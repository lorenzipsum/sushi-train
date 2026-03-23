# Tasks: Refine Belt Interactions

**Input**: Design documents from `/specs/009-refine-belt-interactions/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/refine-belt-interactions-ui-contract.md, quickstart.md

**Tests**: Include targeted automated tests because the plan and quickstart explicitly require view-model, store, app-shell, accessibility, and responsive coverage for the new rendered and interactive behavior.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently after the truly shared interaction foundation is complete.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the shared test fixtures and styling hooks used by multiple stories.

- [x] T001 [P] Add baseline app-shell test fixtures for seat selection, pickable plate, and settings-trigger rendering in src/app/app.spec.ts
- [x] T002 [P] Add shared styling hooks for lit reach cues, selected-seat emphasis, and ring-only plate presentation in src/styles.css

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add only the shared stage hooks required by multiple user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Add settings-trigger output plumbing in src/app/belt-visualization/belt-stage.component.ts and src/app/belt-visualization/belt-stage.html

**Checkpoint**: The app can now support shared stage interaction hooks and common styling before story-specific work begins.

---

## Phase 3: User Story 1 - Pick Plates From The Top Row Reliably (Priority: P1) 🎯 MVP

**Goal**: Make top-row seats able to pick intended nearby plates without changing the overall dining flow.

**Independent Test**: Select each top-row seat, wait for nearby plates to enter the intended zone, and confirm those plates become pickable while out-of-range plates remain blocked.

### Tests for User Story 1

- [x] T004 [P] [US1] Add top-row reach and out-of-range regression coverage in src/app/belt-visualization/belt-view-model.spec.ts
- [x] T005 [P] [US1] Add top-row pickability regression coverage in src/app/belt-visualization/belt-visualization.store.spec.ts

### Implementation for User Story 1

- [x] T006 [US1] Update segment-aware reach radius and top-row expansion logic in src/app/belt-visualization/belt-view-model.ts
- [x] T007 [US1] Align visible reach rendering and within-reach slot cues with the updated top-row radius in src/app/belt-visualization/belt-stage.html and src/styles.css

**Checkpoint**: User Story 1 should allow intended top-row plate picks without widening ownership beyond the expected pickup zone.

---

## Phase 4: User Story 2 - Understand Which Seat Is Active At A Glance (Priority: P1)

**Goal**: Make the selected seat and its reach ownership immediately obvious through stronger visual emphasis.

**Independent Test**: Select, deselect, and switch seats and confirm first-time viewers can identify the active seat and its reach zone without additional explanation.

### Tests for User Story 2

- [x] T008 [P] [US2] Add app-shell rendering coverage for active seat emphasis and reach ownership in src/app/app.spec.ts
- [x] T009 [P] [US2] Add stage view-model coverage for selected-seat and lit-reach presentation in src/app/belt-visualization/belt-view-model.spec.ts

### Implementation for User Story 2

- [x] T010 [US2] Strengthen selected-seat lighting, reach bubble emphasis, and within-reach marker contrast in src/styles.css
- [x] T011 [US2] Add explicit selected-seat reach ownership labels and any required view-model fields for active-seat messaging in src/app/belt-visualization/belt-view-model.ts and src/app/app.ts

**Checkpoint**: User Story 2 should make the active seat and its pickup zone unmistakable without adding structural clutter to the stage.

---

## Phase 5: User Story 3 - Read Plates With Less Visual Noise (Priority: P2)

**Goal**: Simplify moving plates to ring-only visuals while preserving tier identity and interaction states.

**Independent Test**: Compare the belt before and after the change and confirm each moving plate shows only the colored ring while pickable, pending, and rejected states remain understandable.

### Tests for User Story 3

- [x] T012 [P] [US3] Add ring-only plate rendering coverage in src/app/app.spec.ts

### Implementation for User Story 3

- [x] T013 [US3] Remove inner plate center and garnish markup from src/app/belt-visualization/belt-stage.html
- [x] T014 [US3] Rebuild ring-only plate styling while preserving tier colors and pick-state cues in src/styles.css

**Checkpoint**: User Story 3 should leave the belt visually cleaner while keeping plate tier color and interaction feedback intact.

---

## Phase 6: User Story 4 - Adjust Belt Speed From The Main Interface (Priority: P2)

**Goal**: Turn the cogwheel into a working modal flow that updates belt speed through the existing backend contract.

**Independent Test**: Open the cogwheel modal, verify the current speed is preselected, change to a different allowed speed, confirm the update applies after confirmation, and verify cancel leaves speed unchanged.

### Tests for User Story 4

- [x] T015 [P] [US4] Add speed modal state, unchanged-selection, refresh-after-success, and error feedback coverage in src/app/belt-visualization/belt-visualization.store.spec.ts
- [x] T016 [P] [US4] Add cogwheel activation, dialog accessibility, and submit-dismiss coverage in src/app/app.spec.ts

### Implementation for User Story 4

- [x] T017 [US4] Define belt speed modal and feedback types in src/app/api/types.ts
- [x] T018 [US4] Implement belt parameter PATCH support for speed updates in src/app/api/belts.api.ts
- [x] T019 [US4] Implement speed selection, submit, feedback, and refresh flow in src/app/belt-visualization/belt-visualization.store.ts
- [x] T020 [P] [US4] Wire the active cogwheel control and settings event output in src/app/belt-visualization/belt-stage.component.ts and src/app/belt-visualization/belt-stage.html
- [x] T021 [P] [US4] Render the speed modal, current-speed messaging, semantic dialog attributes, and submit-dismiss actions in src/app/app.ts and src/app/app.html
- [x] T022 [US4] Style the speed modal, overlay, focus treatment, and speed outcome feedback in src/app/app.css

**Checkpoint**: User Story 4 should let users change belt speed through the cogwheel modal without leaving the main experience or creating ambiguous unchanged or failed states.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final responsive, accessibility, regression, and validation work across all completed stories.

- [x] T023 [P] Add responsive and smaller-screen coverage for reach cues, ring-only plates, and speed modal rendering in src/app/app.spec.ts and src/app/app.css
- [x] T024 [P] Add semantic dialog, keyboard dismissal, focus management, and submit-state accessibility coverage for the speed modal in src/app/app.spec.ts
- [x] T025 [P] Add regression coverage for seat selection, pick-plate flow, and checkout clarity after the interaction refinements in src/app/app.spec.ts and src/app/belt-visualization/belt-visualization.store.spec.ts
- [x] T026 [P] Update feature validation notes and execution outcomes in specs/009-refine-belt-interactions/quickstart.md
- [x] T027 Run quickstart validation, npm test, and npm run build, then record desktop, smaller-screen, accessibility, and regression results in specs/009-refine-belt-interactions/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2 and delivers the MVP reach fix.
- **User Story 2 (Phase 4)**: Depends on Phase 2 and strengthens selected-seat clarity.
- **User Story 3 (Phase 5)**: Depends on Phase 2 and simplifies plate presentation.
- **User Story 4 (Phase 6)**: Depends on Phase 2 and adds speed control.
- **Polish (Phase 7)**: Depends on the stories you choose to complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: No dependency on other user stories after foundational work.
- **US3**: No dependency on other user stories after foundational work.
- **US4**: No dependency on other user stories after foundational work.

### Suggested Completion Order

- Finish Setup and Foundational first.
- Deliver **US1** as the MVP because it fixes the broken top-row pickup behavior.
- Deliver **US2** next because selection clarity directly supports the same dining interaction model.
- Deliver **US3** and **US4** afterward in either order because both are independently testable after the shared foundation is in place.

### Within Each User Story

- Test tasks should land before implementation tasks and should fail before the implementation is applied.
- Shared data or state shape changes should land before template or styling work that depends on them.
- Each story should be manually validated against its independent test before moving on.

### Parallel Opportunities

- `T001` and `T002` can run in parallel.
- `T004` and `T005` can run in parallel for US1.
- `T008` and `T009` can run in parallel for US2.
- `T015` and `T016` can run in parallel for US4.
- `T020` and `T021` can run in parallel after `T019` defines the store behavior.
- `T023`, `T024`, `T025`, and `T026` can run in parallel before `T027` records final validation.

---

## Parallel Example: User Story 1

```bash
Task: "T004 Add top-row reach and out-of-range regression coverage in src/app/belt-visualization/belt-view-model.spec.ts"
Task: "T005 Add top-row pickability regression coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
```

## Parallel Example: User Story 2

```bash
Task: "T008 Add app-shell rendering coverage for active seat emphasis and reach ownership in src/app/app.spec.ts"
Task: "T009 Add stage view-model coverage for selected-seat and lit-reach presentation in src/app/belt-visualization/belt-view-model.spec.ts"
```

## Parallel Example: User Story 3

```bash
Task: "T012 Add ring-only plate rendering coverage in src/app/app.spec.ts"
Task: "T014 Rebuild ring-only plate styling while preserving tier colors and pick-state cues in src/styles.css"
```

## Parallel Example: User Story 4

```bash
Task: "T015 Add speed modal state, unchanged-selection, refresh-after-success, and error feedback coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T016 Add cogwheel activation, dialog accessibility, and submit-dismiss coverage in src/app/app.spec.ts"
Task: "T020 Wire the active cogwheel control and settings event output in src/app/belt-visualization/belt-stage.component.ts and src/app/belt-visualization/belt-stage.html"
Task: "T021 Render the speed modal, current-speed messaging, semantic dialog attributes, and submit-dismiss actions in src/app/app.ts and src/app/app.html"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate the top-row pickup fix independently before continuing.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 and validate the reach fix.
3. Deliver US2 and validate selected-seat clarity.
4. Deliver US3 and validate ring-only plate readability.
5. Deliver US4 and validate speed control behavior.
6. Finish with cross-cutting validation and build-test verification.

### Parallel Team Strategy

1. One developer completes Phase 1 and Phase 2.
2. After foundational work lands, one developer can take US1 while another takes US2, US3, or US4 if team capacity allows.
3. Finish with shared validation and quality-gate work in Phase 7.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- The task count is 27 total: 2 setup tasks, 1 foundational task, 4 tasks for US1, 4 tasks for US2, 3 tasks for US3, 8 tasks for US4, and 5 polish tasks.
- This revision moves speed-specific work out of shared phases, makes accessibility and smaller-screen validation explicit, and removes the vague shared test task overlap.