# Feature Specification: Pick Plates

**Feature Branch**: `[005-pick-plates]`  
**Created**: 2026-03-15  
**Status**: Draft  
**Input**: User description refined through clarification: preserve the current belt UI, let guests explicitly start dining from a compact selected-seat detail area below the belt UI, pick plates for the selected occupied seat, and avoid accidental occupy or checkout writes from clicking around seats.

## Clarifications

### Session 2026-03-15

- Q: Should the selected-seat detail area follow only the selected seat or keep a separate hidden active dining seat context? → A: The selected-seat detail area always follows the selected seat only; there is no separate hidden active dining seat in this feature.
- Q: How should the pickable plate region be determined for a selected seat? → A: Plates are pickable only when an occupied seat is selected, and only while the plate is within that seat's visual reach, extending at most to its neighboring seats and not farther.

### Session 2026-03-16

- Q: How much may this feature change the current stage and shell presentation? → A: It should preserve the current UI and add the new behavior with minimal visual disruption.
- Q: Where should any new UI elements be added if additional surface is unavoidable? → A: Add them below the current UI so the existing UI elements do not change size or position.
- Q: What does `current-seat box` mean in this feature? → A: It means a compact selected-seat detail area added below the existing belt UI by default, not a redesign that shrinks, crowds, or repositions the current stage. The specification should prefer `selected-seat detail area` as the primary term.
- Q: How should the seat's reachable pick area be shown? → A: Show a seat-anchored reachable area around the selected seat, visually read as a circular or halo-like reach field, while still respecting the rule that pickability extends no farther than neighboring seats.
- Q: How easy should it be to pick a pickable plate? → A: Picking should be easy, with enough click or tap surface around a pickable plate that the guest does not need precise pixel targeting.
- Q: What should happen if a guest tries to pick an unpickable plate? → A: The UI should make the rejection obvious, for example with a brief whole-plate vibration or comparable reject animation, while the plate continues along its belt path.

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Start Dining And Pick Nearby Plates (Priority: P1)

As an anonymous guest, I can explicitly start dining for the selected seat and add nearby plates to that selected occupied seat so I can build my order safely and see the running total as I go.

**Why this priority**: This is the first ordering flow built on top of the seat lifecycle. It delivers the core value of turning a selected seat into the current dining context and adding nearby plates to that seat's open order.

**Independent Test**: Can be fully tested by selecting a free seat, starting dining from the compact selected-seat detail area below the current belt UI, picking a plate that is within that seat's reach, and verifying that the running order summary updates with the new line and total for the selected occupied seat.

**Acceptance Scenarios**:

1. **Given** a free seat is selected, **When** the guest starts dining from the compact selected-seat detail area, **Then** the system creates the open dining order for that selected seat and treats that selected occupied seat as the current write target.
2. **Given** an occupied seat is selected and a plate is within that seat's pickable region, **When** the plate pick succeeds, **Then** the system adds that plate to the selected seat's open order and updates the running order summary from the backend-returned result.
3. **Given** a selected occupied seat already has picked plates, **When** another in-range plate is picked successfully, **Then** the system shows the updated order lines and running total without recomputing those values locally.
4. **Given** a plate is currently pickable for the selected occupied seat, **When** the guest taps or clicks near that plate within its interactive surface, **Then** the pick remains easy to trigger without requiring overly precise aiming.

---

### User Story 2 - Browse Seats Without Accidental Writes (Priority: P2)

As an anonymous guest, I can click seats to inspect them without accidentally creating an empty order or checking out a seat just because I was browsing the counter.

**Why this priority**: The product goal is to prevent accidental writes while the guest is exploring seats. This is the UX correction that makes later ordering and checkout behavior safe and predictable.

**Independent Test**: Can be fully tested by clicking across free and occupied seats without using the current-seat actions, then verifying that no occupy or checkout write occurs until the guest explicitly acts from the compact selected-seat detail area and that the detail area always follows the currently selected seat.

**Acceptance Scenarios**:

1. **Given** the guest clicks a free seat, **When** the guest only changes seat focus, **Then** the system updates the compact selected-seat detail area but does not create an order automatically.
2. **Given** the guest clicks an occupied seat, **When** the guest only changes seat focus, **Then** the system updates the compact selected-seat detail area but does not check out that seat automatically.
3. **Given** the guest selects a different seat, **When** the selected-seat detail area updates to that newly selected seat, **Then** the product does not keep a separate hidden dining context there and does not write to any seat until the guest performs an explicit action.
4. **Given** new seat-selection or ordering UI must be added, **When** that UI is rendered, **Then** it is added below the existing belt surface rather than shrinking, repositioning, or crowding the current stage layout.

---

### User Story 3 - Understand Plate Range And Conflict Limits (Priority: P3)

