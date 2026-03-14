# Feature Specification: Kaiten-Zushi Belt Redesign

**Feature Branch**: `[002-belt-layout-redesign]`  
**Created**: 2026-03-14  
**Status**: Draft  
**Input**: User description: "Redesign the existing sushi belt overview so it feels much more like a real kaiten-zushi restaurant while preserving the trusted read-only behavior from Feature 001."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Feel The Conveyor As A Place (Priority: P1)

As a viewer, I can open the page and immediately feel like I am looking at a real sushi train counter with a rounded-square conveyor layout, seats on all four sides, and a visible service core, rather than a circular systems diagram.

**Why this priority**: The primary purpose of this feature is to change the page from a functional diagram into a restaurant-like experience without losing operational trust. If the conveyor layout and page hierarchy do not feel materially more realistic, the redesign does not deliver its core value.

**Independent Test**: Can be fully tested by loading the page with existing belt data and verifying that the belt dominates the page, uses a rounded-square counter loop with clear straight runs and corners, distributes seats around all four sides, shows a visible kitchen core inside the loop, and preserves the same backend-authoritative motion behavior as Feature 001.

**Acceptance Scenarios**:

1. **Given** a belt snapshot is available, **When** the viewer opens the page, **Then** the system shows a rounded-square conveyor layout with clear corners and longer straight runs that reads as a sushi counter rather than a circular dashboard element.
2. **Given** the page includes support information such as freshness or metrics, **When** the viewer scans the page, **Then** the belt remains the dominant visual element and lower-priority information appears as a compact rail or notice area below the main stage rather than as a competing sidebar.
3. **Given** the belt is moving, **When** no new server response has arrived yet, **Then** the conveyor motion still follows the authoritative timing behavior already established in Feature 001.
4. **Given** the viewer sees the page, **When** they interpret the environment, **Then** the presentation communicates a warm kaiten-zushi atmosphere through counter-like layout, believable conveyor structure, a visible kitchen core, and restrained Kawaii styling.

---

### User Story 2 - Recognize Seats And Food Quickly (Priority: P2)

As a viewer, I can quickly recognize where the seats are, which ones are occupied, and what kind of dish is on each visible plate.

**Why this priority**: The redesign only becomes more useful if the extra space leads to better recognition of seats and food, not just a prettier layout.

**Independent Test**: Can be fully tested by loading representative seeded menu items and seat occupancy data, then verifying that seats read as seats, occupied seats are clearly distinct, and visible plates show recognizable food representations or appropriate fallbacks.

**Acceptance Scenarios**:

1. **Given** seat occupancy data is available, **When** the viewer opens the page, **Then** seats appear as stool-like or counter-place elements distributed around all four sides of the counter in proportion to side length and oriented toward the belt rather than abstract markers.
2. **Given** some seats are occupied, **When** the viewer interprets the stage, **Then** occupied seats are recognizable through more than color alone.
3. **Given** visible plates contain different menu item families, **When** the viewer scans the belt, **Then** nigiri, rolls, gunkan, sides, desserts, and drinks are visually distinguishable from one another.
4. **Given** a visible menu item does not have a highly specific visual treatment, **When** it is rendered, **Then** the system uses a graceful category-level or generic fallback rather than an uninformative blank state.

---

### User Story 3 - Keep Trust During Real-World Use (Priority: P3)

As a viewer, I can still understand the page during loading, pause, degraded freshness, reduced motion, small screens, and unusual data conditions.

**Why this priority**: The redesign must not trade away clarity or resilience. The page still needs to be dependable under the same real-world conditions as Feature 001.

**Independent Test**: Can be fully tested by exercising paused state, degraded freshness, reduced motion, variable slot and seat counts, unknown menu items, and narrow viewports while confirming the page remains understandable and visually prioritized around the belt.

**Acceptance Scenarios**:

1. **Given** reduced-motion presentation is requested, **When** the viewer opens the page, **Then** the page preserves the same understanding of dish placement, seat occupancy, and current state without continuous interpolation.
2. **Given** the page is loading, paused, or degraded, **When** the viewer interprets the screen, **Then** the state remains clear without relying only on color or tiny peripheral panels.
3. **Given** the page is viewed on a smaller screen, **When** the layout adapts, **Then** the belt remains the visual priority and lower-priority content stacks below it.
4. **Given** slot counts, seat counts, or menu items differ from seeded expectations, **When** the page renders, **Then** the layout remains coherent and readable without assuming fixed local defaults.

### Edge Cases

