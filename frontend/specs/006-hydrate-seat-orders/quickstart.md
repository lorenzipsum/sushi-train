# Quickstart: Hydrate Seat Orders After Refresh

## Prerequisites

1. Work on branch `006-hydrate-seat-orders`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing seat and belt endpoints already used by the frontend:
   - `GET /api/v1/belts/{beltId}/snapshot`
   - `GET /api/v1/belts/{beltId}/seats`
   - `GET /api/v1/seats/{seatId}`
   - `POST /api/v1/seats/{seatId}/occupy`
   - `POST /api/v1/seats/{seatId}/order-lines`
   - `POST /api/v1/seats/{seatId}/checkout`
4. Treat the backend as authoritative for whether an occupied seat still has an active order, what lines exist, and what checkout summary should be shown.

## Implementation Outline

1. Extend the belt visualization store so reload restores the previously selected seat when possible and begins dining-context hydration for all occupied seats.
2. Model restoration state explicitly so occupied-seat overview, syncing, unresolved retry, restored open order, and reconciled no-order states are not conflated.
3. Keep the selected-seat detail area as the primary guest-visible surface for syncing, restored running order, unresolved retry, reselection continuity, and final checkout summary states.
4. Keep reach and pickability cues visible during syncing, but block actual picks with syncing-specific feedback until the selected seat has confirmed open-order context.
5. Preserve restored order lines when the guest reselects a seat with an active order, and preserve the final checkout summary in the selected-seat area immediately after checkout.
6. Keep UI changes confined to additional status and summary information inside the existing selected-seat detail area instead of altering the surrounding belt or overall page layout.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Open or prepare one or more occupied seats with backend-authoritative active orders.
3. Reload the page and verify that the previously selected seat is restored when it still exists.
4. Verify that all occupied seats begin restoration immediately after reload instead of waiting for manual reselection.
5. Confirm that an occupied seat in syncing state shows syncing-specific helper text and feedback rather than start-dining messaging.
6. Confirm that reach and pickability cues can remain visible during syncing, but pick attempts are blocked with syncing-specific feedback until restoration completes.
7. After restoration succeeds, verify that the selected occupied seat shows its backend-authoritative running order lines and total without requiring Start dining again.
8. Verify that an occupied seat with an empty open order restores a coherent selected-seat detail state even when no order lines exist yet.
9. Select a different seat and then reselect a seat with an active open order; verify that its prior order lines are still shown and that additional reachable plates can still be added.
10. Simulate a temporary restoration failure and verify that the seat remains visibly unresolved while automatic background retry continues.
11. Verify that a seat whose restoration confirms no active order reconciles to a consistent non-pickable state with no contradictory helper text or actions.
12. Check out a selected seat and verify that the final order summary stays visible in the selected-seat area for that checked-out seat.
13. Confirm the current belt-stage layout and overall page design remain visually intact, with changes limited to additional information inside the selected-seat detail area.

## Automated Verification

1. Add or update store tests for eager occupied-seat restoration after reload, unresolved retry behavior, previously selected seat restoration, and reconciled no-order outcomes.
2. Add or update view-model tests for syncing-state cues, ready-state transitions, and reselection continuity for active open orders.
3. Add or update shell or detail-surface tests for syncing-specific blocked feedback, restored running order visibility, and final checkout summary visibility.
4. Run `npm test`.
5. Run `npm run build`.

## Validation Notes

- 2026-03-22: This feature closes the post-reload inconsistency where occupied overview state could disable Start dining while still lacking restored order context for plate picking.
- 2026-03-22: The selected-seat detail area remains the single guest-facing context surface for restored open orders, unresolved retry states, and final checkout summaries.
- 2026-03-22: Automated validation completed with a one-shot Angular test run via `npx ng test --watch=false` and a production build via `npx ng build`; both completed successfully.
- 2026-03-22: Final regression coverage includes syncing-state detail rendering, checked-out summary rendering, and reconciled no-open-order view-model behavior without changing the surrounding page layout.
