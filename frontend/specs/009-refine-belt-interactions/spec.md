# Feature Specification: Refine Belt Interactions

**Feature Branch**: `[009-refine-belt-interactions]`  
**Created**: 2026-03-23  
**Status**: Draft  
**Input**: User description: "After a user test, i got feedback for improvement:

- the top row seats cannot pick plates, their reach is not enough --> make reach-bubble slightly bigger so the plates can be picked
- it is not so clear if a seat is selected. make the reach-bubble more clear, maybe like having more light? do you have a good idea?
- the plates: the colored ring around is good, keep that one. the middle item --> pls remove it so only the colored ring is displayed
- implement the cogwheel to change the belt speed via modal window

ask questions if needed"

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Pick Plates From The Top Row Reliably (Priority: P1)

As a diner using a top-row seat, I want nearby plates to be reachable and pickable so that I can use the same dining flow as the other seats without feeling blocked by the layout.

**Why this priority**: The feedback identifies a direct usability failure in the core dining flow. If some seats cannot pick plates that appear close enough, the feature breaks trust in the main interaction model.

**Independent Test**: Can be fully tested by selecting each top-row seat, waiting for plates to enter the intended pickup zone, and confirming that those plates can be picked without changing any other dining behavior.

**Acceptance Scenarios**:

1. **Given** a diner selects a top-row seat, **When** a plate enters the intended pickup area for that seat, **Then** the interface allows the diner to pick that plate successfully.
2. **Given** a diner selects a top-row seat, **When** a plate remains outside that seat's allowed pickup area, **Then** the interface does not present it as pickable.
3. **Given** a diner switches between top-row and other seats, **When** pickup availability updates, **Then** each seat shows a reach area that matches its intended pickup capability without regressing other seats.

---

### User Story 2 - Understand Which Seat Is Active At A Glance (Priority: P1)

As a diner choosing a seat, I want the selected seat and its reach area to stand out clearly so that I can immediately tell which seat is active and which nearby plates belong to that choice.

**Why this priority**: Seat selection is the context for picking plates and checking out. If the selected state is ambiguous, users lose confidence in every action that depends on that context.

**Independent Test**: Can be fully tested by selecting, deselecting, and switching seats while observing whether first-time viewers can identify the active seat and its interaction zone from the stage lighting and overlap cues alone.

**Acceptance Scenarios**:

1. **Given** no seat is selected, **When** a diner selects a seat, **Then** the chosen seat becomes visually distinct enough that it is immediately recognizable as the active seat.
2. **Given** a seat is selected, **When** the diner looks at the surrounding stage, **Then** the reach area is visibly emphasized and clearly associated with that seat.
3. **Given** the diner selects a different seat, **When** the stage updates, **Then** the previous seat loses the active emphasis and the new seat gains it without ambiguity.

---

### User Story 3 - Read Plates With Less Visual Noise (Priority: P2)

As a diner watching the belt, I want plates to be visually simpler while keeping their colored ring so that the belt feels cleaner and the important plate cue remains easy to scan.

**Why this priority**: The current plate center treatment adds clutter without being valued in the feedback. Simplifying the plate appearance improves readability and polish without changing the underlying dining flow.

**Independent Test**: Can be fully tested by comparing the belt before and after the change and confirming that only the colored ring remains while plate identity cues stay understandable.

**Acceptance Scenarios**:

1. **Given** a diner views plates on the belt, **When** the updated plate styling is shown, **Then** each plate displays the colored ring without the inner item graphic.
2. **Given** a diner scans multiple plates in motion, **When** the simplified styling is applied, **Then** the belt feels less visually busy while the ring colors remain easy to distinguish.

---

### User Story 4 - Adjust Belt Speed From The Main Interface (Priority: P2)

As a diner or operator using the belt view, I want to change the belt speed from the cogwheel control in a modal window so that I can adjust the pace without leaving the main experience.

**Why this priority**: Speed control is a new user-facing capability that improves control over the experience, but it is secondary to fixing the broken pick and selection clarity issues.

**Independent Test**: Can be fully tested by opening the cogwheel control, reviewing the current speed, changing it to another allowed value, and confirming that the updated speed is applied after confirmation.

**Acceptance Scenarios**:

1. **Given** the diner is on the main belt screen, **When** the cogwheel control is activated, **Then** a modal window opens with the current belt speed and available speed choices.
2. **Given** the modal window is open, **When** the diner confirms a different speed, **Then** the belt adopts the chosen speed and the interface reflects the new setting.
3. **Given** the modal window is open, **When** the diner cancels or closes it, **Then** the belt speed remains unchanged.

### Edge Cases

