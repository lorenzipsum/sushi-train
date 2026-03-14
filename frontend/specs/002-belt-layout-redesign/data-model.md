# Data Model: Kaiten-Zushi Belt Redesign

## 1. Belt Snapshot

Authoritative rendering payload from `GET /api/v1/belts/{id}/snapshot`.

| Field                    | Type          | Source                                   | Notes                                                                    |
| ------------------------ | ------------- | ---------------------------------------- | ------------------------------------------------------------------------ |
| `beltId`                 | `string`      | `BeltSnapshotDto.beltId`                 | Stable backend identifier for the selected belt.                         |
| `beltName`               | `string`      | `BeltSnapshotDto.beltName`               | Human-readable name used in page copy and accessibility labels.          |
| `beltSlotCount`          | `number`      | `BeltSnapshotDto.beltSlotCount`          | Physical slot count that drives spacing and derived path distribution.   |
| `beltBaseRotationOffset` | `number`      | `BeltSnapshotDto.beltBaseRotationOffset` | Authoritative base slot offset for motion derivation.                    |
| `beltOffsetStartedAt`    | `string`      | `BeltSnapshotDto.beltOffsetStartedAt`    | ISO timestamp from which the current offset and speed state is measured. |
| `beltTickIntervalMs`     | `number`      | `BeltSnapshotDto.beltTickIntervalMs`     | Tick cadence used for motion interpolation.                              |
| `beltSpeedSlotsPerTick`  | `number`      | `BeltSnapshotDto.beltSpeedSlotsPerTick`  | Slot speed; `0` means paused.                                            |
| `slots`                  | `Belt Slot[]` | `BeltSnapshotDto.slots`                  | Stable slot identities ordered by physical `positionIndex`.              |

### Validation Rules

- `beltId`, `beltSlotCount`, and the returned slot array must remain backend-authoritative.
- A redesign may change layout geometry, but it must not reinterpret slot identity or mutate slot order.
- Motion derivation still requires `beltBaseRotationOffset`, `beltOffsetStartedAt`, `beltTickIntervalMs`, and `beltSpeedSlotsPerTick`.

## 2. Belt Slot

Stable physical conveyor position derived from the snapshot payload.

| Field           | Type                      | Source                              | Notes                                                            |
| --------------- | ------------------------- | ----------------------------------- | ---------------------------------------------------------------- |
| `slotId`        | `string`                  | `BeltSlotSnapshotDto.slotId`        | Stable slot identity and DOM key anchor.                         |
| `positionIndex` | `number`                  | `BeltSlotSnapshotDto.positionIndex` | Fixed physical order used to place the slot along the belt path. |
| `plate`         | `Plate Occupancy \| null` | `BeltSlotSnapshotDto.plate`         | Optional current plate state for this slot.                      |

### Validation Rules

- `slotId` and `positionIndex` must be preserved verbatim from the backend.
- Empty slots remain visible as part of the conveyor layout even when they contain no plate.

## 3. Plate Occupancy

Displayable dish state nested inside a slot.

| Field                    | Type                                              | Source                                    | Notes                                                               |
| ------------------------ | ------------------------------------------------- | ----------------------------------------- | ------------------------------------------------------------------- |
| `plateId`                | `string`                                          | `PlateSnapshotDto.plateId`                | Stable plate identity.                                              |
| `menuItemId`             | `string`                                          | `PlateSnapshotDto.menuItemId`             | Optional item identifier for future lookups or test fixtures.       |
| `menuItemName`           | `string`                                          | `PlateSnapshotDto.menuItemName`           | Primary visible identity used for visual family mapping.            |
| `tier`                   | `'GREEN' \| 'RED' \| 'GOLD' \| 'BLACK'`           | `PlateSnapshotDto.tier`                   | Accent and plate treatment metadata.                                |
| `status`                 | `'CREATED' \| 'ON_BELT' \| 'PICKED' \| 'EXPIRED'` | `PlateSnapshotDto.status`                 | Display-safe metadata for style and testing.                        |
| `priceAtCreation.amount` | `number`                                          | `PlateSnapshotDto.priceAtCreation.amount` | Optional supporting metadata; not required for primary recognition. |

### Validation Rules

- Occupied state is true only when `plate` exists.
- Menu item names must map to a visual family or a generic fallback without blocking rendering.
- Drinks, soups, desserts, and sushi items must remain visually distinguishable at the category level.

## 4. Menu Item Visual Family

Frontend-derived visual identity used to render recognizable dish art for a plate.

| Field            | Type                                                                                          | Derived From    | Notes                                                                  |
| ---------------- | --------------------------------------------------------------------------------------------- | --------------- | ---------------------------------------------------------------------- |
| `visualKey`      | `string`                                                                                      | Registry lookup | Stable key for the selected dish visual.                               |
| `family`         | `'nigiri' \| 'sashimi' \| 'roll' \| 'gunkan' \| 'side' \| 'dessert' \| 'drink' \| 'fallback'` | Registry lookup | High-level family used for shared art direction and fallback behavior. |
| `overrideName`   | `string \| null`                                                                              | Registry lookup | Optional item-specific override for distinctive seeded dishes.         |
| `vesselType`     | `'plate' \| 'bowl' \| 'cup' \| 'board'`                                                       | Registry lookup | Helps drinks and soups avoid reading like flat sushi pieces.           |
| `accentClass`    | `string`                                                                                      | Registry lookup | Small presentation token for family-specific styling.                  |
| `fallbackReason` | `string \| null`                                                                              | Registry lookup | Optional test/debug note when a generic fallback is used.              |

