# Refine Belt Interactions UI Contract

## Purpose

Define the observable frontend behavior for improved seat reach, clearer selection cues, ring-only moving plates, and modal-based belt speed control inside the existing sushi-belt experience.

## 1. Seat Reach Contract

- The frontend exposes a visible pickup reach zone for the currently selected seat.
- Top-row seats that previously could not pick intended nearby plates must receive a slightly larger effective reach zone.
- The effective pickup rule must remain authoritative for actual pickability of nearby plates.
- The visible reach bubble and lit slot cues may be tuned separately for clarity, as long as they continue to read as belonging to the selected seat and do not falsely imply that out-of-range plates are pickable.
- Plates outside the selected seat's intended reach remain non-pickable.

## 2. Selected Seat Clarity Contract

- The currently selected seat must remain visually distinct from all non-selected seats.
- The selected seat's reach zone must read as belonging to that seat without requiring explanatory text.
- Changing the selected seat must immediately move the active emphasis to the new seat and remove it from the old one.
- Clarity improvements may use light, glow, or stronger contrast, but must not depend on constant motion.

## 3. Plate Simplification Contract

- Default moving plates render as ring-only visuals that preserve the tier color cue.
- The current inner item or garnish graphic is not shown in the default moving plate state.
- Pickable, pending, and rejected states remain visible even after the plate is simplified.
- The simplified plate remains readable against the belt stage during motion.

## 4. Belt Speed Modal Contract

The speed-control flow exposes these observable modal properties:

| Field           | Type             | Description                                                    |
| --------------- | ---------------- | -------------------------------------------------------------- |
| `isOpen`        | `boolean`        | Whether the speed dialog is visible.                           |
| `currentSpeed`  | `number \| null` | Current authoritative belt speed shown when the dialog opens.  |
| `selectedSpeed` | `number \| null` | The speed option currently chosen by the user.                 |
| `allowedSpeeds` | `number[]`       | The selectable speed values the user may choose from.          |
| `isSubmitting`  | `boolean`        | Whether the modal is currently sending a speed change request. |

- The cogwheel in the belt stage opens the speed modal from the main belt experience.
- The modal can be dismissed without changing speed.
- The modal must show which speed is currently active and which new speed is selected.
- The modal submits only valid speed choices allowed by the backend contract.

## 5. Speed Update Outcome Contract

- A successful speed change shows clear confirmation and the belt reflects the updated speed after authoritative refresh.
- Canceling or closing the modal without confirmation leaves the current speed unchanged.
- Choosing the current speed must not imply that a backend update happened; the flow may communicate this either through disabled submission state or unchanged-state messaging.
- If a speed update fails, the frontend shows a clear failure message and keeps the previous speed intact.

## 6. Layout Preservation Contract

- The feature remains inside the current single-screen sushi-belt experience.
- The speed change flow uses a modal overlay rather than expanding the page into a separate settings surface.
- Reach, selection, and plate-visual changes must preserve or improve the readability of the belt stage on desktop and smaller-screen layouts.
- The feature must not change the core dining flow for selecting seats, picking plates, starting dining, or checking out.
