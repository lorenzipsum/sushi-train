# Data Model: Occupy Seat

## 1. Seat Place

Visible seat state rendered on the belt stage.

| Field           | Type      | Source            | Notes                                                                                         |
| --------------- | --------- | ----------------- | --------------------------------------------------------------------------------------------- |
| `seatId`        | `string`  | seat overview API | Stable seat identity and interaction target.                                                  |
| `label`         | `string`  | seat overview API | Short seat label shown to the guest.                                                          |
| `positionIndex` | `number`  | seat overview API | Stable seat order around the counter.                                                         |
| `isOccupied`    | `boolean` | seat overview API | True when the backend reports an active `OPEN` order for the seat.                            |
| `presenceCue`   | `string`  | frontend-derived  | Existing visual state for free vs occupied rendering.                                         |
| `isActionable`  | `boolean` | frontend-derived  | True only when the seat is free and no occupy request is currently in progress for that seat. |

### Validation Rules

- `seatId` and `positionIndex` remain backend-authoritative.
- A seat is occupied iff an `OPEN` order exists for that seat in the backend.
- Occupied seats must remain visible but should not present as the primary action target for occupying.

## 2. Active Dining Record

Durable occupancy record created by a successful occupy action.

| Field        | Type                              | Source                                 | Notes                                                                       |
| ------------ | --------------------------------- | -------------------------------------- | --------------------------------------------------------------------------- |
| `orderId`    | `string`                          | seat detail reconciliation             | Durable identifier for the active occupancy record.                         |
| `seatId`     | `string`                          | occupy success response or seat detail | Seat that owns the active order.                                            |
| `status`     | `OPEN \| CHECKED_OUT \| CANCELED` | backend order model                    | `OPEN` means the seat is currently occupied.                                |
| `createdAt`  | `string`                          | seat detail reconciliation             | Occupancy start time.                                                       |
| `closedAt`   | `string \| null`                  | backend order model                    | Null while occupied; set after checkout or cancellation.                    |
| `lines`      | `OrderLine[]`                     | backend order model                    | May be empty for this feature but becomes relevant for picked plates later. |
| `totalPrice` | `number`                          | backend order model                    | May remain zero at occupy time.                                             |

### Validation Rules

- `orderId` is the durable occupancy/session identifier for later features.
- The feature must not add a separate claim or visit record when the open order already exists.
- The frontend must reconcile a successful occupy with seat detail when the initial write response does not include the active order summary.

## 3. Seat Occupy Result

Returned frontend state after `POST /api/v1/seats/{seatId}/occupy` and, when needed, follow-up seat-detail reconciliation.

| Field           | Type                           | Source                     | Notes                                               |
| --------------- | ------------------------------ | -------------------------- | --------------------------------------------------- |
| `seatId`        | `string`                       | occupy success response    | Confirms which seat was occupied.                   |
| `label`         | `string`                       | occupy success response    | Seat label for UI updates if needed.                |
| `positionIndex` | `number`                       | occupy success response    | Stable stage identity.                              |
| `isOccupied`    | `boolean`                      | occupy success response    | Must be true on success.                            |
| `orderSummary`  | `Active Dining Record \| null` | seat detail reconciliation | Durable occupancy context after the follow-up read. |

### Validation Rules

- Successful frontend occupy results must include enough data to identify the active dining record after reconciliation.
- `orderSummary.orderId` and `orderSummary.createdAt` are the minimum durable additions required by this feature, even when they come from the follow-up seat detail read.

## 4. Occupy Conflict

Clear conflict outcome for first-write-wins behavior.

| Field       | Type                    | Source           | Notes                                                         |
| ----------- | ----------------------- | ---------------- | ------------------------------------------------------------- |
| `status`    | `409`                   | problem response | Indicates the seat was not occupied by this request.          |
| `errorCode` | `SEAT_ALREADY_OCCUPIED` | problem response | Seat-specific conflict identifier.                            |
| `seatId`    | `string`                | problem response | Which seat failed to occupy.                                  |
| `action`    | `string \| null`        | problem response | Optional machine-readable hint such as `checkout-seat-first`. |
| `detail`    | `string`                | problem response | Human-readable explanation for the guest.                     |

### Validation Rules

- Both normal application-level conflicts and unique-constraint-backed conflicts should resolve to the same seat-specific conflict shape.
- Conflict handling must leave the visible seat state consistent with backend truth after reconciliation.

## 5. Seat Not Found Outcome

Returned when the seat no longer exists or is invalid for the request.

| Field      | Type     | Source           | Notes                                              |
| ---------- | -------- | ---------------- | -------------------------------------------------- |
| `status`   | `404`    | problem response | Indicates the seat does not exist for the request. |
| `detail`   | `string` | problem response | Human-readable not-found explanation.              |
| `instance` | `string` | problem response | Request path for debugging and support.            |

### Validation Rules

- Not-found handling must not leave the seat visually marked as occupied by mistake.
- The frontend should reconcile or refresh visible seat state after a not-found result.

## Relationships

- One `Seat Place` can have zero or one active `Active Dining Record`.
- A `Seat Occupy Result` returns the new occupied `Seat Place` plus its active `Active Dining Record`.
- An `Occupy Conflict` means another active `Active Dining Record` already exists for the seat.
- Later features `004-checkout-seat` and `005-pick-plates` should build on the same `Active Dining Record` rather than introducing a new occupancy entity.
