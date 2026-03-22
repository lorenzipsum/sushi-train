# Quickstart: Pick Plates

## Prerequisites

1. Work on branch `005-pick-plates`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing seat and belt endpoints used by the app:
   - `GET /api/v1/belts/{beltId}/snapshot`
   - `GET /api/v1/belts/{beltId}/seats`
   - `POST /api/v1/seats/{seatId}/occupy`
   - `POST /api/v1/seats/{seatId}/order-lines`
   - `POST /api/v1/seats/{seatId}/checkout`
4. Treat the backend as anonymous and seat-ID-driven only; selected seat, reachable area, and reject feedback are frontend UX concerns.

## Implementation Outline

1. Add selected-seat state and selection-only seat actions to the existing belt visualization store.
2. Extend the stage view model to derive a seat-anchored reachable area that stays fixed relative to the selected seat and clamps pickability to neighboring seats.
3. Add a compact selected-seat detail area below the existing belt UI with explicit start dining and checkout actions plus running-order details.
4. Keep the current belt-stage layout visually intact while making pickable plates easier to activate through a forgiving interaction surface.
5. Add brief visible reject feedback for unpickable attempts while preserving continuous belt motion.
6. Continue using successful `pickPlate` responses as the immediate source of truth and follow them with the existing reconciliation refresh.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Confirm the existing belt stage keeps its current size and position before any new detail UI is considered.
3. Click multiple seats and verify seat clicks only change selection and update the selected-seat detail area below the belt UI.
4. Select a free seat, start dining from the selected-seat detail area, and verify the seat becomes occupied without stage layout changes.
5. Confirm the selected seat shows a reachable area that is visibly anchored to the seat rather than to moving slots.
6. Verify the reachable area reads as the seat's pickup range and does not extend beyond neighboring seats.
7. Pick an in-range plate and verify the action is easy to trigger without precise aiming.
8. Verify the running order updates immediately from the backend response.
9. Attempt to pick an unpickable plate and verify the attempted plate shows brief visible reject feedback while continuing along the belt path.
10. Use keyboard navigation to move through the selected-seat detail area and trigger the explicit selected-seat actions supported by the feature.
11. Confirm pickability, reachable-area ownership, and reject feedback remain understandable without relying on color alone.
12. Confirm reduced-motion and degraded-state modes keep reachable-area and reject feedback meaning understandable.

## Automated Verification

1. Add or update view-model tests for selected-seat reachable-area derivation and seat-anchored range ownership.
2. Add or update store tests for selected-seat selection, pick success, out-of-range blocking, and conflict reconciliation.
3. Add or update shell or interaction tests for the selected-seat detail area, keyboard-accessible selected-seat actions, easy-pick interaction surface, and reject feedback behavior.
4. Run `npm test`.
5. Run `npm run build`.

## Validation Notes

- 2026-03-16: This plan supersedes the earlier 005 planning pass because the specification was tightened to preserve the current UI and move new interaction UI below the belt stage.
- 2026-03-16: Reach visualization is now defined as a seat-anchored reachable area rather than moving pickable-slot highlighting.
- 2026-03-16: Easy-pick interaction surface and brief reject feedback for unpickable plates are now explicit scope, not optional polish.
