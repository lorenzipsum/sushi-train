# Data Model: Sushi Belt Visualization

## 1. Belt Snapshot

Authoritative belt rendering payload from `GET /api/v1/belts/{id}/snapshot`.

| Field                    | Type          | Source                                   | Notes                                                                |
| ------------------------ | ------------- | ---------------------------------------- | -------------------------------------------------------------------- |
| `beltId`                 | `string`      | `BeltSnapshotDto.beltId`                 | Stable backend identifier for the selected belt.                     |
| `beltName`               | `string`      | `BeltSnapshotDto.beltName`               | Human-readable label for the page header.                            |
| `beltSlotCount`          | `number`      | `BeltSnapshotDto.beltSlotCount`          | Total number of physical slots; drives ring spacing.                 |
| `beltBaseRotationOffset` | `number`      | `BeltSnapshotDto.beltBaseRotationOffset` | Authoritative base offset in slot units.                             |
| `beltOffsetStartedAt`    | `string`      | `BeltSnapshotDto.beltOffsetStartedAt`    | ISO timestamp from which the current offset/speed state is measured. |
| `beltTickIntervalMs`     | `number`      | `BeltSnapshotDto.beltTickIntervalMs`     | Milliseconds per backend movement tick.                              |
| `beltSpeedSlotsPerTick`  | `number`      | `BeltSnapshotDto.beltSpeedSlotsPerTick`  | Slots moved per tick; `0` means paused.                              |
| `slots`                  | `Belt Slot[]` | `BeltSnapshotDto.slots`                  | Stable slot identities ordered by physical `positionIndex`.          |

### Validation Rules

- `beltId` and `beltSlotCount` must exist before the visualization is considered renderable.
- `beltSlotCount` must be greater than `0`.
- `slots.length` should match `beltSlotCount`; if it does not, rendering still uses the returned `slots` array and marks freshness as degraded in diagnostics/tests.
- Motion interpolation requires all of `beltBaseRotationOffset`, `beltOffsetStartedAt`, `beltTickIntervalMs`, and `beltSpeedSlotsPerTick`.

## 2. Belt Slot

Stable physical belt position from the snapshot payload.

| Field           | Type                      | Source                              | Notes                                      |
| --------------- | ------------------------- | ----------------------------------- | ------------------------------------------ |
| `slotId`        | `string`                  | `BeltSlotSnapshotDto.slotId`        | Stable physical slot identity.             |
| `positionIndex` | `number`                  | `BeltSlotSnapshotDto.positionIndex` | Fixed physical order around the belt ring. |
| `plate`         | `Plate Occupancy \| null` | `BeltSlotSnapshotDto.plate`         | Optional current plate state for the slot. |

### Validation Rules

- `slotId` and `positionIndex` must be preserved verbatim from the backend.
- The frontend must not synthesize or redistribute plates if `plate` is missing.

## 3. Plate Occupancy

Displayable plate information nested inside a slot.

| Field                    | Type                                              | Source                                    | Notes                                                                   |
| ------------------------ | ------------------------------------------------- | ----------------------------------------- | ----------------------------------------------------------------------- |
| `plateId`                | `string`                                          | `PlateSnapshotDto.plateId`                | Stable plate identity.                                                  |
| `menuItemId`             | `string`                                          | `PlateSnapshotDto.menuItemId`             | Useful for future lookups; not required for MVP rendering.              |
| `menuItemName`           | `string`                                          | `PlateSnapshotDto.menuItemName`           | Primary visible label.                                                  |
| `tier`                   | `'GREEN' \| 'RED' \| 'GOLD' \| 'BLACK'`           | `PlateSnapshotDto.tier`                   | Maps to accent colors and visual treatment.                             |
| `priceAtCreation.amount` | `number`                                          | `PlateSnapshotDto.priceAtCreation.amount` | Optional supporting metadata.                                           |
| `status`                 | `'CREATED' \| 'ON_BELT' \| 'PICKED' \| 'EXPIRED'` | `PlateSnapshotDto.status`                 | MVP primarily expects on-belt plates; other states remain display-safe. |
| `expiresAt`              | `string`                                          | `PlateSnapshotDto.expiresAt`              | Optional freshness metadata for future enhancements.                    |

