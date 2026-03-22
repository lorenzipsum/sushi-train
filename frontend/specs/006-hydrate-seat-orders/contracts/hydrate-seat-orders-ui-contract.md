# Hydrate Seat Orders After Refresh UI Contract

## Purpose

Define the observable frontend behavior for restoring occupied-seat dining context after reload while preserving selected-seat continuity, blocked syncing behavior, open-order reselection continuity, and checkout-summary continuity.

## 1. Reload Restoration Contract

- After a full page reload, occupied seat overview alone is not sufficient to treat a seat as pick-ready.
- The frontend begins dining-context restoration for all occupied seats immediately after reload.
- If the previously selected seat still exists, the frontend restores focus to that same seat; otherwise it falls back to the normal default selection behavior.

## 2. Selected-Seat Restoration Contract

The selected-seat detail area exposes one selected seat at a time with these observable properties:

| Field            | Type                   | Description                                                                          |
| ---------------- | ---------------------- | ------------------------------------------------------------------------------------ |
| `selectedSeatId` | `string \| null`       | Current selected seat identity after reload reconciliation.                          |
| `statusLabel`    | `string`               | Guest-facing state such as available, occupied, syncing, unresolved, or checked out. |
| `helperLabel`    | `string`               | Guest-facing explanation of the current seat state.                                  |
| `orderSummary`   | `OrderSummary \| null` | Restored open-order details or final checkout summary when applicable.               |
| `blockedReason`  | `string \| null`       | Why plate picking or actions are currently blocked.                                  |

- The selected-seat detail area remains the primary visible surface for restoration progress, restored running orders, unresolved retry states, and final checkout summary content.

## 3. Syncing And Retry Contract

- While an occupied seat's dining context is still syncing, the selected-seat area must show syncing-specific status and helper messaging.
- While an occupied seat remains unresolved after a temporary restoration failure, the UI must keep that seat visibly unresolved and retry in the background.
- Temporary restoration failure must not fall back to start-dining feedback for an occupied seat.

## 4. Reach And Pickability Contract During Syncing

- Reachable-area and pickability cues may remain visible while a seat is syncing.
- Visible cues during syncing must not enable normal pick behavior.
- Blocked picks during syncing must explain that dining state is still loading rather than implying that dining never started.

## 5. Restored Open-Order Continuity Contract

- When restoration confirms an active open order, the selected-seat detail area shows the backend-authoritative order lines and total.
- When a guest leaves a seat with an active open order and later reselects it, the selected-seat detail area restores the same prior order lines.
- Reselecting a seat with an active open order must still allow additional eligible plates to be added to that same order.

## 6. Reconciliation Contract

- If restoration later confirms no active order exists for a seat that initially appeared occupied, the selected-seat detail area, helper text, action states, and pickability cues must reconcile to one consistent non-pickable state.
- The frontend must not leave the guest in a contradictory combination of occupied-looking seat, disabled Start dining, and blocked pick messaging that claims dining never started.

## 7. Checkout Summary Contract

- After a successful checkout, the selected-seat area shows the final backend-authoritative order summary for the checked-out seat.
- Checkout must not immediately clear or replace that selected-seat summary with another seat's detail state.

## 8. Layout Preservation Contract

- The existing belt, seat, and selected-seat detail layout remains the primary surface.
- This feature must preserve the current belt-stage layout and selected-seat detail structure rather than redesigning the page to support reload recovery.
- Any new guest-visible information for this feature must appear inside the existing selected-seat detail area; the surrounding belt-stage and page layout should remain visually unchanged.
