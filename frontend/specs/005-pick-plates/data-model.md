# Data Model: Pick Plates

## 1. Selected Seat Context

The currently focused seat that governs detail-area content and pick targeting.

| Field           | Type                                             | Source                             | Notes                                                  |
| --------------- | ------------------------------------------------ | ---------------------------------- | ------------------------------------------------------ |
| `seatId`        | `string`                                         | seat overview API                  | Stable selected seat identifier.                       |
| `label`         | `string`                                         | seat overview API                  | Human-readable seat label.                             |
| `positionIndex` | `number`                                         | seat overview API                  | Stable seat location around the counter.               |
| `isOccupied`    | `boolean`                                        | seat overview API                  | Determines whether plate picking can proceed.          |
| `orderSummary`  | `OrderSummaryDto \| null`                        | frontend store plus backend writes | Present when the selected seat has a known open order. |
| `pendingAction` | `'occupy' \| 'checkout' \| 'pick-plate' \| null` | frontend-derived                   | Current write affecting the selected seat.             |

### Validation Rules

- Exactly one selected seat may exist at a time when seats are available.
- The selected seat is the only seat context exposed in the selected-seat detail area.
- Changing selection updates focus only; it must not perform a write by itself.

## 2. Selected-Seat Detail Area

Compact secondary UI surface placed below the existing belt UI by default.

| Field                 | Type             | Source           | Notes                                                     |
| --------------------- | ---------------- | ---------------- | --------------------------------------------------------- |
| `selectedSeatId`      | `string \| null` | frontend-derived | Connects the detail area to the selected seat.            |
| `statusLabel`         | `string`         | frontend-derived | Readable occupied or available state.                     |
| `availableActions`    | `string[]`       | frontend-derived | Explicit actions such as start dining or checkout.        |
| `runningOrderVisible` | `boolean`        | frontend-derived | True when the selected seat has order details to show.    |
| `feedbackState`       | `string \| null` | frontend-derived | Pending, success, or failure feedback shown in this area. |

### Validation Rules

- The detail area must remain secondary to the existing belt stage.
- It must be added below the belt UI by default and must not require shrinking or repositioning the stage.
- It should contain only selected-seat status, explicit actions, running-order details, and related feedback.

## 3. Pickable Region

The seat-owned reachable area that determines whether a visible plate may be picked.

| Field                  | Type                                 | Source                                    | Notes                                                    |
| ---------------------- | ------------------------------------ | ----------------------------------------- | -------------------------------------------------------- |
| `selectedSeatId`       | `string`                             | frontend-derived                          | Associates the region with the selected seat.            |
| `reachableSeatIndices` | `number[]`                           | frontend-derived                          | Includes the selected seat and immediate neighbors only. |
| `reachableSlotIds`     | `string[]`                           | frontend-derived from snapshot and layout | Visible slots currently inside the reachable area.       |
| `visualForm`           | `'halo' \| 'circle' \| 'equivalent'` | frontend-derived                          | Seat-anchored presentation style.                        |
| `isAnchoredToSeat`     | `boolean`                            | frontend-derived                          | Must remain true for this feature.                       |

### Validation Rules

- The reachable area must be visually read as belonging to the seat rather than the moving belt.
- It must not extend beyond neighboring seats.
- It may remain visible when the selected seat is free, but free-seat plates still cannot be picked.

## 4. Plate Interaction Surface

Effective interaction area for a visible plate.

| Field                 | Type                       | Source           | Notes                                                                      |
| --------------------- | -------------------------- | ---------------- | -------------------------------------------------------------------------- |
| `plateId`             | `string`                   | belt snapshot    | Target plate identity.                                                     |
| `slotId`              | `string`                   | belt snapshot    | Visible slot containing the plate.                                         |
| `isPickableNow`       | `boolean`                  | frontend-derived | True only when the seat is occupied and the plate is in range.             |
| `hitAreaState`        | `'standard' \| 'expanded'` | frontend-derived | Indicates whether the plate has enlarged easy-pick interaction affordance. |
| `rejectFeedbackState` | `'idle' \| 'rejecting'`    | frontend-derived | Brief visual rejection state for unpickable attempts.                      |

### Validation Rules

- Pickable plates must expose enough interaction surface for easy picking.
- Reject feedback must be brief and must not imply the belt stopped or the plate left the path.
- Reject feedback should occur on the attempted plate itself when feasible.

## 5. Running Order Summary

Backend-authoritative open-order view for the selected occupied seat.

| Field        | Type                                    | Source                 | Notes                          |
| ------------ | --------------------------------------- | ---------------------- | ------------------------------ |
| `orderId`    | `string`                                | backend `SeatOrderDto` | Active order identifier.       |
| `seatId`     | `string`                                | backend order model    | Owning seat.                   |
| `status`     | `'OPEN' \| 'CHECKED_OUT' \| 'CANCELED'` | backend order model    | Picking requires `OPEN`.       |
| `createdAt`  | `string`                                | backend order model    | Open-order start time.         |
| `lines`      | `OrderLineDto[]`                        | backend order model    | Running list of picked plates. |
| `totalPrice` | `number`                                | backend order model    | Backend-authored total.        |

### Validation Rules

- The frontend must use successful write responses as the immediate source of truth.
- The frontend must not recompute totals or timestamps.
- Empty open orders remain valid and must render correctly.

## 6. Plate Pick Outcome

Guest-visible result of a plate-pick attempt.

| Field                  | Type                                                                                                                                  | Source                              | Notes                                            |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------- | ------------------------------------------------ |
| `tone`                 | `'success' \| 'error'`                                                                                                                | frontend-derived                    | Determines message styling.                      |
| `outcomeType`          | `'success' \| 'seat-not-occupied' \| 'out-of-range' \| 'plate-not-pickable' \| 'resource-conflict' \| 'not-found' \| 'unknown-error'` | frontend-derived                    | Distinguishes user-facing outcomes.              |
| `message`              | `string`                                                                                                                              | frontend-derived or backend problem | Human-readable explanation.                      |
| `rejectAnimationShown` | `boolean`                                                                                                                             | frontend-derived                    | Tracks whether visible plate rejection occurred. |
| `orderSummary`         | `Running Order Summary \| null`                                                                                                       | backend success response            | Present on success.                              |

### Validation Rules

- Out-of-range and other unpickable attempts should be visually distinguishable from success.
- Conflict handling must leave the UI reconciled with backend truth after refresh.
- Plate-level reject feedback must coexist with textual explanation rather than replace it entirely.