- A belt has many visible plates at once; slot and plate sizing should remain readable without turning the hero area into visual clutter or causing heavy overlap on the straight runs.
- A belt has very few occupied slots; the page should still feel like a believable conveyor environment rather than an empty dashboard shell.
- A returned menu item is unknown, newly added, or too specialized for a bespoke visual; the page should fall back to a recognizable generic or category treatment.
- Drinks, soups, or desserts appear on the belt; they should not be mistaken for nigiri or rolls when a cup, bowl, or dessert-like treatment is more legible.
- Seat occupancy data shows no occupied seats; the page should still make seats easy to find, keep the longer top and bottom runs visually roomier than the shorter sides, and clearly orient all seats toward the counter.
- The viewport becomes narrow or short; the belt should remain the primary visual, with secondary content moving lower in the scroll flow.
- The belt is paused or freshness degrades after prior success; the redesigned layout should keep trust-critical messaging as clear as in Feature 001.
- Reduced-motion presentation is active; the page should still communicate the restaurant feel and item recognition without relying on motion.

## Assumptions

- This feature redesigns the existing single read-only belt page rather than introducing a new product area.
- The preferred geometry choice for this phase is a rounded-square counter loop with longer straight runs and softened corners so the belt reads more like a real restaurant conveyor than an ellipse.
- The page should use a warm wood sushi-counter atmosphere with restrained Kawaii styling rather than photorealism or mascot-heavy decoration.
- Secondary information such as freshness, metrics, and supporting state detail can move below the main belt area without reducing the user's trust in the page.
- Menu item visuals should use a scalable strategy: category-based visual families with item-specific overrides for especially recognizable dishes.
- Unknown or future menu items should render through the same fallback system rather than requiring immediate custom artwork.
- The backend remains the authoritative source for slot identity, plate occupancy, seat occupancy, and timing-driven motion behavior.
- This feature preserves the current polling and refresh-after-write behavior from Feature 001 unless a later plan identifies a non-breaking refinement.

## Visual Direction

### Layout Geometry

- The belt should be wide, horizontal, and restaurant-like rather than centered as a compact circle.
- The overall shape should suggest a real conveyor embedded in a counter, with a rounded-square footprint, longer straight runs, softened square corners, and enough lane length to reduce visual crowding between neighboring plates.
- The belt should consume most of the top section width and become the first thing users understand on page load.

### Counter And Seating Language

- The conveyor should feel installed within a counter or serving surface rather than floating in a neutral diagram.
- Seats should resemble stools or place settings arranged in a proportionate rhythm around all four sides of the serving line, with longer top and bottom runs able to carry more seats than the shorter sides.
- Seat silhouettes should visually face the belt so the counter reads like a place guests can sit at.
- Occupied seats should appear visibly in use through shape, props, silhouette, or place-setting cues, not just a tinted badge.
- The inner area of the belt should read as a working kitchen zone with a sushi-chef presence rather than empty decorative space.

### Food Representation

- Plate contents should feel playful and recognizable through stylized food illustrations or iconographic dish art.
- Nigiri, rolls, gunkan, sides, desserts, and drinks should each have a distinct visual family.
- Drinks and soups may use vessel-like presentation when that improves recognition.
- The plate surface and food treatment should be large enough to improve scanning compared with Feature 001.

### Atmosphere And Hierarchy

