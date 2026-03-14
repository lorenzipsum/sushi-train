# Sushi Train Frontend Constitution

## Core Principles

### I. Standalone Angular First

This project uses modern Angular standalone APIs as its default architectural model. New work must use standalone components, `bootstrapApplication`, and provider-based application configuration. NgModules, legacy bootstrap patterns, or framework backtracks are not permitted unless a change is explicitly approved as an exception and documented with a migration reason.

### II. Strict, Simple, Maintainable Code

TypeScript strictness is mandatory. `any` should be avoided, templates should stay readable, and component logic should favor signals, `computed()`, and `ChangeDetectionStrategy.OnPush` for new work. Abstractions must be justified by actual reuse or clear complexity reduction; organizational-only folders, services, or layers are discouraged.

Contract-derived code should remain explicit about ownership: OpenAPI contracts are maintained in `contracts/openapi.json`, generated type output is not hand-edited, and handwritten Angular services should provide the narrow application-facing API surface.

Backend-authoritative state should remain distinct from frontend-derived animation. When backend APIs provide stable entities plus timing metadata, the frontend should render motion from that metadata instead of mutating application state to simulate movement.

### III. Testable Changes Are Required

Changes that alter behavior must remain testable and should add or update automated tests when the affected behavior is meaningful to users or developers. Unit testing in this workspace is Vitest through Angular's unit-test builder, and new tests must follow that stack. Work is not complete if it cannot reasonably pass `npm test` and `npm run build`.

### IV. Accessible, Replaceable UI

User-facing UI must use semantic markup and preserve accessibility as a baseline requirement. Placeholder scaffold content may be replaced outright when delivering real product UI; contributors should prefer clean replacement over layering new features around generated demo content. Static assets belong in `public/`, and component template and style references must stay relative to the component TypeScript file.

### V. Grow the Surface Area Conservatively

This repository currently contains a single small Angular application with no feature routes or shared libraries. Contributors should prefer editing the existing app surface directly. New routes, folders, services, or reusable layers should only be introduced when there is a concrete product need, not speculative future-proofing.

## Technical Constraints

- The application root is `src/main.ts` and `src/app/app.ts`.
- Application-wide providers live in `src/app/app.config.ts`.
- Routes are defined in `src/app/app.routes.ts`.
- REST API configuration lives in `src/app/api/http/api-config.ts` and app-wide HTTP support is registered in `src/app/app.config.ts`.
- The OpenAPI contract lives in `contracts/openapi.json`; generated types live in `src/app/api/generated/` and should be regenerated, not manually maintained.
- For sushi belt views, physical slot data is authoritative from the backend and visual belt motion is derived on the frontend from timing fields such as rotation offset, start time, tick interval, and speed.
- The selector prefix for generated UI pieces is `app`.
- Built-in Angular control flow such as `@if` and `@for` is preferred over legacy structural directives for new code.
- Formatting follows the existing repository style: 2-space indentation, single quotes, and short readable lines compatible with the current Prettier setup.

## Development Workflow And Quality Gates

- Install dependencies with `npm install`.
- Use `npm start` for local development.
- Run `npm run api:types` after contract changes before relying on generated API types.
- Use `npm test` for unit tests and `npm run build` for production verification.
- Changes should be kept small, reviewable, and consistent with the current standalone application structure.
- When introducing new components or views, prefer focused responsibilities, simple templates, and repository-local conventions before adding new architectural layers.
- Reviewers and implementers should check changes against both this constitution and `.github/copilot-instructions.md`; the constitution defines durable principles, while the instruction file provides day-to-day implementation guidance.

## Governance

This constitution defines the long-lived engineering rules for the Sushi Train frontend and takes precedence over ad hoc preferences or generated defaults. Operational guidance in `.github/copilot-instructions.md` must remain consistent with this document and may add detail, but must not contradict these principles.

Amendments must update this file directly, explain the rationale for the change, and preserve a coherent path for existing code. Any exception to these rules should be explicit, limited in scope, and recorded in the corresponding change discussion or specification. Reviews should reject work that introduces unnecessary architectural weight, bypasses strict typing without cause, or drifts away from the approved Angular and testing model.

**Version**: 1.0.0 | **Ratified**: 2026-03-14 | **Last Amended**: 2026-03-14
