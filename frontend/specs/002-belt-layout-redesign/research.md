# Phase 0 Research: Kaiten-Zushi Belt Redesign

## Decision 1: Use a wide squarer loop geometry instead of an ellipse or a complex serpentine belt

- **Decision**: Redesign the stage around a wide rounded-rectangle loop with longer horizontal runs and softened corners, rather than a perfect circle, a more elliptical racetrack, or a multi-turn serpentine path.
- **Rationale**: A squarer loop feels materially closer to a real conveyor counter, creates more usable straight-lane length for plate spacing, and gives the center enough room to read as a kitchen-prep area. It still preserves readability, remains responsive, and can be derived from stable slot indices without introducing brittle path complexity.
- **Alternatives considered**:
  - A perfect circle: rejected because it preserves the main visual limitation of Feature 001.
  - A more elongated ellipse / classic racetrack: rejected because it still leaves the belt reading too rounded and shortens the visually useful straight sections.
  - A complex serpentine conveyor with multiple turns and corners: rejected for this phase because it increases layout, accessibility, and responsiveness risk without being necessary to achieve a strong restaurant feel.

## Decision 2: Preserve the existing data flow and change only the derived layout model

- **Decision**: Keep `BeltsApi`, the current store, and the snapshot/seat polling flow intact, while replacing the stage's circular coordinate derivation with a path-based layout helper.
- **Rationale**: The current data flow already preserves the trust-critical behavior from Feature 001. The redesign goal is mainly spatial and visual, so the lowest-risk path is to keep backend-authoritative state intact and revise only how that state is presented on screen.
- **Alternatives considered**:
  - Rewrite the store around a new layout-specific state model: rejected because it would add structural risk without product benefit.
  - Push layout decisions into the store itself: rejected because geometry calculations are easier to test and evolve as pure view-model helpers.

## Decision 3: Use a category-first visual registry for menu items

- **Decision**: Define a menu-item visual registry that first maps items into recognizable families such as nigiri, sashimi, rolls, gunkan, sides, desserts, and drinks, then applies item-specific overrides for especially distinctive seeded dishes.
- **Rationale**: The seeded item list is broad enough that a one-off visual per item would be heavy, while a pure generic fallback would undercut recognition. A category-first registry keeps maintenance low, supports future menu expansion, and still allows distinctive items like tamago nigiri, ikura gunkan, miso soup, or beer to read clearly.
- **Alternatives considered**:
  - Fully bespoke visual art for every menu item: rejected because it is expensive to maintain and not necessary for first-pass recognition.
  - One generic food icon for all plates: rejected because it misses the core redesign goal of improving food recognition.

## Decision 4: Make occupied seats obvious through belt-facing stool and place-setting cues

- **Decision**: Treat seats as belt-facing sushi-counter stools or places, distribute them in an even guest rhythm across the front edge, and show occupied seats through a stronger in-use cue such as a place setting, guest-presence indicator, or clearly different seat surface treatment.
- **Rationale**: The spec explicitly asks for seats to become more recognizable as seats and for occupied seats to be more obvious. Facing the seats toward the belt and spacing them more evenly makes the whole counter layout easier to read before the user even notices the occupancy cue.
- **Alternatives considered**:
  - Keep the existing marker model and only intensify colors: rejected because it does not materially improve recognizability.
  - Use literal human character illustrations: rejected because it risks visual clutter and drifting into mascot-heavy presentation.

## Decision 5: Use the belt center as a kitchen-prep zone with chef presence

- **Decision**: Treat the inner area of the loop as a working kitchen/prep zone with a visible sushi-chef presence, prep counter, and service details.
- **Rationale**: The redesign felt unfinished when the center of the stage remained a mostly empty decorative region. A kitchen core completes the restaurant read without adding new product behavior.
- **Alternatives considered**:
  - Leave the center mostly empty: rejected because it wastes the most prominent interior space and makes the stage feel like an abstract diagram.
  - Add multiple animated staff figures: rejected because it adds motion noise and unnecessary implementation weight.

## Decision 6: Move operational panels below the main belt hero

- **Decision**: Use a belt-first layout with the main conveyor stage at the top of the page and lower-priority information panels stacked below it.
- **Rationale**: The redesign's primary hierarchy goal is to make the belt feel like a place rather than a dashboard widget. Moving support content below the stage keeps trust-critical information available while letting the conveyor dominate the first screen.
- **Alternatives considered**:
  - Keep a two-column layout with a large sidebar beside the belt: rejected because it continues to split attention and limits horizontal conveyor space.
  - Hide most status information behind toggles or drawers: rejected because the page still needs to communicate loading, pause, and freshness clearly.

## Decision 7: Preserve reduced-motion and degraded-state clarity as non-negotiable behavior

- **Decision**: Treat reduced motion, paused state, and degraded freshness as first-class layout inputs during the redesign rather than secondary polish items.
- **Rationale**: Feature 001 established trust by making those states clear. A more atmospheric layout must not hide them inside the design language.
- **Alternatives considered**:
  - Defer state treatment decisions until visual polish: rejected because readability could regress late in the feature.
  - Simplify reduced-motion mode to a less informative static snapshot: rejected because it would fail the existing behavioral guarantees.

## Decision 8: Keep the redesign local to the current app surface

- **Decision**: Implement the redesign by editing the root app shell and extending the existing `belt-visualization` area with small new helper files for geometry and menu-item visuals.
- **Rationale**: The constitution favors conservative surface growth. This redesign needs new presentation logic, but not new routes, feature modules, or broad service layers.
- **Alternatives considered**:
  - Create a new route or page shell just for the redesign: rejected because the app still has one active product surface.
  - Introduce a shared design system or large asset pipeline first: rejected because the redesign can be delivered with a smaller local visual model.
