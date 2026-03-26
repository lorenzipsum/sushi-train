# Phase 3: Backend Deployment And Security

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions`, `Completed Phases`, and prior phase outcomes as the source of truth.
- Read the `Outcome` section in the most recently completed phase file before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0, Phase 1, Phase 2
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 3 only: backend deployment and backend security.

Goals
- prepare the backend for public cloud deployment
- connect it to cloud Postgres
- implement operator-token protection for all state-changing endpoints
- keep anonymous public read access
- preserve SSE behavior

Rules
- do not start frontend deployment
- do not start CI/CD
- keep changes minimal and focused
- stop after Phase 3
- summarize exactly what changed
- list all protected endpoints
- tell me exactly how to test both read access and protected write access
- tell me what I should verify before approving Phase 4
```

## Summary For Next Agent

- Protect every state-changing backend endpoint with the chosen shared token approach.
- Preserve anonymous public reads and SSE.
- Do not drift into frontend deployment or workflow automation in this phase.
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
