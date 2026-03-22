# Feature Specification: Add Plates To Belt

**Feature Branch**: `[007-add-plates-belt]`  
**Created**: 2026-03-22  
**Status**: Done  
**Input**: User description: "Add a demo-mode operator flow to the existing sushi-belt UI that lets an operator place new plates onto the primary belt from the same application, without disturbing the current guest-facing dining experience."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Place Plates Quickly From The Belt Scene (Priority: P1)

As a demo-mode operator, I can add a new plate to the primary belt from the same sushi-belt experience so I can keep the demo moving without leaving the guest-facing scene.

**Why this priority**: This is the core feature outcome. If operators cannot place plates from the main experience, the feature does not deliver its intended demo value.

**Independent Test**: Can be fully tested by opening the operator placement flow from the belt scene, choosing a menu item, accepting the default values, submitting once, and verifying that success feedback appears and the belt view refreshes while the selected guest seat context remains intact.

**Acceptance Scenarios**:

1. **Given** the operator is viewing the primary belt scene, **When** the operator opens the add-plate control near the kitchen or chef area, chooses a menu item, and confirms with default values, **Then** the system places one new plate onto the currently shown primary belt, shows a positive operator-facing success notice, and refreshes the visible belt state promptly.
2. **Given** the inline control would crowd or noticeably damage the existing belt presentation, **When** the operator starts the add-plate flow, **Then** the system uses a compact secondary surface for the operator interaction while keeping the belt, kitchen, chef, and guest seat presentation visually intact.
3. **Given** a guest seat is currently selected, **When** the operator successfully adds plates to the belt, **Then** the selected-seat detail area remains focused on that guest seat rather than switching into operator mode or losing the guest context.

---

### User Story 2 - Search The Menu And Override Placement Details (Priority: P2)

As a demo-mode operator, I can search the available menu items and adjust placement details before submitting so I can add the right plate configuration even when the operator surface has limited space.

**Why this priority**: The feature must work for the full menu, not only a small preset list, and operators need to balance speed with the ability to make deliberate overrides.

**Independent Test**: Can be fully tested by opening the operator flow, narrowing the menu list with search text, selecting an item from the compact results, changing one or more placement values, and confirming that the submitted placement reflects the chosen overrides.

**Acceptance Scenarios**:

1. **Given** the menu contains more items than can fit comfortably in the operator surface at once, **When** the operator types into the menu-item search field, **Then** the visible result list narrows to matching menu items while still allowing the operator to reach any available item.
2. **Given** the operator selects a menu item, **When** the placement draft is shown, **Then** the system prepopulates sensible default values for the number of plates, tier snapshot, price snapshot, and expiration time while still allowing the operator to change them before submitting.
3. **Given** the operator changes the number of plates, tier snapshot, price snapshot, or expiration time to a valid override, **When** the operator submits the placement, **Then** the system uses the chosen override values instead of the defaults.

---

### User Story 3 - Recover Cleanly From Placement Failures (Priority: P3)

As a demo-mode operator, I can understand why plate placement failed and retry without harming the guest-facing dining flow so the demo remains credible even when backend state changes.

**Why this priority**: The feature is operator-facing, but it runs inside the guest experience. Clear failure handling is required so operators can recover quickly without creating confusing or misleading guest UI changes.

**Independent Test**: Can be fully tested by attempting placement when there are not enough free slots, when the chosen menu item or values are invalid, and when the belt is missing or another generic failure occurs, then verifying that each outcome produces clear feedback and preserves the current guest seat experience.

**Acceptance Scenarios**:

1. **Given** the operator requests more plates than the belt can currently accept, **When** the placement fails, **Then** the system explains that there is not enough free space and tells the operator to reduce the number of plates.
2. **Given** the chosen menu item is no longer valid or one of the entered values is invalid, **When** the operator submits the placement, **Then** the system shows a clear corrective failure notice and keeps the operator flow available for adjustment and retry.
3. **Given** the currently shown belt cannot accept the placement because it is missing or another unexpected failure occurs, **When** the operator submits the flow, **Then** the system shows a clear failure notice, avoids showing misleading success, and leaves the guest seat selection and dining context unchanged.

### Edge Cases

- The operator surface has limited space, but the menu is large enough that the operator still needs a reliable way to find any menu item.
- The operator accepts the default single-plate placement for an item whose default tier or price snapshot differs from other items on the menu.
- The operator enters the maximum allowed plate count and the backend may relax spacing internally, but the frontend still treats the outcome as either a full success or a failure rather than a partial-placement result.
- The operator enters a plate count below the allowed minimum or above the allowed maximum.
- The operator enters a negative price snapshot or an expiration time that is already in the past.
- The chosen menu item disappears or becomes invalid between the moment it is displayed and the moment the operator submits the placement.
- The currently shown belt becomes unavailable while the operator surface is still open.
- The backend uses best-effort spacing and places plates without exposing any separate spacing-specific failure reason.

## Assumptions

- The operator flow is available inside the same application that guests see, with no separate admin app for this feature.
- The feature applies only to the primary belt currently shown on screen.
- The kitchen and chef area remain the visual center of the page even after operator controls are introduced.
- The default placement uses one plate unless the operator chooses a different valid amount.
- The default expiration is two hours from the moment the operator submits the placement unless the operator chooses a different future time.
- The default tier snapshot and price snapshot come from the selected menu item unless the operator chooses overrides.
- The selected-seat detail area remains the guest-focused surface and does not become the primary operator workspace.
- After a successful placement, the visible belt state refreshes immediately and then continues under the normal ongoing automatic update behavior.
- Plate spacing remains backend-managed best effort, so the frontend does not model a separate spacing-specific failure state.

## Non-Goals

