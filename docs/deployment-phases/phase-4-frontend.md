# Phase 4: Frontend Deployment

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions`, `Completed Phases`, and prior phase outcomes as the source of truth.
- Read the `Outcome` section in the most recently completed phase file before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0, Phase 1, Phase 2, Phase 3
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 4 only: frontend deployment.

Goals
- prepare the frontend for public deployment
- connect it to the deployed backend using the chosen routing model
- preserve the simplest workable public setup
- add frontend preview deployment support only if it is low-complexity and still recommended

Rules
- do not start CI/CD yet
- keep changes minimal and focused
- stop after Phase 4
- summarize exactly what changed
- tell me exactly how to test frontend-to-backend connectivity
- tell me what I should verify before approving Phase 5
```

## Summary For Next Agent

- Keep the frontend as the public entrypoint unless Phase 0 approved something else.
- Preserve the same-origin `/api` behavior if possible.
- Preview deployments are optional and should be added only if they stay simple.
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
