# Feature Specification: Checkout Seat

**Feature Branch**: `[004-checkout-seat]`  
**Created**: 2026-03-15  
**Status**: Implemented  
**Input**: User description: "Create the feature specification for 004-checkout-seat so an anonymous guest can finish dining by checking out an occupied seat, see the final checkout summary, and understand stale or missing-seat outcomes without introducing a second seat-session lifecycle."

## Clarifications

### Session 2026-03-15

- Q: How long must the product preserve the final checkout summary from a successful checkout? → A: Preserve it through the current in-app session and reconciliation flows, but not across a full browser reload in this feature.

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Finish Dining At An Occupied Seat (Priority: P1)

As an anonymous guest, I can check out from an occupied seat so my dining session ends and the seat becomes available again.

**Why this priority**: This is the primary guest outcome for the feature. It completes the dining flow started by occupying a seat and frees the seat for the next guest.

**Independent Test**: Can be fully tested by starting from an occupied seat, completing checkout, and verifying that the seat becomes free while the guest is shown the final checked-out summary returned for that checkout.

**Acceptance Scenarios**:

1. **Given** a seat currently has an active dining order, **When** the guest starts checkout from that seat and the checkout succeeds, **Then** the system marks the dining order as checked out, shows the final checkout summary, and makes the seat visibly available again.
2. **Given** checkout succeeds for an occupied seat, **When** the guest views the refreshed seat state after the action, **Then** the seat appears free and no active occupancy remains for that seat.
3. **Given** an occupied seat has no ordered items yet, **When** the guest checks out successfully, **Then** the system still allows checkout and shows a valid final summary with an empty item list and zero total.

---

### User Story 2 - Understand Why Checkout Cannot Happen (Priority: P2)

As an anonymous guest, I can understand why checkout failed when the seat is already free, the active occupancy was lost, or the seat no longer exists.

**Why this priority**: Checkout is a write action that can lose races with other clients or become stale. The guest needs a trustworthy explanation rather than a generic error.

**Independent Test**: Can be fully tested by attempting checkout for a seat whose active occupancy has already ended and for a missing seat, then verifying that the product explains the business reason and reconciles the seat state to backend truth.

**Acceptance Scenarios**:

1. **Given** a seat no longer has an active dining order by the time checkout is attempted, **When** the guest submits checkout, **Then** the system clearly explains that the seat is no longer occupied and does not present the outcome as an unknown failure.
2. **Given** another client finishes checkout first, **When** a stale or repeated checkout attempt completes later, **Then** the system explains that no active occupancy remains and refreshes the seat so it shows as free.
3. **Given** the selected seat no longer exists, **When** the guest attempts checkout, **Then** the system clearly explains that the seat could not be found and removes any misleading assumption that checkout is still possible there.

---

### User Story 3 - Preserve The Final Checkout Summary (Priority: P3)

As a product team, we can preserve the final checked-out summary returned at checkout time so later receipt, history, and real-time features can build on the same lifecycle model.

**Why this priority**: After checkout, later seat views no longer contain the closed dining order. The product must retain the write result itself so the final summary is not lost.

**Independent Test**: Can be fully tested by completing checkout, confirming that the final summary shown to the guest matches the backend-returned checked-out result, and verifying that later in-app seat reconciliation can show the seat as free without replacing or recomputing that stored final summary.

**Acceptance Scenarios**:

1. **Given** checkout succeeds, **When** the final summary is shown to the guest, **Then** it reflects the backend-provided checked-out status, timing, line items, and total without local recalculation.
2. **Given** the seat is viewed again after a successful checkout within the same in-app session, **When** the current seat state is refreshed or reconciled, **Then** the seat appears free while the product still retains the final checkout summary from the completed action for later use.

### Edge Cases

- A guest starts checkout for an occupied seat, but another client completes checkout first.
- A guest repeats checkout rapidly for the same seat after a successful first attempt.
- A guest checks out an occupied seat whose final summary contains no line items and a zero total.
- The seat disappears from backend truth between the time the guest sees it and the time checkout is attempted.
- The seat state changes while the interface is in a degraded, paused, or refresh-reconciling state.
- Reduced-motion preferences are active while checkout success or failure feedback is shown.
- The guest reloads the page immediately after checkout; the seat must still reconcile as free even though this feature does not require the final checkout summary to survive that full reload.

