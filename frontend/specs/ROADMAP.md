# Roadmap

Current frontend state: features `001` through `008` are shipped on `main`. The remaining planned work starts with final design polish.

## Current

| Feature                     | Status | Notes                                                              |
| --------------------------- | ------ | ------------------------------------------------------------------ |
| `001-belt-visualization`    | Done   | Read-only belt baseline                                            |
| `002-belt-layout-redesign`  | Done   | Belt-stage redesign shipped; earlier manual review note remains    |
| `003-occupy-seat`           | Done   | Start seat lifecycle                                               |
| `004-checkout-seat`         | Done   | Checkout flow shipped                                              |
| `005-pick-plates`           | Done   | Guest plate picking shipped                                        |
| `006-hydrate-seat-orders`   | Done   | Reload-time order restoration and selected-seat continuity shipped |
| `007-add-plates-belt`       | Done   | Demo-mode operator plate action shipped in the main belt UI        |
| `008-realtime-belt-updates` | Done   | Server-sent-event belt updates with polling fallback shipped       |

## Next Features

| Order | Feature             | Status   | Depends on                                                                                                                               | Notes                                     |
| ----- | ------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------- |
| 9     | `009-design-polish` | Proposed | `003-occupy-seat`, `004-checkout-seat`, `005-pick-plates`, `006-hydrate-seat-orders`, `007-add-plates-belt`, `008-realtime-belt-updates` | Final UI pass after realtime updates land |

## Dependency Shape

```text
002 -> 003 -> 004
         -> 005 -> 006
         -> 005 -> 007
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
frontend/specs/007-add-plates-belt/
```

Note: `008-realtime-belt-updates` was implemented from a shared backend-plus-frontend effort. Its planning artifacts were maintained one level above `frontend/`, so there is no dedicated `frontend/specs/008-realtime-belt-updates/` folder.

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
