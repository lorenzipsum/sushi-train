# Phase 1: Deployment Foundation

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions` and `Completed Phases` from that file as the source of truth.
- Read the `Outcome` and `Approved Phase 0 Output` sections in [phase-0-inspection.md](./phase-0-inspection.md) before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 1 only: deployment foundation.

Goals
- make only the minimum config/code changes needed to support the chosen cloud deployment shape
- keep the current same-origin `/api` approach if that is still the recommendation
- do not implement CI/CD yet
- do not start database deployment yet

Rules
- keep changes minimal and focused
- stop after Phase 1
- summarize exactly what changed
- tell me exactly how to test Phase 1 locally
- tell me what I should verify before approving Phase 2
```

## Summary For Next Agent

- This phase should only establish the deployment shape and config plumbing.
- Do not fold in database provisioning, backend security implementation, or CI/CD.
- The output must leave the repo in a locally testable state.
- Persist the actual phase result into this file before stopping.

## Status

- State: complete
- Commit:
- Verified: `docker compose config` renders successfully with the new proxy-origin environment shape

## Outcome

- What changed:
  - Updated the frontend Nginx template to proxy `/api` via a full `API_UPSTREAM_ORIGIN` value instead of assuming `http` plus host-only input.
  - Updated local compose/env documentation to use `FRONTEND_API_UPSTREAM_ORIGIN=http://backend:8080`, which matches the local shape and also supports a public Render backend URL later.
  - Added backend `server.forward-headers-strategy=framework` so the app behaves correctly behind the frontend proxy and cloud ingress.
  - Persisted the approved foundation outcome into the deployment docs for later phases.
- What was intentionally deferred:
  - No database provisioning or Render Postgres setup was added yet.
  - No operator-token enforcement was implemented yet.
  - No GitHub Actions or deploy automation was added yet.
  - No frontend preview-deployment setup was added yet.
- How to test:
  - Start the existing local stack with `docker compose up --build`.
  - Open `http://localhost:4200`.
  - Confirm the UI still loads and browser API requests continue to go through same-origin `/api/...`.
  - Confirm backend health remains reachable at `http://localhost:8088/actuator/health`.
  - Optionally override `FRONTEND_API_UPSTREAM_ORIGIN` to another valid origin and confirm the frontend container still starts with the same Nginx template.
- Risks / follow-ups:
  - The exact Render backend URL is still unknown and will be wired in a later phase.
  - Nginx proxy behavior with a public `https` upstream should be validated during frontend deployment.
  - `docker compose` should be used to verify the new env name locally before moving to Phase 2.
