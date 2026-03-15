# Tasks: Checkout Seat

**Input**: Design documents from `/specs/004-checkout-seat/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/checkout-seat-api-contract.md`, `quickstart.md`

**Tests**: Include store, view-model, and app-shell tests because `quickstart.md` explicitly requires automated verification for checkout success, stale/not-found handling, and in-session final-summary retention.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align the existing frontend API surface and test harness with the checkout-seat contract before shared store and UI work begins.

- [x] T001 Align checkout response and problem-detail aliases in `src/app/api/types.ts`
- [x] T002 [P] Confirm the checkout request/response entry point in `src/app/api/seats.api.ts`
- [x] T003 [P] Add checkout-focused test helpers for checked-out orders in `src/app/belt-visualization/belt-visualization.store.spec.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared checkout interaction state and seat-action contract needed by all checkout-seat stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 Create checkout request state, final-summary cache, and shared feedback helpers in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T005 [P] Extend seat interaction fields for occupied-seat checkout actions in `src/app/belt-visualization/belt-view-model.ts`
- [x] T006 [P] Add occupied-seat checkout event plumbing in `src/app/belt-visualization/belt-stage.component.ts`

**Checkpoint**: The store and stage contracts are ready for story-specific checkout behavior.

---

## Phase 3: User Story 1 - Finish Dining At An Occupied Seat (Priority: P1) 🎯 MVP

**Goal**: Let an anonymous guest check out from an occupied seat, see the final checked-out summary, and watch the seat become visibly free again.

**Independent Test**: Load a belt with an occupied seat, start checkout from that seat, confirm the final summary appears, and confirm the seat becomes free after reconciliation.

### Tests for User Story 1

- [x] T007 [P] [US1] Add checkout success and refresh reconciliation coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T008 [P] [US1] Add occupied-seat checkout actionability coverage in `src/app/belt-visualization/belt-view-model.spec.ts`
- [x] T009 [P] [US1] Add checkout confirmation rendering coverage in `src/app/app.spec.ts`
- [x] T010 [P] [US1] Add empty-order checkout coverage for `lines = []` and `totalPrice = 0` in `src/app/belt-visualization/belt-visualization.store.spec.ts` and `src/app/app.spec.ts`

### Implementation for User Story 1

- [x] T011 [US1] Implement the successful checkout write path and refresh-after-write reconciliation in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T012 [US1] Wire occupied-seat checkout actions and pending behavior in `src/app/belt-visualization/belt-stage.component.ts` and `src/app/belt-visualization/belt-stage.html`
- [x] T013 [US1] Add occupied-seat checkout affordances and pending styling in `src/app/belt-visualization/belt-stage.css`
- [x] T014 [US1] Surface checkout success confirmation and final summary details, including empty-order outcomes, in `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`

**Checkpoint**: User Story 1 should let a guest complete checkout successfully and see the seat return to available state.

---

## Phase 4: User Story 2 - Understand Why Checkout Cannot Happen (Priority: P2)

**Goal**: Explain stale-occupancy and missing-seat checkout failures clearly while keeping the rendered seat state aligned with backend truth.

**Independent Test**: Attempt checkout after another client already finished it and against a missing seat, then confirm the UI shows the correct reason and reconciles the seat state.

### Tests for User Story 2

- [x] T015 [P] [US2] Add `409 SEAT_NOT_OCCUPIED` and `404` checkout failure coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T016 [P] [US2] Add stale-checkout and missing-seat feedback rendering coverage in `src/app/app.spec.ts`

### Implementation for User Story 2

- [x] T017 [US2] Normalize checkout `409` and `404` failures with backend-truth reconciliation in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T018 [US2] Render stale-occupancy and missing-seat checkout feedback states in `src/app/app.html` and `src/app/app.css`
- [x] T019 [US2] Keep stale, missing, and in-flight seat actions non-repeatable in `src/app/belt-visualization/belt-view-model.ts` and `src/app/belt-visualization/belt-stage.css`

**Checkpoint**: User Story 2 should clearly communicate why checkout failed and leave seat availability trustworthy.

---

## Phase 5: User Story 3 - Preserve The Final Checkout Summary (Priority: P3)

**Goal**: Preserve the checked-out order summary from the successful checkout response for the current in-app session so later features can build on it.

**Independent Test**: Complete checkout, trigger later in-app reconciliation, and confirm the stored final summary still matches the backend checkout response even though the seat now reads as free.

### Tests for User Story 3

- [x] T020 [P] [US3] Add in-session final-summary retention coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T021 [P] [US3] Add final-summary persistence and receipt-style rendering coverage in `src/app/app.spec.ts`

### Implementation for User Story 3

- [x] T022 [US3] Preserve checked-out summaries separately from active open-order state in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T023 [US3] Expose preserved checkout summary metadata through the app-shell view state in `src/app/app.ts` and `src/app/app.html`
- [x] T024 [US3] Keep current-session checkout summaries stable across refresh and reconcile flows in `src/app/belt-visualization/belt-visualization.store.ts` and `src/app/api/types.ts`

**Checkpoint**: User Story 3 should keep the final checkout summary available during the current session without inventing a new lifecycle model.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and small cross-story refinements.

- [x] T025 [P] Refine reduced-motion, degraded-state, and accessibility messaging for checkout flows in `src/app/app.html`, `src/app/app.css`, and `src/app/belt-visualization/belt-stage.css`
- [x] T026 [P] Align feature verification notes with delivered checkout behavior in `specs/004-checkout-seat/quickstart.md`
- [x] T027 Conduct and document a structured usability review for checkout outcome comprehension in `specs/004-checkout-seat/quickstart.md` and `specs/004-checkout-seat/checklists/requirements.md`
- [x] T028 Run checkout regression verification with `npm test` and `npm run build` and record outcomes in `specs/004-checkout-seat/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user story work.
- **User Story 1 (Phase 3)**: Depends on Foundational completion and delivers the MVP.
- **User Story 2 (Phase 4)**: Depends on User Story 1 because failure handling builds on the checkout interaction surface.
- **User Story 3 (Phase 5)**: Depends on User Story 1 because it preserves the successful checkout result.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: Depends on the checkout flow from US1.
- **US3**: Depends on the successful checkout result from US1, but not on US2.

