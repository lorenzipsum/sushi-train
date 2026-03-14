# Feature Specification: Sushi Belt Visualization

**Feature Branch**: `[001-belt-visualization]`  
**Created**: 2026-03-14  
**Status**: In Review  
**Input**: User description: "Build the first frontend slice for a sushi belt visualization based on the existing backend contract and behavior."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - View the live belt state (Priority: P1)

As a viewer of the sushi belt, I can open the page and understand which slots currently hold plates and how those plates are moving around the belt in a top-down circular view.

**Why this priority**: This is the core value of the feature. Without a trustworthy live belt visualization, the frontend does not deliver the first usable product slice.

**Independent Test**: Can be fully tested by loading a belt with returned slot and timing data, then verifying that the page shows a circular belt, stable slot identities, visible occupied slots, and movement that continues smoothly between refreshes while the belt is running.

**Acceptance Scenarios**:

1. **Given** a belt snapshot with stable slot identities, occupied slots, and non-zero movement data, **When** the viewer opens the page, **Then** the system shows a top-down circular belt with the returned occupied slots and plates positioned according to the current visual rotation.
2. **Given** a belt snapshot indicates the belt is moving, **When** no new server response has arrived yet, **Then** the visual belt motion continues smoothly based on the returned timing information rather than waiting for the next refresh.
3. **Given** a refreshed belt snapshot changes the reported movement state, **When** the new snapshot is applied, **Then** the view trusts the new returned state even if it causes a visible jump in plate positions.
4. **Given** the belt visualization is shown, **When** the viewer sees the interface, **Then** the presentation communicates a Japanese Kawaii style through playful, food-friendly visual treatment without reducing readability of slot, seat, and plate state.
5. **Given** the first version is presented, **When** the viewer scans the page, **Then** the overall composition feels like a warm sushi bar rather than a generic dashboard, with a balanced overview of motion, seats, and current state.

---

### User Story 2 - Understand seat occupancy around the belt (Priority: P2)

As a viewer, I can see the stable seats around the belt and identify which seats are occupied without confusing seat positions with moving belt positions.

**Why this priority**: Seat context is part of the minimum read-only overview and is required for users to interpret the belt layout in the same view.

**Independent Test**: Can be fully tested by loading seat occupancy data alongside belt data and verifying that seats remain fixed in place while the belt rotates, with occupied and unoccupied seats clearly distinguishable.

**Acceptance Scenarios**:

1. **Given** the system has seat occupancy data, **When** the viewer opens the page, **Then** the system shows seats in stable positions around the belt and indicates which seats are occupied.
2. **Given** the belt is visually rotating, **When** the viewer watches the page, **Then** seat positions remain fixed while only belt content appears to move.
3. **Given** seats are shown around the belt, **When** the viewer interprets occupancy, **Then** seat markers appear as soft stool-like elements with clear occupied and unoccupied states rather than abstract technical markers.

---

### User Story 3 - Rely on the view during loading, pause, and recovery states (Priority: P3)

As a viewer, I can tell when data is loading, paused, stale, unavailable, or presented with reduced motion so I do not misread the current state of the belt.

**Why this priority**: Trust in the visualization depends on the viewer being able to distinguish live motion from paused or degraded states.

**Independent Test**: Can be fully tested by exercising initial load, refresh failure, paused belt, empty belt, and reduced-motion conditions and verifying that each state is communicated without requiring any seat interaction features.

**Acceptance Scenarios**:

1. **Given** the page is waiting for its first successful belt snapshot, **When** the viewer opens the page, **Then** the system shows a loading state instead of implying that the belt is empty or paused.
2. **Given** the returned movement state indicates no motion, **When** the viewer observes the belt, **Then** the system shows the belt as paused and keeps visual positions stationary.
3. **Given** a refresh attempt fails after a prior successful load, **When** the viewer remains on the page, **Then** the last successful belt and seat state remains visible and the system indicates that the view is not fully current.
4. **Given** reduced-motion presentation is requested, **When** the viewer opens the page, **Then** the system presents the same belt and seat information without continuous interpolated motion.
5. **Given** loading, paused, or degraded states are shown, **When** the viewer sees those states, **Then** the page keeps the same friendly Kawaii tone without depending on mascots or exaggerated decorative animation.

### Edge Cases