- The page should feel like a sushi train venue, not a generic status monitor.
- Warm materials, subtle depth, plate presence, and restaurant cues should support that feeling without making the page noisy.
- Operational details should remain available but no longer compete with the hero belt area.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST preserve the existing page as a read-only single-belt experience.
- **FR-002**: The system MUST present the primary belt using a rounded-square counter loop instead of the current round stage.
- **FR-003**: The system MUST make the belt the dominant visual element in the top section of the page.
- **FR-004**: The system MUST place lower-priority support information such as freshness, metrics, and auxiliary status content below the main belt area in a compact rail or notice surface that does not compete with the belt.
- **FR-005**: The system MUST preserve stable backend-authoritative slot identity and MUST not simulate movement by reordering slot state.
- **FR-006**: The system MUST preserve backend-authoritative motion behavior derived from timing metadata between successful refreshes.
- **FR-007**: The system MUST preserve stable seat positions that remain independent from belt motion.
- **FR-008**: The system MUST render seats as recognizable seat or counter-place elements rather than abstract dots or generic nodes.
- **FR-009**: The system MUST make occupied seats visually obvious through seat-specific place-setting, silhouette, or occupancy cues beyond color alone.
- **FR-010**: The system MUST increase the apparent size and legibility of slots and plates compared with Feature 001.
- **FR-010a**: The system MUST distribute visible slot positions along the longer straight runs and rounded corners in a way that materially reduces apparent overlap between neighboring plates.
- **FR-011**: The system MUST present visible plate contents using recognizable dish visuals that fit the existing Kawaii-but-readable product direction.
- **FR-012**: The system MUST visually distinguish major dish families, including nigiri, sashimi, rolls, gunkan, sides, desserts, and drinks.
- **FR-013**: The system MUST support category-level fallback visuals and generic fallback visuals for unknown or future menu items.
- **FR-014**: The system MUST support seeded menu items with more specific dish recognition where that materially improves scanning quality.
- **FR-015**: The system MUST preserve clear loading, paused, empty, error, and degraded-freshness states in the redesigned layout.
- **FR-016**: The system MUST preserve reduced-motion support without losing comprehension of seat occupancy, dish placement, or current state.
- **FR-017**: The system MUST maintain readable layout behavior for varying slot counts and seat counts without assuming fixed local defaults.
- **FR-018**: The system MUST remain responsive on desktop and mobile, keeping the belt as the primary visual focus in both contexts.
- **FR-019**: The system MUST remove duplicate page titling, including the duplicated “Main Belt” label.
- **FR-020**: The system MUST preserve the current page's read-refresh behavior, including periodic refresh and the future-compatible refresh-after-write hook.
- **FR-021**: The system MUST communicate occupied seats, plate presence, paused state, and degraded freshness without depending on color alone.
- **FR-022**: The system MUST create a stronger sushi-train atmosphere through counter-like layout, warm material cues, plate depth, and restrained restaurant-themed detail.
- **FR-022a**: The system MUST render a visible kitchen or chef-prep core inside the belt loop, with the chef scene living inside the visible inner lane or island area so the center of the stage reads as working restaurant space.
- **FR-022b**: The system MUST orient and distribute guest seats so the seating rhythm reads as belt-facing counter seating on all four sides of the loop, while allowing the longer top and bottom runs to carry more seats than the shorter left and right sides.
- **FR-022c**: The system MUST use simple but more deliberate plate detailing so the plates read more like stacked kaitenzushi dishes than flat generic circles.
- **FR-023**: The redesign MUST remain within the current application scope and MUST not introduce pickup flows, ordering flows, checkout flows, admin controls, or multi-belt navigation.

### Key Entities _(include if feature involves data)_

- **Belt Layout Surface**: The visible restaurant-like conveyor and counter presentation for one backend-authoritative belt.
- **Belt Slot**: A stable physical position on the conveyor that may be empty or may display one plate.
- **Plate Presentation**: The combined plate, tier treatment, and visible dish representation shown for an occupied slot.
- **Menu Item Visual Family**: The recognizable visual grouping that allows menu items to render as nigiri, sashimi, rolls, gunkan, sides, desserts, drinks, or fallback dishes.
- **Seat Place**: A fixed seat or counter-place around the belt with stable identity and visible occupied or unoccupied state.
- **Status Surface**: The set of page-level signals that communicate loading, freshness, pause state, and reduced-motion behavior.

## Out of Scope

- Plate pickup, ordering, checkout, or other mutation flows.
- Multi-belt navigation or route-driven belt selection.
- Backend contract redesign for menu item visuals.
- Replacing backend-authoritative motion behavior with frontend-only animation logic.
- Introducing mascot-heavy decorative storytelling that competes with comprehension.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In a structured design review using the Feature 001 page as the baseline, at least 4 of 5 reviewers rate the redesigned page at 4 out of 5 or higher for "feels like a kaiten-zushi restaurant" and at least 1 point higher than the prior circular overview.
- **SC-002**: In usability review, at least 90% of test participants can identify the belt as the primary focus of the page within 5 seconds of first view on desktop and mobile.
- **SC-003**: In usability review, at least 90% of test participants can correctly distinguish occupied seats from unoccupied seats on their first attempt.
- **SC-004**: In usability review, at least 90% of test participants can correctly distinguish the major visible dish families on the belt on their first attempt.
- **SC-005**: In acceptance testing, 100% of tested scenarios with varying seat counts, slot counts, and unknown menu items remain readable without broken layout or misleading fallback treatment.
- **SC-006**: Reduced-motion presentation preserves the same understanding of seat occupancy, dish placement, and pause/freshness state as the standard presentation for all acceptance test scenarios.
- **SC-007**: In acceptance testing, the redesign preserves Feature 001 trust guarantees: stable slot identity, backend-authoritative motion, stable seat positions, and clear paused/loading/degraded communication.
