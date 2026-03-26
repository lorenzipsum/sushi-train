# Phase 6: Docs And Final Verification

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions`, `Completed Phases`, and prior phase outcomes as the source of truth.
- Read the `Outcome` section in the most recently completed phase file before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0, Phase 1, Phase 2, Phase 3, Phase 4, Phase 5
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 6 only: docs and final verification.

Goals
- add deployment docs with exact provider-side setup steps
- document env vars and secrets
- summarize free-tier caveats
- verify builds/tests and report exactly what was run

Rules
- do not make unrelated cleanup changes
- keep changes minimal and focused
- stop after Phase 6
- summarize exactly what changed
- give me a final verification checklist
- call out any remaining risks or manual steps
```

## Summary For Next Agent

- This phase should consolidate setup instructions and verification output.
- Avoid unrelated cleanup or refactors.
- The final result should leave the repo with a clear operator guide for deployment and validation.
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
