# Sushi-Train Simulator

![Sushi-Train Banner](docs/pics/cover.jpg)

Sushi-Train is a full-stack demo application that simulates a conveyor-belt sushi restaurant.
It includes a Spring Boot backend, an Angular frontend, and PostgreSQL, with Docker Compose for local startup.

Current release: `0.2.0` (tag: `v0.2.0`)

## What It Does

- Simulates one or more sushi belts with rotating slots and plates
- Supports seat occupancy, plate pickup, and checkout flows
- Exposes a REST API for all core operations
- Streams belt UI refresh events via Server-Sent Events (SSE) with polling fallback in the frontend

## Tech Stack

- Backend: Spring Boot 4, Java 25, Maven
- Frontend: Angular 21, TypeScript, Nginx (for static hosting + API reverse proxy)
- Database: PostgreSQL 17, Flyway migrations
- Local orchestration: Docker Compose

## Repository Structure

```text
sushi-train/
|-- backend/            Spring Boot application
|-- frontend/           Angular application
|-- docs/               Architecture and domain docs
|-- docker-compose.yml  Root compose file (backend + frontend + postgres)
`-- README.md
```

## Quick Start (Docker Compose)

1. Clone the repository.

```bash
git clone https://github.com/lorenzipsum/sushi-train.git
cd sushi-train
```

2. Create local compose environment file.

```bash
cp .env.example .env
```

3. Start all services.

```bash
docker compose up --build
```

4. Open the app.

- Frontend: <http://localhost:4200>
- Backend API base: <http://localhost:8088/api/v1>
- Swagger UI: <http://localhost:8088/swagger-ui/index.html>
- Actuator health: <http://localhost:8088/actuator/health>

## Environment Files

- `.env.example`: template with all compose variables and defaults
- `.env`: local values loaded automatically by Docker Compose

Common variables:

- `FRONTEND_PORT` (default `4200`)
- `BACKEND_PORT` (default `8088`)
- `POSTGRES_PORT` (default `5432`)
- `FRONTEND_API_UPSTREAM_ORIGIN` (default `http://backend:8080` inside Docker network)

## Docker Setup Notes

- There is one compose file at repo root: `docker-compose.yml`.
- Frontend container uses Nginx.
  - Serves Angular static files.
  - Proxies `/api/*` to backend via `API_UPSTREAM_ORIGIN`.
  - Disables proxy buffering for SSE.
- Backend waits for Postgres health check before startup.
- Frontend waits for backend health check before startup.

If `docker compose up` fails with a container name conflict (for example `sushi-train-postgres` already exists), stop and remove old containers first:

```bash
docker compose down --remove-orphans
docker rm -f sushi-train-postgres
```

## Run Without Docker (Optional)

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm ci
npm start
```

For non-Docker local frontend development, `npm start` uses `proxy.conf.json` to route API calls to the backend.

## Cloud Readiness

The current setup is a good base for cloud deployment:

- Stateless frontend served by Nginx
- Backend and database separated by service boundaries
- Runtime config injected via environment variables

Typical next step is to split deployment into:

- Managed Postgres
- Backend service
- Frontend static hosting or container service

## Documentation

- [Architecture Overview](docs/architecture.md)
- [Domain Events](docs/domain-events.md)
- [Domain Model](docs/domain-model.md)

## License

MIT. See [LICENSE](LICENSE).
