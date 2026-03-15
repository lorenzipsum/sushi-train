# Phase 0 Research: Checkout Seat

## Decision 1: Use the checkout success response as the only authoritative final summary

- **Decision**: Treat the successful `POST /api/v1/seats/{seatId}/checkout` response as the canonical final checkout summary and do not try to rebuild that summary from later seat-detail reads.
- **Rationale**: The backend returns a `SeatOrderDto` with the checked-out `orderSummary` on success, while later `GET /api/v1/seats/{seatId}` calls return `isOccupied = false` and `orderSummary = null`. Using the write response directly preserves the final checked-out order exactly as the backend reported it.
- **Alternatives considered**:
  - Re-read seat detail after success and rebuild the summary: rejected because the checked-out order is intentionally absent from later seat-detail reads.
  - Recompute totals or timestamps on the frontend: rejected because totals, timestamps, and final status are backend-authoritative.

## Decision 2: Preserve the final checkout summary in current-session state only

- **Decision**: Store successful checkout summaries in frontend signal state for the current in-app session and reconciliation flow, without requiring persistence across a full browser reload.
- **Rationale**: The specification explicitly limits persistence to the current session. In-memory retention is sufficient for confirmation and later in-session use while avoiding premature storage design.
- **Alternatives considered**:
  - Persist the final summary across full reloads: rejected because it adds storage behavior beyond the clarified scope.
  - Show the summary only once and discard it immediately on refresh: rejected because it would fail the requirement to preserve the final summary for later in-session use.

## Decision 3: Reconcile seat truth with refresh-after-write, not success-time seat-detail reads

- **Decision**: After checkout success or failure, use the existing refresh-after-write behavior to reconcile seat overview and belt snapshot state. Do not require a success-time seat-detail read for checkout.
- **Rationale**: Checkout already returns the final summary needed for confirmation, and the seat overview read is the right authoritative source for whether the seat is visibly free again. This keeps the data flow simpler than the occupy feature.
- **Alternatives considered**:
  - Reuse occupy-style seat-detail reconciliation after successful checkout: rejected because it loses the checked-out summary and adds unnecessary network work.
  - Trust only local UI state after the write: rejected because the frontend must reconcile with backend truth after every outcome.

## Decision 4: Treat `409 SEAT_NOT_OCCUPIED` as a business outcome, not a generic error

- **Decision**: Map checkout `409` responses with `errorCode = SEAT_NOT_OCCUPIED` to a stale-or-already-finished checkout outcome and explain that no active occupancy remains.
- **Rationale**: The backend uses the same conflict for repeated checkout, stale UI, and concurrent loss. Clear copy preserves trust and guides the UI to refresh the seat as free.
- **Alternatives considered**:
  - Surface the 409 as a generic request failure: rejected because it obscures the business meaning.
  - Add a separate "already checked out" state in the frontend lifecycle: rejected because the backend intentionally does not expose a second conflict category.

## Decision 5: Start checkout from occupied seats in the existing stage UI

- **Decision**: Extend the existing seat interaction surface so occupied seats can start checkout from the same seat-based UI, with occupied-state labels and a pending affordance distinct from free-seat occupy behavior.
- **Rationale**: The specification requires checkout to start from an occupied seat in the existing UI. Reusing the current stage keeps scope small and preserves the single-surface interaction model.
- **Alternatives considered**:
  - Add a separate route or dedicated checkout page: rejected because it expands surface area unnecessarily.
  - Add a detached seat list or modal-only launcher: rejected because it moves the action away from the seat that owns the order.

## Decision 6: Reuse the current feedback-card pattern for confirmation and failure messaging

- **Decision**: Extend the existing app-shell feedback pattern with checkout-specific success, pending, and failure states that can display receipt-style final summary details.
- **Rationale**: The current occupy flow already has persistent `aria-live` feedback and a visible location for trust-critical state changes. Reusing that surface minimizes UI sprawl and keeps accessibility behavior consistent.
- **Alternatives considered**:
  - Create a separate toast-only system: rejected because it is weaker for receipt-style confirmation and easier to miss.
  - Replace the entire page with a checkout receipt screen: rejected because the feature should stay inside the current single-belt experience.
