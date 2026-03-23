# Data Model: Design Polish

## 1. Page Shell Presentation State

The frontend-visible presentation state that controls the overall tone and atmosphere of the root shell.

| Field             | Type                                              | Source           | Notes                                                                                 |
| ----------------- | ------------------------------------------------- | ---------------- | ------------------------------------------------------------------------------------- |
| `themeVariant`    | `'playful-cafe'`                                  | frontend-derived | The dominant visual direction for the page shell.                                     |
| `heroTitle`       | `string`                                          | frontend-derived | Main title shown in the hero area.                                                    |
| `heroSubtitle`    | `string \| null`                                  | frontend-derived | Optional supporting line that sets tone without replacing the main heading.           |
| `backgroundMotif` | `'paper' \| 'waves' \| 'sprinkles' \| 'none'`     | frontend-derived | Decorative backdrop treatment used behind the stage.                                  |
| `surfaceTone`     | `'warm' \| 'celebratory' \| 'calm' \| 'degraded'` | frontend-derived | High-level tone used to style supporting surfaces.                                    |
| `isReducedMotion` | `boolean`                                         | frontend-derived | Mirrors the current motion preference so the shell can soften decorative transitions. |

### Validation Rules

- The shell presentation must support the existing loading, empty, ready, and error states without hiding their meaning.
- Decorative treatment must remain secondary to the belt stage.
- `heroSubtitle`, when present, must add tone rather than become the only explanation of current state.

## 2. Belt Stage Presentation Profile

The presentation configuration that shapes how the existing belt stage is visually expressed.

| Field                  | Type                                            | Source           | Notes                                                                                   |
| ---------------------- | ----------------------------------------------- | ---------------- | --------------------------------------------------------------------------------------- |
| `layoutVariant`        | `'current-balanced' \| 'stretched-readable'`    | frontend-derived | Whether the stage preserves current proportions or uses a readability-positive stretch. |
| `densityMode`          | `'comfortable' \| 'compact'`                    | frontend-derived | Responsive density mode for stage-adjacent decorations and labels.                      |
| `kitchenMood`          | `'quiet-chef' \| 'busy-chef' \| 'playful-chef'` | frontend-derived | Decorative tone of the kitchen center while keeping it non-essential.                   |
| `seatBadgeStyle`       | `'plain' \| 'playful'`                          | frontend-derived | Whether seats show playful secondary presentation treatment.                            |
| `plateAccentStyle`     | `'tier-first' \| 'dish-first'`                  | frontend-derived | Balances plate-tier clarity with food personality emphasis.                             |
| `readabilityGuardrail` | `'no-regression'`                               | frontend-derived | Explicit requirement that spatial clarity cannot regress.                               |

### Validation Rules

- The stage profile must not reduce slot, plate, or seat readability below the current implementation.
- `layoutVariant` can only use `'stretched-readable'` after before-and-after validation confirms no regression in seat clarity, slot order readability, plate targeting, and reach-area comprehension on desktop and mobile layouts.
- Decorative kitchen mood must not imply new interactive behaviors.

## 3. Secondary Humor Copy Slot

The optional secondary line that adds humor or charm to an existing state while preserving a plain primary label.

| Field            | Type                                                                                   | Source           | Notes                                                     |
| ---------------- | -------------------------------------------------------------------------------------- | ---------------- | --------------------------------------------------------- |
| `context`        | `'seat-status' \| 'feedback' \| 'empty-state' \| 'loading-state' \| 'section-heading'` | frontend-derived | Where the playful line appears.                           |
| `primaryLabel`   | `string`                                                                               | frontend-derived | The literal, plain-language state label.                  |
| `secondaryLabel` | `string \| null`                                                                       | frontend-derived | Optional playful supporting line.                         |
| `ariaLabel`      | `string`                                                                               | frontend-derived | Accessible text that preserves the literal state meaning. |
| `frequency`      | `'always' \| 'occasional' \| 'rare'`                                                   | frontend-derived | Controls how often a humorous line is shown.              |

### Validation Rules

- `primaryLabel` must remain understandable on its own.
- `secondaryLabel` must never contradict or obscure the actual product state.
- `ariaLabel` must preserve direct meaning rather than rely on jokes.
- Humor-heavy contexts should prefer `'occasional'` or `'rare'` frequency over `'always'`.

## 4. Feedback Presentation Rule

The observable rule set for styling and wording app-level and panel-level notices.

| Field              | Type                                                                       | Source           | Notes                                             |
| ------------------ | -------------------------------------------------------------------------- | ---------------- | ------------------------------------------------- |
| `outcomeType`      | `'loading' \| 'success' \| 'error' \| 'conflict' \| 'retry' \| 'degraded'` | frontend-derived | Core feedback category.                           |
| `tone`             | `'calm' \| 'celebratory' \| 'corrective' \| 'concerned'`                   | frontend-derived | Visual and editorial tone used for the notice.    |
| `primaryMessage`   | `string`                                                                   | frontend-derived | Required plain-language explanation of the state. |
| `secondaryMessage` | `string \| null`                                                           | frontend-derived | Optional playful supporting line.                 |
| `ariaPriority`     | `'polite' \| 'assertive'`                                                  | frontend-derived | Accessibility hint for live region behavior.      |

### Validation Rules

- `primaryMessage` must describe the actual state plainly.
- `secondaryMessage`, when present, must support rather than replace the real explanation.
- Corrective and concerned tones must remain easy to scan during stress states.

## 5. Responsive Presentation Mode

The frontend-derived mode that controls how much decoration and secondary copy are shown on smaller layouts.

| Field                  | Type                                | Source           | Notes                                                    |
| ---------------------- | ----------------------------------- | ---------------- | -------------------------------------------------------- |
| `viewportClass`        | `'desktop' \| 'tablet' \| 'mobile'` | frontend-derived | High-level responsive mode.                              |
| `ornamentLevel`        | `'full' \| 'reduced'`               | frontend-derived | Whether decorative framing appears at full intensity.    |
| `secondaryCopyDensity` | `'full' \| 'trimmed'`               | frontend-derived | Whether playful helper text is fully shown or shortened. |
| `operatorSurfaceMode`  | `'integrated' \| 'compact'`         | frontend-derived | Preserves the existing operator presentation behavior.   |

### Validation Rules

- Smaller layouts must retain the same primary action and state clarity as larger layouts.
- Ornament reduction must remove decoration before it removes functional clarity.
- Existing operator behavior and selected-seat detail comprehension must remain intact at every viewport class.
