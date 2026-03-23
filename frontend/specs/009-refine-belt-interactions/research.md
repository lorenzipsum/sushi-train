# Phase 0 Research: Refine Belt Interactions

## Decision 1: Fix top-row pickup reach in the view-model radius calculation

- **Decision**: Adjust the selected-seat reach radius inside the existing `getReachArea()` view-model logic, with a targeted expansion for top-row seat segments so actual pickability is fixed at the source of truth.
- **Rationale**: The current pickup rule derives from distance between slots and the computed reach circle. Fixing that rule in the view-model resolves the top-row bug, while still leaving room to tune the visible highlight and slot-lighting cues separately for better readability.
- **Alternatives considered**:
  - Visually scale the reach bubble only in CSS: rejected because it would not change `isWithinReach` or `isPickable` and would therefore preserve the underlying bug.
  - Reposition seats or slots in the stage layout: rejected because the feedback asks for a slight reach adjustment, not a broader layout redesign.

## Decision 1a: Separate lit-slot emphasis from actual pickability where clarity benefits from it

- **Decision**: Keep actual pickup reach authoritative in the view-model while allowing the lit slot presentation to extend slightly beyond the strict pickable radius for some seat segments.
- **Rationale**: The approved visual result uses overlap between the selected-seat highlight and the lit slot run to make seat ownership easier to read, especially for bottom and side seats. That clarity improvement should not silently widen which plates are actually pickable.
- **Alternatives considered**:
  - Force the lit slot range to match the exact pick radius everywhere: rejected because the approved visuals read better when the lighting cue is slightly broader.
  - Widen real pickability for every seat to match the broader lighting: rejected because that would change behavior beyond the original bug fix.

## Decision 2: Use layered light cues to clarify the selected seat

- **Decision**: Make the selected seat read more clearly through brighter, slightly stronger light treatment on the selected seat, its reach area, and within-reach slot markers rather than adding new labels or structural callouts.
- **Rationale**: The current stage already has selected-seat, within-reach, and pickable classes. Strengthening those cues is the lowest-risk way to improve clarity while preserving the current spatial model.
- **Alternatives considered**:
  - Add a persistent text badge or floating label near the selected seat: rejected because it introduces new visual clutter into an already dense stage and the approved stage now reads clearly enough without that extra copy.
  - Add continuous pulsing or heavy motion: rejected because the feature must remain readable in reduced-motion contexts and should rely on light, not animation, for clarity.

## Decision 3: Simplify plate rendering to a tier ring only

- **Decision**: Remove the current inner plate center and food garnish layers from the default moving plate presentation and keep the tier-colored ring as the primary visible plate identity cue.
- **Rationale**: User feedback explicitly values the colored ring and wants the center removed. The ring already carries tier color, so keeping it while stripping the inner artwork reduces noise without changing plate identity semantics.
- **Alternatives considered**:
  - Keep the center disc and remove only the food garnish: rejected because the requested end state is effectively ring-only, and a remaining center disc would still occupy the visual space users want simplified.
  - Replace the center garnish with new icons or text: rejected because that would add new noise instead of reducing it.

## Decision 4: Implement speed control through the existing belt PATCH contract

- **Decision**: Extend the existing `BeltsApi` with a focused belt-parameter update method that submits `speedSlotsPerTick` to `PATCH /api/v1/belts/{id}` and treats the backend as authoritative for the final applied speed.
- **Rationale**: The OpenAPI contract already defines `BeltUpdateRequest` and allows `speedSlotsPerTick` values from `0` to `5`. Reusing that contract keeps the feature within existing backend capabilities.
- **Alternatives considered**:
  - Create a frontend-only speed override: rejected because belt timing and snapshot state are backend-authoritative in this app.
  - Extend the feature to edit both speed and tick interval: rejected because the user asked for belt speed, and widening the settings scope would increase risk and UI complexity.

## Decision 5: Use the app-shell dialog pattern for the cogwheel workflow

- **Decision**: Turn the existing disabled cogwheel into an active settings trigger and render a semantic modal dialog in the app shell using the same overlay-card pattern already used for the guide dialog.
- **Rationale**: The app already implements dialog-style overlays without a UI framework dependency. Reusing that pattern keeps implementation small and consistent while matching the request for a modal window.
- **Alternatives considered**:
  - Reuse the operator side-panel pattern for speed settings: rejected because the requested interaction is explicitly modal and the speed change is a short transactional flow, not a larger workspace.
  - Add inline controls directly to the kitchen stat card: rejected because it would crowd the stage and reduce clarity for an action that benefits from explicit confirmation.

## Decision 6: Reuse refresh-after-write reconciliation after speed updates

- **Decision**: After a successful speed change, trigger the store's existing `refreshAfterWrite()` path so the latest belt snapshot and seat state are reconciled immediately.
- **Rationale**: The store already uses immediate refresh after write operations to keep the stage aligned with backend truth. Speed changes affect timing and labels, so they should follow the same authoritative refresh path.
- **Alternatives considered**:
  - Update the local snapshot optimistically without refresh: rejected because the backend remains authoritative for the final applied belt parameters.
  - Wait for the next scheduled polling or SSE update: rejected because it would make the newly changed speed feel delayed or uncertain.
