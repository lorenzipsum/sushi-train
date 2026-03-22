# sushi-train Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-16

## Active Technologies
- N/A, in-memory client state only (002-belt-layout-redesign)
- TypeScript 5.9, Angular 21 standalone + `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts` (003-occupy-seat)
- N/A in frontend; backend-authoritative persistence remains behind existing REST APIs (003-occupy-seat)
- N/A in frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory signal state for current-session checkout confirmation (004-checkout-seat)
- N/A in frontend; backend-authoritative persistence remains behind existing REST APIs, with in-memory signal state for selected seat, active open orders, and current-session checkout summaries (005-pick-plates)

- TypeScript 5.9, Angular 21 standalone + `@angular/core`, `@angular/common`, `@angular/router`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts` (001-belt-visualization)

## Project Structure

```text
backend/
frontend/
tests/
```

## Commands

npm test; npm run lint

## Code Style

TypeScript 5.9, Angular 21 standalone: Follow standard conventions

## Recent Changes
- 005-pick-plates: Added TypeScript 5.9, Angular 21 standalone + `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`
- 005-pick-plates: Added TypeScript 5.9, Angular 21 standalone + `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`
- 004-checkout-seat: Added TypeScript 5.9, Angular 21 standalone + `@angular/core`, `@angular/common`, `@angular/common/http`, `rxjs`, generated DTOs from `src/app/api/generated/openapi.types.ts`


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
