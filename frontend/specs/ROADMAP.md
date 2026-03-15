# Roadmap

## Current

| Feature                    | Status | Notes                    |
| -------------------------- | ------ | ------------------------ |
| `001-belt-visualization`   | Done   | Read-only belt baseline  |
| `002-belt-layout-redesign` | Done   | Manual review still open |
| `004-checkout-seat`        | Done   | Checkout flow shipped    |

## Next Features

| Order | Feature                     | Status   | Depends on                 | Notes                         |
| ----- | --------------------------- | -------- | -------------------------- | ----------------------------- |
| 3     | `003-occupy-seat`           | Done     | `002-belt-layout-redesign` | Start seat lifecycle          |
| 4     | `004-checkout-seat`         | Done     | `003-occupy-seat`          | Close seat lifecycle          |
| 5     | `005-pick-plates`           | Active   | `003-occupy-seat`          | Guest plate action            |
| 6     | `006-add-plates-to-belt`    | Proposed | `005-pick-plates`          | Kitchen or staff plate action |
| 7     | `007-realtime-belt-updates` | Proposed | `003`-`006`                | Replace or reduce polling     |
| 8     | `008-design-polish`         | Proposed | `003`-`007`                | Final UI pass                 |

## Dependency Shape

```text
002 -> 003 -> 004
         -> 005 -> 006
003-006 -> 007
003-007 -> 008
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
