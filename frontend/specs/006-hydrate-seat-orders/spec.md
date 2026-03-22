# Feature Specification: Hydrate Seat Orders After Refresh

**Feature Branch**: `[006-hydrate-seat-orders]`  
**Created**: 2026-03-22  
**Status**: Ready for Implementation  
**Input**: User description: "Hydrate occupied seat order context after reload so the selected-seat detail area, reach cues, action states, and pick behavior remain consistent."

## Clarifications

### Session 2026-03-22

- Q: After reload, should dining-context hydration run for all occupied seats immediately or only for the selected seat first? → A: Hydrate all occupied seats immediately after reload before any occupied seat is presented as ready.
- Q: If dining-context hydration fails temporarily, should the app retry automatically or wait for manual recovery? → A: Retry automatically in the background while keeping the seat visibly unresolved until hydration succeeds or is disproven.
- Q: While a seat is still syncing after reload, should the reachable-area and pickability cues stay visible or be suppressed? → A: Keep the reachable-area and pickability cues visible during syncing, but block picks with syncing-specific messaging.
- Q: Should the app preserve the previously selected seat across reload when it still exists? → A: Preserve the previously selected seat across reload if it still exists; otherwise fall back to the normal default.
- Q: If a guest leaves a seat with an existing open order and later selects it again, should that seat still show its prior order lines and allow adding more lines? → A: Yes. Reselecting a seat with an active open order must restore its prior order lines in the selected-seat area and allow adding additional order lines.
- Q: After checkout, where should the final order summary be shown? → A: Show the checkout summary in the selected-seat area for the checked-out seat.

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Continue Dining After Reload (Priority: P1)

As a guest who reloads the page while a seat already has an open order, I want that seat's running order and pick readiness to return automatically so I can continue dining without restarting the session.

**Why this priority**: This restores the main product flow after a refresh. Without it, the app can appear occupied but still block core dining actions, which breaks trust in the experience.

**Independent Test**: Can be fully tested by reloading the page while a seat already has an open order and verifying that the selected occupied seat restores its order lines, total, and pick-ready behavior without requiring Start dining again.

**Acceptance Scenarios**:

1. **Given** a seat already has an open order before the page reloads, **When** the guest reloads the page, **Then** the previously selected seat is restored when it still exists and, if occupied, restores its running order details and remains able to pick plates without asking the guest to start dining again.
2. **Given** a selected occupied seat has restored order context after reload, **When** the guest picks a reachable plate, **Then** the pick proceeds normally and updates the running order as part of the same dining session.
3. **Given** a seat already has an active open order with previously picked lines, **When** the guest selects a different seat and later reselects the original seat, **Then** the selected-seat area restores the prior order lines for that seat and still allows additional reachable plates to be added to that same open order.

---

### User Story 2 - See Honest Syncing State During Reload Recovery (Priority: P2)

As a guest returning to the page after a reload, I want the selected-seat area to show whether dining context is still syncing so the app does not imply that the seat is fully ready before it actually is.

**Why this priority**: The current inconsistency comes from showing an occupied seat as if it were immediately usable while its order context is still unknown. Honest intermediate states prevent contradictory UI.

**Independent Test**: Can be fully tested by reloading the page while one or more occupied seats have order context still being restored and verifying that occupied seats remain in a syncing-specific state instead of a ready-to-pick state until hydration finishes.

**Acceptance Scenarios**:

1. **Given** the seat overview says the selected seat is occupied but its dining context has not been restored yet, **When** the page first renders after reload, **Then** the selected-seat area shows a syncing-specific state rather than implying that plate picking is ready.
2. **Given** an occupied seat is still syncing its dining context, **When** the guest attempts a blocked action during that syncing period, **Then** the app explains that the dining state is still loading rather than telling the guest to start dining first.
3. **Given** dining-context restoration fails temporarily after reload, **When** the app continues recovery, **Then** it retries in the background while keeping the seat visibly unresolved until hydration succeeds or confirms no active dining record exists.
4. **Given** an occupied seat is still syncing its dining context, **When** the guest sees reach and pickability cues that remain visible during recovery, **Then** those cues do not permit actual picking until syncing completes and blocked attempts use syncing-specific feedback.

---

### User Story 3 - Reconcile Stale Occupancy After Reload (Priority: P3)

