# Phase 0 Research: Pick Plates

## Decision 1: Preserve the current belt-stage presentation and add new interaction UI below it

- **Decision**: Keep the current belt, seat, and stage layout visually intact and add the selected-seat detail area below the existing belt UI by default.
- **Rationale**: The latest product clarification explicitly rejects layout compression and broader stage redesign. The new behavior should feel additive, not like a replacement of the existing belt experience.
- **Alternatives considered**:
  - Add a side panel beside the stage: rejected because it risks shrinking or crowding the current belt presentation.
  - Recompose the stage and controls into a new two-column shell: rejected because it introduces avoidable visual disruption.

## Decision 2: Keep seat clicks selection-only and keep writes outside the stage

- **Decision**: Clicking a seat only changes the selected seat; explicit occupy and checkout actions stay in the selected-seat detail area.
- **Rationale**: This keeps browsing safe and preserves a clear separation between exploration and writes.
- **Alternatives considered**:
  - Allow direct occupy or checkout from seat clicks: rejected because it recreates accidental writes.
  - Use a hidden active dining seat separate from the selected seat: rejected because it adds invisible state and UI ambiguity.

## Decision 3: Show reach as a seat-anchored reachable area rather than slot-based highlighting

- **Decision**: Visualize pickability as a reachable area anchored to the selected seat, readable as a circular, halo-like, or equivalent seat-owned field.
- **Rationale**: The core learning from review is that slot-level highlighting looks like the pickable area is moving with the belt. A seat-owned area better matches user expectation while preserving the neighboring-seat reach rule.
- **Alternatives considered**:
  - Highlight moving pickable slots in place: rejected because it visually ties reach to the moving belt rather than to the seat.
  - Remove the reach cue entirely and rely only on messages: rejected because users still need to understand where picking is allowed before acting.

## Decision 4: Make pickable plates easy to hit by enlarging effective interaction surface

- **Decision**: Give pickable plates enough effective click or tap surface that guests can pick them reliably without precise pixel targeting.
- **Rationale**: The plates remain in motion, so usability depends on forgiving interactions rather than only visual correctness.
- **Alternatives considered**:
  - Keep interaction limited to the visible plate bounds: rejected because it makes moving targets unnecessarily fiddly.
  - Pause the belt during picking: rejected because it alters the authoritative motion model and changes the product feel too much.

## Decision 5: Use brief reject feedback on the plate while keeping belt motion authoritative

- **Decision**: When an unpickable plate is attempted, show brief visible reject feedback on that plate, such as a small whole-plate vibration, while the plate continues its path.
- **Rationale**: This makes failure obvious at the point of interaction without implying that the frontend controls or halts belt motion.
- **Alternatives considered**:
  - Show only text feedback outside the stage: rejected because it delays and weakens cause-and-effect.
  - Freeze or bounce the plate off the path: rejected because it conflicts with the backend-authoritative belt model.

## Decision 6: Keep backend `pickPlate` responses as the immediate running-order source of truth

- **Decision**: Continue updating the running order from the successful `pickPlate` response and then run the usual reconciliation refresh.
- **Rationale**: The backend already returns authoritative order lines and totals, so there is no need for local recomputation or delayed confirmation.
- **Alternatives considered**:
  - Recompute totals locally from visible plates: rejected because pricing and line snapshots are backend-authoritative.
  - Wait for a later seat refresh before showing the new order state: rejected because it weakens feedback after a successful pick.
