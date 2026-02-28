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

From the `backend` directory:

```bash
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
