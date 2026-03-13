# Project Guidelines

## Code Style

- Follow the existing Angular 21 standalone setup: use standalone components, `bootstrapApplication`, and provider-based app configuration.
- Prefer signals for local state, `computed()` for derived state, and `ChangeDetectionStrategy.OnPush` for new components.
- Keep TypeScript strict. Avoid `any`, keep templates simple, and use Angular built-in control flow such as `@if` and `@for`.
- Match the repo formatting: 2-space indentation, single quotes, and short readable lines compatible with the existing Prettier setup.

## Architecture

- The app is a single Angular application rooted at `src/main.ts` and `src/app/app.ts`.
- App-wide providers live in `src/app/app.config.ts`; routes live in `src/app/app.routes.ts`.
- There are currently no feature routes or shared libraries. Keep changes small and consistent with the existing standalone structure instead of introducing NgModules or unnecessary abstraction.

## Build And Test

- Install dependencies with `npm install`.
- Start the dev server with `npm start`.
- Run unit tests with `npm test`.
- Build production output with `npm run build`.
- This workspace uses Vitest through Angular's unit-test builder. Do not write Jasmine or Karma-specific tests.

## Conventions

- Use the `app` selector prefix for generated components.
- Put static assets in `public/` and reference component templates and styles with paths relative to the component TypeScript file.
- When adding UI, note that `src/app/app.html` is still mostly Angular CLI placeholder content and can be replaced rather than preserved line by line.
- Prefer editing the existing small app surface directly unless the feature clearly justifies adding new folders, routes, or services.