As an anonymous guest, I can understand why a plate or seat action cannot proceed when the selected seat is not writable, the plate is out of reach or cannot be picked, or another client won the race first.

**Why this priority**: The backend is anonymous and seat-ID-driven only, so concurrency and stale-state outcomes are expected. Clear messaging is necessary to keep the ordering flow trustworthy.

**Independent Test**: Can be fully tested by trying to pick a plate with no occupied selected seat, trying to pick an out-of-range or unavailable plate, and simulating a conflicting duplicate pick, then verifying that the UI shows distinct explanations and refreshes the relevant seat or belt state.

**Acceptance Scenarios**:

1. **Given** the selected seat is free or no longer has an open order, **When** the guest tries to pick a plate, **Then** the system clearly explains that the selected seat must be occupied before plates can be added.
2. **Given** a plate is outside the selected seat's pickable region or is otherwise not pickable, **When** the guest tries to add it, **Then** the system clearly explains that the plate cannot be picked for the selected seat in its current state and gives immediate visible reject feedback while the plate continues its path.
3. **Given** another client picks the same plate first, **When** the guest's pick attempt completes later, **Then** the system explains that the plate or resource state changed and refreshes the visible belt and running order context.

### Edge Cases

- The guest clicks a plate before starting dining for the selected seat.
- The guest changes seat selection and expects the selected-seat detail area to continue showing the previously occupied seat.
- The selected seat disappears from backend truth while it is still shown in the selected-seat detail area.
- The selected occupied seat is checked out or otherwise loses its open order before a plate pick completes.
- A plate sits just beyond the selected seat's neighboring seats and must not be treated as pickable.
- A guest tries to tap a plate that looks nearby but is outside the selected seat's visible reachable area.
- Another client picks the same plate first and the backend returns a generic `RESOURCE_STATE_CONFLICT` instead of a more specific pickability error.
- The backend returns later seat-detail reads with reconstructable order lines and total but without a guaranteed persisted line order.
- Reduced-motion or degraded-state modes are active while the guest is browsing seats or resolving a plate-pick conflict, including reject feedback for an unpickable plate.

## Assumptions

- The backend remains anonymous and seat-ID-driven only for this feature.
- The selected seat is the only seat context shown in the compact selected-seat detail area for this feature.
- Selecting another seat changes both the viewed seat and the seat that would receive explicit write actions if it is occupied and otherwise valid.
- Plate-picking is restricted by the selected seat's visual reach, which extends no farther than the selected seat's neighboring seats.
- The selected seat's reachable area is visually anchored to the seat itself and should not appear to drift with moving plates or slot markers.
- The compact selected-seat detail area is the single place where occupy and checkout actions are initiated.
- The compact selected-seat detail area should remain secondary to the existing belt stage and should expose only the controls and summary needed for selected-seat interaction.
- Empty open orders after occupy and before the first plate pick are valid and must remain supported.
- The current belt, seat, and stage presentation should remain visually intact unless a change is strictly required for the new behavior.
- If new UI surface is needed for this feature, it should be added below the current belt UI by default so existing element size and position stay unchanged.
- Pickable plates should expose enough interactive surface that they are easy to pick on typical desktop and touch interactions.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST let the guest click any visible seat to make it the selected seat in the UI.
- **FR-002**: The system MUST NOT create an open dining order merely because a free seat was selected.
- **FR-003**: The system MUST let the guest explicitly start dining for a selected free seat from the compact selected-seat detail area.
- **FR-004**: The system MUST treat the currently selected seat as the only seat context shown in the compact selected-seat detail area.
- **FR-005**: The system MUST let the guest pick a plate only when the currently selected seat is occupied and still has an open order.
- **FR-006**: The system MUST let the selected occupied seat act as the write target for plate-pick and checkout actions.
- **FR-007**: The system MUST allow a plate to be picked only when that plate is within the selected seat's pickable region.
- **FR-008**: The system MUST define the selected seat's pickable region as the selected seat's own visual reach extending at most to its neighboring seats and not farther.
- **FR-009**: The system MUST visualize the selected seat's pickable region with a clear seat-range cue that is anchored to the seat itself, such as a circular, halo-like, or equivalent reachable-area effect.
- **FR-010**: The system MUST update the running order summary from the successful plate-pick response and MUST use the backend-returned `orderSummary` as the immediate source of truth.
- **FR-011**: The system MUST show the selected occupied seat's running order summary in the compact selected-seat detail area, including the current order lines and running total.
- **FR-012**: The system MUST NOT recompute backend-owned order status, order start time, per-line snapshot price, or total price.
- **FR-013**: The system MUST let the guest inspect any occupied seat without automatically checking it out.
- **FR-014**: The system MUST keep occupy and checkout as explicit actions in the compact selected-seat detail area rather than automatic results of seat clicks.
- **FR-015**: The system MUST explain when a plate pick cannot proceed because the selected seat is not currently occupied or no longer has an open order.
- **FR-016**: The system MUST explain when a plate cannot be picked because it is outside the selected seat's pickable region or is otherwise not pickable in its current state.
- **FR-017**: The system MUST explain and recover from generic resource-state conflicts by refreshing the relevant belt or seat context.
- **FR-018**: The system MUST refresh or reconcile visible seat and order state after successful and failed plate-pick attempts so the UI remains aligned with backend truth.
- **FR-019**: The system MUST remain honest that selected-seat behavior is a frontend convention and MUST NOT imply backend-enforced ownership by browser or guest.
- **FR-020**: The system MUST preserve compatibility with the checkout flow by allowing the selected occupied seat to be checked out explicitly from the compact selected-seat detail area.
- **FR-021**: The feature MUST stay within the current single-belt application scope and MUST NOT introduce login, guest profiles, multi-belt management, or a second backend seat-session lifecycle.
- **FR-022**: The system MUST preserve the current belt, seat, and stage UI with minimal visual disruption while adding the new behavior.
- **FR-023**: If new UI elements are required for this feature, the system MUST place them below the existing UI by default so the current UI elements do not change size or position.
- **FR-024**: The compact selected-seat detail area MUST be added below the existing belt UI by default and MUST NOT require shrinking, crowding, or repositioning the current belt-stage presentation.
- **FR-025**: The compact selected-seat detail area MUST remain limited to selected-seat status, explicit actions, running-order details, and related feedback rather than expanding into a broader layout redesign.
- **FR-026**: The system MUST render the selected seat's reachable pick area as a visibly bounded area around that seat, readable as the seat's pickup reach and remaining fixed relative to the seat rather than the moving belt.
- **FR-027**: The system MUST provide enough interactive surface for a pickable plate that guests can reliably tap or click it without unusually precise aiming.
- **FR-028**: When a guest attempts to pick a plate that is not currently pickable, the system MUST provide immediate visible reject feedback on that plate, such as a brief whole-plate vibration or equivalent effect, while the plate continues moving along the belt.
- **FR-029**: Any reject feedback for an unpickable plate MUST remain brief and must not make the plate appear to leave, stop, or otherwise break the authoritative belt motion.
- **FR-030**: The system MUST communicate reachable-area ownership, plate pickability, and reject feedback without relying on color alone.
- **FR-031**: Keyboard and pointer users MUST be able to perceive and operate selected-seat browsing and the explicit selected-seat actions supported by this feature.

