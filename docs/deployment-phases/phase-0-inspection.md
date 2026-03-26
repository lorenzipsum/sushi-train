# Phase 0: Inspection And Recommendation

## Agent Instructions

- Read [deployment-plan.md](../deployment-plan.md) first.
- Do not edit application files in this phase.
- When this phase is approved, update the `Locked Decisions`, `Completed Phases`, and checklist in [deployment-plan.md](../deployment-plan.md).

## Prompt

```text
Phase 0 only.

Inspect the current `sushi-train` repo and do not edit any files.

Deliver exactly:
1. recommended provider setup
2. deployment and security approach
3. exact files you expect to change
4. risks and caveats
5. phased implementation plan

Requirements
- prefer Render unless there is a concrete repo-specific blocker
- preserve the current same-origin `/api` model if practical
- account for Spring Boot 4, Java 25, Flyway, PostgreSQL, SSE, Angular 21, and the existing Dockerfiles
- explain how operator-token protection should work for write endpoints
- explain how CI should gate deploys
- explain whether frontend preview deployments are worth it here

Stop after Phase 0 and wait for approval.
```

## Summary For Next Agent

- This phase is analysis only.
- No files should be created or edited.
- The output should lock the provider, routing, security, and CI/CD direction.
- After approval, update the checklist in [deployment-plan.md](../deployment-plan.md).

## Status

- State: complete
- Commit:
- Verified: repo inspected; no application files edited in this phase

## Outcome

- What changed:
  - Inspected the repo and recommended Render for frontend, backend, and Postgres.
  - Confirmed the repo is best served by preserving the current same-origin `/api` model.
  - Identified backend operator-token protection as the right security tradeoff for public `dev`.
  - Identified GitHub Actions as the CI/CD gate and deploy trigger path.
- What was intentionally deferred:
  - No application, CI, or deployment file changes were made.
  - Exact implementation details for Nginx upstream config, backend token enforcement, and workflows are deferred to later phases.
- How to test:
  - Review [deployment-plan.md](../deployment-plan.md) and confirm the locked decisions match the approved Phase 0 direction.
  - Confirm no code or runtime files were changed as part of this phase.
- Risks / follow-ups:
  - Decide whether `GET /api/v1/orders` should remain public or become operator-only.
  - Render frontend proxying may require switching Nginx upstream handling to a public `https` backend URL.
  - Free-tier behavior will include cold starts and demo-grade persistence only.

## Approved Phase 0 Output

### Recommended Provider Setup

Use Render for everything:

- Render Postgres for the database
- Render Web Service for the Spring Boot backend
- Render Web Service for the Angular frontend container running Nginx

Why:

- The repo already has deployable Dockerfiles for backend and frontend.
- The frontend already expects same-origin `/api` through Nginx proxying.
- The backend is a normal long-running Spring Boot app with SSE and Postgres, which fits a container web service.
- This minimizes cross-provider routing and CORS complexity.

### Deployment And Security Approach

Deployment shape:

- Public entrypoint: frontend Render service
- Frontend Nginx proxies `/api/*` to the backend Render service
- Backend connects to Render Postgres
- Browser model stays same-origin where practical

Security model:

- Anonymous public read access is allowed for read-only endpoints and SSE
- All state-changing backend endpoints must require a shared operator token
- Recommended transport: request header such as `X-Operator-Token`
- Validation should happen centrally in backend infrastructure, not duplicated in controllers
- The token must never be embedded in public frontend code

CI/CD gating:

- GitHub Actions should run changed-part checks on branch and PR pushes
- `main` deploys only after CI passes
- Prefer GitHub Actions triggering Render deploys over unmanaged auto-deploys
- Frontend previews are optional and only worth adding if they stay low-complexity

Persistence expectation:

- Free-tier persistence is acceptable only for demo/dev use
- Long-term durability is not assumed

### Exact Files Expected To Change In Later Phases

Likely backend files:

- `backend/src/main/resources/application.yaml`
- `backend/src/main/resources/application-local.yaml`
- possibly a new cloud-oriented application profile
- new backend config and request-protection classes
- backend controller tests

Likely frontend files:

- `frontend/nginx.conf`
- possibly `frontend/Dockerfile`
- possibly frontend API config only if same-origin turns out insufficient

Likely repo/devops/docs files:

- `.env.example`
- possibly `docker-compose.yml` for local/cloud parity improvements
- new `.github/workflows/*`
- deployment docs under `docs/`

### Risks And Caveats

- Free-tier cold starts and sleeping services are expected
- Frontend proxying to the backend public URL may require Nginx changes
- SSE behavior must be preserved through reverse proxy settings
- Java 25 support should remain Docker-based to avoid provider runtime mismatches
- Changed-parts-only CI is practical but not trivial
- Public visibility of `GET /api/v1/orders` should be explicitly decided
- Operator token is acceptable for `dev` only if kept server-side

### Phased Implementation Plan

1. Phase 1: deployment foundation
2. Phase 2: database foundation
3. Phase 3: backend deployment and security
4. Phase 4: frontend deployment
5. Phase 5: CI/CD
6. Phase 6: docs and final verification
