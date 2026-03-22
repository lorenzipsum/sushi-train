# Tasks: Add Plates To Belt

**Input**: Design documents from `/specs/007-add-plates-belt/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/add-plates-to-belt-ui-contract.md, quickstart.md

**Tests**: Include API-wrapper, store, operator-surface, and app-shell tests because `quickstart.md` explicitly requires automated verification for menu loading, search, overrides, failure handling, and refresh-after-success behavior.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently after the shared operator-flow foundation is complete.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align the existing API and typing surface with the operator plate-placement contract before UI or store behavior is added.

- [x] T001 Align operator placement request, response, menu-item, and notice aliases in `src/app/api/types.ts`
- [x] T002 [P] Create paginated menu retrieval wrapper in `src/app/api/menu-items.api.ts`
- [x] T003 [P] Extend belt placement API support in `src/app/api/belts.api.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared operator-placement state and shell plumbing required by all user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 Create shared operator placement state, menu loading lifecycle, baseline menu-item selection state, draft helpers, duplicate-submission lockout, and notice lifecycle in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T005 [P] Create the compact operator surface component in `src/app/belt-visualization/operator-plate-placement.component.ts`, `src/app/belt-visualization/operator-plate-placement.component.html`, and `src/app/belt-visualization/operator-plate-placement.component.css`
- [x] T006 [P] Extend kitchen-anchor and fallback-presentation stage plumbing in `src/app/belt-visualization/belt-view-model.ts`, `src/app/belt-visualization/belt-stage.component.ts`, `src/app/belt-visualization/belt-stage.html`, and `src/app/belt-visualization/belt-stage.css`
- [x] T007 [P] Wire the operator surface through `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`

**Checkpoint**: The app can represent the operator workflow shell, preloaded menu state, and shared placement state consistently before story-specific behavior is completed.

---

## Phase 3: User Story 1 - Place Plates Quickly From The Belt Scene (Priority: P1) 🎯 MVP

**Goal**: Let the operator open the kitchen-area flow, place default plates onto the visible primary belt, and see immediate success without disturbing guest seat context.

**Independent Test**: Open the operator flow from the belt scene, choose a menu item, accept the default values, submit once, and verify that a success notice appears, the belt refreshes immediately, and the selected guest seat detail remains intact.

### Tests for User Story 1

- [x] T008 [P] [US1] Add default-placement success, pending-submit state, and duplicate-submission lockout coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts` and `src/app/belt-visualization/operator-plate-placement.component.spec.ts`
- [x] T009 [P] [US1] Add kitchen-entry rendering and guest-context preservation coverage in `src/app/app.spec.ts`

### Implementation for User Story 1

- [x] T010 [US1] Implement operator flow open or close behavior and default draft initialization in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T011 [US1] Implement successful plate-placement submission, immediate authoritative refresh, and duplicate-submission release in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T012 [US1] Render the default menu-item placement action, success notice, and guest-context-preserving operator surface in `src/app/belt-visualization/operator-plate-placement.component.ts`, `src/app/belt-visualization/operator-plate-placement.component.html`, `src/app/belt-visualization/operator-plate-placement.component.css`, and `src/app/app.html`

**Checkpoint**: User Story 1 should let an operator add a default plate from the belt scene and see the result immediately without disrupting guest dining context.

---

## Phase 4: User Story 2 - Search The Menu And Override Placement Details (Priority: P2)

**Goal**: Let the operator search the full menu in limited space and submit valid manual overrides for placement details.

**Independent Test**: Open the operator flow, narrow the menu list with search text, choose a menu item from the compact results, change one or more placement values, and verify that the submitted placement reflects the chosen overrides.

### Tests for User Story 2

- [x] T013 [P] [US2] Add menu loading, search filtering, and compact result-list coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts` and `src/app/belt-visualization/operator-plate-placement.component.spec.ts`
- [x] T014 [P] [US2] Add valid override submission coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts` and `src/app/app.spec.ts`

### Implementation for User Story 2

- [x] T015 [US2] Implement client-side search narrowing over the full menu list established during foundational work in `src/app/belt-visualization/belt-visualization.store.ts` and `src/app/belt-visualization/operator-plate-placement.component.ts`
- [x] T016 [US2] Implement override draft fields, validation state, and compact selection results in `src/app/belt-visualization/operator-plate-placement.component.ts`, `src/app/belt-visualization/operator-plate-placement.component.html`, and `src/app/belt-visualization/operator-plate-placement.component.css`
- [x] T017 [US2] Implement selected-item defaults, manual override submission mapping, and layout-safe fallback presentation in `src/app/belt-visualization/belt-visualization.store.ts`, `src/app/belt-visualization/belt-stage.html`, `src/app/belt-visualization/belt-stage.css`, and `src/app/app.css`

**Checkpoint**: User Story 2 should support faster full-menu discovery and valid override submission without expanding the app into a larger admin surface.

---

## Phase 5: User Story 3 - Recover Cleanly From Placement Failures (Priority: P3)

**Goal**: Show clear corrective failure notices, keep retry state intact, and preserve guest context when placement fails.

**Independent Test**: Attempt placement with insufficient free slots, invalid menu item or values, and a missing-belt or generic failure, then verify that each outcome shows clear guidance while leaving the selected guest seat experience unchanged.

### Tests for User Story 3

- [x] T018 [P] [US3] Add known backend failure normalization coverage in `src/app/belt-visualization/belt-visualization.store.spec.ts`
- [x] T019 [P] [US3] Add retry-friendly failure rendering and guest-context stability coverage in `src/app/app.spec.ts` and `src/app/belt-visualization/operator-plate-placement.component.spec.ts`

### Implementation for User Story 3

- [x] T020 [US3] Implement normalized placement failure handling for capacity, invalid menu item, invalid values, missing belt, malformed request, and generic outcomes in `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T021 [US3] Preserve the placement draft across corrective failures and keep selected-seat guest context stable in `src/app/belt-visualization/belt-visualization.store.ts` and `src/app/app.html`
- [x] T022 [US3] Render corrective error notices, retry affordances, and non-destructive failure states in `src/app/belt-visualization/operator-plate-placement.component.ts`, `src/app/belt-visualization/operator-plate-placement.component.html`, and `src/app/belt-visualization/operator-plate-placement.component.css`

