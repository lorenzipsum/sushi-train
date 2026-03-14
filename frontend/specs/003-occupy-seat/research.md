# Phase 0 Research: Occupy Seat

## Decision 1: Use the existing open Order as the occupancy model

- **Decision**: Treat seat occupancy as the existence of an `OPEN` order for the seat and do not introduce a new seat-session, visit, or claim concept.
- **Rationale**: The backend already uses the open order as the source of truth for seat occupancy, plate pickup, and checkout. Reusing that model keeps the feature aligned with real domain behavior and avoids duplicating lifecycle concepts.
- **Alternatives considered**:
  - Add a separate `SeatSession` or `SeatClaim`: rejected because it duplicates an already implemented lifecycle and would create unnecessary mapping complexity.
  - Treat occupancy as a UI-only toggle: rejected because it would not provide a durable parent record for later checkout and picked-plate features.

## Decision 2: Keep the occupy action seat-based in both UI and API usage

- **Decision**: Start occupancy from clicking a visible free seat and call the existing `POST /api/v1/seats/{seatId}/occupy` endpoint.
- **Rationale**: The stage already presents seats as the primary interaction surface, and the backend already exposes the correct seat-oriented write endpoint. This keeps the user flow simple and the frontend logic easy to reason about.
- **Alternatives considered**:
  - Use a separate side form or modal before occupying: rejected because the product intent is direct seat-taking from the stage.
  - Introduce an order-first API abstraction in the frontend: rejected because order identity only exists after the occupy action succeeds.

## Decision 3: Preserve first-write-wins with a clear conflict outcome

- **Decision**: Design the frontend around one successful occupy outcome and one explicit `409 SEAT_ALREADY_OCCUPIED` failure outcome for concurrent attempts.
- **Rationale**: The backend already enforces first-write-wins through locking and a unique constraint. The frontend should reflect that behavior clearly rather than inventing retries or ambiguous fallback states.
- **Alternatives considered**:
  - Last-write-wins: rejected because it would violate current backend behavior and undermine trust.
  - Silent refresh on conflict without an explicit message: rejected because it would make the failure feel random to the guest.

## Decision 4: Reconcile successful occupy writes with seat detail to capture durable order context

- **Decision**: Accept the current `SeatStateDto` occupy response and immediately reconcile it with `GET /api/v1/seats/{seatId}` so the frontend can retain `orderId` and `createdAt` after a successful occupy.
- **Rationale**: The generated OpenAPI contract currently exposes `SeatStateDto` for `POST /api/v1/seats/{seatId}/occupy`, while later flows still need the durable open-order identifier. A follow-up seat-detail read preserves the required context without inventing a parallel seat-session concept.
- **Alternatives considered**:
  - Require the occupy endpoint itself to return `SeatOrderDto` before shipping the feature: rejected for now because the current backend/frontend contract already compiles and the immediate follow-up read satisfies the workflow.
  - Add a new opaque occupancy token separate from `orderId`: rejected because the backend already has a durable record identifier.

## Decision 5: Keep polling and refresh-after-write for this feature

- **Decision**: Preserve the current refresh-after-write plus periodic polling model for seat occupancy updates.
- **Rationale**: Realtime delivery is a later roadmap feature. Keeping current delivery semantics isolates `003-occupy-seat` to the product behavior change instead of mixing domain and transport changes.
- **Alternatives considered**:
  - Introduce SSE or WebSocket updates now: rejected because it increases risk and spreads scope across the roadmap.
  - Rely only on local optimistic state after occupy: rejected because backend truth must remain authoritative.
