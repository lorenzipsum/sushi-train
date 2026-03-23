# Tasks: Design Polish

**Input**: Design documents from `/specs/008-design-polish/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/design-polish-ui-contract.md, quickstart.md

**Tests**: Include targeted automated tests for shell-state rendering, presentation metadata, and surface clarity because the redesign changes meaningful rendered behavior. Finish with quickstart validation plus `npm test` and `npm run build` after implementation.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently after the shared presentation foundation is complete.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the shared visual primitives and root-shell styling baseline that every story builds on.

- [x] T001 Define shared playful-cafe design tokens, typography imports, and global motion variables in `src/styles.css`
- [x] T002 Align root shell CSS custom properties, shared surface utilities, and page-level spacing foundations in `src/app/app.css`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add the shared presentation metadata, shell wiring, and stage hooks required by all user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 Create page-shell presentation state, hero copy hooks, and state-card tone plumbing in `src/app/app.ts`, `src/app/app.html`, and `src/app/app.css`
- [x] T004 [P] Extend stage presentation metadata and readability guardrails in `src/app/belt-visualization/belt-view-model.ts` and `src/app/belt-visualization/belt-layout.ts`
- [x] T005 [P] Add shared stage structure hooks for decorative framing, kitchen personality, and readability-safe overlays in `src/app/belt-visualization/belt-stage.component.ts` and `src/app/belt-visualization/belt-stage.html`
- [x] T006 [P] Add shared presentation tone and secondary-copy support for seat-detail and operator surfaces in `src/app/belt-visualization/belt-visualization.store.ts`, `src/app/belt-visualization/selected-seat-detail.component.ts`, and `src/app/belt-visualization/operator-plate-placement.component.ts`
- [x] T007 [P] Establish shared responsive density and reduced-ornament styling across `src/app/belt-visualization/belt-stage.css`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/app/belt-visualization/operator-plate-placement.component.css`

**Checkpoint**: The app can expose a consistent presentation model, shell structure, and stage-safe styling foundation before any story-specific polish begins.

---

## Phase 3: User Story 1 - Enjoy A More Delightful Sushi Experience (Priority: P1) 🎯 MVP

**Goal**: Make the overall experience feel like a playful kaiten-zushi cafe with a stronger visual identity while preserving the existing workflows.

**Independent Test**: Open the main belt experience and confirm the shell, hero, stage, and supporting panels feel more playful, warm, and authored without changing what users can do.

### Implementation for User Story 1

- [x] T008A [P] [US1] Add app-shell rendering coverage for loading, empty, degraded, and fatal presentation states in `src/app/app.spec.ts`
- [x] T008 [P] [US1] Refresh the hero, loading, empty, degraded, and fatal shell composition in `src/app/app.html`
- [x] T009 [US1] Implement the new page-shell atmosphere, state-card styling, and typographic hierarchy in `src/app/app.css` and `src/styles.css`
- [x] T010 [P] [US1] Redesign the belt-stage decorative framing, kitchen-center presentation, and stage ornament layout in `src/app/belt-visualization/belt-stage.html` and `src/app/belt-visualization/belt-stage.css`
- [x] T011 [P] [US1] Refine dish-family accent mapping and visual identity hooks in `src/app/belt-visualization/menu-item-visuals.ts`
- [x] T012 [US1] Integrate the refreshed shell and stage presentation for desktop and mobile balance in `src/app/app.css`, `src/app/app.html`, and `src/app/belt-visualization/belt-stage.css`

**Checkpoint**: User Story 1 should deliver a clearly more charming and visually cohesive sushi-cafe experience without any workflow change.

---

## Phase 4: User Story 2 - Keep Dining Actions Clear While Styling Changes (Priority: P2)

**Goal**: Preserve or improve seat, plate, slot, and action clarity while the presentation becomes more expressive.

**Independent Test**: Run through seat selection, plate-picking, checkout, and operator access flows and confirm that states, action targets, and feedback remain as clear as or clearer than before.

### Implementation for User Story 2

