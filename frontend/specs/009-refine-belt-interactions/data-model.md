# Data Model: Refine Belt Interactions

## 1. Selected Seat Emphasis State

The frontend-derived state that tells the stage which seat is currently active and how strongly it should be emphasized.

| Field               | Type                 | Source                                | Notes                                                                             |
| ------------------- | -------------------- | ------------------------------------- | --------------------------------------------------------------------------------- |
| `selectedSeatId`    | `string \| null`     | frontend-derived                      | Current active seat used for selection, reach rendering, and seat-detail context. |
| `isSelected`        | `boolean`            | frontend-derived                      | Per-seat flag used to style the active seat differently from others.              |
| `selectionTone`     | `'default' \| 'lit'` | frontend-derived                      | Presentation hint for stronger active-seat lighting treatment.                    |
| `selectedSeatLabel` | `string \| null`     | backend-derived then frontend-derived | Human-readable label surfaced to assistive text and detail cards.                 |

### Validation Rules

- Only one seat can be selected at a time.
- Selection emphasis must move immediately when the selected seat changes.
- Visual emphasis must remain understandable without relying on animation.

## 2. Seat Reach Presentation State

The visible and behavioral pickup zone associated with the selected seat.

| Field           | Type                                                     | Source           | Notes                                                              |
| --------------- | -------------------------------------------------------- | ---------------- | ------------------------------------------------------------------ |
| `seatId`        | `string`                                                 | frontend-derived | The selected seat that owns the reach zone.                        |
| `xPercent`      | `number`                                                 | frontend-derived | Horizontal position of the reach center in stage coordinates.      |
| `yPercent`      | `number`                                                 | frontend-derived | Vertical position of the reach center in stage coordinates.        |
| `radiusPercent` | `number`                                                 | frontend-derived | Effective pickup radius used for actual plate pickability.         |
| `slotLightingRadiusPercent` | `number`                                     | frontend-derived | Slightly broader visual lighting radius used only for lit slot presentation. |
| `seatSegment`   | `'top-straight' \| 'bottom-straight' \| 'curve' \| null` | frontend-derived | Layout segment used to apply the targeted top-row reach expansion. |
| `ariaLabel`     | `string`                                                 | frontend-derived | Accessible description of the active pickup zone.                  |

### Validation Rules

- `radiusPercent` must stay bounded so the reach zone remains associated with one seat instead of visually claiming the whole belt.
- Top-row seats may receive a larger radius than other seats, but the expansion must remain small and intentional.
- The actual pickability rule must always use `radiusPercent`.
- `slotLightingRadiusPercent`, when present, may extend slightly beyond `radiusPercent` for clarity, but must not change which plates are actually pickable.

## 3. Plate Presentation State

The frontend-derived presentation of moving plates after simplification to ring-only visuals.

| Field           | Type      | Source                             | Notes                                                                                        |
| --------------- | --------- | ---------------------------------- | -------------------------------------------------------------------------------------------- |
| `plateId`       | `string`  | backend-derived                    | Plate identity used for picking and pending state.                                           |
| `tierClass`     | `string`  | frontend-derived from backend tier | Existing color cue that remains visible after simplification.                                |
| `showsRingOnly` | `boolean` | frontend-derived                   | Indicates that inner center and garnish layers are not rendered in the default moving state. |
| `isPickable`    | `boolean` | frontend-derived                   | Existing pickability flag based on reach, seat readiness, and pending state.                 |
| `isPendingPick` | `boolean` | frontend-derived                   | Existing pending state during pick submission.                                               |
| `isRejected`    | `boolean` | frontend-derived                   | Existing short-lived reject cue after backend refusal.                                       |

### Validation Rules

- Every visible moving plate must preserve its tier-color cue after simplification.
- Ring-only rendering must not remove pickable, pending, or rejected visual states.
- The simplified plate must remain legible in motion against the belt background.

## 4. Belt Speed Modal State

The frontend-derived state for opening, closing, and interacting with the speed-change modal.

| Field           | Type             | Source           | Notes                                                                             |
| --------------- | ---------------- | ---------------- | --------------------------------------------------------------------------------- |
| `isOpen`        | `boolean`        | frontend-derived | Whether the speed dialog is currently visible.                                    |
| `currentSpeed`  | `number \| null` | backend-derived  | The current authoritative `speedSlotsPerTick` from the latest snapshot.           |
| `selectedSpeed` | `number \| null` | frontend-derived | The speed option currently chosen in the modal.                                   |
| `allowedSpeeds` | `number[]`       | contract-derived | Available choices constrained by the backend contract range `0..5`.               |
| `isSubmitting`  | `boolean`        | frontend-derived | Whether a speed change request is in flight.                                      |
| `canSubmit`     | `boolean`        | frontend-derived | Whether the chosen speed differs from the current speed and the modal can submit. |

### Validation Rules

- The modal must open with the current speed preselected.
- `selectedSpeed` must stay within the allowed backend range.
- Submission must be blocked while a speed update is in flight.
- Submission must remain blocked when the selected speed matches the current speed.
- Dismissing the modal must not change `currentSpeed`.

## 5. Belt Speed Update Feedback State

The frontend-visible outcome state for speed-change attempts.

| Field          | Type                                                                                | Source           | Notes                                                                       |
| -------------- | ----------------------------------------------------------------------------------- | ---------------- | --------------------------------------------------------------------------- |
| `tone`         | `'success' \| 'error' \| 'neutral'`                                                 | frontend-derived | Presentation tone for the outcome message.                                  |
| `title`        | `string`                                                                            | frontend-derived | Short summary of the result.                                                |
| `detail`       | `string`                                                                            | frontend-derived | Clear description of whether the speed changed, failed, or stayed the same. |
| `outcomeType`  | `'success' \| 'unchanged' \| 'invalid-choice' \| 'missing-belt' \| 'unknown-error'` | frontend-derived | Normalized outcome category for speed updates.                              |
| `appliedSpeed` | `number \| null`                                                                    | backend-derived  | Final authoritative speed value after success.                              |

### Validation Rules

- Success feedback must reflect the backend-authoritative applied speed.
- Error feedback must keep the previous speed intact and explain the failure clearly.
- If the selected speed is unchanged, the UI may block submission without emitting a separate feedback banner.
