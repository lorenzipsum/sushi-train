# Phase 5: CI/CD

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions`, `Completed Phases`, and prior phase outcomes as the source of truth.
- Read the `Outcome` section in the most recently completed phase file before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0, Phase 1, Phase 2, Phase 3, Phase 4
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 5 only: CI/CD.

Goals
- add GitHub Actions for changed-parts-only build/test on branch and PR pushes
- ensure deploys on `main` happen only after CI passes
- keep workflows as simple as possible
- prefer GitHub Actions triggering provider deploys rather than unmanaged auto-deploys

Rules
- do not start docs/final cleanup yet
- keep changes minimal and focused
- stop after Phase 5
- summarize exactly what changed
- tell me exactly how to validate the workflows
- explain any GitHub/provider secrets I need to add
- tell me what I should verify before approving Phase 6
```

## Summary For Next Agent

- This phase is for workflow automation only.
- Build, test, and deploy should run only for changed parts where practical.
- Keep provider auto-deploy behavior aligned with CI gating.
- Persist the actual phase result into this file before stopping.

## Status

- State: pending
- Commit:
- Verified:

## Outcome

- What changed:
- What was intentionally deferred:
- How to test:
- Risks / follow-ups:
