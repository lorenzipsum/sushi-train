# Tasks: Hydrate Seat Orders After Refresh

**Input**: Design documents from `/specs/006-hydrate-seat-orders/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/hydrate-seat-orders-ui-contract.md, quickstart.md

**Tests**: Include store, view-model, and app-shell tests because `quickstart.md` explicitly requires automated verification for reload hydration, syncing, reconciliation, and checkout-summary continuity.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently after the shared foundational restoration work is complete.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align the existing API and selected-seat detail surface with the new reload-restoration contract before story work begins.

- [x] T001 Align restoration-related seat-order aliases and summary typing in `src/app/api/types.ts`
- [x] T002 [P] Confirm read-side seat restoration entry points and shared response handling in `src/app/api/seats.api.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared restoration lifecycle and selected-seat plumbing required by all reload-hydration stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Create restoration-state records, retry scheduling, and selected-seat persistence helpers in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T004 [P] Extend stage and detail view-model contracts for syncing, unresolved, restored, and checked-out states in `src/app/belt-visualization/belt-view-model.ts`
- [x] T005 [P] Add selected-seat detail inputs and rendering states for restoration status in `src/app/belt-visualization/selected-seat-detail.component.ts`, `src/app/belt-visualization/selected-seat-detail.component.html`, and `src/app/belt-visualization/selected-seat-detail.component.css`
- [x] T006 [P] Wire selected-seat detail restoration bindings through `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`

**Checkpoint**: The app can represent reload restoration states consistently even before story-specific behavior is completed.

---

## Phase 3: User Story 1 - Continue Dining After Reload (Priority: P1) 🎯 MVP

**Goal**: Restore the previously selected occupied seat, its running order, and its pick-ready behavior after a full page reload.

**Independent Test**: Reload the page while a selected seat already has an open order, then verify the same seat is restored, its existing order lines are shown, and reachable plates can be picked without starting dining again.

### Tests for User Story 1

- [x] T007 [P] [US1] Add eager reload-hydration and previous-selection restoration coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T008 [P] [US1] Add restored running-order, empty-open-order, and pick-ready view-state coverage in `src/app/belt-visualization/belt-view-model.spec.ts` and `src/app/app.spec.ts`

### Implementation for User Story 1

- [x] T009 [US1] Implement eager occupied-seat hydration and previous selected-seat restoration in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T010 [US1] Preserve restored open-order summaries across deselect and reselect flows in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T011 [US1] Surface restored running-order totals, lines, empty-open-order detail state, and ready action state in `src/app/belt-visualization/belt-view-model.ts`, `src/app/belt-visualization/selected-seat-detail.component.ts`, and `src/app/belt-visualization/selected-seat-detail.component.html`
- [x] T012 [US1] Keep restored selected-seat context and order summary visibility wired through `src/app/app.ts` and `src/app/app.html`

**Checkpoint**: User Story 1 should restore an occupied dining session after reload and allow normal continued picking.

---

## Phase 4: User Story 2 - See Honest Syncing State During Reload Recovery (Priority: P2)

**Goal**: Show syncing and unresolved retry states honestly while keeping visible cues behaviorally blocked until restoration completes.

**Independent Test**: Reload while an occupied seat is still restoring, then verify the selected-seat area shows syncing or unresolved status, blocked picks use syncing-specific feedback, and visible cues do not permit normal picks.

### Tests for User Story 2

- [x] T013 [P] [US2] Add syncing-state and automatic retry restoration coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T014 [P] [US2] Add syncing pickability and blocked-feedback coverage in `src/app/belt-visualization/belt-view-model.spec.ts` and `src/app/app.spec.ts`

### Implementation for User Story 2

