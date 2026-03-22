# Roadmap

## Current

| Feature                    | Status | Notes                                                              |
| -------------------------- | ------ | ------------------------------------------------------------------ |
| `001-belt-visualization`   | Done   | Read-only belt baseline                                            |
| `002-belt-layout-redesign` | Done   | Belt-stage redesign shipped; earlier manual review note remains    |
| `003-occupy-seat`          | Done   | Start seat lifecycle                                               |
| `004-checkout-seat`        | Done   | Checkout flow shipped                                              |
| `005-pick-plates`          | Done   | Guest plate picking shipped                                        |
| `006-hydrate-seat-orders`  | Done   | Reload-time order restoration and selected-seat continuity shipped |

## Next Features

| Order | Feature                     | Status   | Depends on                                                                                                                                  | Notes                                      |
| ----- | --------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| 7     | `007-add-plates-to-belt`    | Proposed | `005-pick-plates`                                                                                                                           | Demo-mode operator plate action in same UI |
| 8     | `008-realtime-belt-updates` | Proposed | `003-occupy-seat`, `004-checkout-seat`, `005-pick-plates`, `006-hydrate-seat-orders`, `007-add-plates-to-belt`                              | Replace or reduce polling                  |
| 9     | `009-design-polish`         | Proposed | `003-occupy-seat`, `004-checkout-seat`, `005-pick-plates`, `006-hydrate-seat-orders`, `007-add-plates-to-belt`, `008-realtime-belt-updates` | Final UI pass                              |

## Dependency Shape

```text
002 -> 003 -> 004
         -> 005 -> 006
         -> 005 -> 007
003-007 -> 008
003-008 -> 009
```

## Spec Kit Rules

1. One roadmap item becomes one feature folder under `frontend/specs/`.
2. The roadmap only tracks order, status, and dependencies.
3. All implementation detail belongs in the feature folder.

## Feature Folder Pattern

```text
frontend/specs/003-occupy-seat/
frontend/specs/004-checkout-seat/
frontend/specs/005-pick-plates/
frontend/specs/006-hydrate-seat-orders/
```

Each feature should contain:

- `spec.md`
- `plan.md`
- `research.md`
- `data-model.md`
- `contracts/`
- `tasks.md`

## Status Legend

- `Proposed`: roadmap only
- `Active`: spec or implementation started
- `Done`: merged
- `Blocked`: waiting on dependency or decision
