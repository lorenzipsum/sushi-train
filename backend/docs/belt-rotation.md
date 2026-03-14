# Belt Rotation And Speed

This document explains the belt rotation functionality end-to-end from the backend so a frontend agent can derive the correct frontend behavior.

## 1. Core Backend Model

The backend does not physically rotate plates between slots in the database.

Instead, the backend stores:

- a stable slot list with fixed `positionIndex`
- a stable seat list with fixed `positionIndex`
- belt timing metadata:
  - `baseRotationOffset`
  - `offsetStartedAt`
  - `tickIntervalMs`
  - `speedSlotsPerTick`

Rotation is virtual and time-based.

Relevant source:

- [Belt domain model](../src/main/java/com/lorenzipsum/sushitrain/backend/domain/belt/Belt.java)

## 2. Rotation Formula

The live rotation offset is computed from time:

```text
elapsedMs = max(0, now - offsetStartedAt)
elapsedTicks = floor(elapsedMs / tickIntervalMs)
currentOffset = (baseRotationOffset + elapsedTicks * speedSlotsPerTick) % slotCount
```

To render a plate visually:

```text
visualPositionIndex = (slot.positionIndex + currentOffset) % slotCount
```

This means:

- `slot.positionIndex` is the physical slot identity
- `visualPositionIndex` is the client-derived current on-screen location

Important implications:

- the API does not return already-rotated slot order
- the frontend must compute the current visual position itself

Backend implementation:

- [Belt domain model](../src/main/java/com/lorenzipsum/sushitrain/backend/domain/belt/Belt.java)

## 3. Belt Speed Semantics

`tickIntervalMs`:

- how often the belt advances
- lower value means more frequent ticks
- min `100`
- max `5000`
- default `500`

`speedSlotsPerTick`:

- how many slots are advanced per tick
- min `0`
- max `5`
- default `1`
- `0` means paused

Limits are defined in:

- [Belt domain model](../src/main/java/com/lorenzipsum/sushitrain/backend/domain/belt/Belt.java)

Request validation is defined in:

- [Belt update request DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltUpdateRequest.java)

## 4. Important Behavior: Rotation Is Metadata, Not Slot Mutation

The backend snapshot query returns slots ordered by physical `positionIndex`.

It also returns belt timing parameters alongside the slot and plate data.

Relevant query and mapping:

- [Belt snapshot query](../src/main/java/com/lorenzipsum/sushitrain/backend/infrastructure/persistence/jpa/query/BeltJpaQuery.java)
- [Belt view and slot allocation adapter](../src/main/java/com/lorenzipsum/sushitrain/backend/infrastructure/persistence/jpa/adapter/JpaBeltViewAndSlotAllocationAdapter.java)
- [Belt API mapper](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltApiMapper.java)

This is the key frontend rule:

- treat slots as static storage
- treat movement as a client-side rendering calculation

## 5. Relevant REST Endpoints

### 5.1 Read Belt Overview

`GET /api/v1/belts`

Returns all belts with metadata including:

- `id`
- `name`
- `slotCount`
- `baseRotationOffset`
- `tickIntervalMs`
- `speedSlotsPerTick`
- `offsetStartedAt`

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)
- [Belt DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltDto.java)

### 5.2 Read Full Belt

`GET /api/v1/belts/{id}`

Returns:

- belt metadata
- all slots
- all seats

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)
- [Full belt DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/FullBeltDto.java)

### 5.3 Read Belt Snapshot For Rendering

`GET /api/v1/belts/{id}/snapshot`

This is the best endpoint for frontend belt rendering.

It returns:

- `beltId`
- `beltName`
- `beltSlotCount`
- `beltBaseRotationOffset`
- `beltOffsetStartedAt`
- `beltTickIntervalMs`
- `beltSpeedSlotsPerTick`
- ordered `slots`
- each slot may contain an optional `plate`

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)
- [Belt snapshot DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltSnapshotDto.java)
- [Belt slot snapshot DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltSlotSnapshotDto.java)

### 5.4 Read Seat Occupancy For A Belt

`GET /api/v1/belts/{id}/seats`

Returns:

- seat id
- label
- position index
- occupied/free state

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)

### 5.5 Update Belt Speed / Timing

`PATCH /api/v1/belts/{id}`

Body can contain one or both:

```json
{
  "tickIntervalMs": 250,
  "speedSlotsPerTick": 2
}
```

Validation rules:

- at least one field must be provided
- `tickIntervalMs` in `[100, 5000]`
- `speedSlotsPerTick` in `[0, 5]`

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)
- [Belt service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/belt/BeltService.java)
- [Belt update request DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/dto/BeltUpdateRequest.java)

