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
- There are currently no feature routes or shared libraries. Keep changes small and consistent with the existing standalone structure instead of introducing NgModules or unnecessary abstraction.

## Build And Test

- Install dependencies with `npm install`.
- Start the dev server with `npm start`.
- Regenerate API types with `npm run api:types` after changing `contracts/openapi.json`.
- Run unit tests with `npm test`.
- Build production output with `npm run build`.
- This workspace uses Vitest through Angular's unit-test builder. Do not write Jasmine or Karma-specific tests.

## Conventions

- Use the `app` selector prefix for generated components.
- Put static assets in `public/` and reference component templates and styles with paths relative to the component TypeScript file.
- Use `HttpClient` plus small focused services in `src/app/api/` for API access, and treat `src/app/api/generated/openapi.types.ts` as generated code rather than hand-edited source.
- Keep the API base URL behind `src/app/api/http/api-config.ts` instead of hardcoding endpoint roots throughout the app.
- When adding UI, note that `src/app/app.html` is still mostly Angular CLI placeholder content and can be replaced rather than preserved line by line.
- Prefer editing the existing small app surface directly unless the feature clearly justifies adding new folders, routes, or services.
