# Belt Visualization UI Contract

## Purpose

Define the observable behavior that the Angular frontend must preserve while implementing the first read-only sushi belt overview.

## 1. Upstream Inputs

### Belt Discovery

- **Source**: `BeltsApi.getAllBelts()`
- **Contract**:
  - Returns an ordered list of belt summaries.
  - The frontend tracks the first belt with a non-empty `id` for MVP rendering.
  - If the list is empty, the page enters an `empty` first-load state instead of showing a broken visualization.

### Belt Snapshot

- **Source**: `BeltsApi.getBeltSnapshot(beltId)`
- **Contract**:
  - Provides authoritative belt identity, slot count, timing metadata, and slot occupancy.
  - Slots remain keyed by `slotId` and ordered by stable `positionIndex`.
  - The frontend may derive on-screen angles from timing fields, but it must not mutate slot identity or reorder the authoritative slot list to simulate motion.

### Seat Overview

- **Source**: `BeltsApi.getSeatOverview(beltId)`
- **Contract**:
  - Provides authoritative seat occupancy and stable `positionIndex` values.
  - Seat positions remain fixed on screen regardless of belt rotation.

### Motion Preference

- **Source**: `window.matchMedia('(prefers-reduced-motion: reduce)')`
- **Contract**:
  - When true, the frontend disables continuous interpolation.
  - The page still shows the latest authoritative belt and seat state, loading state, pause state, and freshness indicators.

### Future Manual Refresh Trigger

- **Source**: feature-local `refreshAfterWrite()` entry point
- **Contract**:
  - A future successful write flow may call this trigger to request an immediate read refresh.
  - The trigger must not require routing or global event infrastructure in this slice.

## 2. Polling And Freshness Contract

- Initial sequence:
  1. Fetch the belt list.
  2. Track the first belt ID.
  3. Fetch belt snapshot and seat overview in parallel.
- First ready render:
  - The page may transition out of its initial loading state once the first authoritative belt snapshot is available.
  - Seat data may arrive later or degrade independently while the last valid seat state remains visible.
- Repeat sequence:
  - Poll snapshot and seat overview every 3 seconds.
- Failure behavior:
  - If a refresh fails before any successful data exists, show a first-load error state.
  - If a refresh fails after prior success, preserve the last successful belt and seat renderings and mark the view as degraded.
  - Belt and seat data freshness are tracked independently, but the page may show one combined degraded indicator.

## 3. Rendered View-Model Contract

### Belt Stage Item

Each rendered slot on the circular stage exposes:

| Field            | Type             | Description                                         |
| ---------------- | ---------------- | --------------------------------------------------- |
| `slotId`         | `string`         | Stable DOM key and future interaction anchor.       |
| `positionIndex`  | `number`         | Stable physical order from the backend.             |
| `visualAngleDeg` | `number`         | Derived angle used for rendering on the ring.       |
| `isOccupied`     | `boolean`        | True only when a plate is present in the snapshot.  |
| `plateLabel`     | `string \| null` | Menu item name for visible occupied slots.          |
| `plateTier`      | `string \| null` | Tier token used for Kawaii color accents.           |
| `plateStatus`    | `string \| null` | Supporting status metadata for styling and testing. |

### Seat Marker

Each rendered seat exposes:

| Field            | Type             | Description                                  |
| ---------------- | ---------------- | -------------------------------------------- |
| `seatId`         | `string`         | Stable DOM key.                              |
| `positionIndex`  | `number`         | Stable fixed order around the seat ring.     |
| `visualAngleDeg` | `number`         | Fixed angle derived only from seat position. |
| `label`          | `string \| null` | Visible short label.                         |
| `isOccupied`     | `boolean`        | Occupied/unoccupied stool treatment.         |

### Status Surface

The page-level status surface exposes:

| Field              | Type                                 | Description                                                    |
| ------------------ | ------------------------------------ | -------------------------------------------------------------- |
| `initialLoadState` | `loading \| ready \| empty \| error` | First page state.                                              |
| `freshnessState`   | `current \| degraded`                | Whether the visible data reflects the latest refresh attempts. |
| `isPaused`         | `boolean`                            | True when authoritative belt speed is zero.                    |
| `isReducedMotion`  | `boolean`                            | Mirrors the user preference.                                   |
| `selectedBeltName` | `string \| null`                     | Visible page heading.                                          |

## 4. Accessibility And Presentation Contract

- The page must use semantic structure with a clear heading and support text.
- Status changes for loading and degraded freshness must be announced through accessible text, not color alone.
- Belt motion is the only continuous animation; all other transitions remain subtle.
- Kawaii styling must preserve readable contrast and clear distinction between occupied and unoccupied seats and slots.

## 5. Non-Goals For This Contract

- No seat interaction, pickup flow, checkout flow, or admin controls.
- No assumption of fixed slot count, seat count, or slot-to-seat mapping.
- No multi-belt picker or route-driven belt navigation in this slice.
