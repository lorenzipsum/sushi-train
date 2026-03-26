# Deployment Plan

This plan tracks the phased cloud deployment work for the public `dev` environment.

This file is the canonical handoff file for future agents.

How to use this plan:

- The user can say only `do phase X`.
- The agent should first read this file.
- The agent should then read the linked file for that phase under `docs/deployment-phases/`.
- The agent should use the `Locked Decisions`, `Completed Phases`, and `Phase Outcome` sections below as the source of truth.
- After finishing a phase, the agent should update this file and the corresponding phase file before stopping.

Recommended execution model:

- Run one phase per chat.
- Review the diff after each phase.
- Test the phase before moving on.
- Commit after each approved implementation phase.
- Start a new chat for the next phase to reduce context drift.

## Phase Checklist

- [x] [Phase 0: Inspection And Recommendation](./deployment-phases/phase-0-inspection.md)
- [x] [Phase 1: Deployment Foundation](./deployment-phases/phase-1-foundation.md)
- [ ] [Phase 2: Database Foundation](./deployment-phases/phase-2-database.md)
- [ ] [Phase 3: Backend Deployment And Security](./deployment-phases/phase-3-backend.md)
- [ ] [Phase 4: Frontend Deployment](./deployment-phases/phase-4-frontend.md)
- [ ] [Phase 5: CI/CD](./deployment-phases/phase-5-cicd.md)
- [ ] [Phase 6: Docs And Final Verification](./deployment-phases/phase-6-docs-verification.md)

## Locked Decisions

Fill this in after Phase 0 is approved. Future agents must treat this section as authoritative.

- Provider choice: Render for frontend, backend, and Postgres unless a concrete repo-specific blocker appears during implementation.
- Routing model: Preserve same-origin `/api` by keeping the frontend as the public entrypoint and proxying API traffic through frontend Nginx to the backend.
- Backend security model: Anonymous public read access is allowed; all state-changing backend endpoints must require a shared operator token sent in a request header and validated server-side only.
- CI/CD gating model: GitHub Actions runs changed-part build/test checks on branch and PR pushes; deployments on `main` happen only after CI passes; prefer GitHub Actions triggering Render deploys over unmanaged provider auto-deploys.
- Preview deployment decision: Frontend preview deployments are useful only if they stay low-complexity on Render; backend preview deployments are optional and not required for the first pass.

## Completed Phases

- Phase 0: Inspection And Recommendation
- Phase 1: Deployment Foundation

## Open Questions

- Should public read access include `GET /api/v1/orders`, or should orders be operator-only?

## Latest Approved Outcome

Phase 1 established the deployment foundation on top of the Phase 0 baseline:

- Recommended provider setup: Render Postgres + Render backend web service + Render frontend web service.
- Public entrypoint: frontend service.
- Browser/API model: preserve same-origin `/api`.
- Backend runtime approach: keep Docker-based deployment to avoid platform JDK/runtime surprises and preserve Java 25 support.
- Frontend proxy model: use a full upstream origin variable so local Docker and Render can share the same Nginx template.
- Reverse-proxy handling: backend now explicitly respects forwarded headers from the frontend proxy/cloud ingress.
- Security approach: require operator token on all state-changing backend endpoints; keep token out of public frontend code.
- Persistence expectation: demo-grade free-tier persistence is acceptable, but not durable production-grade storage.
- CI/CD direction: start from zero with root GitHub Actions workflows and gate deploys through CI.

## Phase Outcome Template

Copy this structure into the relevant phase file when a phase completes:

```text
Status
- State: complete
- Commit:
- Verified:

Outcome
- What changed:
- What was intentionally deferred:
- How to test:
- Risks / follow-ups:
```

## Per-Phase Operating Loop

1. Start a new chat for the next phase.
2. Restate completed phases and locked decisions.
3. Ask the agent to implement that phase only.
4. Review the diff for scope creep.
5. Run tests and smoke checks.
6. Commit if acceptable.
7. Continue to the next phase.

## Agent Contract

If the user says `do phase X`, the agent should:

1. Read [deployment-plan.md](./deployment-plan.md).
2. Read the matching phase file in [deployment-phases](./deployment-phases).
3. Infer the locked decisions and completed phases from the docs instead of asking the user to restate them.
4. Implement only that phase.
5. Update the checklist in this file.
6. Update the phase file with the actual outcome for the next handoff.
7. Stop and summarize what changed, how it was tested, and what should be verified before the next phase.

## Persistence Rule

Future agents must persist the actual result of every completed phase into the repo before stopping.

Minimum required updates after each phase:

1. Mark the phase checkbox in this file.
2. Update `Completed Phases` in this file.
3. Update `Latest Approved Outcome` in this file if the phase changed any locked assumption.
4. Update the corresponding phase file:
   - `Status`
   - `Outcome`
   - actual test/verification notes
   - remaining risks or follow-ups

Chat history must not be treated as the only source of truth for phase outcomes.
