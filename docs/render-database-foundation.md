# Render Database Foundation

This document captures the Phase 2 database setup for the public `dev` environment.

## Recommended Shape

- Provider: Render Postgres
- Runtime profile: `render`
- Migration strategy: Flyway runs automatically on backend startup
- Seed strategy: existing Flyway seed migrations populate demo data on first boot against an empty database

## Required Backend Environment Variables

Set these on the backend service:

- `SPRING_PROFILES_ACTIVE=render`
- `APP_ENVIRONMENT=dev`
- `DB_URL=jdbc:postgresql://<render-host>:5432/<database>?sslmode=require`
- `DB_USER=<render-user>`
- `DB_PASSWORD=<render-password>`

Optional tuning variables:

- `DB_MAX_POOL_SIZE=5`
- `DB_MIN_IDLE=0`
- `DB_CONNECTION_TIMEOUT_MS=30000`
- `DB_IDLE_TIMEOUT_MS=600000`
- `DB_MAX_LIFETIME_MS=1800000`

## Notes On Render Postgres URLs

Use a JDBC URL for `DB_URL`.

Expected format:

```text
jdbc:postgresql://<host>:5432/<database>?sslmode=require
```

For the public Render connection, keep `sslmode=require`.

If a later phase uses Render private networking instead of the public hostname, keep the same JDBC format and only switch host/port as needed.

## Flyway Strategy

Flyway remains enabled in the `render` profile.

Behavior:

- `V1__init_schema.sql` creates the schema
- `V2__seed_menu_items.sql` seeds menu items
- `V3__seed_belt.sql` seeds the baseline belt and seats

This means a fresh `dev` database will bootstrap itself on first backend startup.

Important constraints:

- Do not edit existing Flyway migrations after they have been applied to a shared database.
- Add new migrations for all future schema or seed changes.
- If a throwaway dev database gets into a bad state, replacing the database is simpler than trying to repair migration history manually.

## Seed / Demo Data Behavior

The current migrations already provide demo-ready baseline data:

- menu items
- one main belt
- belt slots
- seats

This is sufficient for the public `dev` environment and no extra seed runner is needed in this phase.

## What This Phase Does Not Do

- It does not provision Render Postgres yet.
- It does not deploy the backend yet.
- It does not add operator-token protection yet.
- It does not add CI/CD yet.

## Verification

Before moving to the next phase:

1. Confirm the backend has a `render` profile in `backend/src/main/resources/application-render.yaml`.
2. Confirm the required backend secrets/env vars are documented.
3. Confirm Flyway seed behavior is acceptable for public `dev`.

