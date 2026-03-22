# Phase 0 Research: Hydrate Seat Orders After Refresh

## Decision 1: Hydrate all occupied seats immediately after reload

- **Decision**: Begin dining-context hydration for every occupied seat as soon as the post-reload seat overview is available.
- **Rationale**: The bug comes from treating occupied overview state as if it implied restored dining context. Eager hydration closes that gap consistently across the whole belt instead of only for whichever seat the guest happens to reselect first.
- **Alternatives considered**:
  - Hydrate only the selected seat first: rejected because non-selected occupied seats could still look authoritative before their dining context is known.
  - Hydrate seats only when selected later: rejected because it prolongs contradictory state and makes restoration depend on extra guest interaction.

## Decision 2: Preserve the previously selected seat across reload when possible

- **Decision**: Restore focus to the previously selected seat when it still exists; otherwise fall back to the normal default selection behavior.
- **Rationale**: Reload should feel like continuing the same dining session, not like starting the browsing flow again from an arbitrary seat.
- **Alternatives considered**:
  - Always reset to the default seat: rejected because it discards guest context even when the prior seat is still valid.
  - Preserve selection only when the seat is still occupied: rejected because a previously selected seat can remain relevant even if hydration later reconciles it to a different final state.

## Decision 3: Keep cues visible during syncing but block behaviorally

- **Decision**: Keep reachable-area and pickability cues visible while dining context is syncing, but block actual picks with syncing-specific feedback until hydration completes.
- **Rationale**: The current UI already teaches the guest where interaction happens. Removing those cues during reload recovery would make the interface jumpy, while keeping them visible-but-blocked preserves continuity without claiming readiness.
- **Alternatives considered**:
  - Hide all cues during syncing: rejected because it would make reload recovery feel like a temporary UI collapse rather than a temporary loading state.
  - Leave cues visible and allow optimistic picks: rejected because it would reintroduce the backend-authority mismatch that caused the bug.

## Decision 4: Retry temporary hydration failures automatically

- **Decision**: Retry unresolved dining-context hydration in the background while keeping the affected seat visibly unresolved.
- **Rationale**: Temporary failures should not force manual recovery for a normal refresh scenario, but the UI also cannot pretend the seat is ready while truth is still unknown.
- **Alternatives considered**:
  - Wait for manual refresh or reselection: rejected because it pushes recovery burden onto the guest for transient backend or network lag.
  - Surface a hard error immediately with no retry: rejected because it turns a recoverable state into a needless dead end.

## Decision 5: Preserve open-order continuity across reselection and checkout summary continuity after checkout

- **Decision**: When a guest reselects a seat with an active open order, restore its prior lines and allow more lines to be added; after checkout, keep the checked-out seat selected long enough to show its final summary in the selected-seat area.
- **Rationale**: Selection is a browsing affordance, not an ownership reset. Guests must be able to leave and return to the same seat context without losing order visibility, and checkout should conclude with a summary instead of an abrupt empty state.
- **Alternatives considered**:
  - Treat reselection as a fresh view with no carried order lines: rejected because it hides backend-authoritative order continuity.
  - Clear the selected-seat area immediately after checkout: rejected because it removes the final confirmation the guest expects to see.
