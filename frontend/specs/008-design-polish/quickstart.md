# Quickstart: Design Polish

## Prerequisites

1. Work on branch `008-design-polish`.
2. From `C:\se\sushi-train\frontend`, install dependencies with `npm install`.
3. Keep the existing backend endpoints and frontend data flow unchanged for this feature.
4. Treat the current belt, seat, plate, operator, and realtime interactions as behavioral baselines that the redesign must preserve.

## Implementation Outline

1. Audit the current visual and copy surfaces in `src/app/app.html`, `src/app/app.css`, `src/styles.css`, and the `belt-visualization` templates and styles.
2. Define or refine a shared token layer for palette, typography, spacing, depth, decorative motifs, and feedback tone that supports a playful sushi-cafe identity.
3. Refresh the page shell so loading, empty, error, and ready states feel authored and charming without changing their meaning.
4. Redesign the belt stage presentation, kitchen framing, seats, plates, and surrounding surfaces so the app feels more kawaii and funny while preserving or improving readability.
5. Update selected-seat detail and operator surfaces so their copy and presentation match the new visual tone while keeping primary actions and messages explicit.
6. Introduce only small semantic presentation metadata where templates need a clear secondary label, decorative state, or responsive presentation hook.
7. Verify that humor remains secondary to comprehension and that reduced-motion and mobile layouts preserve the same task clarity.

## Manual Verification

1. Start the app with `npm start` from `C:\se\sushi-train\frontend`.
2. Review the loading, empty, ready, and fatal error surfaces and confirm each state still reads plainly before any playful secondary copy.
3. Confirm the main belt stage remains the dominant visual element and that belt, slot, plate, and seat positions are at least as clear as in the current experience.
4. Select free and occupied seats and verify that state cues remain immediately distinguishable without relying only on color.
5. Trigger plate-pick, checkout, and operator-placement flows and confirm the redesigned UI preserves the same actions, sequencing, and feedback meanings.
6. Verify that humorous copy appears as a supporting layer rather than replacing important status labels or instructions.
7. Check the experience on a narrow viewport and confirm that decoration trims down before functional clarity does.
8. Enable reduced motion and confirm the product still feels polished without adding new mandatory motion.
9. Confirm that realtime update and degraded-state messaging remain trustworthy and readable during ongoing updates.

## Automated Verification

1. Add or update targeted tests only where semantic output, view-model presentation metadata, or observable rendering conditions change.
2. Run `npm test`.
3. Run `npm run build`.

## Validation Notes

- This feature should prefer CSS, template, and copy refinements over behavioral rewrites.
- Any layout stretch or rebalance must be justified by equal-or-better belt readability.
- The strongest jokes should live in secondary copy, decorative framing, and feedback moments, not in primary controls or accessible labels.

## Stakeholder Design Review

- Review outcome: pass for the intended soft cafe kawaii direction. The updated shell, hero, stage framing, kitchen character, and supporting surfaces now read as warmer, more authored, and more playful without turning the product into a game UI.
- Review outcome: pass for humor restraint. The strongest jokes live in secondary lines such as the chef note, helper copy, and feedback subtext, while primary headings and controls remain literal.
- Review outcome: pass for scope discipline. The redesign stays inside the presentation layer and preserves the existing seat, plate, checkout, and operator workflows.

## Readability And Usability Review

- Review outcome: pass for core action clarity on desktop and mobile. Seat labels, reach-area meaning, operator entry, and state cards remain readable with the current-balanced stage layout.
- Review outcome: pass for dense counters. The stage now trims ornaments and compacts seat labels through deterministic presentation metadata instead of stretching the layout.
- Review outcome: pass for reduced-motion and degraded-state trust. Literal notices remain first, while secondary humorous copy is visually subordinate.

## Content Review

- Review outcome: pass for primary versus secondary language. Primary labels such as loading, empty, fatal, seat status, and operator actions remain explicit and task-oriented.
- Review outcome: pass for supporting humor. Secondary copy adds personality in the shell, stage, selected-seat detail, operator surface, and feedback notices without replacing required instructions.
- Review outcome: pass for consistency. The product voice stays dry and warm rather than loud, with recurring kitchen/chef cues instead of random one-off jokes.

## Implementation Validation

- Automated verification passed with `npx ng test --watch=false`.
- Production build passed with `npm run build`.