**Checkpoint**: User Story 3 should let operators recover from known failures without confusing or displacing the guest-facing dining flow.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final refinement and full validation across all stories.

- [x] T023 [P] Refine operator accessibility labels, keyboard flow, kitchen-themed copy, and fallback-surface polish in `src/app/belt-visualization/operator-plate-placement.component.html`, `src/app/belt-visualization/operator-plate-placement.component.css`, `src/app/belt-visualization/belt-stage.html`, and `src/app/app.html`
- [x] T024 Run quickstart validation steps, `npm test`, and `npm run build`, then record feature-specific validation notes in `specs/007-add-plates-belt/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2 and delivers the MVP default-placement flow.
- **User Story 2 (Phase 4)**: Depends on Phase 2 and extends the shared operator flow with menu search and overrides.
- **User Story 3 (Phase 5)**: Depends on Phase 2 and extends the shared operator flow with failure recovery.
- **Polish (Phase 6)**: Depends on the stories you choose to complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: No dependency on other user stories after foundational work because baseline menu loading and menu-item selection are established during foundational work.
- **US3**: No dependency on other user stories after foundational work.

### Within Each User Story

- Write the listed tests first and confirm they fail before implementation.
- Finalize store behavior before locking UI copy and visual polish.
- Keep guest-context preservation aligned with placement submission and failure handling before considering a story complete.

### Parallel Opportunities

- `T002` and `T003` can run together in Phase 1.
- `T005`, `T006`, and `T007` can run together once `T004` defines the shared operator state shape.
- `T008` and `T009` can run together for US1.
- `T013` and `T014` can run together for US2.
- `T018` and `T019` can run together for US3.
- `T023` can proceed while `T024` is being validated manually.

---

## Parallel Example: User Story 1

```bash
# Launch the US1 test work together:
Task: "T008 Add default-placement success, pending-submit state, and duplicate-submission lockout coverage in src/app/belt-visualization/belt-visualization.store.spec.ts and src/app/belt-visualization/operator-plate-placement.component.spec.ts"
Task: "T009 Add kitchen-entry rendering and guest-context preservation coverage in src/app/app.spec.ts"
```

## Parallel Example: User Story 2

```bash
# Launch the US2 test work together:
Task: "T013 Add menu loading, search filtering, and compact result-list coverage in src/app/belt-visualization/belt-visualization.store.spec.ts and src/app/belt-visualization/operator-plate-placement.component.spec.ts"
Task: "T014 Add valid override submission coverage in src/app/belt-visualization/belt-visualization.store.spec.ts and src/app/app.spec.ts"
```

## Parallel Example: User Story 3

```bash
# Launch the US3 test work together:
Task: "T018 Add known backend failure normalization coverage in src/app/belt-visualization/belt-visualization.store.spec.ts"
Task: "T019 Add retry-friendly failure rendering and guest-context stability coverage in src/app/app.spec.ts and src/app/belt-visualization/operator-plate-placement.component.spec.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate the default placement flow independently before moving on.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 so operators can place default plates from the main belt scene.
3. Add US2 so operators can find any menu item and submit valid overrides.
4. Add US3 so failures become corrective and retry-friendly without disturbing guests.
5. Finish with cross-cutting polish and quickstart validation.

### Parallel Team Strategy

1. One developer completes Phase 1 and `T004`.
2. Additional developers can split `T005`, `T006`, and `T007` once the shared operator state shape is set.
3. After foundational work lands, one developer can take US1 while others start US2 and US3 if team capacity allows.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- The task count is 24 total: 7 shared tasks, 5 tasks for US1, 5 tasks for US2, 5 tasks for US3, and 2 polish tasks.
- Baseline menu loading and selection are established before US2 so User Story 1 remains independently testable.
- The primary user-story execution order is US1 first for MVP value, then US2 and US3 in either priority order or parallel after foundational work.