### Validation Rules

- A slot is considered occupied only when `plate` is present.
- Tier-based styling must preserve readable contrast against the warm page palette.

## 4. Seat State

Authoritative seat occupancy payload from `GET /api/v1/belts/{id}/seats`.

| Field           | Type      | Source                       | Notes                                      |
| --------------- | --------- | ---------------------------- | ------------------------------------------ |
| `seatId`        | `string`  | `SeatStateDto.seatId`        | Stable seat identity.                      |
| `label`         | `string`  | `SeatStateDto.label`         | Display label for the seat marker.         |
| `positionIndex` | `number`  | `SeatStateDto.positionIndex` | Fixed position around the outer seat ring. |
| `isOccupied`    | `boolean` | `SeatStateDto.isOccupied`    | Drives occupied/unoccupied stool styling.  |

### Validation Rules

- Seat positions remain fixed and are never adjusted by belt motion.
- The frontend must render whatever seat count the endpoint returns and must not assume a fixed mapping from slots to seats.

## 5. Visual Rotation State

Pure frontend-derived state used to place stable slots on screen.

| Field                      | Type      | Derived From                               | Notes                                      |
| -------------------------- | --------- | ------------------------------------------ | ------------------------------------------ |
| `authoritativeOffsetSlots` | `number`  | `beltBaseRotationOffset`                   | Base snapshot offset before interpolation. |
| `derivedOffsetSlots`       | `number`  | Offset, speed, tick interval, current time | Fractional slot offset used for animation. |
| `isMoving`                 | `boolean` | `beltSpeedSlotsPerTick > 0`                | Controls motion status and interpolation.  |
| `isReducedMotion`          | `boolean` | `matchMedia` preference                    | Forces discrete rendering only.            |
| `renderedAt`               | `number`  | `Date.now()` or animation frame time       | Used only for pure calculation inputs.     |

### State Transitions

- `paused -> moving`: when a fresh snapshot returns `beltSpeedSlotsPerTick > 0` with valid timing fields.
- `moving -> paused`: when a fresh snapshot returns `beltSpeedSlotsPerTick = 0`.
- `moving -> moving with jump`: when a fresh snapshot changes offset, tick interval, or speed; the frontend trusts the new snapshot immediately.
- `moving -> reduced-motion discrete`: when the user preference switches to reduced motion.

## 6. View Status

User-visible page state for loading and freshness.

| Field              | Type                                         | Meaning                                                                              |
| ------------------ | -------------------------------------------- | ------------------------------------------------------------------------------------ |
| `selectedBeltId`   | `string \| null`                             | `null` until the first belt list response resolves with a usable belt.               |
| `initialLoadState` | `'loading' \| 'ready' \| 'empty' \| 'error'` | Controls the first page experience before a successful snapshot/seat pair exists.    |
| `freshnessState`   | `'current' \| 'degraded'`                    | Indicates whether the latest visible data reflects the most recent refresh attempts. |
| `beltDataAgeMs`    | `number \| null`                             | Age of last successful belt snapshot, used for stale messaging.                      |
| `seatDataAgeMs`    | `number \| null`                             | Age of last successful seat overview, used for stale messaging.                      |
| `lastErrorMessage` | `string \| null`                             | Human-readable recovery hint for degraded or empty states.                           |

### State Transitions

- `loading -> ready`: first successful belt selection plus snapshot and seat data available.
- `loading -> empty`: belt list is empty.
- `loading -> error`: belt discovery fails before any successful data is shown.
- `ready/current -> ready/degraded`: a refresh fails after prior success.
- `ready/degraded -> ready/current`: a later refresh succeeds.

## Relationships

- One `Belt Snapshot` contains many `Belt Slot` records.
- One `Belt Slot` may contain zero or one `Plate Occupancy`.
- One selected belt has many `Seat State` records, fetched separately from the snapshot.
- One `Visual Rotation State` is derived from one latest successful `Belt Snapshot` plus client time and motion preference.
- One `View Status` summarizes the coordination of belt discovery, belt snapshot freshness, and seat overview freshness.