- Creating a separate admin dashboard or standalone operator application.
- Expanding this feature to belts other than the primary belt currently shown on screen.
- Turning the selected-seat detail area into the main plate-placement workflow.
- Redesigning the guest belt layout, guest seat-selection model, or guest dining flow as part of operator controls.
- Introducing a separate frontend error state for minimum-gap placement failure.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST provide a demo-mode operator flow for placing new plates within the same sushi-belt application that guests use.
- **FR-002**: The system MUST present the operator entry point inside or immediately adjacent to the belt scene near the kitchen or chef area.
- **FR-003**: If placing the controls directly in that area would noticeably harm the existing layout or visual design, the system MUST present the operator flow in a compact secondary surface instead.
- **FR-004**: The system MUST preserve the current guest-facing belt layout and keep the kitchen and chef area visually central while the operator flow is available.
- **FR-005**: The system MUST keep the selected-seat detail area focused on guest seat context rather than repurposing it as the main operator surface.
- **FR-006**: The system MUST target plate placement only to the primary belt that is currently shown on screen.
- **FR-007**: The system MUST require the operator to choose a menu item before plate placement can be submitted.
- **FR-008**: The system MUST allow the operator to choose from the full available menu-item list within the operator flow.
- **FR-009**: The system MUST provide text search that narrows the visible menu-item results within the operator flow.
- **FR-010**: The system MUST keep the menu-item results usable within a limited-space operator surface.
- **FR-011**: The system MUST prepopulate a new placement draft with sensible defaults once a menu item is chosen.
- **FR-012**: The system MUST default the number of plates to 1.
- **FR-013**: The system MUST default the expiration time to two hours after placement.
- **FR-014**: The system MUST default the tier snapshot and price snapshot to the selected menu item's current defaults.
- **FR-015**: The system MUST allow the operator to override the number of plates, tier snapshot, price snapshot, and expiration time before submission.
- **FR-016**: The system MUST allow placement to succeed without requiring any manual overrides when the defaults are acceptable.
- **FR-017**: The system MUST accept only plate counts from 1 through 10.
- **FR-018**: The system MUST accept only price snapshot values that are zero or greater.
- **FR-019**: The system MUST accept only expiration times that are in the future.
- **FR-020**: The system MUST present a clear pending state while a placement is being submitted and MUST prevent duplicate submissions from the same operator action.
- **FR-021**: On successful placement, the system MUST show a positive operator-facing notice that fits the kitchen or chef presentation.
- **FR-022**: On successful placement, the system MUST refresh the visible belt state immediately and then continue under the normal ongoing automatic update behavior.
- **FR-023**: On successful placement, the system MUST preserve the current guest seat selection and guest dining context shown on screen.
- **FR-024**: If placement fails because there is not enough free capacity, the system MUST explain that there is not enough free space and MUST instruct the operator to reduce the number of plates.
- **FR-025**: If placement fails because the chosen menu item is invalid or no longer available, the system MUST show a clear corrective failure notice.
- **FR-026**: If placement fails because one or more entered values are invalid, the system MUST identify the submission as invalid and keep the operator flow available for correction and retry.
- **FR-027**: If placement fails because the currently shown belt is missing or unavailable, the system MUST show a clear failure notice and MUST NOT imply that placement succeeded.
- **FR-028**: If placement fails because the submitted request is malformed or another generic failure occurs, the system MUST show a clear failure notice that the operator can understand without exposing internal implementation detail.
- **FR-029**: After a failed placement, the system MUST keep the guest-facing belt and selected-seat experience stable rather than switching the page into an operator-focused state.
- **FR-030**: After a failed placement that the operator can correct, the system MUST preserve the current placement draft closely enough that the operator can adjust and retry without rebuilding the entire flow.
- **FR-031**: The system MUST treat plate spacing as backend-managed best effort and MUST NOT introduce a separate frontend-only failure state for spacing rules.
- **FR-032**: The feature MUST remain a compact demo-mode control within the existing experience and MUST NOT turn the page into an admin dashboard.

### Key Entities _(include if feature involves data)_

- **Operator Placement Surface**: The compact operator entry point and interaction area used to start, review, and submit a plate-placement action from within the belt scene.
- **Menu Item Choice**: The selected menu item the operator wants to place on the belt, found from the full available list and optionally narrowed through search.
- **Placement Draft**: The operator-visible set of placement values, including chosen menu item, number of plates, tier snapshot, price snapshot, and expiration time.
- **Placement Outcome Notice**: The success or failure message shown to the operator after submission, including any retry guidance.
- **Primary Belt View State**: The currently visible guest-facing belt scene that must refresh after success and remain stable during failure handling.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In manual validation, an operator can place a default single plate onto the visible primary belt in 30 seconds or less from opening the operator flow.
- **SC-002**: In acceptance testing, 100% of successful default placements show a positive operator-facing notice and a visibly refreshed belt state without disrupting the selected guest seat context.
- **SC-003**: In acceptance testing, 100% of successful override placements reflect the operator's chosen valid values rather than silently reverting to defaults.
- **SC-004**: In acceptance testing, 100% of not-enough-space failures tell the operator to reduce the number of plates.
- **SC-005**: In acceptance testing, 100% of invalid menu-item, invalid-value, missing-belt, malformed-request, and generic failure cases produce a clear failure notice without showing misleading success.
- **SC-006**: In manual validation, operators can narrow a large menu list to the intended menu item within three search or selection interactions after opening the picker.
- **SC-007**: In manual validation, the guest-facing belt layout, kitchen focus, and selected-seat detail experience remain recognizably unchanged apart from the compact operator control required for this feature.
- **SC-008**: In acceptance testing, 100% of operator placement attempts leave the selected-seat detail area in guest context rather than converting it into an operator workspace.
