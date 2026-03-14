# Quickstart: Occupy Seat

## Prerequisites

1. Work on branch `003-occupy-seat`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing seat endpoints:
   - `POST /api/v1/seats/{seatId}/occupy`
   - `GET /api/v1/seats/{seatId}`
   - `GET /api/v1/belts/{beltId}/seats`

## Implementation Outline

1. Extend the frontend API surface so the app can call `POST /api/v1/seats/{seatId}/occupy` and immediately reconcile with `GET /api/v1/seats/{seatId}` when it needs durable order context.
2. Update the belt visualization store to expose an occupy-seat action, request-progress state, conflict handling, and reconciliation after success or failure.
3. Update the belt stage/view model so free seats are actionable, occupied seats remain visibly distinct, and occupied seats do not appear freely clickable.
4. Surface clear user feedback for occupy success, seat-already-occupied conflict, and seat-not-found outcomes.
5. Preserve current polling and refresh-after-write behavior for consistency with existing belt state updates.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Load a belt with at least one free seat.
3. Click a free seat and confirm it becomes occupied.
4. Refresh the page and confirm the same seat still appears occupied from backend data.
5. Attempt to occupy a seat that another client already took and confirm the UI shows a clear seat-already-occupied message.
6. Attempt to occupy a seat that does not exist and confirm the UI shows a clear not-found message.
7. Confirm occupied seats still read clearly in reduced-motion, paused, and degraded states.

## Automated Verification

1. Add or update tests for the occupy-seat store flow, including success, conflict, and not-found handling.
2. Add or update tests for the seat view model or stage interaction state so free seats and occupied seats render with the correct actionability.
3. Run `npm test`.
4. Run `npm run build`.

## Validation Notes

- 2026-03-15: Added automated coverage for occupy success, conflict, not-found handling, free-seat actionability, durable order context after seat-detail reconciliation, and app-shell feedback rendering.
- 2026-03-15: `npx ng test --watch=false` passed.
- 2026-03-15: `npm run build` passed with the existing `belt-stage.css` component-style budget warning still present.
- 2026-03-15: Manual browser verification against a live backend is still pending.

## Planned Follow-On Compatibility Checks

1. Confirm occupy success plus immediate seat-detail reconciliation exposes the active `orderId` and `createdAt` needed by `004-checkout-seat`.
2. Confirm the same active order can later act as the parent record for `005-pick-plates`.