### 5.6 Create Plates On Belt

`POST /api/v1/belts/{id}/plates`

Body example:

```json
{
  "menuItemId": "uuid",
  "numOfPlates": 2,
  "tierSnapshot": "GREEN",
  "priceAtCreation": 450,
  "expiresAt": "2026-03-14T12:00:00Z"
}
```

Relevant files:

- [Belt controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/belt/BeltController.java)
- [Belt service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/belt/BeltService.java)

### 5.7 Seat Interaction Endpoints

Occupy seat:

- `POST /api/v1/seats/{id}/occupy`

Read seat state:

- `GET /api/v1/seats/{id}`

Pick plate for seat:

- `POST /api/v1/seats/{id}/order-lines`

Request body:

```json
{
  "plateId": "uuid"
}
```

Checkout:

- `POST /api/v1/seats/{id}/checkout`

Relevant files:

- [Seat controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/seat/SeatController.java)
- [Order service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/order/OrderService.java)
- [Pick plate request DTO](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/seat/dto/PickPlateRequest.java)

### 5.8 Expire Plate

`POST /api/v1/plates/{id}/expire`

This:

- marks the plate as expired
- clears its slot assignment

Relevant files:

- [Plate controller](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/plate/PlateController.java)
- [Plate service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/plate/PlateService.java)

## 6. How Plate Placement Works

When creating plates on the belt, the backend:

1. verifies the belt exists
2. loads free slots with locking
3. checks free slot count
4. chooses target slots
5. creates plate records
6. changes plate state to `ON_BELT`
7. assigns plates to the selected slots

Relevant service:

- [Belt service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/belt/BeltService.java)

### 6.1 Spacing Rule

The backend has a configurable plate spacing rule:

- `app.belt.placement.min-empty-slots-between-new-plates`

Default:

- `5`

Relevant config:

- [Application configuration](../src/main/resources/application.yaml)
- [Belt placement properties](../src/main/java/com/lorenzipsum/sushitrain/backend/infrastructure/config/BeltPlacementProperties.java)

Placement algorithm:

- first pass prefers the gap rule
- second pass fills remaining requests even if the gap rule must be ignored
- wrap-around spacing is explicitly ignored

Relevant file:

- [Belt slot placement logic](../src/main/java/com/lorenzipsum/sushitrain/backend/application/belt/BeltSlotPlacement.java)

Frontend implication:

- do not assume new plates are always evenly spaced
- trust the returned assigned slot positions or the next snapshot

## 7. How Picking A Plate Works

When a seat picks a plate, the backend:

1. loads the seat
2. loads the plate
3. loads the corresponding menu item
4. verifies the seat has an open order
5. changes the plate state to picked
6. clears the plate's slot assignment
7. creates an order line from the plate data

Relevant file:

- [Order service](../src/main/java/com/lorenzipsum/sushitrain/backend/application/order/OrderService.java)

Important limitation:

- the backend does not check whether the plate is currently in front of the seat
- the backend only checks seat/order state and plate pickability

Frontend implication:

- if the product should only allow a seat to pick a visually reachable plate, that rule must be enforced in the frontend

## 8. Plate State Rules

Relevant domain file:

- [Plate domain model](../src/main/java/com/lorenzipsum/sushitrain/backend/domain/plate/Plate.java)

Behavior:

- `place()` moves the state to `ON_BELT`
- `pick()` fails only for expired plates
- `expire()` fails only for picked plates

Practical frontend implications:

- expired plates should disappear from the belt after refresh
- picked plates should disappear from the belt after refresh
- the backend does not enforce a visual seat-to-plate reachability rule

## 9. Demo Animator As Reference Implementation

The demo console animator is the clearest backend-side expression of intended rendering behavior.

It:

- computes live offset using `belt.currentOffsetAt(Instant.now())`
- renders each plate at `floorMod(slot.positionIndex + offset, slotCount)`

Relevant file:

- [Demo belt console animator](../src/main/java/com/lorenzipsum/sushitrain/backend/infrastructure/demo/DemoBeltConsoleAnimator.java)

This should be treated as the reference frontend rendering model.

## 10. Frontend Derivation Rules

Recommended frontend source of truth:

- use `GET /api/v1/belts/{id}/snapshot` as the main belt rendering payload
- use `GET /api/v1/belts/{id}/seats` for seat occupancy overview
- optionally use `GET /api/v1/seats/{id}` for seat-specific order state

Recommended frontend behavior:

