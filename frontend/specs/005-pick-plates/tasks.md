# Tasks: Pick Plates

**Input**: Design documents from `/specs/005-pick-plates/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: No separate test-first tasks are generated because the specification does not require a TDD workflow. Validation still includes updating and running the existing Angular spec coverage during implementation.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently once the shared foundation is complete.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`, `US3`)
- Every task includes the exact file path to create or modify

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the shared frontend types and UI surface used by all three stories.

- [x] T001 Add selected-seat detail and plate-pick outcome interfaces in `src/app/api/types.ts`
- [x] T002 [P] Create the compact selected-seat detail component scaffold in `src/app/belt-visualization/selected-seat-detail.component.ts`, `src/app/belt-visualization/selected-seat-detail.component.html`, and `src/app/belt-visualization/selected-seat-detail.component.css`
- [x] T003 [P] Register the new selected-seat detail surface in `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the shared selection, reach, and API plumbing that every story depends on.

**⚠️ CRITICAL**: No user story work should begin until this phase is complete.

- [ ] T004 Extend seat-order API result mapping for occupy, pick, checkout, and conflict handling in `src/app/api/seats.api.ts` and `src/app/api/http/problem-detail.ts`
- [x] T005 [P] Add selected-seat state, pending selected-seat actions, and refresh hooks to `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T006 [P] Derive selected-seat reach metadata, plate interaction state, and detail-area view data in `src/app/belt-visualization/belt-view-model.ts`
- [x] T007 Wire the store and derived view model into the app shell and selected-seat detail component in `src/app/app.ts` and `src/app/belt-visualization/selected-seat-detail.component.ts`

**Checkpoint**: Shared selection, API, and reach infrastructure are ready for story implementation.

---

## Phase 3: User Story 1 - Start Dining And Pick Nearby Plates (Priority: P1) 🎯 MVP

**Goal**: Let the selected free seat start dining explicitly and let the selected occupied seat pick nearby plates while showing the backend-authoritative running order below the existing stage.

**Independent Test**: Select a free seat, start dining from the selected-seat detail area, pick an in-range plate with a forgiving hit target, and verify the running order updates from the backend response without changing the existing stage layout.

### Implementation for User Story 1

- [x] T008 [US1] Implement explicit start-dining and selected-seat order hydration flow in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T009 [P] [US1] Render selected-seat status, start-dining action, and running-order content in `src/app/belt-visualization/selected-seat-detail.component.html` and `src/app/belt-visualization/selected-seat-detail.component.css`
- [ ] T010 [US1] Implement successful in-range plate-pick submission and backend `orderSummary` adoption in `src/app/api/seats.api.ts` and `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T011 [P] [US1] Add easy-to-hit pick interaction surfaces for pickable plates in `src/app/belt-visualization/belt-stage.component.ts`, `src/app/belt-visualization/belt-stage.html`, and `src/app/belt-visualization/belt-stage.css`
- [x] T012 [US1] Connect plate-pick outputs and selected-seat detail updates through `src/app/app.ts` and `src/app/app.html`
- [x] T013 [US1] Update selected-seat start-dining and successful pick coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts` and `src/app/belt-visualization/belt-view-model.spec.ts`

**Checkpoint**: User Story 1 is functional and can be validated as the MVP slice.

---

## Phase 4: User Story 2 - Browse Seats Without Accidental Writes (Priority: P2)

**Goal**: Keep seat browsing safe by making seat clicks selection-only and by keeping all write actions explicit in the compact below-stage detail area.

**Independent Test**: Click across free and occupied seats, verify selection changes and detail-area content update, and confirm that no occupy or checkout write occurs until the guest uses an explicit action below the stage.

### Implementation for User Story 2

- [x] T014 [US2] Make seat clicks selection-only and prevent implicit occupy or checkout side effects in `src/app/belt-visualization/belt-stage.component.ts` and `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T015 [P] [US2] Render selected, free, and occupied seat browsing states without shrinking or repositioning the stage in `src/app/app.html`, `src/app/app.css`, and `src/app/belt-visualization/selected-seat-detail.component.css`
- [x] T016 [US2] Add explicit selected-seat messaging that avoids implying any hidden active dining seat in `src/app/belt-visualization/selected-seat-detail.component.html` and `src/app/belt-visualization/belt-view-model.ts`
- [x] T017 [US2] Implement explicit checkout action, success state, and selected-seat detail refresh in `src/app/belt-visualization/selected-seat-detail.component.ts`, `src/app/belt-visualization/selected-seat-detail.component.html`, `src/app/app.ts`, and `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T018 [US2] Update browsing and explicit-checkout coverage in `src/app/app.spec.ts` and `src/app/belt-visualization/belt-visualization.store.spec.ts`

**Checkpoint**: User Story 2 is independently testable and browsing no longer performs accidental writes.

---

## Phase 5: User Story 3 - Understand Plate Range And Conflict Limits (Priority: P3)

**Goal**: Explain why picks fail, show a seat-owned reachable area, and reconcile the UI cleanly after stale or conflicting outcomes.

**Independent Test**: Try to pick with no occupied selected seat, try an out-of-range plate, and simulate a conflicting pick so the UI shows distinct explanations, brief reject feedback, and refreshed backend-aligned state.

### Implementation for User Story 3

