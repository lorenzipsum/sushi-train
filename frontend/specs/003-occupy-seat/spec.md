# Feature Specification: Occupy Seat

**Feature Branch**: `[003-occupy-seat]`  
**Created**: 2026-03-15  
**Status**: Draft  
**Input**: User description: "Add the first write flow so an anonymous guest can occupy a free seat by clicking it in the UI."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Take A Free Seat (Priority: P1)

As an anonymous guest, I can click a free seat and immediately take it so the seat becomes mine for later dining actions.

**Why this priority**: This is the first write action in the product and the foundation for later guest flows such as picking plates and checking out.

**Independent Test**: Can be fully tested by loading a belt with at least one free seat, clicking that seat, and verifying that the seat becomes occupied and stays occupied after the page refreshes.

**Acceptance Scenarios**:

1. **Given** a seat is free, **When** the guest clicks that seat, **Then** the system marks the seat as occupied and confirms the seat is now active for later guest actions.
2. **Given** a seat has just been occupied successfully, **When** the page refreshes or reloads, **Then** the same seat still appears occupied from the backend-authoritative state.

---

### User Story 2 - Understand Why A Seat Cannot Be Taken (Priority: P2)

As an anonymous guest, I can clearly understand when a seat cannot be occupied because someone else already took it or because the seat no longer exists.

**Why this priority**: The first write flow must be trustworthy. Clear conflict and not-found behavior prevents the product from feeling random or stale.

**Independent Test**: Can be fully tested by attempting to occupy an already occupied seat and a missing seat, then verifying that the guest sees a clear explanation and the visible seat state stays correct.

**Acceptance Scenarios**:

1. **Given** a seat becomes occupied before the guest's request completes, **When** the guest tries to occupy it, **Then** the system rejects the request and clearly says that the seat is already occupied.
2. **Given** the guest triggers an occupy action for a seat that no longer exists, **When** the request completes, **Then** the system clearly says the seat was not found and does not show a false occupied state.

---

### User Story 3 - Keep Occupancy Ready For Later Flows (Priority: P3)

As a product team, we can treat an occupied seat as the start of a durable dining session so checkout and picked plates can build on the same record later.

**Why this priority**: This feature should not create a dead-end toggle. It must create a durable occupancy record that later seat and plate flows can safely reuse.

**Independent Test**: Can be fully tested by occupying a seat and verifying that the returned occupied state includes durable occupancy context that can identify the active dining session later.

**Acceptance Scenarios**:

1. **Given** a seat is occupied successfully, **When** the system returns the updated seat state, **Then** the response includes durable occupancy context for the active dining session.
2. **Given** a seat is already occupied, **When** later flows need to act on that seat, **Then** the product can rely on the existing active dining session instead of creating a separate occupancy concept.

### Edge Cases

- A guest clicks a seat that appears free, but another client occupies it first.
- A guest clicks the same free seat multiple times in quick succession.
- The backend reports that the seat does not exist or no longer belongs to the visible belt.
- The page reloads after a successful occupy action and must still show the seat as occupied.
- Reduced-motion mode is active while the guest occupies a seat.
- The belt is paused or freshness is degraded while the occupy action succeeds or fails.

## Assumptions

- Guests are anonymous for this feature and do not need to log in, enter a name, or provide other identity data.
- Occupying a seat starts from clicking a free seat on the stage rather than using a separate form.
- A seat counts as occupied when there is an active dining order for that seat.
- The active dining order created during occupy becomes the durable parent record for later checkout and plate-pickup flows.
- Polling and refresh-after-write behavior may remain in place for this feature.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST let an anonymous guest attempt to occupy a free seat directly from the seat shown in the main stage.
- **FR-002**: The system MUST prevent occupied seats from appearing as freely available after backend confirmation.
- **FR-003**: The system MUST create an active dining record when a free seat is occupied successfully.
- **FR-004**: The system MUST treat that active dining record as the durable occupancy record for later checkout and picked-plate flows.
- **FR-005**: The system MUST preserve backend-authoritative seat state and MUST not infer successful occupancy from UI state alone.
- **FR-006**: The system MUST apply first-write-wins behavior when multiple clients try to occupy the same seat.
- **FR-007**: The system MUST show a clear conflict message when a guest tries to occupy a seat that is already occupied.
- **FR-008**: The system MUST show a clear not-found message when the requested seat no longer exists.
- **FR-009**: The system MUST return enough occupied-seat context after a successful occupy action to identify the active dining record for later features.
- **FR-010**: The system MUST include the seat identity, occupied state, active dining record identifier, and occupancy start time in the successful occupy result.
- **FR-011**: The system MUST refresh or reconcile the visible seat state after a successful or failed occupy action so the UI remains consistent with backend truth.
- **FR-012**: The system MUST keep occupied seats visibly distinct from free seats without relying only on color.
- **FR-013**: The system MUST preserve reduced-motion, paused, and degraded-state clarity while occupy actions are in progress or after they complete.
- **FR-014**: The feature MUST stay within the current single-belt application scope and MUST not introduce login, guest profiles, or multi-belt management.

### Key Entities _(include if feature involves data)_

- **Seat Place**: A visible seat around the belt with stable identity, label, position, and free or occupied state.
- **Active Dining Record**: The durable record created when a seat is occupied, used as the source of truth for occupancy and later guest actions.
- **Seat Occupy Result**: The returned seat state after an occupy attempt, including whether the seat is occupied and the active dining context when successful.
- **Occupy Conflict**: The business outcome returned when another guest already occupies the same seat first.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of successful occupy attempts on free seats result in the selected seat showing as occupied after the next visible refresh.
- **SC-002**: In acceptance testing, 100% of concurrent double-occupy scenarios result in exactly one success and one clear conflict outcome.
- **SC-003**: In usability review, at least 90% of participants correctly understand on first attempt whether a failed occupy action happened because the seat was already taken.
- **SC-004**: In acceptance testing, 100% of successful occupy responses include enough durable occupancy context to support later checkout and picked-plate features without inventing a second session concept.
