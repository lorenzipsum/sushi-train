# Sushi Train Backend

Spring Boot backend for the Sushi Train simulator.

Repository: https://github.com/lorenzipsum/sushi-train

## Purpose

This service is the domain and persistence layer of the Sushi Train application.
It manages sushi belt behavior, seat occupancy, order lifecycle, and plate handling.

It provides:

- REST APIs for belts, seats, plates, menu items, and orders.
- PostgreSQL persistence with Flyway migrations.
- Scheduled jobs for operational consistency (for example plate expiry and data-integrity repair).
- Optional demo-mode console belt animation.

## Tech Stack

- Java 25
- Spring Boot 4.x
- PostgreSQL
- Flyway
- Springdoc OpenAPI (Swagger)
- Docker / Docker Compose

## Run Locally

### Option 1: Docker Compose (recommended)

From the repository root:

```bash
docker compose up --build
```

Optional:

- copy `.env.example` to `.env`
- adjust database credentials, published ports, or Spring profile there

If Flyway fails with a checksum mismatch after migration file changes, reset local containers and volumes:

```bash
docker compose down -v
docker compose up --build
```

Backend:

- http://localhost:8088

Swagger UI (when enabled by active profile):

- http://localhost:8088/swagger-ui/index.html

### Option 2: Run directly with Maven

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Configuration Profiles

- `local`: local datasource + Flyway + Swagger enabled.
- `demo-mode`: enables console belt animation.
- `show-sql`: verbose SQL logging.
- `azure`: Azure-oriented datasource config with SSL-enabled PostgreSQL connection defaults.

The Docker setup reads database and port settings from environment variables, which makes the same container images easier to reuse in cloud deployments.
The backend also exposes Actuator health at `/actuator/health`, which is the intended probe endpoint for later cloud orchestration.

## Azure Runtime Configuration

The backend now includes an `azure` Spring profile intended for Azure Container Apps plus Azure Database for PostgreSQL Flexible Server.

Recommended runtime environment variables for Azure:

- `SPRING_PROFILES_ACTIVE=azure`
- `APP_ENVIRONMENT=azure`
- `DB_HOST=<azure-postgresql-hostname>`
- `DB_PORT=5432`
- `DB_NAME=sushitrain`
- `DB_USER=<database-username>`
- `DB_PASSWORD=<database-password>`
- `DB_SSL_MODE=require`

Alternative:

- provide the full JDBC URL through `DB_URL`
- keep `DB_USER` and `DB_PASSWORD` as separate runtime values

Azure profile behavior:

- keeps Flyway enabled
- keeps schema validation enabled
- defaults PostgreSQL SSL mode to `require`
- still allows overriding the full JDBC URL when needed

This is intentionally additive to the existing local Docker Compose workflow. Local execution can keep using the current `demo-mode` or `local` setup without Azure-specific configuration.

## Azure Containerization And Deployment Approach

The backend Docker image is intended to be built locally, pushed to Azure Container Registry, and then used by Azure Container Apps.

Current containerization approach:

- backend image is built from `backend/Dockerfile`
- the Docker build packages the Spring Boot application during the image build
- the Dockerfile now copies the produced executable JAR without hard-coding the Maven project version
- container listens on port `8080`
- health endpoint remains `/actuator/health`

Recommended image naming:

- repository: `sushi-train-backend`
- initial tag for manual iteration: `dev-latest`

Example local build from the repository root:

```powershell
docker build -t <acr-login-server>/sushi-train-backend:dev-latest backend
```

Example push flow:

```powershell
az acr login --name <acr-name>
docker push <acr-login-server>/sushi-train-backend:dev-latest
```

Initial Azure Container Apps assumptions for the backend:

- one backend container app
- one replica only
- target port `8080`
- runtime configuration injected through environment variables
- image pulled from Azure Container Registry
- public ingress for the first working deployment path

Why public ingress is the first choice:

- it keeps the first end-to-end deployment path simpler
- it avoids introducing internal DNS and private ingress complexity too early
- it allows the frontend integration step to target a clear backend base URL first

Trade-off:

- this is less restrictive than an internal-only backend setup
- it can be revisited later after the frontend deployment path is stable

## Project Layout

```text
src/main/java/com/lorenzipsum/sushitrain/backend
|- domain/          # business model
|- application/     # use-case services
|- infrastructure/  # persistence, schedulers, adapters
`- interfaces/      # REST controllers and DTOs
```

## Abstraction And Naming

To keep boundaries explicit and naming consistent across layers:

- `*Repository` (domain): aggregate persistence contracts (load/save aggregate roots).
- `*QueryPort` (application): read/projection use-cases that are not aggregate repositories.
- `*CommandPort` (application): write/operational actions that are not aggregate repositories.
- `Jpa*Repository` / `Jpa*Adapter` (infrastructure): JPA implementations of repositories and ports.

This keeps application services readable by intent: aggregate state changes go through repositories, while
cross-aggregate reads and operational DB actions go through query/command ports.

```text
src/main/resources
|- application.yaml
|- application-local.yaml
|- application-demo-mode.yaml
|- application-show-sql.yaml
`- db/migration/    # Flyway SQL scripts
```

## Common Commands

```bash
./mvnw clean test
./mvnw verify
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## E2E Scenario Script

Script path:

- `scripts/e2e-scenario.ps1`

What it covers:

- create batches of plates on the belt
- occupy seats, pick plates, checkout
- expire on-belt plates
- negative case: try to expire a picked plate
- change belt speed

Prerequisites:

- backend is running and reachable (default: `http://localhost:8088`)
- belt, seats, and menu items are available (for example via `local` / `demo-mode`)

Run (default values):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-scenario.ps1
```

Run against custom base URL:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-scenario.ps1 -BaseUrl http://localhost:8088
```

Deterministic run (stable random selection):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\e2e-scenario.ps1 -BaseUrl http://localhost:8088 -Seed 42
```

Optional parameters:

- `-CreateBatches` (default `5`)
- `-PlatesPerBatch` (default `10`)
- `-PickCount` (default `5`)
- `-ExpireOnBeltCount` (default `3`)
- `-Seed` (optional deterministic random seed)

If the app fails on startup after Flyway migration changes while using Docker Compose, reset DB volume:

```bash
docker compose down -v
docker compose up --build
```

## Known Warning

Lombok on newer JDKs may emit this warning during tests/build:

```text
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by lombok.permit.Permit
WARNING: Please consider reporting this to the maintainers of class lombok.permit.Permit
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
```

Reference:
https://github.com/projectlombok/lombok/issues/3852#issuecomment-3009156228