As a guest who reloads the page after seat state changed elsewhere, I want the app to reconcile stale or missing dining context so the selected-seat detail area, action states, and pick cues all settle on one consistent truth.

**Why this priority**: Reload recovery is not complete unless stale occupied-seat signals can be corrected when the dining record no longer exists. This protects the UI from remaining in a contradictory half-restored state.

**Independent Test**: Can be fully tested by reloading when the seat overview shows an occupied seat but restored dining context later shows no active order, then confirming the app settles into a consistent non-pickable state.

**Acceptance Scenarios**:

1. **Given** the selected seat initially appears occupied after reload, **When** the restored dining context shows no active order, **Then** the UI reconciles to a consistent non-pickable state and does not leave Start dining, checkout, helper text, or pick cues in conflict.
2. **Given** dining context restoration fails or returns outdated information, **When** the app finishes reconciliation, **Then** the selected-seat detail area communicates the final state clearly and avoids implying that plate picking is available when it is not.
3. **Given** the guest checks out a selected seat, **When** checkout completes, **Then** the selected-seat area shows the final order summary for that checked-out seat rather than dropping the guest into an empty or unrelated detail state immediately.

### Edge Cases

- A seat appears occupied immediately after reload, but its dining context takes longer to restore than the belt and seat overview data.
- The selected seat changes while the previous selected occupied seat is still restoring dining context.
- Multiple seats are occupied after reload, but only one selected seat is currently visible in the detail area.
- A seat with an active open order is deselected and later reselected, and its prior order lines must still be visible and extendable.
- Dining context restoration shows that the seat is now free even though the initial seat overview marked it occupied.
- Dining context restoration succeeds for an occupied seat with an empty open order and no previously picked plates.
- The guest attempts to pick a plate during dining-context restoration and the app must explain the temporary block without implying a brand-new dining session is required.
- Dining context restoration fails temporarily while the rest of the page remains usable and retries continue in the background.

## Assumptions

- The backend remains the source of truth for whether an occupied seat still has an active dining record.
- The selected-seat detail area continues to show one selected seat at a time rather than introducing a second hidden dining context.
- When possible, reload should preserve the guest's previously selected seat instead of switching focus to a different default seat.
- The current belt-stage layout and selected-seat detail placement remain in scope and should be preserved.
- UI changes should stay confined to additional state and summary information inside the existing selected-seat detail area rather than altering the surrounding belt or page layout.
- Restoring dining context after reload should prioritize correctness and consistency over making the seat appear immediately ready.
- Occupied seats should be rehydrated eagerly after reload rather than waiting for seat-by-seat interaction.
- Temporary restoration failures should recover through automatic background retry rather than depending on immediate manual guest intervention.
- Reach and pickability cues may remain visible during syncing, but they must not be interpreted as proof that picking is already available.
- Empty open orders remain valid once dining context has been restored.
- Selecting away from a seat does not close, reset, or replace that seat's active open order.
- Checkout should leave the checked-out seat selected long enough for its final summary to be shown in the selected-seat area.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST begin restoring dining context for all occupied seats after a full page reload instead of relying only on occupied-seat visibility.
- **FR-002**: The system MUST avoid presenting any occupied seat as ready for plate picking until that seat's dining context has been restored.
- **FR-003**: The system MUST distinguish between a free seat with no dining context, an occupied seat with dining context still syncing, and an occupied seat with confirmed active dining context.
- **FR-004**: The system MUST show a syncing-specific selected-seat state while an occupied seat's dining context is still being restored.
- **FR-005**: The system MUST NOT imply that plate picking is ready while the selected occupied seat's dining context is still syncing.
- **FR-006**: The system MUST block plate picking while the selected occupied seat's dining context is still syncing.
- **FR-007**: When plate picking is blocked because dining context is still syncing, the system MUST explain that the seat state is still loading rather than telling the guest to start dining first.
- **FR-007A**: The system MAY keep reachable-area and pickability cues visible while dining context is still syncing, but those cues MUST remain behaviorally blocked until hydration completes.
- **FR-008**: When restored dining context confirms an active order, the system MUST restore the running order details for the selected seat without requiring a new start-dining action.
- **FR-009**: When restored dining context confirms an active order, the system MUST allow the selected seat to resume normal plate-pick behavior.
- **FR-009A**: When a guest reselects a seat that still has an active open order, the system MUST restore that seat's previously recorded order lines in the selected-seat area.
- **FR-009B**: When a guest reselects a seat that still has an active open order, the system MUST allow additional eligible plates to be added to that same open order.
- **FR-010**: When restored dining context shows no active order for a seat that initially appeared occupied, the system MUST reconcile the selected-seat detail area, action states, and pick cues to one consistent non-pickable state.
- **FR-011**: The system MUST keep helper text, disabled action states, reach-area cues, and pickability cues mutually consistent after reload.
- **FR-012**: The system MUST preserve the current belt-stage layout and selected-seat detail structure while adding reload recovery behavior.
- **FR-012A**: The system MUST keep visual design and layout changes confined to additional status and summary information inside the existing selected-seat detail area rather than changing the surrounding belt-stage or overall page layout.
- **FR-013**: The system MUST support restoration for occupied seats that have an active order with previously picked lines and for occupied seats whose active order is still empty.
- **FR-014**: If dining context restoration fails temporarily, the system MUST communicate that the seat state is still unresolved and MUST avoid presenting the seat as ready for plate picking.
- **FR-015**: The system MUST reconcile the visible selected-seat state to backend truth after reload when seat overview and restored dining context disagree.
- **FR-016**: The system MUST support simultaneous reload-time restoration for multiple occupied seats without requiring the guest to select each seat to start restoration.
- **FR-017**: If dining context restoration fails temporarily, the system MUST retry restoration automatically in the background while keeping the affected seat visibly unresolved until hydration succeeds or confirms no active order exists.
- **FR-018**: The system MUST restore the previously selected seat across reload when that seat still exists; otherwise it MUST fall back to the normal default selection behavior.
- **FR-019**: After a successful checkout, the system MUST show the checked-out seat's final order summary in the selected-seat area.

