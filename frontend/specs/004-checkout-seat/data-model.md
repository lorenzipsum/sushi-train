# Data Model: Checkout Seat

## 1. Seat Place

Visible seat state rendered on the belt stage.

| Field           | Type                                     | Source            | Notes                                                                                          |
| --------------- | ---------------------------------------- | ----------------- | ---------------------------------------------------------------------------------------------- |
| `seatId`        | `string`                                 | seat overview API | Stable seat identity and checkout target.                                                      |
| `label`         | `string`                                 | seat overview API | Human-readable seat label used in stage and feedback copy.                                     |
| `positionIndex` | `number`                                 | seat overview API | Stable seat order around the counter.                                                          |
| `isOccupied`    | `boolean`                                | seat overview API | True only when the backend reports an active `OPEN` order for the seat.                        |
| `seatAction`    | `'occupy' \| 'checkout' \| null`         | frontend-derived  | Free seats expose occupy, occupied seats expose checkout, unavailable states expose no action. |
| `isPending`     | `boolean`                                | frontend-derived  | True while an occupy or checkout request is active for the seat.                               |
| `presenceCue`   | `'available' \| 'occupied' \| 'pending'` | frontend-derived  | Existing visual seat state, extended to cover checkout pending.                                |

### Validation Rules

- Seat availability remains backend-authoritative and must be reconciled after every checkout attempt.
- Occupied seats must stay visibly distinct and communicate checkout intent without relying only on color.
- A seat becomes free again when there is no active `OPEN` order for it.

## 2. Active Dining Order

The current open order that makes a seat occupied before checkout begins.

| Field        | Type                                    | Source                              | Notes                                                               |
| ------------ | --------------------------------------- | ----------------------------------- | ------------------------------------------------------------------- |
| `orderId`    | `string`                                | seat detail or prior frontend state | Durable identifier for the active order before checkout.            |
| `seatId`     | `string`                                | backend order model                 | Seat that owns the active order.                                    |
| `status`     | `'OPEN' \| 'CHECKED_OUT' \| 'CANCELED'` | backend order model                 | Checkout starts only when status is effectively `OPEN`.             |
| `createdAt`  | `string`                                | backend order model                 | Time the seat became occupied.                                      |
| `closedAt`   | `string \| null`                        | backend order model                 | Null before checkout and set by the backend when checkout succeeds. |
| `lines`      | `OrderLine[]`                           | backend order model                 | May be empty and must not block checkout.                           |
| `totalPrice` | `number`                                | backend order model                 | Backend-authoritative total for the order, which may be `0`.        |

### Validation Rules

- A seat is occupied exactly when it has one active `OPEN` order.
- The feature must not introduce a second visit or seat-session entity separate from the order.
- Empty orders remain valid checkout candidates.

## 3. Final Checkout Summary

The successful checked-out order data returned by the checkout endpoint and preserved for the current session.

| Field                     | Type            | Source                    | Notes                                                           |
| ------------------------- | --------------- | ------------------------- | --------------------------------------------------------------- |
| `seatId`                  | `string`        | checkout success response | Associates the final summary with the seat that was just freed. |
| `label`                   | `string`        | checkout success response | Receipt-style seat label for confirmation copy.                 |
| `positionIndex`           | `number`        | checkout success response | Stable seat identity on the belt stage.                         |
| `isOccupied`              | `false`         | checkout success response | Must be false on successful checkout.                           |
| `orderSummary.orderId`    | `string`        | checkout success response | Final checked-out order identifier.                             |
| `orderSummary.status`     | `'CHECKED_OUT'` | checkout success response | Final backend-authoritative order status.                       |
| `orderSummary.createdAt`  | `string`        | checkout success response | Original occupancy start time.                                  |
| `orderSummary.closedAt`   | `string`        | checkout success response | Checkout completion time set by the backend.                    |
| `orderSummary.lines`      | `OrderLine[]`   | checkout success response | Final line items, which may be empty.                           |
| `orderSummary.totalPrice` | `number`        | checkout success response | Final backend total, which may be zero.                         |

### Validation Rules

- The frontend must preserve this summary directly from the success response and must not recompute any of its business fields.
- The summary must remain available through current-session reconcile flows even after later seat-detail reads return `orderSummary = null`.
- This feature does not require the summary to survive a full browser reload.

## 4. Checkout Feedback

User-facing checkout outcome state shown in the app shell.

| Field          | Type                             | Source                       | Notes                                             |
| -------------- | -------------------------------- | ---------------------------- | ------------------------------------------------- |
| `tone`         | `'success' \| 'error'`           | frontend-derived             | Determines the visible feedback style.            |
| `title`        | `string`                         | frontend-derived             | Short, seat-specific outcome title.               |
| `detail`       | `string`                         | frontend-derived             | Human-readable explanation of what happened.      |
| `seatId`       | `string`                         | checkout request or response | Used to keep the message tied to the target seat. |
| `seatLabel`    | `string`                         | seat state or response       | Human-friendly seat label shown with the outcome. |
| `finalSummary` | `Final Checkout Summary \| null` | checkout success response    | Present for success, absent for failure.          |

### Validation Rules

- Success feedback must be strong enough for confirmation or receipt-style use during the current session.
- Failure feedback for `SEAT_NOT_OCCUPIED` must explain that no active occupancy remains rather than using unknown-error language.
- Missing-seat feedback must not imply checkout can still succeed later without a refreshed seat context.

## 5. Checkout Availability Outcome

Backend business outcomes that determine the post-checkout UI path.

| Field       | Type                          | Source                     | Notes                                                             |
| ----------- | ----------------------------- | -------------------------- | ----------------------------------------------------------------- |
| `status`    | `200 \| 404 \| 409`           | checkout endpoint          | Distinguishes success, missing seat, and stale occupancy.         |
| `errorCode` | `'SEAT_NOT_OCCUPIED' \| null` | problem response           | Canonical stale or repeated checkout signal when status is `409`. |
| `action`    | `string \| null`              | problem response           | Machine-readable hint such as `occupy-seat-first`.                |
| `detail`    | `string`                      | response or problem detail | Human-readable explanation used for UI messaging.                 |

### Validation Rules

- `409 SEAT_NOT_OCCUPIED` must be treated as a business-state loss, not an unknown transport failure.
- `404` must trigger seat-state reconciliation so the missing seat does not remain misleadingly actionable.
- `200` success must free the seat in visible state after reconciliation.

## Relationships

- One `Seat Place` can have zero or one `Active Dining Order` before checkout.
- A successful checkout converts the `Active Dining Order` into a `Final Checkout Summary` and returns the `Seat Place` to free state.
- `Checkout Feedback` presents either the stored `Final Checkout Summary` or a failure explanation for the targeted `Seat Place`.
- A `Checkout Availability Outcome` drives which feedback appears and whether the seat remains occupied or reconciles to free.