- [x] T013A [P] [US2] Add stage and view-model coverage for primary labels, secondary labels, and readability-safe presentation metadata in `src/app/belt-visualization/belt-view-model.spec.ts` and `src/app/belt-visualization/belt-layout.spec.ts`
- [x] T013B [P] [US2] Add selected-seat-detail and operator-surface rendering coverage for action clarity and feedback hierarchy in `src/app/app.spec.ts`
- [x] T013 [US2] Extend primary-label, secondary-label, and state-clarity presentation data for shell and stage surfaces in `src/app/belt-visualization/belt-view-model.ts` and `src/app/belt-visualization/belt-visualization.store.ts`
- [x] T014 [P] [US2] Implement readability-safe seat, slot, reach-area, and action-target refinements in `src/app/belt-visualization/belt-stage.html`, `src/app/belt-visualization/belt-stage.css`, and `src/app/belt-visualization/belt-layout.ts`, keeping current proportions unless before-and-after validation shows the stretched layout is at least as clear
- [x] T015 [P] [US2] Refresh selected-seat detail hierarchy, state cues, and action emphasis in `src/app/belt-visualization/selected-seat-detail.component.html` and `src/app/belt-visualization/selected-seat-detail.component.css`
- [x] T016 [P] [US2] Refresh operator-surface hierarchy, notice emphasis, and field readability in `src/app/belt-visualization/operator-plate-placement.component.html` and `src/app/belt-visualization/operator-plate-placement.component.css`
- [x] T017 [US2] Align app-shell status messaging, mobile density trimming, and reduced-ornament behavior in `src/app/app.html`, `src/app/app.css`, and `src/app/belt-visualization/belt-stage.css`

**Checkpoint**: User Story 2 should keep the redesigned interface trustworthy and easy to scan across the existing dining and operator flows.

---

## Phase 5: User Story 3 - Smile At Tasteful Product Humor (Priority: P3)

**Goal**: Add controlled, funny, and memorable personality through secondary copy and decorative cues without undermining clarity.

**Independent Test**: Review empty, loading, success, retry, and decorative moments and confirm the humor feels intentional, low-frequency, and secondary to the literal state communication.

### Implementation for User Story 3

- [x] T018 [P] [US3] Add playful shell copy, section framing, and low-frequency decorative humor to `src/app/app.html` and `src/app/app.css`
- [x] T019 [P] [US3] Add kitchen-character cues, decorative jokes, and optional stage-level secondary labels in `src/app/belt-visualization/belt-stage.html`, `src/app/belt-visualization/belt-stage.css`, and `src/app/belt-visualization/belt-view-model.ts`
- [x] T020 [P] [US3] Layer deadpan helper text and supportive secondary labels into `src/app/belt-visualization/selected-seat-detail.component.html` and `src/app/belt-visualization/operator-plate-placement.component.html`
- [x] T021 [US3] Map plain primary notices and playful secondary messaging for feedback surfaces in `src/app/belt-visualization/belt-visualization.store.ts`, `src/app/app.html`, and `src/app/belt-visualization/operator-plate-placement.component.html`

**Checkpoint**: User Story 3 should make the product feel funny and memorable while keeping every important state plainly understandable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final refinement and full validation across all stories.

- [x] T022 [P] Refine responsive ornament trimming, accessibility labels, and reduced-motion polish across `src/app/app.css`, `src/app/belt-visualization/belt-stage.css`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/app/belt-visualization/operator-plate-placement.component.css`
- [x] T023 [P] Run stakeholder design review for visual identity and humor appropriateness, then record outcomes in `specs/008-design-polish/quickstart.md`
- [x] T024 [P] Run readability and usability validation for seat, slot, plate, and action clarity across desktop and mobile in `specs/008-design-polish/quickstart.md`
- [x] T025 [P] Run content review for plain primary labels versus playful secondary copy across shell, stage, seat-detail, and operator surfaces in `specs/008-design-polish/quickstart.md`
- [x] T026 Run quickstart validation steps, `npm test`, and `npm run build`, then record feature-specific validation notes in `specs/008-design-polish/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2 and delivers the MVP visual identity refresh.
- **User Story 2 (Phase 4)**: Depends on Phase 2 and strengthens readability and action clarity.
- **User Story 3 (Phase 5)**: Depends on Phase 2 and layers tasteful humor onto the redesigned surfaces.
- **Polish (Phase 6)**: Depends on the stories you choose to complete.

### User Story Dependencies

- **US1**: No dependency on other user stories after foundational work.
- **US2**: No dependency on other user stories after foundational work.
- **US3**: No dependency on other user stories after foundational work.

