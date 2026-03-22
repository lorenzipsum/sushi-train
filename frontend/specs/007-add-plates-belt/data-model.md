# Data Model: Add Plates To Belt

## 1. Operator Placement Surface State

The operator-visible state for opening, closing, and interacting with the plate-placement flow inside the belt scene.

| Field              | Type                                      | Source           | Notes                                                                  |
| ------------------ | ----------------------------------------- | ---------------- | ---------------------------------------------------------------------- |
| `isOpen`           | `boolean`                                 | frontend-derived | Whether the operator flow is currently visible.                        |
| `presentationMode` | `'inline-kitchen' \| 'secondary-surface'` | frontend-derived | Chosen presentation based on available visual space and layout safety. |
| `isMenuLoading`    | `boolean`                                 | frontend-derived | Indicates that the menu list is currently being retrieved.             |
| `menuLoadError`    | `string \| null`                          | frontend-derived | Operator-facing menu retrieval failure message.                        |
| `isSubmitting`     | `boolean`                                 | frontend-derived | Indicates that placement is currently being submitted.                 |
| `notice`           | `OperatorPlacementNotice \| null`         | frontend-derived | Most recent success or failure notice for the operator.                |

### Validation Rules

- The operator surface must remain secondary to the guest-facing stage.
- `isSubmitting` must disable duplicate submissions from the same visible draft.
- A failure notice must not clear guest seat context or imply that the guest workflow changed.

## 2. Menu Item Search State

The operator-visible state used to search and select a menu item from the full available list.

| Field                   | Type                | Source           | Notes                                                             |
| ----------------------- | ------------------- | ---------------- | ----------------------------------------------------------------- |
| `query`                 | `string`            | frontend-derived | Current search text entered by the operator.                      |
| `allMenuItems`          | `MenuItemSummary[]` | backend-derived  | The full current menu list available to the operator.             |
| `filteredMenuItems`     | `MenuItemSummary[]` | frontend-derived | The visible narrowed result list shown inside the compact picker. |
| `selectedMenuItemId`    | `string \| null`    | frontend-derived | The current chosen menu item.                                     |
| `selectedMenuItemLabel` | `string \| null`    | frontend-derived | Human-readable label used in the placement draft and notices.     |

### Validation Rules

- The operator must be able to reach any menu item from the full menu list.
- Search text may reduce the visible list, but it must not hide the currently selected item without also clearing or reselecting deliberately.
- The compact picker must remain usable when the menu list is larger than the visible results area.

## 3. Placement Draft

The operator-editable placement request prepared before submission.

| Field             | Type                                            | Source                               | Notes                                                                 |
| ----------------- | ----------------------------------------------- | ------------------------------------ | --------------------------------------------------------------------- |
| `beltId`          | `string`                                        | backend-derived                      | The primary belt currently shown on screen.                           |
| `menuItemId`      | `string \| null`                                | frontend-derived                     | Required chosen menu item identity.                                   |
| `menuItemName`    | `string \| null`                                | frontend-derived                     | Display label for operator confirmation.                              |
| `numOfPlates`     | `number`                                        | frontend-derived                     | Defaults to 1 and must remain within the supported range.             |
| `tierSnapshot`    | `'GREEN' \| 'RED' \| 'GOLD' \| 'BLACK' \| null` | backend-derived then frontend-edited | Defaults from the selected menu item and may be manually overridden.  |
| `priceAtCreation` | `number \| null`                                | backend-derived then frontend-edited | Defaults from the selected menu item and may be manually overridden.  |
| `expiresAt`       | `string \| null`                                | frontend-derived                     | Defaults to two hours in the future and may be manually overridden.   |
| `isDefaultDraft`  | `boolean`                                       | frontend-derived                     | Indicates whether the operator has changed any default-derived value. |

### Validation Rules

- `menuItemId` is required before submission.
- `numOfPlates` must remain between 1 and 10.
- `priceAtCreation`, when provided, must be zero or greater.
- `expiresAt`, when provided, must represent a future time.
- The draft must be submittable without manual overrides when the defaults are acceptable.

## 4. Operator Placement Notice

The operator-visible success or failure message after a placement attempt.

| Field          | Type                                                                                                                                     | Source           | Notes                                            |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ---------------- | ------------------------------------------------ |
| `tone`         | `'success' \| 'error'`                                                                                                                   | frontend-derived | Presentation tone for the notice.                |
| `title`        | `string`                                                                                                                                 | frontend-derived | Short outcome summary.                           |
| `detail`       | `string`                                                                                                                                 | frontend-derived | Corrective or confirmatory explanation.          |
| `outcomeType`  | `'success' \| 'not-enough-space' \| 'invalid-menu-item' \| 'invalid-values' \| 'missing-belt' \| 'malformed-request' \| 'unknown-error'` | frontend-derived | Normalized operator-facing outcome category.     |
| `createdCount` | `number \| null`                                                                                                                         | backend-derived  | Present for successful placements.               |
| `menuItemName` | `string \| null`                                                                                                                         | frontend-derived | Optional display name used in success messaging. |

### Validation Rules

- Success notices must read as operator-facing confirmation and fit the kitchen or chef presentation.
- Error notices must guide retry when the failure is operator-correctable.
- A notice must never claim success if the backend-authoritative placement did not succeed.

## 5. Belt Refresh Reconciliation State

The state that coordinates post-placement refresh with the existing polling loop.

| Field                         | Type             | Source           | Notes                                                                                 |
| ----------------------------- | ---------------- | ---------------- | ------------------------------------------------------------------------------------- |
| `primaryBeltId`               | `string \| null` | backend-derived  | The belt currently tracked by the visualization store.                                |
| `refreshTriggeredByPlacement` | `boolean`        | frontend-derived | Indicates that an immediate authoritative refresh was triggered by placement success. |
| `lastPlacementAttemptAt`      | `string \| null` | frontend-derived | Timestamp of the most recent submission attempt.                                      |
| `lastPlacementSuccessAt`      | `string \| null` | frontend-derived | Timestamp of the most recent successful placement.                                    |

### Validation Rules

- Successful placement must trigger an immediate refresh of backend-authoritative belt state.
- Ongoing polling remains the long-running reconciliation mechanism after the immediate refresh.
- Guest seat selection and dining detail state remain stable across both success and failure reconciliation.