### Key Entities _(include if feature involves data)_

- **Selected Seat**: The seat the guest is currently focusing on in the UI, used to populate the selected-seat detail area and guide what the guest is viewing.
- **Selected-Seat Detail Area**: A compact selected-seat detail area placed below the existing belt UI by default, used to show selected-seat status, explicit actions, running-order details, and related feedback without changing the current stage layout.
- **Pickable Region**: The visual and interaction range for the selected seat, shown as a seat-anchored reachable area around the selected seat, extending at most through neighboring seats and determining which plates may be picked.
- **Running Order Summary**: The backend-authoritative open-order summary for the selected occupied seat, including order identifier, status, created time, order lines, and running total.
- **Plate Pick Outcome**: The result of attempting to add a plate to the selected occupied seat, including success, stale-seat conflicts, out-of-range or not-pickable plate conflicts, and generic resource-state conflicts.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of successful plate picks update the selected occupied seat's running order summary with the backend-returned line and total after the next visible UI update.
- **SC-002**: In acceptance testing, 100% of seat-only browsing interactions avoid creating an empty order or checking out a seat until the guest uses an explicit action in the selected-seat detail area.
- **SC-003**: In manual feature validation, pickable plates are identifiable before interaction through the selected seat's reachable-area cue and plate pickability affordances.
- **SC-004**: In acceptance testing, 100% of stale-seat, out-of-range or not-pickable plate, and concurrent-conflict outcomes produce a distinct explanation and a refreshed UI state rather than an unspecified failure.
- **SC-005**: In acceptance testing, the feature preserves the current belt-stage layout without shrinking or repositioning existing core UI elements, except where the user has explicitly approved a change.
- **SC-006**: In manual feature validation, the selected seat's reachable pickup area is clearly understandable as belonging to that seat rather than to moving belt positions.
- **SC-007**: In acceptance testing, guests can successfully trigger picks on currently pickable plates without requiring unusually precise click or tap placement.
- **SC-008**: In manual feature validation, keyboard users can move through selected-seat controls and trigger the explicit selected-seat actions required by this feature, and the UI still communicates pickability and reject feedback without relying on color alone.