### Validation Rules

- Every known menu item must resolve to a family-level representation.
- Unknown menu items must resolve to the fallback family instead of rendering nothing.
- Visual mapping must be deterministic for the same menu item name.

## 5. Belt Layout Point

Pure frontend-derived geometry used to place slots and plates on the redesigned conveyor path.

| Field          | Type     | Derived From                                     | Notes                                                                                            |
| -------------- | -------- | ------------------------------------------------ | ------------------------------------------------------------------------------------------------ |
| `pathProgress` | `number` | `(positionIndex + renderOffset) / beltSlotCount` | Normalized progress around the squarer counter-loop path after timing-derived motion is applied. |
| `xPercent`     | `number` | Path layout helper                               | Position of the slot center within the stage surface.                                            |
| `yPercent`     | `number` | Path layout helper                               | Position of the slot center within the stage surface.                                            |
| `tangentDeg`   | `number` | Path layout helper                               | Optional orientation hint for plates or slot markers on straight/corner sections.                |
| `segment`      | `string` | Path layout helper                               | Describes whether the point falls on a straight run or a rounded corner.                         |

### Validation Rules

- Derived geometry must preserve the same order as `positionIndex`.
- Geometry changes must not imply that slots physically reorder themselves.
- The layout helper must remain responsive for varying slot counts and viewport sizes.

## 6. Seat Place

Frontend-rendered seat or counter-place state derived from seat overview data.

| Field           | Type      | Source / Derived From        | Notes                                                                      |
| --------------- | --------- | ---------------------------- | -------------------------------------------------------------------------- |
| `seatId`        | `string`  | `SeatStateDto.seatId`        | Stable seat identity.                                                      |
| `label`         | `string`  | `SeatStateDto.label`         | Visible short label.                                                       |
| `positionIndex` | `number`  | `SeatStateDto.positionIndex` | Stable order around the serving counter.                                   |
| `isOccupied`    | `boolean` | `SeatStateDto.isOccupied`    | Backend-authoritative occupied flag.                                       |
| `xPercent`      | `number`  | Seat layout helper           | Position of the seat/place within the page stage.                          |
| `yPercent`      | `number`  | Seat layout helper           | Position of the seat/place within the page stage.                          |
| `facingDeg`     | `number`  | Seat layout helper           | Presentation rotation applied to keep the seat silhouette facing the belt. |
| `presenceCue`   | `string`  | Derived from occupancy state | Presentation token for an in-use or available seat state.                  |

### Validation Rules

- Seat positions remain stable and never move with the belt.
- Occupied-state cues must remain understandable without relying only on color.
- Seat layout must work with varying seat counts from the backend and keep the seating rhythm visually balanced, including weighting longer top and bottom runs more heavily than the shorter left and right sides when seat counts increase.

## 7. Kitchen Core

Frontend-derived atmospheric stage content rendered inside the loop.

| Field            | Type       | Derived From       | Notes                                                                                |
| ---------------- | ---------- | ------------------ | ------------------------------------------------------------------------------------ |
| `counterInset`   | `string`   | Stage presentation | Shared token controlling the kitchen island footprint inside the visible inner lane. |
| `chefPresence`   | `boolean`  | Stage presentation | Indicates that a chef-prep silhouette should be rendered inside the kitchen island.  |
| `serviceAccents` | `string[]` | Stage presentation | Decorative prep details such as board, lamp, service tray, or mat accents.           |

### Validation Rules

- Kitchen-core content is atmospheric only and must not obscure slots, plates, or seat cues.
- The inner kitchen presentation must remain stable in paused and reduced-motion modes.

## 8. View Status

User-visible page state that survives the redesign.

| Field              | Type                                         | Meaning                                                                   |
| ------------------ | -------------------------------------------- | ------------------------------------------------------------------------- |
| `selectedBeltId`   | `string \| null`                             | `null` until the first belt list response resolves with a usable belt.    |
| `initialLoadState` | `'loading' \| 'ready' \| 'empty' \| 'error'` | First-load state before a successful snapshot is available.               |
| `freshnessState`   | `'current' \| 'degraded'`                    | Whether the last visible data reflects the most recent refresh attempts.  |
| `isPaused`         | `boolean`                                    | True when authoritative belt speed is zero.                               |
| `isReducedMotion`  | `boolean`                                    | Mirrors the user motion preference and disables continuous interpolation. |
| `lastErrorMessage` | `string \| null`                             | Human-readable message for degraded or fatal cases.                       |

### State Transitions

- `loading -> ready`: first successful belt snapshot arrives.
- `loading -> empty`: belt discovery succeeds but returns no usable belt.
- `loading -> error`: initial belt discovery or initial snapshot fails before any data is shown.
- `ready/current -> ready/degraded`: a later refresh fails while last-known-good state remains visible.
- `ready/degraded -> ready/current`: a subsequent refresh succeeds.

## Relationships

- One `Belt Snapshot` contains many `Belt Slot` records.
- One `Belt Slot` may contain zero or one `Plate Occupancy`.
- One `Plate Occupancy` resolves to one `Menu Item Visual Family` in the frontend.
- One `Belt Layout Point` is derived for each rendered slot from stable `positionIndex` order.
- One selected belt has many `Seat Place` records derived from the separate seat overview response.
- One `Kitchen Core` is rendered inside the loop as an atmospheric presentation layer.
- One `View Status` summarizes loading, freshness, pause, and reduced-motion state across the redesigned page.
