# Sushi-Train Simulator

![Sushi-Train Banner](docs/pics/cover.jpg)

Sushi-Train is a full-stack demo application that simulates a conveyor-belt sushi restaurant.
It includes a Spring Boot backend, an Angular frontend, and PostgreSQL, with Docker Compose for local startup.

Current release: `0.2.0` (tag: `v0.2.0`)

## Overview

- Backend: Spring Boot 4, Java 25, Maven
- Frontend: Angular 21, TypeScript, Nginx
- Database: PostgreSQL 17 with Flyway migrations
- Run options: local without Docker, local with Docker Compose, or Azure with Terraform

## How To Run

Sushi-Train supports three run modes:

- locally without Docker
- locally with Docker Compose
- on Azure with Terraform

For daily development, use local execution or Docker Compose. Use Terraform only when you want the Azure-hosted setup.

### Locally Without Docker

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

### Locally With Docker Compose

```bash
git clone https://github.com/lorenzipsum/sushi-train.git
cd sushi-train
```

```bash
cp .env.example .env
```

```bash
docker compose up --build
```

Open:

- Frontend: <http://localhost:4200>
- Backend API base: <http://localhost:8088/api/v1>
- Swagger UI: <http://localhost:8088/swagger-ui/index.html>
- Actuator health: <http://localhost:8088/actuator/health>

If `docker compose up` fails with container name conflicts, run:

```bash
docker compose down --remove-orphans
docker rm -f sushi-train-postgres
```

Environment files:

- `.env.example`: template with all compose variables and defaults
- `.env`: local values loaded automatically by Docker Compose

### On Azure With Terraform

The Azure Terraform root lives in [infra/terraform/azure](infra/terraform/azure).

Use this path when you want the full Azure-hosted stack.

- Terraform operations (`Deploy`, `Update`, `Remove`): [infra/terraform/azure/OPERATIONS.md](infra/terraform/azure/OPERATIONS.md)
- Terraform structure and variables: [infra/terraform/azure/README.md](infra/terraform/azure/README.md)
- Rollout and smoke tests: [docs/azure-deploy-runbook.md](docs/azure-deploy-runbook.md)

Quick Terraform commands from repository root:

```powershell
terraform -chdir=infra/terraform/azure init
terraform -chdir=infra/terraform/azure plan -var-file="terraform.tfvars"
terraform -chdir=infra/terraform/azure apply -var-file="terraform.tfvars"
terraform -chdir=infra/terraform/azure destroy -var-file="terraform.tfvars"
```

## Documentation

- [Architecture Overview](docs/architecture.md)
- [Domain Events](docs/domain-events.md)
- [Domain Model](docs/domain-model.md)
- [Terraform Operations (Azure)](infra/terraform/azure/OPERATIONS.md)
- [Terraform Azure Root](infra/terraform/azure/README.md)
- [Azure Deployment Plan](docs/azure-deployment-plan.md)
- [Azure Deploy Runbook](docs/azure-deploy-runbook.md)
- [Azure Progress](docs/azure-progress.md)

## License

MIT. See [LICENSE](LICENSE).
