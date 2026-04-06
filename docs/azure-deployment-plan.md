# Azure Deployment Plan

## Purpose

Prepare Sushi-Train for a simple, incremental deployment to Azure using Terraform Community Edition from a local Windows laptop.

The goal of this effort is to reach a first working end-to-end deployment path that is easy to understand, cheap to run, and straightforward to evolve later.

This planning document is intentionally limited to step 1. It records the target architecture, constraints, assumptions, and the implementation path before Terraform resources are introduced.

## Target Architecture

The intended first Azure deployment consists of:

- One Azure Resource Group for all project resources.
- One Azure Container Registry to store backend and frontend images.
- One Azure Database for PostgreSQL Flexible Server for the managed relational database.
- One Azure Container Apps environment.
- One backend Azure Container App.
- One frontend Azure Container App.

Application shape:

- Frontend remains a separate container, served by Nginx.
- Backend remains a separate Spring Boot container.
- Database moves from local Compose Postgres to managed PostgreSQL.
- Frontend and backend are deployed independently.
- No AKS, no Kubernetes manifests, and no multi-region design.

Expected traffic flow:

- Browser reaches the frontend container app over public HTTP(S).
- Frontend forwards `/api/*` requests to the backend.
- Backend connects to Azure Database for PostgreSQL using runtime environment variables.

## Current Repository Facts Relevant To Azure

