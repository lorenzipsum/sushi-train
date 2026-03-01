# 2026-02-28 - Mapper and Port Naming Troubleshooting

## Context

Session focused on IDE/build issues around `BeltApiMapper`, plus naming consistency for ports/repositories and project-wide terminology cleanup.

## Questions Asked

- Why IntelliJ could not access `BeltApiMapper`.
- Whether mixed naming (`*Repository` vs `*Port`) is correct in application services.
- How to standardize naming project-wide and document conventions.
- Warnings around unused return values and Maven reimport troubleshooting.

## Key Guidance

- Treat IDE mapper resolution errors as either generated-source sync or missing-file issues and verify both.
- Use a consistent naming rule for abstraction roles across the codebase.
- Document naming conventions in README to keep future changes aligned.

## Decisions Made

- Decision: Apply and document naming consistency across ports/adapters/repositories.
- Rationale: Improves readability and lowers confusion during refactoring.
- Owner: Project team.
- Date: 2026-02-28.

## Action Items

- [ ] Keep annotation processing and Maven project reload steps in troubleshooting docs.
- [ ] Enforce naming conventions in code reviews.

## References

- Session source: local Codex session log `rollout-2026-02-28T14-20-26-019ca31e-bc29-7651-96ac-892de0c7d751.jsonl`.
