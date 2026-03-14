# Tasks: Occupy Seat

**Input**: Design documents from `/specs/003-occupy-seat/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/occupy-seat-api-contract.md, quickstart.md

**Tests**: Include store, view-model, and app-shell tests because `quickstart.md` explicitly requires automated verification for success, conflict, not-found, and occupancy-context handling.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align the existing frontend API surface with the occupy-seat contract before store and UI work begins.

- [x] T001 Align occupy-seat DTO aliases and problem-detail usage in `src/app/api/types.ts`
- [x] T002 [P] Confirm the seat occupy request/response entry points in `src/app/api/seats.api.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared interaction state needed by all occupy-seat stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Create occupy request state, feedback signals, and write-flow helpers in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T004 [P] Extend seat interaction fields and occupancy context in `src/app/belt-visualization/belt-view-model.ts`
- [x] T005 [P] Add click/output plumbing for seat actions in `src/app/belt-visualization/belt-stage.component.ts`

**Checkpoint**: The store and stage contracts are ready for story-specific behavior.

---

## Phase 3: User Story 1 - Take A Free Seat (Priority: P1) 🎯 MVP

**Goal**: Let an anonymous guest click a free seat and occupy it through the backend-authoritative write flow.

**Independent Test**: Load a belt with a free seat, click it, confirm the seat becomes occupied, then refresh and confirm the occupied state remains.

### Tests for User Story 1

- [x] T006 [P] [US1] Add successful occupy and refresh reconciliation coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T007 [P] [US1] Add free-seat actionability coverage in `src/app/belt-visualization/belt-view-model.spec.ts`

### Implementation for User Story 1

- [x] T008 [US1] Implement the successful occupy write path with refresh-after-write in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T009 [US1] Wire free-seat click handling and accessible seat actions in `src/app/belt-visualization/belt-stage.component.ts` and `src/app/belt-visualization/belt-stage.html`
- [x] T010 [US1] Add actionable, busy, and occupied seat affordances in `src/app/belt-visualization/belt-stage.css`
- [x] T011 [US1] Surface occupy success feedback in `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`

**Checkpoint**: User Story 1 should allow a free seat to be occupied and stay occupied after reconciliation.

---

## Phase 4: User Story 2 - Understand Why A Seat Cannot Be Taken (Priority: P2)

**Goal**: Explain conflict and not-found failures clearly while keeping the rendered seat state aligned with backend truth.

**Independent Test**: Attempt to occupy an already occupied seat and a missing seat, then confirm the UI shows the correct message and does not display a false occupied result.

### Tests for User Story 2

- [x] T012 [P] [US2] Add conflict and not-found occupy failure coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T013 [P] [US2] Add failure-message rendering coverage in `src/app/app.spec.ts`

### Implementation for User Story 2

- [x] T014 [US2] Normalize `409` and `404` occupy failures with reconciliation logic in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T015 [US2] Render conflict and not-found feedback states in `src/app/app.html` and `src/app/app.css`
- [x] T016 [US2] Keep occupied and in-flight seats non-actionable in `src/app/belt-visualization/belt-view-model.ts` and `src/app/belt-visualization/belt-stage.css`

**Checkpoint**: User Story 2 should clearly communicate why occupy failed and leave seat state trustworthy.

---

## Phase 5: User Story 3 - Keep Occupancy Ready For Later Flows (Priority: P3)

**Goal**: Preserve the active dining record returned by occupy so later checkout and plate-pickup flows can reuse the same order context.

**Independent Test**: Occupy a seat and confirm the frontend keeps the returned `orderId` and `createdAt` available after success and later reconciliation.

### Tests for User Story 3

- [x] T017 [P] [US3] Add durable occupancy context coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T018 [P] [US3] Add occupied-seat context and aria coverage in `src/app/belt-visualization/belt-view-model.spec.ts`

### Implementation for User Story 3

- [x] T019 [US3] Preserve `orderId` and `createdAt` from occupy responses in `src/app/api/types.ts` and `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T020 [US3] Expose durable occupancy metadata through the stage/app view state in `src/app/belt-visualization/belt-view-model.ts` and `src/app/app.html`
- [x] T021 [US3] Reuse seat-detail reconciliation for durable occupancy context in `src/app/api/seats.api.ts` and `src/app/belt-visualization/belt-visualization.store.ts`

**Checkpoint**: User Story 3 should leave the frontend ready to anchor later seat and plate flows on the same active order.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and small cross-story refinements.

- [x] T022 [P] Refine reduced-motion and degraded-state occupy messaging in `src/app/app.html`, `src/app/app.css`, and `src/app/belt-visualization/belt-stage.css`
- [ ] T023 Run quickstart validation steps and record any feature-specific notes in `specs/003-occupy-seat/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2; establishes the MVP.
- **User Story 2 (Phase 4)**: Depends on Phase 3 because failure messaging builds on the occupy flow.
- **User Story 3 (Phase 5)**: Depends on Phase 3 and can begin after the basic occupy flow works.
- **Polish (Phase 6)**: Depends on the stories you choose to complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: Depends on the occupy interaction from US1.
- **US3**: Depends on the successful occupy result from US1, but not on US2.

### Within Each User Story

- Write the listed tests first and confirm they fail before implementation.
- Update store and data handling before the final UI copy and styling pass.
- Reconcile backend truth after each write outcome before considering the story complete.

### Parallel Opportunities

- `T001` and `T002` can proceed together after the contract review.
- `T004` and `T005` can proceed together once `T003` defines the shared interaction state.
- `T006` and `T007` can run together for US1.
- `T012` and `T013` can run together for US2.
- `T017` and `T018` can run together for US3.
- `T022` can proceed while `T023` is being validated manually.

---

## Parallel Example: User Story 1

```bash
# Launch the US1 test work together:
Task: "T006 Add successful occupy and refresh reconciliation coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T007 Add free-seat actionability coverage in src/app/belt-visualization/belt-view-model.spec.ts"
```

## Parallel Example: User Story 2

```bash
# Launch the US2 test work together:
Task: "T012 Add conflict and not-found occupy failure coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T013 Add failure-message rendering coverage in src/app/app.spec.ts"
```

## Parallel Example: User Story 3

```bash
# Launch the US3 test work together:
Task: "T017 Add durable occupancy context coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T018 Add occupied-seat context and aria coverage in src/app/belt-visualization/belt-view-model.spec.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate the occupy flow independently before moving on.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 as the first working write flow.
3. Add US2 for trustworthy failure handling.
4. Add US3 so later checkout and plate-pickup features can reuse the same order context.
5. Finish with cross-cutting polish and quickstart validation.

### Parallel Team Strategy

1. One developer completes Phase 1 and `T003`.
2. A second developer can take `T004` while a third takes `T005`.
3. After US1 lands, one developer can handle US2 while another starts US3.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- Keep the implementation aligned to the backend truth that occupancy is the seat's active `OPEN` order, not a separate frontend session concept.
