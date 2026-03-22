# Phase 0 Research: Add Plates To Belt

## Decision 1: Prefer a kitchen-anchored operator entry point with a compact fallback surface

- **Decision**: Place the operator entry point in or immediately adjacent to the existing kitchen or chef area when the belt-stage layout can absorb it cleanly, and fall back to a compact secondary surface if inline controls would crowd the guest-facing scene.
- **Rationale**: The product direction explicitly wants the operator flow to feel like part of the same sushi-belt experience, but not at the cost of degrading the current guest presentation.
- **Alternatives considered**:
  - Always use a modal or detached overlay: rejected because it hides the intended connection to the kitchen or chef presentation when inline placement is feasible.
  - Always embed a full form directly inside the stage: rejected because the existing stage is already visually dense and must remain guest-first.

## Decision 2: Load the full menu list and search locally inside the operator flow

- **Decision**: Retrieve the full available menu list through the existing paginated menu endpoint, cache it inside the operator state for the current session, and narrow the visible result list through frontend text search.
- **Rationale**: There is no backend search API today, but the API supports page sizes up to 200, which is sufficient for a compact operator flow that needs full-list reach and responsive narrowing.
- **Alternatives considered**:
  - Add a server-side search dependency to the feature: rejected because it expands scope beyond the confirmed frontend feature and is unnecessary for the current menu contract.
  - Force operators to scroll an unsorted full list with no search: rejected because the operator surface is intentionally space-constrained.

## Decision 3: Keep operator placement state separate from selected-seat guest state

- **Decision**: Model operator placement as its own workflow and feedback state rather than overloading the selected-seat detail area or guest seat summary state.
- **Rationale**: The selected-seat detail area is already a guest-context surface. Reusing it for operator controls would blur the boundary between dining and demo operations and directly violates the feature scope.
- **Alternatives considered**:
  - Reuse the selected-seat detail component for plate placement: rejected because it would turn a guest-facing surface into the main operator workspace.
  - Introduce a second global page shell or admin sidebar: rejected because the feature must remain compact and inside the existing experience.

## Decision 4: Derive placement defaults from the selected menu item and only override when needed

- **Decision**: Initialize each placement draft from the chosen menu item with defaults for one plate, the menu item's current tier, the menu item's current base price, and an expiration two hours in the future.
- **Rationale**: The operator flow is optimized for fast demo operation. Defaults should satisfy the common case while still supporting explicit overrides when the operator needs them.
- **Alternatives considered**:
  - Require every field to be filled manually: rejected because it slows down the main operator use case.
  - Hide all override controls behind a separate advanced workflow: rejected because tier, price, and expiration overrides are in scope and should remain directly reachable.

## Decision 5: Reuse the existing refresh-after-write reconciliation path after successful placement

- **Decision**: After a successful placement, trigger the same immediate authoritative refresh path already used for other writes, then continue the normal polling loop.
- **Rationale**: The store already has a tested refresh-after-write pattern for keeping belt and seat state synchronized to backend truth. Reusing it keeps behavior consistent and limits new moving parts.
- **Alternatives considered**:
  - Wait for the next scheduled polling interval: rejected because operator feedback would feel delayed and the newly placed plates might appear stale.
  - Optimistically inject created plates into the visible snapshot before refresh: rejected because the backend remains authoritative for actual slot placement and spacing relaxation.

## Decision 6: Translate backend placement failures into a small set of corrective operator notices

- **Decision**: Normalize the known placement failures into clear operator-facing categories: reduce plate count when capacity is insufficient, correct the chosen menu item or field values when the request is invalid, and show a generic clear failure notice for missing belt or malformed or unexpected outcomes.
- **Rationale**: The operator needs quick recovery guidance inside the demo flow, not raw backend terminology. The error model must still preserve the distinction between correctable input problems and broader operational failures.
- **Alternatives considered**:
  - Surface raw backend problem details directly: rejected because they are inconsistent for operator UX and expose implementation language.
  - Model a separate spacing-specific failure state: rejected because the backend contract does not expose minimum-gap failure as a distinct observable outcome.
