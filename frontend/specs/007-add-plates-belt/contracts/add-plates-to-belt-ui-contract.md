# Add Plates To Belt UI Contract

## Purpose

Define the observable frontend behavior for a demo-mode operator placing new plates onto the currently shown primary belt without disturbing the guest-facing dining experience.

## 1. Operator Entry Contract

- The frontend exposes a compact operator entry point from the same belt scene the guest sees.
- The preferred entry point lives in or immediately adjacent to the kitchen or chef area.
- If that placement would noticeably harm the stage layout or visual quality, the frontend uses a compact secondary surface instead.

## 2. Guest Context Preservation Contract

- The existing selected-seat detail area remains a guest-focused surface.
- Opening, submitting, or dismissing the operator flow must not convert the selected-seat detail area into the primary operator workspace.
- Successful or failed operator actions must preserve the currently selected guest seat and any guest-facing dining context already shown.

## 3. Menu Selection Contract

The operator surface exposes these observable menu-selection properties:

| Field                   | Type                | Description                                              |
| ----------------------- | ------------------- | -------------------------------------------------------- |
| `query`                 | `string`            | Current text used to narrow the visible menu list.       |
| `results`               | `MenuItemSummary[]` | Visible menu items matching the current narrowing state. |
| `selectedMenuItemId`    | `string \| null`    | Currently chosen menu item for placement.                |
| `selectedMenuItemLabel` | `string \| null`    | Human-readable name of the chosen menu item.             |

- The operator must be able to reach the full menu list from the compact flow.
- Search narrows visible results but does not change the underlying menu contract.

## 4. Placement Draft Contract

The operator surface exposes a placement draft with these observable fields:

| Field             | Type             | Description                                                                        |
| ----------------- | ---------------- | ---------------------------------------------------------------------------------- |
| `menuItemId`      | `string \| null` | Required chosen menu item.                                                         |
| `numOfPlates`     | `number`         | Plate count, defaulting to 1.                                                      |
| `tierSnapshot`    | `string \| null` | Tier value derived from the chosen menu item unless overridden.                    |
| `priceAtCreation` | `number \| null` | Price value derived from the chosen menu item unless overridden.                   |
| `expiresAt`       | `string \| null` | Future expiration value defaulting to two hours after placement unless overridden. |
| `isSubmitting`    | `boolean`        | Whether the draft is currently being submitted.                                    |

- The operator can submit a valid draft without making any manual overrides.
- The operator can override plate count, tier, price, and expiration before submission.
- Invalid values must be blocked or corrected with clear operator-facing feedback.

## 5. Success Contract

- A successful placement shows a positive operator-facing notice that fits the kitchen or chef presentation.
- After success, the frontend triggers an immediate refresh of the authoritative belt view.
- The existing polling loop continues as the long-running reconciliation mechanism after that immediate refresh.

## 6. Failure Contract

- If there is not enough free capacity, the frontend tells the operator to reduce the number of plates.
- If the chosen menu item or entered values are invalid, the frontend shows a corrective notice and keeps the operator flow available for retry.
- If the currently shown belt is missing or the request otherwise fails unexpectedly, the frontend shows a clear failure notice and must not imply success.
- The frontend does not model a separate minimum-gap placement failure, because spacing remains backend-managed best effort.

## 7. Layout Preservation Contract

- The feature remains a compact demo-mode control inside the current sushi-belt experience.
- The frontend must not redesign the page into an admin dashboard for this feature.
- The kitchen and chef area remain the visual center of the page even when operator controls are present.
