# Quickstart: Checkout Seat

## Prerequisites

1. Work on branch `004-checkout-seat`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing seat endpoints and current order-backed occupancy model:
   - `POST /api/v1/seats/{seatId}/checkout`
   - `GET /api/v1/belts/{beltId}/seats`
   - `GET /api/v1/seats/{seatId}`
4. Keep in mind that successful checkout returns the only authoritative checked-out order summary; later seat-detail reads do not.

## Implementation Outline

1. Extend the belt visualization store with checkout-pending state, checkout feedback, and in-session retention for successful final checkout summaries.
2. Update the seat view model and stage interaction so occupied seats can start checkout from the existing UI with clear occupied-state labels and pending behavior.
3. Surface a receipt-style confirmation card that displays backend-authoritative `status`, `createdAt`, `closedAt`, `lines`, and `totalPrice` from the successful checkout response.
4. Handle `409 SEAT_NOT_OCCUPIED` as a stale or already-finished checkout outcome and `404` as a missing-seat outcome, with clear user messaging in both cases.
5. Reuse the current refresh-after-write path so seat overview and belt state reconcile after every checkout success or failure.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Load a belt with at least one occupied seat.
3. Start checkout from an occupied seat and confirm the seat becomes visibly free after the refresh/reconcile cycle.
4. Confirm the success UI shows the backend-returned final order status, open time, close time, line items, and total without recomputation.
5. Check out an occupied seat whose order has no lines and confirm success still shows an empty list and zero total.
6. Attempt a stale or repeated checkout and confirm the UI explains that no active occupancy remains and refreshes the seat as free.
7. Attempt checkout for a seat that no longer exists and confirm the UI explains that the seat is missing and the visible seat state is refreshed.
8. Refresh the browser after a successful checkout and confirm the seat remains free even though the final summary is not required to survive the full reload.
9. Confirm reduced-motion and degraded-state messaging remain readable during pending, success, and failure states.

## Automated Verification

1. Add or update store tests for checkout success, `SEAT_NOT_OCCUPIED`, not-found handling, and current-session summary retention.
2. Add or update view-model or stage tests for occupied-seat checkout actionability and pending-state rendering.
3. Add or update app-shell tests for checkout confirmation details and error messaging.
4. Run `npm test`.
5. Run `npm run build`.

## Validation Notes

- 2026-03-15: Planning assumes the existing generated API contract already exposes `SeatsApi.checkout()` and `SeatOrderDto` for successful checkout.
- 2026-03-15: Structured usability review passed for checkout outcome comprehension. The occupied-seat action announces checkout intent, pending copy distinguishes checkout from seat occupation, stale and missing-seat failures explain why checkout cannot proceed, and the success card preserves backend-authored order details including empty-order totals.
- 2026-03-15: Automated regression verification passed again after moving the stage artwork from `src/app/belt-visualization/belt-stage.css` into `src/styles.css`. `npx ng test --watch=false` still passed, and `npm run build` completed without the prior `anyComponentStyle` budget warning.
- 2026-03-15: Live backend verification against `http://localhost:8088` confirmed the expected checkout lifecycle on real seat data: a successful checkout returned `CHECKED_OUT` with `closedAt`, a follow-up seat read returned `isOccupied = false` with no `orderSummary`, a repeated checkout returned `409 SEAT_NOT_OCCUPIED`, an occupy-then-checkout flow on a previously free seat produced a zero-line, zero-total checked-out order, and a missing seat returned `404 Resource not found`.
- 2026-03-15: The running frontend at `http://localhost:4200` responded with the expected `KAITENZUSHI` shell and live counter metrics. Browser click-through was not automated by the available tools, so UI interaction coverage still relies on the passing Angular tests plus the live backend verification above.

## Planned Follow-On Compatibility Checks

1. Confirm the stored in-session final checkout summary can feed a later receipt or order-history surface without changing the checkout lifecycle model.
2. Confirm refresh-after-write behavior remains compatible with later realtime seat updates when that roadmap item is introduced.
