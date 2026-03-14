# Project Guidelines

## Code Style

- Follow the existing Angular 21 standalone setup: use standalone components, `bootstrapApplication`, and provider-based app configuration.
- Prefer signals for local state, `computed()` for derived state, and `ChangeDetectionStrategy.OnPush` for new components.
- Keep TypeScript strict. Avoid `any`, keep templates simple, and use Angular built-in control flow such as `@if` and `@for`.
- Match the repo formatting: 2-space indentation, single quotes, and short readable lines compatible with the existing Prettier setup.

## Architecture

- The app is a single Angular application rooted at `src/main.ts` and `src/app/app.ts`.
- App-wide providers live in `src/app/app.config.ts`; routes live in `src/app/app.routes.ts`.
- REST API contracts live in `contracts/openapi.json`, generated API types live in `src/app/api/generated/`, and handwritten Angular API services live in `src/app/api/`.
- Belt rotation is virtual: backend responses keep physical slots stable, and the frontend must derive on-screen belt motion from snapshot timing fields instead of treating slot order as already rotated.
- There are currently no feature routes or shared libraries. Keep changes small and consistent with the existing standalone structure instead of introducing NgModules or unnecessary abstraction.

## Build And Test

- Install dependencies with `npm install`.
- Start the dev server with `npm start`.
- When running the frontend against a local backend, prefer an Angular dev proxy and relative `/api/...` browser calls instead of hardcoding host URLs in UI code.
- Regenerate API types with `npm run api:types` after changing `contracts/openapi.json`.
- Run unit tests with `npm test`.
- Build production output with `npm run build`.
- This workspace uses Vitest through Angular's unit-test builder. Do not write Jasmine or Karma-specific tests.

## Conventions

- Use the `app` selector prefix for generated components.
- Put static assets in `public/` and reference component templates and styles with paths relative to the component TypeScript file.
- Use `HttpClient` plus small focused services in `src/app/api/` for API access, and treat `src/app/api/generated/openapi.types.ts` as generated code rather than hand-edited source.
- Prefer relative `/api/...` calls in browser-facing code and keep environment-specific routing in a proxy or edge layer; avoid scattering absolute backend origins throughout the app.
- If an application-level base URL is still needed for non-browser contexts, keep it behind `src/app/api/http/api-config.ts` instead of hardcoding endpoint roots throughout the app.
- For belt rendering, prefer `GET /api/v1/belts/{id}/snapshot` as the authoritative plate/slot payload and `GET /api/v1/belts/{id}/seats` for seat occupancy; keep slots stable in state and derive visual positions client-side from belt timing metadata.
- For the sushi belt experience, prefer a Japanese Kawaii visual direction: playful but clear typography, warm food-forward color palettes, rounded shapes, friendly status states, and motion that feels charming without hiding data or harming accessibility.
- When adding UI, note that `src/app/app.html` is still mostly Angular CLI placeholder content and can be replaced rather than preserved line by line.
- Prefer editing the existing small app surface directly unless the feature clearly justifies adding new folders, routes, or services.