1. fetch snapshot
2. compute current offset continuously from client time
3. map each slot to a visual position
4. render plates based on derived visual positions
5. refresh snapshot after mutations like:
   - create plates
   - pick plate
   - expire plate
   - update speed/timing

## 11. Suggested Frontend Formulas

```ts
type BeltSnapshot = {
  beltId: string;
  beltSlotCount: number;
  beltBaseRotationOffset: number;
  beltOffsetStartedAt: string;
  beltTickIntervalMs: number;
  beltSpeedSlotsPerTick: number;
  slots: Array<{
    slotId: string;
    positionIndex: number;
    plate: null | {
      plateId: string;
      menuItemId: string;
      menuItemName: string;
      tier: string;
      priceAtCreation: { amount: number };
      status: string;
      expiresAt: string;
    };
  }>;
};

function getCurrentOffset(snapshot: BeltSnapshot, nowMs: number): number {
  const startedAtMs = Date.parse(snapshot.beltOffsetStartedAt);
  const elapsedMs = Math.max(0, nowMs - startedAtMs);
  const elapsedTicks = Math.floor(elapsedMs / snapshot.beltTickIntervalMs);
  return (
    (snapshot.beltBaseRotationOffset +
      elapsedTicks * snapshot.beltSpeedSlotsPerTick) %
    snapshot.beltSlotCount
  );
}

function getVisualPositionIndex(
  slotPositionIndex: number,
  currentOffset: number,
  slotCount: number
): number {
  return (slotPositionIndex + currentOffset) % slotCount;
}
```

## 12. Optional Frontend-Only Seat Reachability Rule

If the UI should only allow picking when a plate is in front of a seat, that logic must be client-side.

Example:

```ts
function isPlateReachableBySeat(
  plateVisualIndex: number,
  seatPositionIndex: number,
  slotCount: number,
  tolerance = 0
): boolean {
  const delta = Math.abs(plateVisualIndex - seatPositionIndex);
  const wrapped = slotCount - delta;
  return Math.min(delta, wrapped) <= tolerance;
}
```

This is not enforced by the backend.

## 13. Polling And Animation Strategy

Recommended:

- poll snapshot every `2` to `5` seconds
- animate between polls using the local formula and local clock
- force immediate refresh after writes

Reason:

- no push mechanism was found in the backend
- the backend returns enough timing data for client-side animation

## 14. Error Handling Contract

Global errors are mapped in:

- [Controller advice](../src/main/java/com/lorenzipsum/sushitrain/backend/interfaces/rest/common/ControllerAdvice.java)

Main cases:

- `400` invalid parameter, malformed JSON, validation error
- `404` missing belt, seat, plate, menu item
- `409` not enough free slots
- `409` seat already occupied
- `409` seat not occupied
- `409` plate not pickable

Useful frontend handling:

- refresh snapshot on `409 plate not pickable`
- prompt to occupy seat on `409 seat not occupied`
- reduce batch size on `409 not enough free slots`

## 15. Local Seeded Default Data

Seeded local belt:

- name: `Main Belt`
- slot count: `192`
- tick interval: `500`
- speed slots per tick: `1`
- seat count: `24`
- seat positions: every `8` slots

Relevant migration:

- [Belt seed migration](../src/main/resources/db/migration/V3__seed_belt.sql)

## 16. Known Caveats In Current Backend

### 16.1 Speed/Timing Update May Jump

The current implementation updates the speed/tick value and then rebases the offset at `now`.

That likely means a speed or tick change can cause a visible jump instead of continuity.

Relevant code:

- [Belt domain model](../src/main/java/com/lorenzipsum/sushitrain/backend/domain/belt/Belt.java)

Frontend expectation:

- after `PATCH`, trust the returned persisted values
- if a visual jump occurs, assume that is backend-consistent

### 16.2 Pickup Does Not Check Seat Proximity

The backend currently allows any occupied seat to pick any pickable plate by `plateId`.

Frontend expectation:

- enforce proximity in UI if desired

### 16.3 New Plate Spacing Is Best-Effort

Gap placement is preferred, but not guaranteed for all requested plates.

Frontend expectation:

- never infer plate positions
- always render from returned snapshot data

## 17. Recommended Agent Contract

Another frontend agent implementing from this backend should assume:

- authoritative belt render data: `GET /api/v1/belts/{id}/snapshot`
- authoritative seat occupancy: `GET /api/v1/belts/{id}/seats`
- belt rotation is virtual
- client computes motion continuously
- slots remain physically stable in API responses
- writes should be followed by refresh

In short:

- backend owns the physical state
- frontend owns the visual rotation
