# Quickstart: Refine Belt Interactions

## Prerequisites

1. Work on branch `009-refine-belt-interactions`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing endpoints used by this feature:
   - `GET /api/v1/belts`
   - `GET /api/v1/belts/{beltId}/snapshot`
   - `GET /api/v1/belts/{beltId}/seats`
   - `POST /api/v1/seats/{seatId}/order-lines`
   - `PATCH /api/v1/belts/{beltId}`
4. Treat the backend as authoritative for final belt speed, slot state, and pickability outcomes after any write.

## Implementation Outline

1. Extend the frontend belt API wrapper with a focused belt-parameter update method for `speedSlotsPerTick`.
2. Add speed-control modal state and submission handling to the existing belt visualization store, including unchanged, success, and failure feedback.
3. Turn the existing disabled cogwheel in the belt stage into an active settings trigger and render a semantic modal dialog in the app shell.
4. Adjust the belt-stage view-model reach calculation so top-row seats receive the small effective reach increase needed for intended picks.
5. Strengthen selected-seat clarity through updated seat, reach-area, and lit-slot styling without changing the structural stage layout.
6. Simplify moving plates to ring-only visuals while preserving tier color and existing pickable, pending, and rejected states.
7. Reuse the store's existing immediate refresh-after-write behavior after a successful speed change.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Select each top-row seat and confirm plates entering the intended nearby reach zone become pickable.
3. Confirm plates outside the selected seat's intended zone remain non-pickable.
4. Switch quickly between seats and verify the selected seat and reach bubble always remain obvious without stale highlights.
5. Confirm moving plates render as ring-only visuals with no inner garnish or center item.
6. Verify the tier ring colors remain easy to distinguish while the belt is moving.
7. Open the cogwheel control and confirm a speed modal appears with the current speed preselected.
8. Change to a different allowed speed and confirm success feedback appears and the belt speed label updates after refresh.
9. Open the modal again and dismiss it without confirming; verify the belt speed stays unchanged.
10. Try selecting the current speed and verify the dialog keeps the apply action inactive instead of implying that a backend update is pending.
11. Trigger a failed speed update and verify the UI keeps the old speed intact with clear error feedback.
12. Confirm selected-seat detail, pick-plate flow, and checkout flow still behave as before.

## Automated Verification

1. Add or update view-model tests for top-row reach math, within-reach behavior, and ring-only plate rendering.
2. Add or update store tests for speed modal state, speed submission, unchanged selection handling, error handling, and refresh-after-success behavior.
3. Add or update app-shell or component tests for cogwheel activation and modal open-close-submit behavior.
4. Run `npm test`.
5. Run `npm run build`.

## Validation Notes

- 2026-03-23: The plan keeps the feature inside the current standalone Angular app and avoids a separate settings route or dashboard.
- 2026-03-23: The reach fix is intentionally placed in the view-model so actual pickability stays authoritative, while the visible highlight and lit-slot cues may be tuned separately for clarity.
- 2026-03-23: Belt speed edits are limited to `speedSlotsPerTick`; tick interval remains out of scope for this feature.
- 2026-03-23: The modal flow reuses the app's existing overlay pattern instead of adding a UI framework dependency.
- 2026-03-23: `npx ng test --watch=false` passed with 7 test files and 65 tests green after adding coverage for top-row reach, ring-only plates, and the speed dialog flow.
- 2026-03-23: `npm run build` completed successfully and produced `dist/frontend`; the build still reports the existing `src/app/app.css` budget warning at 12.23 kB against the 10 kB warning threshold.
