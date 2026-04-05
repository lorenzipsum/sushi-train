# Phase 2: Database Foundation

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Use `Locked Decisions`, `Completed Phases`, and prior phase outcomes as the source of truth.
- Read the `Outcome` section in the most recently completed phase file before implementing this phase.
- Update both this file and [deployment-plan.md](../deployment-plan.md) when the phase is done.

## Prompt

```text
Context
- Repo: `sushi-train`
- Completed phases: Phase 0, Phase 1
- Locked decisions:
  - Use Render for frontend, backend, and postgres unless a concrete blocker appears
  - Preserve same-origin `/api`
  - Protect state-changing backend endpoints with a shared operator token
  - Keep work phase-scoped only

Task
Implement Phase 2 only: database foundation.

Goals
- add the config/docs/code changes needed for the cloud Postgres setup
- define env vars and secrets
- ensure Flyway migration strategy is clear
- define seed/demo-data behavior
- keep this phase limited to DB foundation only

Rules
- do not start backend deployment
- keep changes minimal and focused
- stop after Phase 2
- summarize exactly what changed
- tell me exactly how to test this phase
- tell me what I should verify before approving Phase 3
```

## Summary For Next Agent

- This phase is about Postgres connectivity, migrations, and seed/demo-data only.
- Avoid backend auth/protection logic and frontend routing changes here.
- The result should clearly document required secrets and env vars.
- Persist the actual phase result into this file before stopping.

## Status

- State: complete
- Commit:
- Verified: `backend\\mvnw.cmd -q -DskipTests package` completed successfully

## Outcome

- What changed:
  - Added a dedicated cloud datasource profile in `backend/src/main/resources/application-render.yaml`.
  - Documented the Render Postgres environment variables, JDBC URL format, Flyway behavior, and seed-data strategy in `docs/render-database-foundation.md`.
  - Updated `backend/README.md` so the `render` profile and required env vars are discoverable from the backend docs.
  - Persisted the approved Phase 2 outcome into the deployment docs for later phases.
- What was intentionally deferred:
  - Render Postgres itself was not provisioned in this phase.
  - Backend deployment wiring was not added in this phase.
  - Operator-token enforcement was not implemented in this phase.
  - CI/CD and deploy automation were not added in this phase.
- How to test:
  - Review `backend/src/main/resources/application-render.yaml` and confirm it defines the cloud datasource via `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
  - Review `docs/render-database-foundation.md` and confirm the JDBC URL format and seed-data behavior are acceptable.
  - Build the backend and confirm the new profile file is packaged without changing runtime behavior for local Docker.
- Risks / follow-ups:
  - The exact Render hostname, database name, and credentials will still need to be provisioned later.
  - Flyway seed migrations are appropriate for a fresh demo database, but any future shared data changes must be added as new migrations only.
  - Public-vs-private Render database networking is still a deployment-time choice for a later phase.
