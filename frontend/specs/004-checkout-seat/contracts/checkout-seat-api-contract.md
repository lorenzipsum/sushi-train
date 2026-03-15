# Checkout Seat API Contract

## Purpose

Define the observable backend contract and frontend usage rules for checking out an occupied seat without introducing a second seat-session lifecycle.

## 1. Existing Source Of Truth

- A seat is occupied iff there is an active `OPEN` order for that seat.
- Checkout ends that active order by transitioning it to `CHECKED_OUT` and setting `closedAt`.
- The successful checkout response is the only seat-scoped response that includes the final checked-out order summary.
- Later seat-detail reads return the current open order only, which means they return `orderSummary = null` after successful checkout.

## 2. Write Endpoint

### Request

- **Method**: `POST`
- **Path**: `/api/v1/seats/{seatId}/checkout`
- **Body**: none required

Example request body when an empty JSON payload is still emitted by the client:

```json
{}
```

## 3. Success Response

### Required Behavior

- If the seat still has an active `OPEN` order, the backend checks out that order.
- Success returns HTTP `200 OK` with `isOccupied = false` and the final checked-out `orderSummary`.
- The frontend must preserve this response directly for confirmation and later in-session use.

### Success Shape

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

### Required Success Fields

- Top level:
  - `seatId`
  - `label`
  - `positionIndex`
  - `isOccupied` which must be `false`
- `orderSummary`:
  - `orderId`
  - `seatId`
  - `status` which must be `CHECKED_OUT`
  - `createdAt`
  - `closedAt`
  - `lines`
  - `totalPrice`

### Frontend Rules

- Use this successful write response as the authoritative final summary.
- Do not recompute `status`, `createdAt`, `closedAt`, `lines`, or `totalPrice`.
- Allow empty `lines` and `totalPrice = 0`.

## 4. Conflict Response

### Required Behavior

- If the seat exists but no active `OPEN` order remains, the backend returns the canonical stale-state checkout conflict.
- This same response covers repeated checkout, stale UI state, and concurrent-loss behavior.

### Conflict Shape

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

### Frontend Rules

- Interpret `409 SEAT_NOT_OCCUPIED` as "no active occupancy remains" rather than as an unknown failure.
- Refresh or reconcile seat overview after this outcome so the seat reflects backend truth.
- Do not invent a separate `ALREADY_CHECKED_OUT` lifecycle state in the frontend model.

## 5. Not Found Response

### Required Behavior

- If the seat does not exist, the backend returns a standard not-found result.

### Not Found Shape

```json
{
  "type": "https://api.sushitrain/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Seat not found: <seatId>",
  "instance": "/api/v1/seats/<seatId>/checkout"
}
```

### Frontend Rules

- Show a clear missing-seat explanation.
- Refresh visible seat state so the stale seat does not remain misleadingly actionable.

## 6. Read Contracts Used By This Feature

### Seat Overview

- **Method**: `GET`
- **Path**: `/api/v1/belts/{beltId}/seats`
- **Used for**: backend-authoritative refresh of visible seat availability after checkout attempts

Example post-checkout shape:

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false
}
```

### Seat Detail

- **Method**: `GET`
- **Path**: `/api/v1/seats/{seatId}`
- **Used for**: current seat truth when needed, not for reconstructing the final checked-out summary

Expected post-checkout shape:

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": false,
  "orderSummary": null
}
```

## 7. Frontend Implications

- The frontend should start checkout only from seats currently shown as occupied in the existing UI.
- The frontend should preserve the successful `SeatOrderDto` in current-session state for confirmation and later in-session use.
- The frontend should refresh or reconcile seat state after success, `SEAT_NOT_OCCUPIED`, and not-found responses.
- The frontend should keep the seat visibly available again after success, even though later seat-detail reads no longer contain the closed order summary.
- The feature does not require full page-reload persistence of the final summary.