- A returned belt contains no plates in any slot; the viewer should still see the belt structure and seat overview rather than an error.
- The returned slot count or seat count differs from seeded local defaults; the view must still render correctly without assuming fixed counts or spacing.
- The movement state reports zero speed; the visualization must remain stationary and communicate that the belt is paused.
- The backend returns a new movement state after a speed change that causes an abrupt visual jump; the frontend must trust and render the new returned state.
- The belt and seat responses arrive at different times; the view must avoid implying that seats move with the belt and should continue showing the most recent valid state for each source.
- The first seat response is slower than the first belt snapshot; the view may start rendering the belt once the snapshot is available while seat state continues loading or later degrades independently.
- A refresh fails temporarily after the page has already shown valid data; the user should keep the last known good state and see that freshness is degraded.
- Reduced-motion presentation is active while the belt is otherwise moving; occupancy and relative positions must remain understandable without continuous rotation animation.
- Decorative styling becomes too playful or dense and makes it harder to distinguish seats, occupied slots, or plate states; the design must favor legibility over novelty.
- The warm color palette reduces contrast for status or text; the design must preserve accessible contrast while keeping the intended Kawaii tone.

## Assumptions

- This first slice covers a single read-only sushi belt overview page rather than a full multi-belt management experience.
- The initial visual presentation is a top-down circular belt view.
- The visual direction for this feature is Japanese Kawaii, using a friendly, charming presentation that still preserves operational clarity and accessibility.
- The initial palette direction is a warm sushi bar look with cream, coral, salmon, matcha, and restrained soy-inspired accent tones.
- The first version uses charming UI styling without mascot characters or strong illustrated personalities.
- Decorative motion should be subtle and charming, with the belt rotation as the primary motion and only gentle supporting transitions elsewhere.
- The layout should prioritize a balanced overview rather than a sparse hero treatment or dense operational dashboard.
- Seats should read visually as soft, rounded stool-like markers with simple but clear state styling.
- Automatic refresh happens on a repeating interval between 2 and 5 seconds, and the view is also refreshed immediately after future successful write actions initiated elsewhere in the product.
- The current implementation treats the backend as a single-belt source for this page and always tracks the first returned belt rather than exposing belt-selection UI.
- Physical slot identities remain stable in the returned data, and the frontend derives visible motion from returned timing information instead of treating slot order as already rotated.
- Future interactive pickup scope may add seat-to-plate reachability rules, but that enforcement is not part of this read-only slice.

## Visual Direction

### Typography

- Headline typography should feel friendly and characterful, similar to inviting restaurant signage, while still remaining easy to scan at a glance.
- Supporting text should use a clean, highly legible sans-serif style that stays readable in labels, occupancy states, loading messages, and compact metadata.
- Numeric values and short status labels should remain crisp and unambiguous rather than overly decorative.

### Palette Tokens

- **Rice Cream**: the main light background tone for the page and broad surfaces.
- **Salmon Coral**: the primary highlight tone for active emphasis, calls to attention, and key decorative accents.
- **Tuna Rose**: a secondary warm accent for supporting highlights and layered playful detail.
- **Matcha Green**: the positive or healthy state accent for active freshness, occupied seats when appropriate, or reassuring status cues.
- **Soy Brown**: a grounding neutral for borders, belt material accents, and subtle depth.
- **Seaweed Ink**: the primary readable text and icon tone, used wherever clarity matters most.
- **Wasabi Gold**: a restrained premium accent for special plate tiers or celebratory emphasis, used sparingly.

### Shape Language

- The interface should favor rounded silhouettes, soft capsules, circular plates, and gentle ring shapes over sharp-edged dashboard geometry.
- Seats should resemble simplified soft stools or padded markers rather than abstract technical dots.
- Containers, status badges, and supporting panels should feel plush and welcoming, using soft radius and mild depth instead of hard shadows.

### Icon And Illustration Tone

- Icons should be simple, friendly, and food-aware without becoming mascot-driven.
- Any decorative visual details should support the sushi-bar atmosphere through materials, garnish-like accents, or charming micro-details rather than cartoon characters.
- Plate and seat states should remain recognizable even when decorative styling is removed.

### Motion Rules

- Belt rotation is the primary motion system and should feel smooth, calm, and trustworthy rather than fast or flashy.
- Secondary motion should be subtle and supportive: gentle fades, mild scale emphasis, soft shimmer, or restrained highlight transitions are acceptable.
- Decorative bounce, exaggerated wobble, or playful motion loops should not compete with the belt movement.
- Reduced-motion presentation must preserve the same hierarchy, charm, and comprehension without continuous belt interpolation.

### Information Hierarchy

