# Belt Layout Redesign UI Contract

## Purpose

Define the observable behavior that the Angular frontend must preserve while redesigning the belt overview from a circular stage into a more realistic kaiten-zushi counter layout.

## 1. Upstream Inputs

### Belt Discovery

- **Source**: `BeltsApi.getAllBelts()`
- **Contract**:
  - Returns an ordered list of belt summaries.
  - The frontend continues to track the first belt with a non-empty `id`.
  - An empty list still results in an `empty` first-load state.

### Belt Snapshot

- **Source**: `BeltsApi.getBeltSnapshot(beltId)`
- **Contract**:
  - Provides authoritative belt identity, slot count, timing metadata, and slot occupancy.
  - Slots remain keyed by `slotId` and ordered by stable `positionIndex`.
  - The redesign may change surface geometry, but it must not reorder or rename authoritative slots.

### Seat Overview

- **Source**: `BeltsApi.getSeatOverview(beltId)`
- **Contract**:
  - Provides authoritative seat occupancy and stable `positionIndex` values.
  - Seat places remain fixed relative to the counter layout and do not move with the belt.

### Motion Preference

- **Source**: `window.matchMedia('(prefers-reduced-motion: reduce)')`
- **Contract**:
  - When true, the frontend disables continuous interpolation.
  - The page must still communicate dish placement, seat occupancy, pause state, and freshness clearly.

### Future Manual Refresh Trigger

- **Source**: feature-local `refreshAfterWrite()` entry point
- **Contract**:
  - Future successful write flows may request an immediate read refresh through this hook.
  - The redesign must not remove this shared refresh path.

## 2. Polling And Freshness Contract

- Initial sequence:
  1. Fetch the belt list.
  2. Track the first belt ID.
  3. Fetch belt snapshot and seat overview in parallel.
- First ready render:
  - The page may leave its initial loading state once the first authoritative belt snapshot is available.
  - Seat data may arrive later while the belt hero already renders.
- Repeat sequence:
  - Poll belt snapshot and seat overview every 3 seconds.
- Failure behavior:
  - Preserve the last successful belt and seat rendering during recoverable failures.
  - Surface degraded freshness without blanking the redesigned stage.

## 3. Rendered View Contract

### Belt Hero Surface

- The page must show a wide horizontal conveyor hero area above any compact status rail or secondary notice surface.
- The belt must read as a sushi-train counter rather than a centered circular dashboard diagram.
- The belt loop should read as a rounded-square counter with longer straight runs, not as a highly elliptical ring.
- The belt must remain the dominant visual element on desktop and mobile.
- The inner area of the loop must read as working kitchen space, including a chef-prep presence.

### Belt Stage Item

Each rendered slot on the redesigned conveyor exposes:

| Field           | Type                         | Description                                                       |
| --------------- | ---------------------------- | ----------------------------------------------------------------- |
| `slotId`        | `string`                     | Stable DOM key and future interaction anchor.                     |
| `positionIndex` | `number`                     | Stable physical order from the backend.                           |
| `pathProgress`  | `number`                     | Derived normalized position along the squarer counter-loop path.  |
| `surfaceX`      | `number`                     | Derived screen position within the redesigned stage.              |
| `surfaceY`      | `number`                     | Derived screen position within the redesigned stage.              |
| `segment`       | `string`                     | Derived path segment such as straight run or rounded corner.      |
| `tangentDeg`    | `number`                     | Derived orientation hint used for plate presentation.             |
| `plate`         | `Plate Presentation \| null` | Optional rendered plate presentation metadata for occupied slots. |

When `plate` is present, the rendered plate presentation exposes:

| Field              | Type     | Description                                                        |
| ------------------ | -------- | ------------------------------------------------------------------ |
| `plateLabel`       | `string` | Visible and accessible plate description.                          |
| `menuVisualKey`    | `string` | Visual registry key used for dish presentation.                    |
| `menuVisualFamily` | `string` | Dish family such as nigiri, roll, gunkan, side, dessert, or drink. |
| `plateTier`        | `string` | Tier token used for plate accents.                                 |

### Seat Place

Each rendered seat/place exposes:

| Field           | Type             | Description                                                  |
| --------------- | ---------------- | ------------------------------------------------------------ |
| `seatId`        | `string`         | Stable DOM key.                                              |
| `positionIndex` | `number`         | Stable fixed order around the counter layout.                |
| `label`         | `string \| null` | Visible short label.                                         |
| `surfaceX`      | `number`         | Derived seat position within the page stage.                 |
| `surfaceY`      | `number`         | Derived seat position within the page stage.                 |
| `facingDeg`     | `number`         | Derived orientation hint used to keep seats facing the belt. |
| `isOccupied`    | `boolean`        | Occupied/unoccupied state from the backend.                  |
| `presenceCue`   | `string`         | Non-color-only cue indicating whether the place is in use.   |

- Seat places must remain stable, belt-facing, independent from belt motion, and distributed in proportion to counter side length so the longer top and bottom runs can carry more seats than the shorter sides.

### Status Surface

The page-level status surface exposes:

| Field              | Type                                 | Description                                            |
| ------------------ | ------------------------------------ | ------------------------------------------------------ |
| `initialLoadState` | `loading \| ready \| empty \| error` | First page state.                                      |
| `freshnessState`   | `current \| degraded`                | Whether visible data reflects recent refresh attempts. |
| `isPaused`         | `boolean`                            | True when authoritative belt speed is zero.            |
| `isReducedMotion`  | `boolean`                            | Mirrors the user preference.                           |
| `selectedBeltName` | `string \| null`                     | Visible belt identity if shown in copy.                |

## 4. Menu Item Visual Mapping Contract

- Known menu items must resolve to a dish visual family or an item-specific override.
- Unknown menu items must render a generic fallback treatment instead of blank content.
- Major families must remain distinguishable at a glance: nigiri, sashimi, rolls, gunkan, sides, desserts, and drinks.
- Drinks and soups may use vessel-specific visuals when that improves recognition.

## 5. Accessibility And Presentation Contract

- The page must use semantic structure with a clear main content hierarchy.
- Occupied seats, paused state, degraded freshness, and reduced-motion state must not rely on color alone.
- Reduced-motion mode must preserve the same reading order and core understanding as the animated mode.
- The page must not show duplicate primary titling such as two visible “Main Belt” headings.
- Degraded freshness should remain visible on the page shell and may also apply a subtle visual warning treatment to the stage without blanking the last known good layout.
- A compact metrics rail or inline notice beneath the stage is acceptable in place of larger secondary side panels, as long as the belt remains the primary visual focus.
- Chef or kitchen visuals are atmospheric only and must not interfere with slot, plate, or seat readability.
- Decorative plate detailing should improve the sense of real kaitenzushi dishes without undermining dish-family readability.

## 6. Non-Goals For This Contract

- No pickup, ordering, checkout, or admin flows.
- No multi-belt selection UI.
- No backend-driven visual asset contract.
- No requirement to implement a complex serpentine belt path in this feature.
