# Design Tokens: Sushi Belt Visualization

## Purpose

This document derives the first-pass CSS design tokens for the read-only sushi belt visualization feature from the approved visual direction in `spec.md`.

These tokens are intended to be implemented as CSS custom properties in the frontend when the feature is built. They define a warm sushi bar, Japanese Kawaii tone without requiring mascot-heavy illustration or overly decorative motion.

## Token Principles

- Keep the belt as the focal visual system.
- Prefer warm, food-forward colors over cold dashboard neutrals.
- Preserve clear readability for text, occupancy states, and degraded-freshness messaging.
- Use soft radius and mild depth to create a plush, welcoming feel.
- Keep non-belt motion subtle and supportive.

## Suggested CSS Variables

```css
:root {
  --color-rice-cream-50: #fffaf4;
  --color-rice-cream-100: #f8f0e5;
  --color-salmon-coral-400: #ff8a73;
  --color-salmon-coral-500: #f56f5a;
  --color-tuna-rose-400: #f29aac;
  --color-tuna-rose-500: #e97f95;
  --color-matcha-300: #b7d98b;
  --color-matcha-500: #77a95d;
  --color-soy-brown-300: #b19177;
  --color-soy-brown-500: #7b5a45;
  --color-seaweed-ink-700: #33403b;
  --color-seaweed-ink-900: #1f2a26;
  --color-wasabi-gold-400: #d8bb5b;
  --color-danger-plum-500: #b85c7a;
  --color-fog-blue-200: #dce8e8;

  --surface-page: var(--color-rice-cream-50);
  --surface-panel: rgba(255, 255, 255, 0.82);
  --surface-belt-outer: #d9b38c;
  --surface-belt-inner: #f3dfc9;
  --surface-seat: #f7d8bf;
  --surface-seat-occupied: #cfe7bf;
  --surface-plate: #fffdf9;
  --surface-plate-shadow: rgba(78, 52, 42, 0.12);

  --text-strong: var(--color-seaweed-ink-900);
  --text-default: var(--color-seaweed-ink-700);
  --text-soft: rgba(51, 64, 59, 0.72);
  --text-on-accent: #fffdf9;

  --border-soft: rgba(123, 90, 69, 0.18);
  --border-strong: rgba(123, 90, 69, 0.32);

  --accent-primary: var(--color-salmon-coral-500);
  --accent-secondary: var(--color-tuna-rose-500);
  --accent-positive: var(--color-matcha-500);
  --accent-premium: var(--color-wasabi-gold-400);
  --accent-muted: var(--color-soy-brown-300);
  --accent-info: var(--color-fog-blue-200);
  --accent-danger: var(--color-danger-plum-500);

  --plate-tier-green: #9acb7f;
  --plate-tier-red: #f07d72;
  --plate-tier-gold: #d7b24f;
  --plate-tier-black: #4b4b52;

  --radius-xs: 8px;
  --radius-sm: 14px;
  --radius-md: 22px;
  --radius-lg: 32px;
  --radius-round: 999px;

  --shadow-soft: 0 8px 24px rgba(78, 52, 42, 0.08);
  --shadow-float: 0 14px 36px rgba(78, 52, 42, 0.12);
  --shadow-plate: 0 6px 16px var(--surface-plate-shadow);

  --space-2xs: 4px;
  --space-xs: 8px;
  --space-sm: 12px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 48px;

  --font-display: 'Baloo 2', 'Quicksand', 'Nunito', sans-serif;
  --font-body: 'Nunito Sans', 'Inter', sans-serif;

  --text-title-xl: 700 2.5rem/1.05 var(--font-display);
  --text-title-lg: 700 1.8rem/1.1 var(--font-display);
  --text-body-lg: 600 1rem/1.5 var(--font-body);
  --text-body: 500 0.95rem/1.5 var(--font-body);
  --text-meta: 600 0.8rem/1.4 var(--font-body);

  --motion-belt-easing: linear;
  --motion-soft-easing: cubic-bezier(0.22, 1, 0.36, 1);
  --motion-fade-duration: 180ms;
  --motion-soft-duration: 240ms;
  --motion-emphasis-duration: 320ms;
}
```

## Token Usage Guidance

### Background And Surfaces

- Use `--surface-page` for the page background, optionally layered with subtle gradients or soft radial highlights.
- Use `--surface-panel` for cards, status panels, and supporting UI around the belt.
- Use `--surface-belt-outer` and `--surface-belt-inner` to distinguish the conveyor ring from the inner belt void.

### Text And Status

- Use `--text-strong` for headings, key counters, and occupancy labels.
- Use `--text-default` for standard body content.
- Use `--text-soft` for freshness timestamps, secondary descriptions, or helper text.
- Pair any accent background with `--text-on-accent` only when contrast remains accessible.

### Seats

- Use `--surface-seat` for free seats and `--surface-seat-occupied` for occupied seats.
- Seat borders should use `--border-soft` or `--border-strong` depending on visual scale.
- Seats should use `--radius-lg` or `--radius-round` to preserve the stool-like silhouette.

### Plates And Tiers

- Use `--surface-plate` and `--shadow-plate` for plate discs.
- Use the plate tier tokens directly to distinguish plate categories while keeping text and status indicators readable.
- Keep premium accents restrained; use `--plate-tier-gold` or `--accent-premium` sparingly.

### Motion

- The belt's continuous rotation should use `--motion-belt-easing`.
- Hover, refresh, focus, and state transitions should use `--motion-soft-easing`.
- Do not layer bounce or elastic motion on top of the main belt animation.

## Reduced Motion Guidance

- Preserve all color, shape, spacing, and hierarchy tokens in reduced-motion mode.
- Disable continuous interpolation while keeping subtle non-essential transitions minimal or removed.
- Reduced motion should feel calm and polished rather than stripped of visual identity.

## Notes For Implementation

- These tokens are a first-pass proposal and should be implemented semantically, not copied blindly into component styles.
- If the chosen display fonts are unavailable, keep the shape and spacing language and fall back to rounded, highly legible alternatives.
- During implementation, verify all status colors against WCAG AA contrast requirements before finalizing the palette.