### Key Entities _(include if feature involves data)_

- **Seat Overview State**: The initial post-reload seat state that indicates whether a seat appears free or occupied before dining context has been fully restored.
- **Dining Context Restoration State**: The guest-visible state of whether a selected occupied seat's active dining record is syncing, confirmed, missing, or unresolved after reload.
- **Selected Seat Detail State**: The information shown for the selected seat, including helper text, action readiness, running order visibility, and any syncing or reconciliation feedback.
- **Restored Running Order**: The backend-authoritative order details for an occupied seat after reload, including whether an active dining record exists and what order details should be shown.
- **Checkout Summary State**: The final backend-authoritative order summary shown in the selected-seat area immediately after checkout for the currently selected seat.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of reloads for seats with active dining records restore a usable running order without requiring the guest to start dining again.
- **SC-002**: In acceptance testing, 100% of occupied seats whose dining context is still restoring present a syncing-specific state rather than a ready-to-pick state.
- **SC-003**: In acceptance testing, 100% of blocked pick attempts during dining-context restoration return syncing-specific feedback instead of start-dining feedback.
- **SC-004**: In acceptance testing, 100% of reload cases where seat overview and restored dining context disagree settle into one consistent final state for helper text, action availability, and pickability.
- **SC-005**: In manual validation, the current belt-stage layout and selected-seat detail structure remain recognizably unchanged while reload recovery behavior is added.
- **SC-005A**: In manual validation, no visible layout or design changes occur outside the existing selected-seat detail area beyond the additional status and summary information required for reload recovery.
- **SC-006**: In acceptance testing, 100% of temporary restoration failures keep the affected seat in an unresolved state during automatic retry rather than reverting to contradictory ready-or-not-ready messaging.
- **SC-007**: In acceptance testing, 100% of visible reach and pickability cues shown during syncing remain blocked by syncing-specific feedback rather than allowing normal pick behavior or showing start-dining feedback.
- **SC-008**: In acceptance testing, 100% of reloads where the previously selected seat still exists restore focus to that same seat rather than switching to a different seat unexpectedly.
- **SC-009**: In acceptance testing, 100% of seats with active open orders still show their prior order lines and accept additional order lines when reselected later in the same session.
- **SC-010**: In acceptance testing, 100% of successful checkouts show the final order summary in the selected-seat area for the checked-out seat.