### Within Each User Story

- Write the listed tests first and confirm they fail before implementation.
- Update shared store and data handling before the final UI copy and styling pass.
- Reconcile backend truth after each checkout outcome before considering the story complete.

### Parallel Opportunities

- `T001`, `T002`, and `T003` can proceed together in Setup.
- `T005` and `T006` can proceed together once `T004` defines the shared checkout state.
- `T007`, `T008`, `T009`, and `T010` can run together for US1.
- `T015` and `T016` can run together for US2.
- `T020` and `T021` can run together for US3.
- `T025`, `T026`, and `T027` can proceed while `T028` is being prepared.

---

## Parallel Example: User Story 1

```text
# Launch the US1 test work together:
Task: "T007 Add checkout success and refresh reconciliation coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T008 Add occupied-seat checkout actionability coverage in src/app/belt-visualization/belt-view-model.spec.ts"
Task: "T009 Add checkout confirmation rendering coverage in src/app/app.spec.ts"
Task: "T010 Add empty-order checkout coverage for lines = [] and totalPrice = 0 in src/app/belt-visualization/belt-visualization.store.spec.ts and src/app/app.spec.ts"
```

## Parallel Example: User Story 2

```text
# Launch the US2 test work together:
Task: "T015 Add 409 SEAT_NOT_OCCUPIED and 404 checkout failure coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T016 Add stale-checkout and missing-seat feedback rendering coverage in src/app/app.spec.ts"
```

## Parallel Example: User Story 3

```text
# Launch the US3 test work together:
Task: "T020 Add in-session final-summary retention coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T021 Add final-summary persistence and receipt-style rendering coverage in src/app/app.spec.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup.
2. Complete Foundational work.
3. Complete User Story 1.
4. Validate the checkout flow independently before moving on.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 as the first working checkout flow.
3. Add US2 for trustworthy stale and missing-seat handling.
4. Add US3 so later receipt and history features can reuse the preserved final summary.
5. Finish with cross-cutting polish and quickstart validation.

### Parallel Team Strategy

1. One developer completes Setup and `T004`.
2. A second developer can take `T005` while a third takes `T006`.
3. After US1 lands, one developer can handle US2 while another starts US3.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- Keep the implementation aligned to the backend truth that checkout closes the seat's active `OPEN` order rather than a separate frontend session object.