## Assumptions

- A seat is considered occupied exactly when it has one active dining order that is still open.
- Checkout ends that active dining order rather than closing a separate seat, visit, or session record.
- Checkout begins from the existing occupied-seat experience in the current single-belt application rather than a new dedicated workflow.
- Backend-reported status, totals, timestamps, and final summary details are the source of truth and should not be recomputed by the product.
- After each checkout attempt, the product can refresh or reconcile visible seat state so the interface matches backend truth.
- Preserving the final checkout summary means retaining the successful checkout result for the current in-app session strongly enough for confirmation and later in-session use, even though later seat-detail reads no longer contain that closed order and this feature does not require full page-reload persistence.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST let an anonymous guest start checkout from a seat that is currently shown as occupied in the existing single-belt experience.
- **FR-002**: The system MUST complete checkout only when the selected seat still has an active dining order at the moment the backend confirms the action.
- **FR-003**: The system MUST treat a successful checkout as ending the active dining order and making the seat free again.
- **FR-004**: The system MUST show a clear success state that confirms the seat is now available and the dining session has ended.
- **FR-005**: The system MUST use the successful checkout result itself as the authoritative final checkout summary for guest confirmation and later in-session product use.
- **FR-006**: The system MUST display the backend-provided final status, start time, close time, line items, and total exactly as returned for a successful checkout.
- **FR-007**: The system MUST allow checkout to succeed even when the occupied seat has no line items and a zero total.
- **FR-008**: The system MUST not block checkout based on frontend assumptions about minimum order size, non-empty line items, or locally derived totals.
- **FR-009**: The system MUST treat a no-longer-occupied checkout result as a stale, repeated, or lost-race business outcome rather than a generic unknown failure.
- **FR-010**: The system MUST clearly explain when checkout cannot happen because no active occupancy remains for the seat.
- **FR-011**: The system MUST clearly explain when checkout cannot happen because the requested seat no longer exists.
- **FR-012**: The system MUST reconcile the visible seat state with backend truth after every successful or failed checkout attempt.
- **FR-013**: The system MUST make the seat visibly available again after a successful checkout without requiring the guest to infer that change from the summary alone.
- **FR-014**: The system MUST preserve the current single-belt scope and MUST not introduce login, guest profiles, multi-belt management, or a separate seat-session or visit abstraction.
- **FR-015**: The system MUST preserve accessibility, reduced-motion clarity, and understandable degraded-state feedback during checkout actions and after their outcomes are shown.
- **FR-016**: The system MUST keep the checkout lifecycle compatible with future receipt, order history, and real-time features by relying on the same order-based occupancy model rather than inventing a second lifecycle model.
- **FR-017**: The system MUST preserve the final checkout summary across in-app refresh and reconcile flows within the current session, but this feature does not require that summary to survive a full browser reload.

### Key Entities _(include if feature involves data)_

- **Seat Place**: A seat shown around the belt with stable identity, label, position, and a current free-or-occupied state.
- **Active Dining Order**: The currently open dining record that makes a seat occupied and is the record ended by checkout.
- **Final Checkout Summary**: The authoritative checked-out result returned when checkout succeeds, including final status, timing, ordered lines, and total.
- **Checkout Availability Outcome**: The business result that explains whether checkout succeeded, failed because no active occupancy remained, or failed because the seat no longer existed.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of successful checkout attempts from occupied seats result in the selected seat showing as free after the next visible reconciliation.
- **SC-002**: In acceptance testing, 100% of stale, repeated, or concurrent-losing checkout attempts produce a clear no-longer-occupied outcome instead of an unspecified failure.
- **SC-003**: In usability review, at least 90% of participants correctly identify on first attempt whether checkout succeeded, the seat is now free, or checkout failed because no active occupancy remained.
- **SC-004**: In acceptance testing, 100% of successful checkout results preserve the backend-returned final summary strongly enough to support confirmation or later receipt-style features throughout the current in-app session even after later seat views no longer include the closed order.