- The rotating belt should remain the focal visual element.
- Seats, occupancy state, and freshness or loading cues should be clearly visible but visually subordinate to the belt itself.
- The page should feel like a balanced overview from a welcoming sushi venue, not a sparse landing page and not a dense operations console.

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The system MUST present a read-only top-down circular visualization of a sushi belt.
- **FR-001a**: The system MUST express the sushi belt page in a Japanese Kawaii visual style using friendly, food-themed visual language, rounded and playful presentation, and a warm, inviting tone while keeping state information easy to read.
- **FR-001b**: The system MUST use a warm sushi bar palette anchored in light neutrals and food-forward accent colors rather than a cold dashboard palette or harsh neon treatment.
- **FR-001c**: The system MUST achieve the Kawaii tone through layout, shape, color, and subtle visual detail rather than relying on mascot characters as a requirement for the MVP.
- **FR-002**: The system MUST render returned belt slots as stable physical positions in application state and MUST not treat refreshed slot ordering as if slots physically moved around the belt.
- **FR-003**: The system MUST display returned plate occupancy for each slot exactly as provided by the authoritative belt snapshot data and MUST not infer missing plates or redistribute plates for visual balance.
- **FR-004**: The system MUST derive on-screen belt motion from returned timing and speed information so that motion appears continuous between successful refreshes.
- **FR-005**: The system MUST trust newly returned movement data whenever fresh data arrives, including cases where the visible position of plates changes abruptly after a speed update.
- **FR-006**: The system MUST treat a reported speed of zero as a paused belt and display stationary belt content.
- **FR-007**: The system MUST display seats as stable positions around the belt that do not move with belt rotation.
- **FR-008**: The system MUST display seat occupancy from the authoritative seat occupancy data and clearly distinguish occupied seats from unoccupied seats.
- **FR-008a**: The system MUST present seats as soft, rounded stool-like markers whose visual design supports the Kawaii direction while preserving quick occupancy recognition.
- **FR-009**: The system MUST support belt layouts using the returned number of slots and seats and MUST not depend on seeded local defaults or any fixed slot-to-seat counts.
- **FR-010**: The system MUST automatically refresh belt and seat data on a repeating interval between 2 and 5 seconds.
- **FR-011**: The system MUST support an immediate refresh after future successful write actions so the view can quickly reflect authoritative backend state changes.
- **FR-011a**: The current page implementation MUST expose a feature-local refresh-after-write hook so future mutation flows can trigger an immediate reread without introducing route coupling or global event infrastructure in this slice.
- **FR-012**: The system MUST provide distinct loading, error, and degraded-freshness states so viewers can tell whether they are seeing initial, current, or last-known data.
- **FR-013**: The system MUST preserve the last successful visualization state during recoverable refresh failures until newer valid data is available or the viewer leaves the page.
- **FR-014**: The system MUST provide a reduced-motion presentation that preserves belt and seat information without requiring continuous interpolation.
- **FR-014a**: The system MUST preserve the Kawaii visual identity in loading, paused, error, and reduced-motion states without relying on excessive motion or decorative effects that obscure data.
- **FR-014b**: The system MUST keep non-belt motion subtle and supportive, using only gentle emphasis or transition effects outside the primary belt movement.
- **FR-015**: The MVP MUST remain read-only and MUST not require pickup interactions, seat actions, admin controls, or other mutation flows to deliver value.
- **FR-016**: The visualization MUST preserve stable seat and slot identities so future interaction scope can add seat-to-plate reachability rules without redefining the underlying view model.
- **FR-017**: The page MUST maintain a balanced information hierarchy where the rotating belt is the focal point and supporting status elements remain readable without turning the screen into a dense control panel.

### Key Entities _(include if feature involves data)_

- **Belt Snapshot**: The current authoritative belt view for one belt, including stable slot identities, current slot occupancy, and movement information used to derive visual rotation.
- **Belt Slot**: A physical slot on the belt with a stable identity and position index that may be empty or occupied by a plate.
- **Plate Occupancy**: The presence and displayable state of a plate in a specific slot at the time of the snapshot.
- **Seat**: A fixed viewer position around the belt with a stable identity and occupancy state.
- **Visual Rotation State**: The frontend-derived representation of where stable slot content should appear on screen at a given moment based on the latest authoritative movement information.

## Out of Scope

- Seat interaction flows, including pickup, ordering, or reachability enforcement in the UI.
- Administrative controls for creating belts, changing speed, or placing plates.
- Real-time transport beyond the defined periodic refresh behavior.
- Any assumption that the product supports only one fixed slot count, seat count, or spacing pattern.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: In acceptance testing, 100% of tested belt snapshots are rendered without assuming a fixed slot count, fixed seat count, or inferred plate placement.
- **SC-002**: During normal operation, viewers see refreshed authoritative belt and seat state within 5 seconds of a backend state change.
- **SC-003**: In usability review, at least 90% of test participants can correctly identify whether the belt is moving, paused, loading, or showing degraded freshness without external explanation.
- **SC-004**: In usability review, at least 90% of test participants can correctly identify occupied seats and occupied belt slots from the visualization on their first attempt.
- **SC-005**: Reduced-motion presentation preserves the same occupancy and pause information as the standard view for all acceptance test scenarios.
- **SC-006**: In design review, the interface is recognizably Japanese Kawaii in tone and presentation while still meeting the readability and state-recognition outcomes defined above.
- **SC-007**: In design review, the page is judged to match a warm sushi bar tone with subtle, non-mascot Kawaii styling and a balanced information layout.