- The repository already has separate backend and frontend Dockerfiles.
- Local orchestration currently uses Docker Compose with backend, frontend, and PostgreSQL.
- The backend already supports runtime datasource configuration through `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
- The backend exposes health at `/actuator/health`.
- The frontend is served by Nginx and proxies `/api/` through `API_UPSTREAM`.
- Frontend API calls are already built around relative `/api/...` paths, which is useful for cloud routing.
- Flyway migrations are already part of backend startup.

These facts make a container-based Azure deployment a reasonable next step without major re-architecture.

## Assumptions

- Terraform will be run locally from the developer laptop using PowerShell.
- Terraform state will remain local for now.
- Work will happen on a dedicated Git branch managed manually outside this change.
- Azure CLI authentication can be used locally when Terraform is introduced.
- The project remains a personal learning and portfolio application, not an enterprise production system.
- Small, explicit Terraform files are preferred over reusable abstraction layers at this stage.
- A single Azure region will be used.
- A single active backend deployment and a single active frontend deployment are sufficient.
- The backend application can be adapted where needed for cloud-friendly runtime configuration.
- Managed PostgreSQL is preferred over a self-hosted database container.

## Constraints

- Keep the solution boring, explicit, and easy to read.
- Keep cost low and avoid unnecessary always-on infrastructure where practical.
- Preserve the existing local Docker Compose execution path as a supported way to run the project locally.
- Avoid enterprise-grade networking unless there is a clear technical need.
- Avoid Terraform modularization until the basic deployment is working and the structure is proven useful.
- Keep frontend and backend as separate deployable containers.
- Do not introduce Kubernetes manifests.
- Do not introduce AKS.
- Do not introduce multi-region deployment, failover, or high-availability complexity beyond what managed Azure services already provide.
- Keep local Terraform workflow understandable for a single developer.

## Non-Goals

The following are intentionally out of scope for the first implementation path:

- Remote Terraform state.
- Shared multi-environment infrastructure strategy.
- Complex CI/CD automation.
- Blue/green or canary deployment workflows.
- Custom virtual network design unless a service forces it.
- Private endpoints, WAF, Front Door, API Management, or enterprise ingress layers.
- Horizontal scale-out beyond one frontend replica and one backend replica.
- Observability stacks beyond basic platform logs and smoke tests.
- Production-grade secret rotation and governance.
- Disaster recovery, cross-region resilience, or zero-downtime guarantees.

## Important Technical Decisions

### 1. Start With Plain Terraform In One Root Project

The initial Terraform layout should stay in one simple root configuration rather than introducing internal modules early.

Chosen location:

- `infra/terraform/azure`

Reason:

- The target architecture is small.
- A single developer can understand the full graph directly.
- Refactoring into modules later is cheaper than starting with the wrong abstraction.

Initial root files:

- `README.md`
- `versions.tf`
- `providers.tf`
- `variables.tf`
- `locals.tf`
- `main.tf`
- `outputs.tf`
- `terraform.tfvars.example`
- `.gitignore`

### 2. Keep Terraform State Local Initially

Terraform state will remain local on the laptop during the first implementation.

Reason:

- This reduces setup complexity.
- It fits a learning and portfolio workflow.
- It keeps the first deployment path easy to explain.

Trade-off:

- Collaboration and state durability are weaker than with remote state.

### 3. Use Azure Container Apps Instead Of AKS

Both backend and frontend will run on Azure Container Apps.

Reason:

- Lower operational complexity.
- Good fit for small containerized services.
- No need to manage Kubernetes control plane concepts.

### 4. Use Managed PostgreSQL

The database target is Azure Database for PostgreSQL Flexible Server.

Reason:

- Managed backups and operations are preferable to running Postgres in a container.
- This aligns with the existing application design, which already expects PostgreSQL and Flyway.

### 5. Preserve Frontend And Backend Separation

Frontend and backend stay as separate deployable containers.

Reason:

- This matches the current repository structure.
- It keeps responsibilities clear.
- It allows independent image builds and deployment changes later.

### 6. Prefer Runtime Environment Variables Over Build-Time Cloud Coupling

Backend database and application settings should continue to be provided at runtime.
Frontend-to-backend routing should also stay configurable by environment.

Reason:

- This keeps images portable.
- The same containers can be reused across local and Azure workflows with minimal change.

### 7. Favor Lowest-Complexity Defaults First

Initial resource choices should prefer the smallest practical options that still allow the app to run.

Examples to evaluate in later steps:

- Small ACR SKU.
- Small PostgreSQL Flexible Server SKU.
- Conservative Container Apps replica settings.

### 8. Keep Azure Deployment Additive To Local Docker Compose

The Azure deployment path should complement the existing local Docker Compose workflow, not replace it.

Reason:

- Docker Compose already provides a clear local end-to-end execution path.
- Preserving local execution reduces friction for development and debugging.
- Reusing the same container boundaries keeps the hosting model easier to understand.

Practical implication:

- Local runs should continue to work with the existing Compose setup.
- Azure-specific work should rely mainly on environment-specific configuration and infrastructure provisioning.
- Avoid introducing changes that make the application depend on Azure-only behavior for normal local execution.

## Open Questions And Risks

### Open Questions

- Should the backend container app be public for the first iteration, or internal with only the frontend exposed publicly?
- Should the backend be allowed to scale to zero for cost savings, or should it stay at one minimum replica to avoid cold starts?
- Which Azure region offers acceptable cost and quota availability for this project?
- Does Azure Database for PostgreSQL Flexible Server require any network choices that add complexity worth avoiding in the first pass?
- Should Flyway run as part of backend startup in Azure exactly as it does locally, or should migration execution become a separate operational step later?
- What exact runtime variables will the frontend need for backend routing once deployed to Container Apps?

### Risks

- Container Apps ingress and internal DNS details may require small frontend or Nginx adjustments.
- PostgreSQL connection settings may need backend tuning once running in Azure.
- If the backend scales to zero, cold starts may make the app feel slow during demos.
- If the backend stays always on, cost may be higher than desired for a personal project.
- Azure service limits, SKU availability, or region-specific restrictions may affect the final low-cost configuration.
- Local-only Terraform state is easy to start with but easy to misplace if not handled carefully.

## Why This Design Is Intentionally Simple

This design is deliberately small because the objective is not to demonstrate every Azure feature. The objective is to create a clear first deployment path that matches the current repository structure and can be explained, debugged, and evolved by one developer.

The current application already has the right basic separation:

- frontend container
- backend container
- PostgreSQL database
- environment-based configuration

The plan therefore avoids introducing extra platforms, extra abstraction layers, and extra operational concerns before they are justified. If the first simple version works well, later steps can add refinement from a stable baseline instead of solving multiple kinds of complexity at once.

## Planned Implementation Sequence

The intended incremental sequence is tracked in [azure-progress.md](./azure-progress.md).

## Current Terraform Structure

The initial Terraform root now lives in `infra/terraform/azure`.

The purpose of this structure is to provide a stable, readable starting point before any Azure resources are added.

It intentionally includes:

- version and provider declarations
- root input variables
- shared locals and common tags
- the Azure Resource Group as the first real managed resource
- the Azure Container Registry as the second real managed resource
- outputs for immediately useful resource values
- an example `terraform.tfvars` file for local execution
- ignore rules for local Terraform state and working files

It intentionally does not yet include:

- remote state
- modules
- CI/CD automation

## Current Implemented Infrastructure

The Terraform root currently creates only one Azure resource:

- one Azure Resource Group
- one Azure Container Registry

Naming approach:

- Resource Group name is derived from `resource_name_prefix` and `environment_name`
- current pattern: `<resource_name_prefix>-<environment_name>-rg`

Why start with only the Resource Group:

- it provides a safe first end-to-end Terraform apply target
- it establishes the location and tagging baseline for later resources
- it keeps the first infrastructure change easy to review and easy to destroy if needed

## Current Container Registry Design

The Azure Container Registry is intentionally configured with simple defaults:

- explicit registry name supplied by input variable
- `Basic` SKU by default
- admin user disabled by default

Why the registry name is explicit:

- Azure Container Registry names must be globally unique
- automatic name generation would hide an important deployment constraint
- an explicit name is easier to reason about during local learning and troubleshooting

Why `Basic` is the default:

- it fits the low-cost direction of the project
- it is sufficient for a first end-to-end deployment path

Why admin access is disabled by default:

- later deployment steps should prefer explicit Azure identity-based access where practical
- enabling the admin user too early would add convenience at the cost of a less disciplined baseline