### Within Each User Story

- Finalize the shared data or metadata shape before locking story-specific copy or styling.
- Preserve literal primary labels before introducing playful secondary language.
- Validate mobile and reduced-motion readability before considering a story complete.

### Parallel Opportunities

- `T004`, `T005`, `T006`, and `T007` can run in parallel once `T003` establishes the shared shell presentation shape.
- `T008A`, `T008`, `T010`, and `T011` can run in parallel for US1.
- `T013A` and `T013B` can run in parallel for US2 before implementation work starts.
- `T014`, `T015`, and `T016` can run in parallel for US2 after `T013` lands.
- `T018`, `T019`, and `T020` can run in parallel for US3.
- `T022`, `T023`, `T024`, and `T025` can run in parallel while `T026` is being prepared.

---

## Parallel Example: User Story 1

```bash
# Launch the US1 visual work together:
Task: "T008A Add app-shell rendering coverage for loading, empty, degraded, and fatal presentation states in src/app/app.spec.ts"
Task: "T008 Refresh the hero, loading, empty, degraded, and fatal shell composition in src/app/app.html"
Task: "T010 Redesign the belt-stage decorative framing, kitchen-center presentation, and stage ornament layout in src/app/belt-visualization/belt-stage.html and src/app/belt-visualization/belt-stage.css"
Task: "T011 Refine dish-family accent mapping and visual identity hooks in src/app/belt-visualization/menu-item-visuals.ts"
```

## Parallel Example: User Story 2

```bash
# Launch the US2 clarity work together:
Task: "T013A Add stage and view-model coverage for primary labels, secondary labels, and readability-safe presentation metadata in src/app/belt-visualization/belt-view-model.spec.ts and src/app/belt-visualization/belt-layout.spec.ts"
Task: "T013B Add selected-seat-detail and operator-surface rendering coverage for action clarity and feedback hierarchy in src/app/app.spec.ts"
Task: "T014 Implement readability-safe seat, slot, reach-area, and action-target refinements in src/app/belt-visualization/belt-stage.html, src/app/belt-visualization/belt-stage.css, and src/app/belt-visualization/belt-layout.ts"
Task: "T015 Refresh selected-seat detail hierarchy, state cues, and action emphasis in src/app/belt-visualization/selected-seat-detail.component.html and src/app/belt-visualization/selected-seat-detail.component.css"
Task: "T016 Refresh operator-surface hierarchy, notice emphasis, and field readability in src/app/belt-visualization/operator-plate-placement.component.html and src/app/belt-visualization/operator-plate-placement.component.css"
```

## Parallel Example: User Story 3

```bash
# Launch the US3 humor-layer work together:
Task: "T018 Add playful shell copy, section framing, and low-frequency decorative humor to src/app/app.html and src/app/app.css"
Task: "T019 Add kitchen-character cues, decorative jokes, and optional stage-level secondary labels in src/app/belt-visualization/belt-stage.html, src/app/belt-visualization/belt-stage.css, and src/app/belt-visualization/belt-view-model.ts"
Task: "T020 Layer deadpan helper text and supportive secondary labels into src/app/belt-visualization/selected-seat-detail.component.html and src/app/belt-visualization/operator-plate-placement.component.html"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Validate that the app feels materially more playful and cohesive before moving on.

### Incremental Delivery

1. Finish Setup and Foundational work.
2. Deliver US1 so the product gains a strong visual identity refresh.
3. Add US2 so the more expressive UI stays trustworthy and easy to scan.
4. Add US3 so the product gains controlled humor and memorable personality.
5. Finish with cross-cutting polish and quickstart validation.

### Parallel Team Strategy

1. One developer completes Phase 1 and `T003`.
2. Additional developers can split `T004`, `T005`, `T006`, and `T007` once the shell presentation shape is set.
3. After foundational work lands, one developer can take US1 while others prepare US2 and US3 work if team capacity allows.

---

## Notes

- All tasks follow the required checklist format with task ID, optional parallel marker, optional story label, and explicit file paths.
- The MVP scope is Phase 3 / US1.
- The task count is 29 total: 7 shared tasks, 6 tasks for US1, 7 tasks for US2, 4 tasks for US3, and 5 polish tasks.
- The primary story order is US1 first for visible value, then US2 for trust and clarity, then US3 for humor layering.
