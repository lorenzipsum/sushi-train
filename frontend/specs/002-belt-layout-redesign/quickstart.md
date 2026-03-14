# Quickstart: Kaiten-Zushi Belt Redesign

## Prerequisites

1. Decide which backend runtime you are using:
   - local Spring run: backend typically available at `http://localhost:8080`
   - Docker Compose run: backend available at `http://localhost:8088`
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Keep `proxy.conf.json` in place so browser calls continue to use relative `/api/...` requests.

## Implementation Outline

1. Redesign the root page shell in `frontend/src/app/app.html` and `frontend/src/app/app.css` so the belt becomes a wide top-section hero with a compact status rail or notice surface beneath it instead of a competing sidebar.
2. Extend `frontend/src/app/belt-visualization/` with a pure path-layout helper for a squarer rounded-rectangle conveyor geometry.
3. Update `frontend/src/app/belt-visualization/belt-view-model.ts` so stable slot indices and seats map into the new stage layout without changing backend-authoritative identity rules.
4. Add a small menu-item visual registry that maps seeded menu items into visual families and item-specific overrides, with a fallback for unknown items.
5. Update the stage component and styles so seats read as belt-facing seat places or stools, longer top and bottom runs can hold more seats than the shorter sides, the interior inner lane reads as a kitchen with a chef presence, and plate content becomes more recognizable.
6. Preserve the current store, API calls, motion derivation, reduced-motion handling, and refresh-after-write hook.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Confirm the page still tracks the first returned belt and remains read-only.
3. Confirm the belt now reads as a wide squarer conveyor or counter layout rather than a circular or overly elliptical stage.
4. Confirm the belt occupies the top visual priority and any metrics or degraded-state messaging stays in a compact rail beneath it.
5. Confirm seats read as stools or counter places, face the belt, and distribute proportionally so the longer top and bottom runs carry more seats than the shorter sides.
6. Confirm the center of the loop reads as a kitchen or chef-prep zone inside the visible inner lane rather than as empty decorative space.
7. Confirm visible dish families are recognizable on plates, including drinks, desserts, sides, and sushi items.
8. Confirm unknown menu items render a graceful fallback visual instead of empty or broken content.
9. Confirm the page still preserves paused, loading, degraded, and reduced-motion understanding.
10. Confirm the redesigned layout stays coherent on desktop and mobile widths.

## Automated Verification

1. Verify `frontend/src/app/belt-visualization/belt-layout.spec.ts` covers slot progress, turn segments, weighted seat distribution, belt-facing orientation, and responsive sizing.
2. Verify `frontend/src/app/belt-visualization/menu-item-visuals.spec.ts` covers seeded dish mapping plus family and generic fallback behavior.
3. Verify `frontend/src/app/belt-visualization/belt-view-model.spec.ts` covers racetrack coordinates, larger plates, seat-place metadata, and dish-family metadata.
4. Verify `frontend/src/app/belt-visualization/belt-visualization.store.spec.ts` and `frontend/src/app/app.spec.ts` cover reduced motion, degraded freshness, and the belt-first page hierarchy.
5. Run `npm test`.
6. Run `npm run build`.

## Structured Review Procedure

1. Compare the redesigned page against the Feature 001 baseline with 5 reviewers and record each `SC-001` score for restaurant feel on a 1-5 scale.
2. Run a 5-second first-view check on desktop and mobile with at least 10 participants and record whether each participant identifies the belt as the primary focus for `SC-002`.
3. Run first-attempt recognition checks for seat occupancy and major dish families and record pass/fail counts for `SC-003` and `SC-004`.
4. Record the review results in `frontend/specs/002-belt-layout-redesign/checklists/requirements.md` before closing the feature.

## Suggested Fixture Coverage

1. Moving belt with a full mix of seeded item families.
2. Paused belt with visible occupied seats and several empty slots.
3. Unknown menu item requiring generic fallback rendering.
4. High slot count where plate sizing and spacing must remain legible across the longer straight runs.
5. Narrow viewport where the lower-priority content stacks below the hero stage.
6. Reduced-motion mode with the redesigned stage and food visuals.
7. Recoverable refresh failure after a prior successful render.
