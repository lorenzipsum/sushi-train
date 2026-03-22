# Quickstart: Add Plates To Belt

## Prerequisites

1. Work on branch `007-add-plates-belt`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Ensure the backend exposes the existing belt and menu endpoints needed by this feature:
   - `GET /api/v1/belts`
   - `GET /api/v1/belts/{beltId}/snapshot`
   - `GET /api/v1/belts/{beltId}/seats`
   - `POST /api/v1/belts/{beltId}/plates`
   - `GET /api/v1/menu-items`
4. Treat the backend as authoritative for final slot placement, placement defaults when omitted, and plate visibility on the next belt snapshot.

## Implementation Outline

1. Extend the frontend API layer with a focused menu-items wrapper for paginated retrieval and a plate-placement method on the existing belts API wrapper.
2. Add operator placement state to the existing belt visualization store for menu loading, search text, selected item, draft defaults, manual overrides, pending submission, and operator notices.
3. Introduce a compact operator surface tied to the kitchen or chef area, with a compact fallback presentation if inline controls would damage the current belt layout.
4. Keep the selected-seat detail area guest-focused and route all placement controls, validation, and retry notices through the operator surface instead.
5. Reuse the store's existing immediate refresh-after-write behavior so placement success updates the authoritative belt state promptly while the normal polling loop continues in the background.
6. Validate that known placement failures produce operator-facing corrective feedback without changing guest seat selection or the current dining context.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Confirm the current belt scene still presents the kitchen and chef area as the visual center.
3. Open the operator plate-placement flow from the kitchen or chef area and confirm it does not visually overtake the guest stage.
4. Verify that the operator can search the available menu list and still reach any menu item from the compact results area.
5. Select a menu item and confirm the draft defaults to one plate, the menu item's current tier and price, and an expiration two hours in the future.
6. Submit a placement using the defaults and verify that a positive operator-facing notice appears and the belt visibly refreshes without clearing the selected guest seat context.
7. Submit a placement with valid overrides for plate count, tier, price, and expiration and verify that the placed result reflects those overrides.
8. Trigger a not-enough-space failure and verify the notice tells the operator to reduce the number of plates.
9. Trigger invalid menu-item or invalid-value failures and verify the operator can correct the draft and retry without rebuilding the whole flow.
10. Trigger a missing-belt or generic malformed failure and verify the app shows a clear failure notice without implying success.
11. Confirm that guest seat selection, selected-seat detail, and guest dining flow remain stable throughout operator success and failure cases.

## Automated Verification

1. Add or update API-wrapper tests for menu retrieval and belt plate creation request mapping if service tests are present.
2. Add or update store tests for menu loading, search filtering, default derivation, override submission, known failure normalization, and refresh-after-success behavior.
3. Add or update shell or stage tests for operator-surface rendering, fallback presentation behavior, operator notice rendering, and guest-context preservation.
4. Run `npm test`.
5. Run `npm run build`.

## Validation Notes

- 2026-03-22: This plan keeps the feature inside the current single-belt Angular app and avoids creating a separate admin dashboard or route.
- 2026-03-22: The operator flow remains visually tied to the kitchen or chef presentation while preserving the guest-facing selected-seat detail area as guest-only context.
- 2026-03-22: Immediate authoritative refresh after success remains aligned with the store's existing refresh-after-write pattern rather than introducing optimistic belt mutation.
- 2026-03-22: Automated verification passed with `npm test -- --watch=false` and `npm run build` from `C:\se\sushi-train\frontend`.
- 2026-03-22: The operator surface now preloads the full paginated menu, supports local search and override submission, and keeps the selected guest seat context stable during success and failure flows.
- 2026-03-22: Default placement drafts now initialize to one plate, the selected menu item's current tier and price, and an expiration two hours in the future.
