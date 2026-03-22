# Pick Plates UI Contract

## Purpose

Define the observable frontend behavior for selected-seat dining, seat-anchored reach visualization, easy plate picking, and reject feedback while preserving the current sushi belt presentation.

## 1. Layout Preservation Contract

- The existing belt, seat, and stage UI remains the primary surface.
- New feature UI must be added below the existing belt UI by default.
- The feature must not require shrinking, crowding, or repositioning the current belt-stage presentation.
- The selected-seat detail area remains compact and secondary to the stage.

## 2. Selected-Seat Detail Area Contract

The page exposes one selected-seat detail area with these observable properties:

| Field              | Type                   | Description                                                |
| ------------------ | ---------------------- | ---------------------------------------------------------- |
| `selectedSeatId`   | `string \| null`       | Stable selected seat identity.                             |
| `label`            | `string`               | Human-readable selected seat label.                        |
| `isOccupied`       | `boolean`              | Whether the selected seat currently has an open order.     |
| `availableActions` | `string[]`             | Explicit actions such as start dining or checkout.         |
| `orderSummary`     | `OrderSummary \| null` | Running open-order details for the selected occupied seat. |

- Seat clicks must only change the selected seat.
- The selected-seat detail area must always follow the selected seat.
- No separate hidden dining seat may be implied.

## 3. Reachable Area Contract

### Rule

- The selected seat owns a visible reachable area that determines which passing plates may be picked.
- The reachable area extends at most through neighboring seats.
- The reachable area must remain visually anchored to the seat rather than to moving slots or passing plates.

### Presentation

- The reachable area may be rendered as a circular, halo-like, or equivalent seat-owned visual effect.
- The reachable area must remain understandable in reduced-motion mode.
- The reachable area should make it obvious which seat currently owns the pickup range.

## 4. Plate Interaction Contract

### Pickable Plates

- A plate is pickable only when:
  - the selected seat is occupied
  - the selected seat still has an open order
  - the plate is currently within the selected seat's reachable area
- Pickable plates must expose enough interaction surface for reliable click and tap activation.

### Unpickable Plates

- If a guest attempts to pick an unpickable plate, the UI must:
  - show immediate visible reject feedback on that plate when feasible
  - keep the plate moving along its normal belt path
  - explain why the plate could not be picked
- Reject feedback may be a brief whole-plate vibration or equivalent effect, but it must not imply that the belt or plate state was authoritatively changed by the frontend.

## 5. Running Order Contract

- Successful `pickPlate` responses become the immediate running-order source of truth.
- The selected-seat detail area shows the selected occupied seat's current order lines and total.
- The frontend must not recompute totals, timestamps, or line pricing.
- Empty open orders remain valid and must render correctly.

## 6. Failure And Reconciliation Contract

- Free-seat picks must explain that dining must start first.
- Out-of-range and otherwise unpickable plates must explain that the plate is not currently pickable for the selected seat.
- Generic conflicts must explain that the seat or plate state changed and then reconcile visible state.
- All successful and failed pick attempts must be followed by appropriate reconciliation with backend truth.

## 7. Accessibility And Interaction Contract

- Seat selection, reachable-area ownership, plate pickability, and reject feedback must not rely on color alone.
- Keyboard and pointer users must be able to perceive and operate the selected-seat behavior.
- Pickable plates must remain easy enough to activate during motion without unusually precise aiming.
- Reachable-area and reject feedback cues must remain understandable under reduced motion and degraded refresh conditions.
