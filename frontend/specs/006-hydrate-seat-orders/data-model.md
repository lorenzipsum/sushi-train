# Data Model: Hydrate Seat Orders After Refresh

## 1. Seat Reload Context

The seat state known immediately after reload from the belt seat overview.

| Field                   | Type      | Source            | Notes                                                                            |
| ----------------------- | --------- | ----------------- | -------------------------------------------------------------------------------- |
| `seatId`                | `string`  | seat overview API | Stable seat identity.                                                            |
| `label`                 | `string`  | seat overview API | Human-readable seat label.                                                       |
| `positionIndex`         | `number`  | seat overview API | Stable seat location around the counter.                                         |
| `isOccupied`            | `boolean` | seat overview API | Initial post-reload occupancy signal.                                            |
| `wasPreviouslySelected` | `boolean` | frontend-derived  | Indicates whether reload should restore focus to this seat when it still exists. |

### Validation Rules

- Seat reload context alone must not be treated as proof that a seat is already pick-ready.
- When the previously selected seat still exists, reload should preserve that selection rather than shifting focus immediately to a different default seat.

## 2. Dining Context Restoration State

The guest-visible status of whether a seat's active dining record has been restored after reload.

| Field                   | Type                                                                                   | Source                       | Notes                                                                                 |
| ----------------------- | -------------------------------------------------------------------------------------- | ---------------------------- | ------------------------------------------------------------------------------------- |
| `seatId`                | `string`                                                                               | frontend-derived             | Restored seat identity.                                                               |
| `restorationStatus`     | `'syncing' \| 'confirmed-open-order' \| 'confirmed-no-order' \| 'unresolved-retrying'` | frontend-derived             | Tracks the seat's current restoration lifecycle.                                      |
| `hasRetryInFlight`      | `boolean`                                                                              | frontend-derived             | Indicates whether a background retry is currently underway after a temporary failure. |
| `lastKnownOrderSummary` | `OrderSummaryDto \| null`                                                              | backend restoration response | Present when an active order has been restored.                                       |
| `resolutionMessage`     | `string \| null`                                                                       | frontend-derived             | Human-readable status or unresolved feedback.                                         |

### Validation Rules

- A seat in `syncing` or `unresolved-retrying` must not be treated as ready for plate picking.
- `confirmed-open-order` is the only restored state that can re-enable normal pick behavior.
- `confirmed-no-order` must reconcile any previously contradictory occupied-only presentation.

### State Transitions

- `syncing` → `confirmed-open-order` when restoration confirms an active order.
- `syncing` → `confirmed-no-order` when restoration confirms no active order exists.
- `syncing` → `unresolved-retrying` when restoration fails temporarily.
- `unresolved-retrying` → `confirmed-open-order` or `confirmed-no-order` when a retry succeeds.

## 3. Selected Seat Detail State

The detail-area state shown for the currently selected seat.

| Field                          | Type                                   | Source           | Notes                                                                                 |
| ------------------------------ | -------------------------------------- | ---------------- | ------------------------------------------------------------------------------------- |
| `selectedSeatId`               | `string \| null`                       | frontend-derived | Currently focused seat identity.                                                      |
| `selectionRestoredAfterReload` | `boolean`                              | frontend-derived | Whether reload restored the same previously selected seat.                            |
| `statusLabel`                  | `string`                               | frontend-derived | Guest-facing status such as available, occupied, syncing, unresolved, or checked out. |
| `helperLabel`                  | `string`                               | frontend-derived | Guest-facing explanation of the seat's current state.                                 |
| `orderSummaryVisible`          | `boolean`                              | frontend-derived | Whether running-order or checkout-summary content is currently shown.                 |
| `blockedReason`                | `'syncing' \| 'no-open-order' \| null` | frontend-derived | Why picks or actions are blocked, if applicable.                                      |

### Validation Rules

- The selected-seat detail state must remain internally consistent with action availability, feedback, and visible order content.
- Reselecting a seat with a restored active order must restore that seat's prior order lines rather than resetting the selected-seat detail view.

## 4. Restored Running Order

Backend-authoritative open-order state shown for a selected occupied seat after restoration or reselection.

| Field        | Type                                    | Source                 | Notes                                             |
| ------------ | --------------------------------------- | ---------------------- | ------------------------------------------------- |
| `orderId`    | `string`                                | backend `SeatOrderDto` | Active order identifier.                          |
| `seatId`     | `string`                                | backend order model    | Owning seat.                                      |
| `status`     | `'OPEN' \| 'CHECKED_OUT' \| 'CANCELED'` | backend order model    | Only `OPEN` supports additional order lines.      |
| `createdAt`  | `string`                                | backend order model    | Dining-session start time.                        |
| `lines`      | `OrderLineDto[]`                        | backend order model    | Previously picked and newly restored order lines. |
| `totalPrice` | `number`                                | backend order model    | Backend-authored running total.                   |

### Validation Rules

- The frontend must not recompute restored lines or totals.
- Reselecting a seat with an `OPEN` order must preserve the restored lines and allow additional eligible plates to extend the same order.
- Empty open orders remain valid and must still render correctly.

## 5. Checkout Summary State

The final backend-authoritative order summary shown in the selected-seat area immediately after checkout.

| Field                  | Type              | Source                                | Notes                                                                                            |
| ---------------------- | ----------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `seatId`               | `string`          | backend checkout response             | Checked-out seat identity.                                                                       |
| `orderSummary`         | `OrderSummaryDto` | backend checkout response             | Final order lines and total returned by checkout.                                                |
| `statusLabel`          | `string`          | frontend-derived from backend summary | Guest-facing final status label.                                                                 |
| `visibleWhileSelected` | `boolean`         | frontend-derived                      | Keeps the checked-out seat summary visible in the selected-seat area immediately after checkout. |

### Validation Rules

- Successful checkout must not immediately clear the selected-seat detail area before the final summary is shown.
- The selected-seat area must show the final summary for the checked-out seat rather than jumping to another seat's details.
