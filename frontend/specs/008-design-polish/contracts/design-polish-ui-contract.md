# Design Polish UI Contract

## Purpose

Define the observable frontend behavior that the final design-polish pass must preserve while making the sushi-train experience more playful, funny, and kawaii.

## 1. Functional Continuity Contract

- The redesign must preserve the current guest-facing workflows for seat selection, dining start, plate picking, checkout, operator plate placement, and realtime belt updates.
- The redesign must not require backend contract changes or new primary user flows.
- The frontend may refine presentation and wording, but the meaning and availability of existing actions must remain intact.

## 2. Page Shell Contract

- The page shell must feel more authored and more like a playful kaiten-zushi cafe than the current version.
- Initial loading, empty, ready, degraded, and fatal-error states must remain clearly distinguishable.
- The belt stage must remain the dominant surface of the page, with decorative atmosphere staying secondary.

## 3. Belt Stage Readability Contract

Each rendered stage must preserve these observable qualities:

| Field       | Type                  | Description                                                                 |
| ----------- | --------------------- | --------------------------------------------------------------------------- |
| `beltName`  | `string`              | Visible or accessible identity of the current belt.                         |
| `slotCount` | `number`              | Current slot population context preserved from the existing view model.     |
| `slots`     | `Slot Presentation[]` | Stable rendered slot surfaces keyed to current authoritative slot identity. |
| `seats`     | `Seat Presentation[]` | Stable rendered seat surfaces keyed to current authoritative seat identity. |
| `reachArea` | `Reach Area \| null`  | Current seat reach cue when a seat is selected.                             |

For each `Slot Presentation`:

| Field           | Type                         | Description                                                       |
| --------------- | ---------------------------- | ----------------------------------------------------------------- |
| `slotId`        | `string`                     | Stable slot identity.                                             |
| `positionIndex` | `number`                     | Stable physical order already provided by the current experience. |
| `isOccupied`    | `boolean`                    | Whether the slot currently contains a visible plate.              |
| `isWithinReach` | `boolean`                    | Whether the slot is currently reachable from the selected seat.   |
| `plate`         | `Plate Presentation \| null` | Current plate presentation when present.                          |

For each `Seat Presentation`:

| Field            | Type                                     | Description                                |
| ---------------- | ---------------------------------------- | ------------------------------------------ |
| `seatId`         | `string`                                 | Stable seat identity.                      |
| `label`          | `string`                                 | Visible seat label.                        |
| `presenceCue`    | `'available' \| 'occupied' \| 'pending'` | Non-color-only state cue.                  |
| `isSelected`     | `boolean`                                | Whether the seat is the current selection. |
| `statusLabel`    | `string`                                 | Plain primary state label.                 |
| `secondaryLabel` | `string \| null`                         | Optional playful supporting label.         |

- The redesign may stretch or rebalance stage proportions, but it must not make slot order, seat placement, or action targets harder to read.
- The kitchen and chef center may become more expressive, but they must not interfere with the visibility of slots, plates, or seats.

## 4. Humor Layering Contract

- Important states must retain plain primary labels.
- Playful or funny language may appear as secondary labels, helper text, empty-state framing, celebratory notes, retry cues, or decorative headings.
- Decorative jokes, mascot cues, ingredient motifs, and kitchen personality must remain clearly non-essential.
- The humor should feel authored and consistent rather than random or high-frequency.

## 5. Feedback Contract

The redesigned UI must continue to expose clear feedback for these observable state categories:

| Outcome Type | Required Behavior                                                                          |
| ------------ | ------------------------------------------------------------------------------------------ |
| `loading`    | Clearly explain that data or a write action is in progress.                                |
| `success`    | Confirm the action or successful state plainly before any playful flourish.                |
| `error`      | State the problem clearly and avoid implying success.                                      |
| `conflict`   | Explain that the requested state changed or is no longer available.                        |
| `retry`      | Encourage recovery without hiding the actual condition.                                    |
| `degraded`   | Signal that the page is showing last known good information with stale or delayed refresh. |

- Humor may support these messages, but it must not replace their literal meaning.

## 6. Responsive And Reduced-Motion Contract

- Desktop and mobile layouts must preserve the same primary task clarity.
- Decorative elements and secondary copy may reduce in density on smaller layouts before any functional content is removed.
- Reduced-motion mode must preserve the same understanding of belt state, seat state, and feedback state as motion-enabled mode.
- The redesign must not depend on new continuous animation to communicate identity.

## 7. Non-Goals For This Contract

- No new guest workflows or operator workflows.
- No backend-driven theme or asset contract.
- No mascot-led game mechanics or novelty interactions.
- No visual direction that sacrifices belt readability for decoration.
