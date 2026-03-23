# Phase 0 Research: Design Polish

## Decision 1: Keep the redesign presentation-first and preserve current behavioral ownership

- **Decision**: Limit the feature to presentation, copy tone, and layout framing changes in the existing app shell and `belt-visualization` area, while preserving the current backend contracts, store responsibilities, and guest workflows.
- **Rationale**: The specification explicitly defines design polish as a non-functional redesign. The current store already owns seat, plate, checkout, operator, and realtime behavior, so the safest path is to keep those behaviors authoritative and avoid scope creep into new interaction logic.
- **Alternatives considered**:
  - Rework the feature as a broader interaction redesign: rejected because it would violate the non-goals and make the feature harder to validate against current functionality.
  - Add a new presentation-specific page or route: rejected because the constitution favors conservative surface growth and the current root app shell is already the intended product surface.

## Decision 2: Put most of the visual polish into the existing shell, stage, and secondary panels

- **Decision**: Concentrate the redesign in `src/app/app.html`, `src/app/app.css`, `src/styles.css`, `src/app/belt-visualization/belt-stage.html`, `src/app/belt-visualization/belt-stage.css`, `src/app/belt-visualization/selected-seat-detail.component.html`, `src/app/belt-visualization/selected-seat-detail.component.css`, and `src/app/belt-visualization/operator-plate-placement.component.html` and `.css`.
- **Rationale**: These files already define the atmosphere, stage framing, state cards, selected-seat surface, operator surface, and kitchen presentation. Reusing them keeps the redesign grounded in the current composition instead of inventing new layers.
- **Alternatives considered**:
  - Add a separate design-system package or shared component library: rejected because the app is still a single small Angular surface and does not justify extra architecture.
  - Keep the redesign entirely in global CSS without template changes: rejected because some humorous secondary lines, structural framing, and decorative support regions need light template support.

## Decision 3: Preserve the current belt-readability contract and only stretch layout when it is clearly better

- **Decision**: Treat the current belt, slot, plate, and seat readability as a baseline contract. Default to the current stage proportions, and permit proportional stage or spacing refinements only when before-and-after validation confirms equal-or-better comprehension of slot order, seat position, reach cues, and action targets on desktop and mobile layouts.
- **Rationale**: The user explicitly called out that belt, slots, plates, and seats must remain at least as good as now, and the current layout is already backed by `belt-layout.ts`, `belt-view-model.ts`, and the existing stage template. A polish pass must not regress this trust-critical surface.
- **Alternatives considered**:
  - Freeze the layout entirely and change only colors and typography: rejected because the specification allows stretching or improving the stage if readability improves.
  - Pursue a dramatic stage re-layout as the core of the feature: rejected because it would shift the feature from polish into another structural redesign.

## Decision 4: Layer humor as secondary copy and decorative framing, never as the primary state signal

- **Decision**: Keep literal labels and accessible state descriptions primary, and place deadpan or playful language in secondary helper text, status sublabels, empty states, success notes, retry notices, and decorative headings.
- **Rationale**: Humor is part of the requested emotional outcome, but the specification and constitution both prioritize clarity and accessibility. The safest pattern is to let users understand the real state first and then notice the joke.
- **Alternatives considered**:
  - Replace core labels like occupancy or pending with jokes: rejected because it would undermine immediate comprehension and make important UI states ambiguous.
  - Remove humor from critical feedback entirely: rejected because the feature specifically aims to make the product feel funny and memorable.

## Decision 5: Use kitchen, chef, and ingredient cues as recurring character signals rather than new features

- **Decision**: Build the funny and kawaii tone around recurring non-essential cues in the kitchen center, patterned backgrounds, decorative ingredient moments, and visual personality in the current chef and food surfaces.
- **Rationale**: The stage already has a kitchen core and chef presence, which makes it the most natural anchor for personality. Reinforcing that existing motif creates a cohesive “playful sushi cafe” feel without introducing false affordances.
- **Alternatives considered**:
  - Introduce a full mascot-led interaction system: rejected because the feature should feel like a polished product, not a game or novelty app.
  - Spread unrelated jokes evenly across all surfaces: rejected because the humor would feel random instead of authored.

## Decision 6: Preserve reduced-motion parity by emphasizing static identity over added continuous animation

- **Decision**: Make the redesign feel rich through color, texture, silhouette, and short supportive transitions while keeping reduced-motion mode functionally equivalent and avoiding any new mandatory continuous motion.
- **Rationale**: The current experience already has important motion derived from backend timing and realtime updates. A polish pass should not add continuous decorative animation that reduced-motion users would have to lose in order to keep the app usable.
- **Alternatives considered**:
  - Add bouncing, floating, or mascot-loop animations as the main expression of personality: rejected because those effects would either distract from the belt or disappear entirely in reduced-motion mode.
  - Strip all transitions in reduced-motion mode and accept a flatter experience: rejected because the product still needs to feel intentionally designed when motion is reduced.

## Decision 7: Introduce only minimal presentation metadata when templates need explicit semantic style hooks

- **Decision**: Keep the current state and view-model architecture intact, but allow small presentation metadata additions where needed for secondary labels, decorative tone selection, or clearer template structure.
- **Rationale**: Some humor and design requirements depend on templates knowing whether a state should carry a playful subline, a certain presentation tone, or a decorative variant. Small semantic additions are cheaper and safer than over-encoding styling in templates or duplicating logic in CSS alone.
- **Alternatives considered**:
  - Forbid any TypeScript changes at all: rejected because that would force brittle template logic or prevent some clear secondary presentation semantics.
  - Create a separate presentation-only store layer: rejected because it would add needless architecture for a small frontend.