- [x] T015 [US2] Implement syncing and unresolved-retrying restoration transitions with background retry in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T016 [US2] Keep reachable-area and pickability cues visible-but-blocked during syncing in `src/app/belt-visualization/belt-view-model.ts`, `src/app/belt-visualization/belt-stage.component.ts`, and `src/app/belt-visualization/belt-stage.html`
- [x] T017 [US2] Render syncing-specific helper text, blocked reasons, and unresolved retry messaging in `src/app/belt-visualization/selected-seat-detail.component.html`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/app/app.html`

**Checkpoint**: User Story 2 should prevent contradictory readiness signals while restoration is still in progress.

---

## Phase 5: User Story 3 - Reconcile Stale Occupancy After Reload (Priority: P3)

**Goal**: Reconcile seats whose occupied overview no longer matches backend dining truth and preserve the final checkout summary in the selected-seat area.

**Independent Test**: Reload with a seat that appears occupied but has no active order, confirm the UI settles into one consistent non-pickable state, then check out a seat and verify its final summary remains visible in the selected-seat area.

### Tests for User Story 3

- [x] T018 [P] [US3] Add confirmed-no-order reconciliation and checkout-summary persistence coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T019 [P] [US3] Add final reconciliation and checked-out detail rendering coverage in `src/app/belt-visualization/belt-view-model.spec.ts` and `src/app/app.spec.ts`

### Implementation for User Story 3

- [x] T020 [US3] Reconcile stale occupied seats to confirmed-no-order and consistent action state in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T021 [US3] Persist and surface final checkout summaries for the selected seat in `src/app/belt-visualization/belt-visualization.store.ts`, `src/app/belt-visualization/selected-seat-detail.component.ts`, and `src/app/belt-visualization/selected-seat-detail.component.html`
- [x] T022 [US3] Align reconciled helper text, disabled actions, and pickability cues across `src/app/belt-visualization/belt-view-model.ts`, `src/app/belt-visualization/belt-stage.html`, and `src/app/app.html`

**Checkpoint**: User Story 3 should resolve stale post-reload occupancy cleanly and preserve final checkout confirmation in the selected-seat surface.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and cross-story refinement.

- [x] T023 [P] Refine restoration-state copy, accessibility labels, and no-layout-regression checks for the selected-seat detail surface in `src/app/belt-visualization/selected-seat-detail.component.html`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/app/app.html`
- [x] T024 Run quickstart validation steps, `npm test`, and `npm run build`, then record feature-specific notes in `specs/006-hydrate-seat-orders/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2 and establishes the MVP reload-restoration flow.
- **User Story 2 (Phase 4)**: Depends on Phase 2 and extends the shared restoration lifecycle with syncing and retry behavior.
- **User Story 3 (Phase 5)**: Depends on Phase 2 and extends the shared restoration lifecycle with reconciliation and checkout-summary continuity.
- **Polish (Phase 6)**: Depends on the stories you choose to complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: No dependency on other user stories after foundational work.
- **US3**: No dependency on other user stories after foundational work.

### Within Each User Story

- Write the listed tests first and confirm they fail before implementation.
- Update store state and restoration transitions before final UI copy and styling.
- Reconcile selected-seat detail, action state, and pickability cues together before considering the story complete.

### Parallel Opportunities

- `T001` and `T002` can proceed together.
- `T004`, `T005`, and `T006` can proceed together once `T003` defines the shared restoration lifecycle.
- `T007` and `T008` can run together for US1.
- `T013` and `T014` can run together for US2.
- `T018` and `T019` can run together for US3.
- `T023` can proceed while `T024` is being validated manually.

---

## Parallel Example: User Story 1

```bash
# Launch the US1 test work together:
Task: "T007 Add eager reload-hydration and previous-selection restoration coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T008 Add restored running-order, empty-open-order, and pick-ready view-state coverage in src/app/belt-visualization/belt-view-model.spec.ts and src/app/app.spec.ts"
```

## Parallel Example: User Story 2

```bash
# Launch the US2 test work together:
Task: "T013 Add syncing-state and automatic retry restoration coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T014 Add syncing pickability and blocked-feedback coverage in src/app/belt-visualization/belt-view-model.spec.ts and src/app/app.spec.ts"
```

## Parallel Example: User Story 3

```bash
# Launch the US3 test work together:
Task: "T018 Add confirmed-no-order reconciliation and checkout-summary persistence coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T019 Add final reconciliation and checked-out detail rendering coverage in src/app/belt-visualization/belt-view-model.spec.ts and src/app/app.spec.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate the reload-restoration flow independently before moving on.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 so reload restores the active dining session.
3. Add US2 for trustworthy syncing and retry behavior.
4. Add US3 for stale-state reconciliation and final checkout-summary continuity.
5. Finish with cross-cutting polish and quickstart validation.

### Parallel Team Strategy

1. One developer completes Phase 1 and `T003`.
2. Additional developers can split `T004`, `T005`, and `T006` once the restoration lifecycle shape is set.
3. After foundational work lands, one developer can take US1 while others start US2 and US3 if team capacity allows.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- The task count is 24 total: 6 foundational tasks, 6 tasks for US1, 5 tasks for US2, 5 tasks for US3, and 2 polish tasks.
- Keep backend truth authoritative for whether an occupied seat still has an active open order or only a final checkout summary.
