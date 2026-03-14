# Occupy Seat API Contract

## Purpose

Define the observable backend contract for occupying a seat from the frontend without introducing a new seat-session abstraction.

## 1. Existing Source Of Truth

- A seat is occupied iff there is an `OPEN` order for that seat.
- The `OPEN` order is the durable occupancy/session record for this feature.
- `orderId` is the durable identifier that later checkout-seat and pick-plates features should build on.

## 2. Write Endpoint

### Request

- **Method**: `POST`
- **Path**: `/api/v1/seats/{seatId}/occupy`
- **Body**: none required

Example request body when an empty JSON payload is still emitted by the client:

```json
{}
```

## 3. Success Response

### Required Behavior

- If the seat is free, the backend creates a new `OPEN` order for that seat.
- The current generated frontend contract returns `SeatStateDto`, so the frontend must reconcile with `GET /api/v1/seats/{seatId}` to capture durable occupancy context.

### Current Write Response Shape

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": true
}
```

### Required Reconciliation Shape

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": true,
  "orderSummary": {
    "orderId": "0a9f2d8a-b7a7-4a1c-a7aa-4efc2b3fd8d1",
    "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
    "status": "OPEN",
    "createdAt": "2026-03-15T03:00:00Z",
    "closedAt": null,
    "lines": [],
    "totalPrice": 0
  }
}
```

### Minimum Required Fields After Reconciliation

- `seatId`
- `isOccupied`
- `orderSummary.orderId`
- `orderSummary.createdAt`

## 4. Conflict Response

### Required Behavior

- First write wins.
- If another client occupies the seat first, the backend returns a seat-specific conflict result.
- Both the normal application-level check and the unique-constraint safety net should converge on the same business-facing response shape.

### Conflict Shape

```json
{
  "type": "https://api.sushitrain/errors/seat-already-occupied",
  "title": "Seat already occupied",
  "status": 409,
  "detail": "Seat <seatId> already has an open order.",
  "instance": "/api/v1/seats/<seatId>/occupy",
  "errorCode": "SEAT_ALREADY_OCCUPIED",
  "seatId": "<seatId>",
  "action": "checkout-seat-first"
}
```

## 5. Not Found Response

### Required Behavior

- If the seat does not exist, the backend returns a not-found result.

### Not Found Shape

```json
{
  "type": "https://api.sushitrain/errors/not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Seat not found: <seatId>",
  "instance": "/api/v1/seats/<seatId>/occupy"
}
```

## 6. Read Contracts Used By This Feature

### Seat Overview

- **Method**: `GET`
- **Path**: `/api/v1/belts/{beltId}/seats`
- **Required fields for this feature**:
  - `seatId`
  - `label`
  - `positionIndex`
  - `isOccupied`

Example shape:

```json
{
  "seatId": "9c3c8d28-6b7d-4e88-9f8e-2c66db2c5c3a",
  "label": "A1",
  "positionIndex": 0,
  "isOccupied": true
}
```

### Seat Detail

- **Method**: `GET`
- **Path**: `/api/v1/seats/{seatId}`
- **Used for**: seat-specific reconciliation and access to the current open order summary if needed.

## 7. Frontend Implications

- The frontend should start occupy actions from free seats only.
- The frontend must treat occupy success as backend-authoritative only after the request resolves.
- The frontend should immediately reconcile successful occupies with `GET /api/v1/seats/{seatId}` and should refresh or reconcile seat state after success, conflict, or not-found responses.
- The returned `orderId` should be preserved in the frontend model as the durable occupancy identifier for later features.