- A top-row seat receives a larger reach area, but neighboring seats must not appear to own the same plate at the same time in a confusing way.
- A diner switches seats quickly while plates are moving, and the active-seat emphasis must update cleanly without leaving stale highlights behind.
- The simplified plate appearance removes inner artwork, but the remaining ring colors must still be distinguishable in motion and against the belt background.
- The speed control modal opens while no seat is selected and must still work independently of seat-based actions.
- The speed control modal is dismissed without confirmation and must not change the belt speed.
- A diner reopens the speed modal while the current speed is already selected, and the dialog must make it clear that no new update is ready to apply.
- If belt speed cannot be changed at that moment, the interface must explain that clearly and keep the previous speed intact.

## Assumptions

- The seat-selection, plate-picking, and checkout flows remain the same apart from the requested clarity and reach adjustments.
- Top-row reach should be expanded only enough to make intended nearby plates pickable, not to broaden seat ownership beyond the expected dining area.
- The colored ring remains the primary visual cue for plate identity after the inner plate artwork is removed.
- The belt speed feature uses the existing product concept of allowed speed settings rather than introducing arbitrary free-form values.
- The cogwheel control is available from the main belt experience and does not require navigating to a separate page.

## Non-Goals

- Redesigning the full seat layout or changing the overall dining workflow.
- Introducing new plate categories, new seat actions, or new operator dashboards.
- Changing the meaning of the plate ring colors.
- Turning the speed adjustment into a persistent settings page or a multi-step configuration workflow.

## UX Goals

- Make active seat selection unmistakable with stronger visual emphasis.
- Preserve the charm of the reach bubble while making it clearer, brighter, and easier to associate with the selected seat.
- Reduce plate noise so the belt reads more cleanly in motion.
- Keep the speed adjustment flow lightweight and obvious from the main screen.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST allow intended nearby plates to be picked from top-row seats that are currently unable to reach them.
- **FR-002**: The system MUST keep plate pickup unavailable for plates outside a selected seat's intended pickup area.
- **FR-003**: The system MUST present the selected seat with a stronger visual emphasis than non-selected seats.
- **FR-004**: The system MUST make the selected seat's reach area visually prominent enough that users can associate it with the active seat at a glance.
- **FR-005**: The system MUST update active-seat emphasis immediately when the diner changes the selected seat.
- **FR-006**: The system MUST preserve the colored plate ring as the visible plate styling cue.
- **FR-007**: The system MUST remove the current inner plate item graphic from the default moving plate presentation.
- **FR-008**: The system MUST keep plate colors distinguishable after the inner plate item graphic is removed.
- **FR-009**: The system MUST provide a cogwheel-triggered modal for belt speed adjustment from the main belt experience.
- **FR-010**: The system MUST show the current belt speed within the speed adjustment flow.
- **FR-011**: Users MUST be able to choose a different allowed belt speed and confirm the change from the modal.
- **FR-012**: Users MUST be able to dismiss the speed adjustment modal without changing the current speed.
- **FR-013**: The system MUST provide clear feedback after a speed change succeeds or fails, and MUST keep the apply action inactive when no change is pending.
- **FR-014**: The system MUST preserve existing dining actions and must not make seat selection, plate picking, or checkout less understandable than before.
- **FR-015**: The system MUST keep the updated visual cues understandable on both desktop and smaller-screen layouts.

### Key Entities _(include if feature involves data)_

- **Seat Selection State**: The currently active seat and the visual cues that indicate its ownership of nearby interactions.
- **Seat Reach Zone**: The visible pickup area associated with a selected seat that determines which nearby plates are presented as reachable.
- **Plate Ring Marker**: The simplified plate appearance that uses only the colored ring as the primary visual identifier.
- **Belt Speed Setting**: The currently applied belt pace and the set of allowed user-selectable speed options.
- **Speed Control Modal**: The temporary dialog used to review and change the belt speed without leaving the main screen.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In manual validation, 100% of top-row seats can successfully pick plates that enter their intended pickup area.
- **SC-002**: In first-look usability checks, at least 90% of participants correctly identify the selected seat within 3 seconds of the state appearing.
- **SC-003**: In first-look usability checks, at least 90% of participants correctly identify which nearby area belongs to the selected seat without verbal guidance.
- **SC-004**: In visual review, 100% of default moving plates display only the colored ring and no inner item graphic.
- **SC-005**: In comparison review, at least 80% of evaluators report that the simplified plate appearance feels clearer or equally clear compared with the prior version.
- **SC-006**: In task validation, at least 90% of participants can open the cogwheel control and change the belt speed to another allowed value in under 15 seconds.
- **SC-007**: In manual validation, canceling or closing the speed adjustment modal leaves the belt speed unchanged in 100% of test cases.
- **SC-008**: In regression review, existing seat selection, plate picking, and checkout tasks remain fully completable after the change set.
