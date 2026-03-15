# 004-checkout-seat Backend Truth

## Short domain model

In the current backend, checking out a seat does **not** close a separate seat/session object. A seat is occupied exactly when it has an `OPEN` order, and checkout means: find that seat's current open order, transition the order status to `CHECKED_OUT`, set `closedAt`, save it, and return a seat response containing the now-closed order summary. After that, the seat is considered free because there is no longer any `OPEN` order for it.

---

## 1. What exactly does `POST /api/v1/seats/{seatId}/checkout` do today?

It does this:

1. Loads the seat by `seatId`
2. Fails with `404` if the seat does not exist
3. Loads the seat's current `OPEN` order using a `FOR UPDATE` query
4. Fails with `409` if there is no active `OPEN` order
5. Calls `order.checkout()`
6. Persists the changed order
7. Returns a seat response with:
   - the seat info
   - `isOccupied = false`
   - the final checked-out order summary

This is implemented in the service and controller.

---

## 2. What response body does it return on success?

Success returns HTTP `200 OK` with a `SeatOrderDto`:

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false,
  "orderSummary": {
    "orderId": "0a9f2d8a-b7a7-4a1c-a7aa-4efc2b3fd8d1",
    "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
    "status": "CHECKED_OUT",
    "createdAt": "2026-03-15T03:00:00Z",
    "closedAt": "2026-03-15T03:42:00Z",
    "lines": [
      {
        "menuItemName": "Salmon Nigiri",
        "plateTier": "GREEN",
        "price": 100
      }
    ],
    "totalPrice": 100
  }
}
```

---

## 3. Does success always include the final closed order summary? Which fields are guaranteed?

Yes.

On successful checkout, the backend always builds the response from the saved checked-out order, so the response includes the final closed order summary.

Guaranteed top-level fields:
- `seatId`
- `label`
- `positionIndex`
- `isOccupied` which should be `false`

Guaranteed `orderSummary` fields:
- `orderId`
- `seatId`
- `status` which should be `CHECKED_OUT`
- `createdAt`
- `closedAt`
- `lines`
- `totalPrice`

Notes:
- `closedAt` should be non-null on successful checkout
- `lines` may be empty
- `totalPrice` may be `0`

---

## 4. What happens if the seat does not exist?

HTTP `404 Not Found`

Response shape:

```json
{
  "type": "https://api.sushitrain/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Seat not found: <seatId>",
  "instance": "/api/v1/seats/<seatId>/checkout"
}
```

---

## 5. What happens if the seat exists but has no active `OPEN` order?

HTTP `409 Conflict`

The backend throws `SeatNotOccupiedException`.

Response shape:

```json
{
  "type": "https://api.sushitrain/errors/seat-not-occupied",
  "title": "Seat not occupied",
  "status": 409,
  "detail": "Seat <seatId> has no open order.",
  "instance": "/api/v1/seats/<seatId>/checkout",
  "errorCode": "SEAT_NOT_OCCUPIED",
  "seatId": "<seatId>",
  "action": "occupy-seat-first"
}
```

This is the canonical invalid-seat-state checkout error.

---

## 6. What happens if two clients try to checkout the same seat concurrently?

Real backend behavior:

- The first client that locks the open order succeeds
- The second client will not find an `OPEN` order anymore after the first one commits
- The second client gets:
  - HTTP `409`
  - `errorCode = SEAT_NOT_OCCUPIED`

So concurrent double-checkout behaves like:
- first checkout wins
- second checkout sees "seat not occupied"

There is no separate "already checked out" error.

---

## 7. Does checkout require the order to contain at least one order line, or can an empty occupied seat be checked out?

An empty occupied seat **can** be checked out.

There is no rule requiring at least one order line.

`Order.checkout()` only checks:
- order status must be `OPEN`

It does **not** check:
- minimum number of lines
- minimum total
- non-zero amount

So this is valid:
- occupy seat
- pick no plates
- checkout successfully

The result should be:
- `status = CHECKED_OUT`
- `lines = []`
- `totalPrice = 0`

---

## 8. After successful checkout, should `GET /api/v1/belts/{beltId}/seats` immediately show the seat as free?

Yes, on a fresh backend read after the checkout transaction commits.

Why:
- seat overview derives occupancy from whether an `OPEN` order exists
- after checkout, the order is `CHECKED_OUT`
- therefore the seat is no longer occupied

So the next fresh `GET /api/v1/belts/{beltId}/seats` should show:

```json
{
  "seatId": "...",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false
}
```

---

## 9. After successful checkout, should `GET /api/v1/seats/{seatId}` return the checked-out order, no active order, or something else?

It returns:

- `isOccupied = false`
- `orderSummary = null`

Important detail:
- `GET /api/v1/seats/{seatId}` only includes the current `OPEN` order
- after checkout there is no `OPEN` order anymore
- so the endpoint does **not** return the last checked-out order

Example after successful checkout:

```json
{
  "seatId": "...",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false,
  "orderSummary": null
}
```

So:
- the checkout write response includes the final checked-out order summary
- later seat reads do not

---

## 10. Are there business rules around totals, timestamps, or final order status that the frontend must preserve or display?

Yes.

### Final status

On successful checkout:
- final status is `CHECKED_OUT`

### Timestamps

- `createdAt` stays from when the order was opened
- `closedAt` is set at checkout time
- database also enforces `closedAt >= createdAt`

### Totals

- `totalPrice` is computed from the order lines
- it is not independently entered by frontend
- frontend should treat it as backend-authoritative

### Empty order totals

- an empty checked-out order is valid
- `totalPrice` may be `0`

### Recommendation for frontend

Frontend should:
- display backend `status`
- display backend `createdAt`
- display backend `closedAt`
- display backend `totalPrice`
- not recompute or override authoritative totals/status/timestamps

---

## 11. Is there a canonical error shape or error code for checkout conflicts or invalid seat state?

Yes.

### Seat not occupied

Canonical checkout conflict:
- HTTP `409`
- `errorCode = SEAT_NOT_OCCUPIED`

```json
{
  "type": "https://api.sushitrain/errors/seat-not-occupied",
  "title": "Seat not occupied",
  "status": 409,
  "detail": "Seat <seatId> has no open order.",
  "instance": "/api/v1/seats/<seatId>/checkout",
  "errorCode": "SEAT_NOT_OCCUPIED",
  "seatId": "<seatId>",
  "action": "occupy-seat-first"
}
```

### Seat not found

Canonical not-found:
- HTTP `404`

```json
{
  "type": "https://api.sushitrain/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Seat not found: <seatId>",
  "instance": "/api/v1/seats/<seatId>/checkout"
}
```

### Invalid UUID

If `seatId` is malformed:
- HTTP `400`

```json
{
  "type": "https://api.sushitrain/errors/invalid-parameter",
  "title": "Invalid parameter",
  "status": 400,
  "detail": "Parameter 'id' must be a UUID",
  "instance": "/api/v1/seats/not-a-uuid/checkout"
}
```

---

## 12. Important edge cases the frontend should know

### Repeated checkout

If frontend retries checkout after success:
- backend returns `409 SEAT_NOT_OCCUPIED`

Reason:
- there is no active `OPEN` order anymore

### Stale client

If client still thinks seat is occupied, but another client already checked it out:
- checkout returns `409 SEAT_NOT_OCCUPIED`

### Concurrent checkout

If two clients checkout at nearly the same time:
- one succeeds
- the other gets `409 SEAT_NOT_OCCUPIED`

### Checked-out order is only in the write response

After successful checkout:
- `POST /checkout` returns the final checked-out order summary
- `GET /seats/{seatId}` does not return that checked-out order later

So if frontend needs to show checkout confirmation details:
- it should use the checkout success response directly
- or use orders APIs/history views later, not seat detail

### Empty order checkout

Valid and supported:
- occupied seat with zero lines can be checked out
- frontend should not assume at least one line is required

---

## Recommended frontend-facing interpretations

### Success

Interpret as:
- checkout completed
- seat is now free
- returned `orderSummary` is the final receipt/confirmation payload
- safe to reconcile local seat state to `isOccupied = false`

### Conflict

Interpret `409 SEAT_NOT_OCCUPIED` as:
- no active occupancy remains for this seat
- likely stale client, repeated checkout, or concurrent checkout loss
- frontend should refresh seat state and treat seat as free unless a fresh read says otherwise

### Not found

Interpret `404` as:
- seat id is invalid in business terms or no longer exists
- frontend should stop retrying and refresh seat inventory/context

---

## Response examples reflecting the actual backend contract

### Success

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false,
  "orderSummary": {
    "orderId": "0a9f2d8a-b7a7-4a1c-a7aa-4efc2b3fd8d1",
    "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
    "status": "CHECKED_OUT",
    "createdAt": "2026-03-15T03:00:00Z",
    "closedAt": "2026-03-15T03:42:00Z",
    "lines": [],
    "totalPrice": 0
  }
}
```

### Conflict: no active open order

```json
{
  "type": "https://api.sushitrain/errors/seat-not-occupied",
  "title": "Seat not occupied",
  "status": 409,
  "detail": "Seat 9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a has no open order.",
  "instance": "/api/v1/seats/9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a/checkout",
  "errorCode": "SEAT_NOT_OCCUPIED",
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "action": "occupy-seat-first"
}
```

### Not found

```json
{
  "type": "https://api.sushitrain/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Seat not found: 9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "instance": "/api/v1/seats/9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a/checkout"
}
```

---

## Source anchors

- Checkout endpoint: `/api/v1/seats/{id}/checkout`
- Success DTO shape: `SeatOrderDto`
- Checkout service logic:
  - load seat
  - load open order
  - `order.checkout()`
  - return `isOccupied = false` with checked-out `orderSummary`
- `GET /api/v1/seats/{id}` only returns the current open order, not the last checked-out order
- Occupancy queries are based on whether an `OPEN` order exists
- Canonical checkout conflict is `SEAT_NOT_OCCUPIED`