- [ ] T019 [US3] Map seat-not-occupied, out-of-range, plate-not-pickable, and generic resource-conflict outcomes in `src/app/api/seats.api.ts`, `src/app/api/types.ts`, and `src/app/api/http/problem-detail.ts`
- [x] T020 [P] [US3] Render the seat-anchored reachable area and non-color pickability cues in `src/app/belt-visualization/belt-stage.html` and `src/app/belt-visualization/belt-stage.css`
- [x] T021 [P] [US3] Add brief plate-level reject feedback that preserves continuous belt motion in `src/app/belt-visualization/belt-stage.component.ts` and `src/app/belt-visualization/belt-stage.css`
- [x] T022 [US3] Reconcile failed pick attempts and surface distinct selected-seat feedback in `src/app/belt-visualization/belt-visualization.store.ts` and `src/app/belt-visualization/selected-seat-detail.component.html`
- [x] T023 [US3] Update failure-state, reach-ownership, and reject-feedback coverage in `src/app/belt-visualization/belt-view-model.spec.ts` and `src/app/belt-visualization/belt-visualization.store.spec.ts`

**Checkpoint**: User Story 3 is independently testable and failure handling is clear and backend-aligned.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize accessibility, validation, and cross-story cleanup.

- [x] T024 Implement non-color pickability cues and keyboard-accessible selected-seat actions in `src/app/belt-visualization/belt-stage.html`, `src/app/belt-visualization/belt-stage.component.ts`, `src/app/belt-visualization/selected-seat-detail.component.html`, and `src/app/belt-visualization/selected-seat-detail.component.ts`
- [x] T025 [P] Align reduced-motion and accessibility treatment for reachable-area and reject-feedback styling in `src/app/belt-visualization/belt-stage.css`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/styles.css`
- [x] T026 Update accessibility coverage for keyboard interaction and non-color cues in `src/app/app.spec.ts` and `src/app/belt-visualization/belt-view-model.spec.ts`
- [x] T027 Run `npm test` and `npm run build` after the story-level test updates
- [ ] T028 Run the feature validation steps in `specs/005-pick-plates/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup** has no dependencies and can begin immediately.
- **Phase 2: Foundational** depends on Phase 1 and blocks all user stories.
- **Phase 3: User Story 1** depends on Phase 2.
- **Phase 4: User Story 2** depends on Phase 2.
- **Phase 5: User Story 3** depends on Phase 2 and benefits from the pick flow completed in User Story 1.
- **Phase 6: Polish** depends on the desired user stories being complete.

### User Story Dependencies

- **US1** can start as soon as the foundational phase is done.
- **US2** can start as soon as the foundational phase is done.
- **US3** can start after the foundational phase, but it should be finished after the main pick flow from US1 is in place.

### Parallel Opportunities

- `T002` and `T003` can run in parallel during setup.
- `T005` and `T006` can run in parallel during the foundational phase.
- `T009` and `T011` can run in parallel within US1 once `T008` begins exposing selected-seat state.
- `T015` can run in parallel with `T014` inside US2.
- `T020` and `T021` can run in parallel inside US3.
- `T025` and `T026` can run in parallel during polish after `T024` establishes the interaction model.

---

## Parallel Example: User Story 1

```text
T009 Render selected-seat status, start-dining action, and running-order content in src/app/belt-visualization/selected-seat-detail.component.html and src/app/belt-visualization/selected-seat-detail.component.css
T011 Add easy-to-hit pick interaction surfaces for pickable plates in src/app/belt-visualization/belt-stage.component.ts, src/app/belt-visualization/belt-stage.html, and src/app/belt-visualization/belt-stage.css
```

## Parallel Example: User Story 2

```text
T014 Make seat clicks selection-only and prevent implicit occupy or checkout side effects in src/app/belt-visualization/belt-stage.component.ts and src/app/belt-visualization/belt-visualization.store.ts
T015 Render selected, free, and occupied seat browsing states without shrinking or repositioning the stage in src/app/app.html, src/app/app.css, and src/app/belt-visualization/selected-seat-detail.component.css
```

## Parallel Example: User Story 3

```text
T020 Render the seat-anchored reachable area and non-color pickability cues in src/app/belt-visualization/belt-stage.html and src/app/belt-visualization/belt-stage.css
T021 Add brief plate-level reject feedback that preserves continuous belt motion in src/app/belt-visualization/belt-stage.component.ts and src/app/belt-visualization/belt-stage.css
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate the MVP against the independent test for US1 before adding broader browsing or failure-state refinements.

### Incremental Delivery

1. Finish Setup and Foundational so selected-seat state, reach derivation, and API plumbing are stable.
2. Deliver US1 to enable explicit dining start and in-range picking.
3. Deliver US2 to guarantee safe browsing and preserve the current stage layout semantics.
4. Deliver US3 to complete range explanation, reject feedback, and reconciliation behavior.
5. Finish with Polish to cover accessibility, Angular specs, and quickstart validation.

### Parallel Team Strategy

1. One developer can own API and store groundwork in `src/app/api/` and `src/app/belt-visualization/belt-visualization.store.ts`.
2. A second developer can build the selected-seat detail surface in `src/app/belt-visualization/selected-seat-detail.component.*` and `src/app/app.*` once the shared interfaces exist.
3. A third developer can build reach visualization and reject feedback in `src/app/belt-visualization/belt-stage.*` after the foundational reach metadata is available.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional user-story label, and exact file paths.
- The plan preserves the existing stage as the primary surface and keeps new interaction UI below it by default.
- Story-level test updates in `T013`, `T018`, and `T023` keep each user story independently verifiable before the final `T027` and `T028` validation steps.
- `T024` through `T026` close the remaining accessibility gap by making keyboard use and non-color cues explicit implementation and validation work rather than contract-only guidance.
