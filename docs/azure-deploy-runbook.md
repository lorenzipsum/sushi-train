# Azure Deploy Runbook

This document is the operational reference for updating the current Azure deployment of Sushi-Train.

Progress tracking for this Azure rollout lives in `docs/azure-progress.md`.

Current scope:

- Terraform-managed Azure infrastructure
- backend container image rollout
- frontend container image rollout
- backend smoke tests and common checks
- end-to-end browser-facing smoke tests

## Current Deployment Shape

The current Azure deployment consists of:

- one Terraform root in `infra/terraform/azure`
- one Azure Resource Group
- one Azure Container Registry
- one Azure Database for PostgreSQL Flexible Server
- one Azure Container Apps environment
- one backend Azure Container App
- one frontend Azure Container App

The backend is API-only. The frontend Container App is the intended browser entry point.

## Release Checklist

Use this checklist for a normal Azure rollout.

### Infrastructure-only change

1. Review the Terraform diff.
2. Apply Terraform from `infra/terraform/azure`.
3. Run the smoke tests in this document.

Commands:

```powershell
cd infra/terraform/azure
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

### Backend release

1. Pick a new backend image tag.
2. Build the backend image.
3. Log in to ACR.
4. Push the backend image.
5. Apply Terraform.
6. Run backend and frontend smoke tests.

### Frontend release

1. Pick a new frontend image tag.
2. Build the frontend image.
3. Log in to ACR.
4. Push the frontend image.
5. Apply Terraform.
6. Run backend and frontend smoke tests.

### End-to-end verification

1. Open the frontend URL.
2. Confirm the UI loads.
3. Confirm `${frontendUrl}/api/version` works through the proxy.
4. Confirm `${backendUrl}/actuator/health` is healthy.

## Prerequisites

Required local tools:

- Azure CLI
- Terraform Community Edition
- Docker Desktop or another local Docker engine

Recommended local checks:

```powershell
az login
az account show --output table
terraform version
docker version
```

Terraform root:

```powershell
cd infra/terraform/azure
```

## Useful Terraform Outputs

Use these commands from `infra/terraform/azure` when you need the current deployed values:

```powershell
terraform output resource_group_name
terraform output container_registry_name
terraform output container_registry_login_server
terraform output postgresql_server_fqdn
terraform output backend_container_app_name
terraform output backend_container_app_url
terraform output frontend_container_app_name
terraform output frontend_container_app_url
```

## Workflow A: Infrastructure Change Only

Use this when only Terraform-managed infrastructure changed and no new application image is required.

From `infra/terraform/azure`:

```powershell
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

Use this workflow for changes such as:

- PostgreSQL settings
- Container App environment settings
- backend environment variables
- backend scale, ingress, or probe configuration

After apply, run the smoke checks in the smoke-test section below.

## Workflow B: Backend Application Change

Use this when backend code, backend config files, or backend Docker image contents changed.

### Recommended Approach

Use a new explicit image tag for each rollout instead of reusing `dev-latest`.

Reason:

- it makes the rollout state visible in Terraform
- it avoids ambiguity around image caching
- it makes rollback easier

Example tag:

- `2026-04-06-fix-root-404`

### Step 1: Choose a New Backend Image Tag

Update `backend_image_tag` in `infra/terraform/azure/terraform.tfvars`.

Example:

```hcl
backend_image_tag = "2026-04-06-fix-root-404"
```

### Step 2: Build The Backend Image

From the repository root:

```powershell
$acrLoginServer = terraform -chdir=infra/terraform/azure output -raw container_registry_login_server
docker build -t ${acrLoginServer}/sushi-train-backend:2026-04-06-fix-root-404 backend
```

Concrete example for the current environment:

```powershell
docker build -t sushitraindevacr2026a.azurecr.io/sushi-train-backend:dev-latest .\backend
```

### Step 3: Log In To Azure Container Registry

From the repository root:

```powershell
$acrName = terraform -chdir=infra/terraform/azure output -raw container_registry_name
az acr login --name $acrName
```

Concrete example for the current environment:

```powershell
az acr login --name sushitraindevacr2026a
```

### Step 4: Push The Backend Image

From the repository root:

```powershell
$acrLoginServer = terraform -chdir=infra/terraform/azure output -raw container_registry_login_server
docker push ${acrLoginServer}/sushi-train-backend:2026-04-06-fix-root-404
```

Concrete example for the current environment:

```powershell
docker push sushitraindevacr2026a.azurecr.io/sushi-train-backend:dev-latest
```

### Step 5: Apply Terraform

From `infra/terraform/azure`:

```powershell
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

This creates a new backend Container App revision pointing at the new image tag.

Note:

- the concrete example above uses the current ACR name and the mutable tag `dev-latest`
- this is fine for quick manual iteration
- for clearer rollouts and easier rollback, prefer a new explicit tag for each deployment

## Workflow C: Backend Configuration Change With No Code Change

If only Terraform-managed backend settings changed, for example:

- `backend_swagger_enabled`
- CPU or memory
- ingress
- environment variables

then no image rebuild is required.

Run only:

```powershell
cd infra/terraform/azure
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

## Workflow D: Frontend Application Change

Use this when frontend code, frontend Nginx config, or frontend Docker image contents changed.

### Recommended Approach

Use a new explicit image tag for each rollout instead of reusing `dev-latest`.

Example tag:

- `2026-04-06-frontend-release-1`

### Step 1: Choose A New Frontend Image Tag

Update `frontend_image_tag` in `infra/terraform/azure/terraform.tfvars`.

Example:

```hcl
frontend_image_tag = "2026-04-06-frontend-release-1"
```

### Step 2: Build The Frontend Image

From the repository root:

```powershell
$acrLoginServer = terraform -chdir=infra/terraform/azure output -raw container_registry_login_server
docker build -t ${acrLoginServer}/sushi-train-frontend:2026-04-06-frontend-release-1 frontend
```

Concrete example for the current environment:

```powershell
docker build -t sushitraindevacr2026a.azurecr.io/sushi-train-frontend:dev-latest .\frontend
```

### Step 3: Log In To Azure Container Registry

From the repository root:

```powershell
$acrName = terraform -chdir=infra/terraform/azure output -raw container_registry_name
az acr login --name $acrName
```

Concrete example for the current environment:

```powershell
az acr login --name sushitraindevacr2026a
```

### Step 4: Push The Frontend Image

From the repository root:

```powershell
$acrLoginServer = terraform -chdir=infra/terraform/azure output -raw container_registry_login_server
docker push ${acrLoginServer}/sushi-train-frontend:2026-04-06-frontend-release-1
```

Concrete example for the current environment:

```powershell
docker push sushitraindevacr2026a.azurecr.io/sushi-train-frontend:dev-latest
```

### Step 5: Apply Terraform

From `infra/terraform/azure`:

```powershell
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

This creates a new frontend Container App revision pointing at the new image tag.

Note:

- the concrete example above uses the current ACR name and the mutable tag `dev-latest`
- this is fine for quick manual iteration
- for clearer rollouts and easier rollback, prefer a new explicit tag for each deployment

## Workflow E: Frontend Configuration Change With No Code Change

If only Terraform-managed frontend settings changed, for example:

- `frontend_container_cpu`
- `frontend_container_memory`
- `frontend_ingress_external_enabled`
- `frontend_api_upstream_scheme`

then no image rebuild is required.

Run only:

```powershell
cd infra/terraform/azure
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

## Smoke Tests

Run these checks after any backend or frontend rollout.

First fetch the current URLs:

```powershell
$backendUrl = terraform -chdir=infra/terraform/azure output -raw backend_container_app_url
$frontendUrl = terraform -chdir=infra/terraform/azure output -raw frontend_container_app_url
```

### Minimum Checks

```powershell
Invoke-RestMethod -Uri "$backendUrl/actuator/health"
Invoke-RestMethod -Uri "$backendUrl/api/version"
Invoke-WebRequest -Uri $frontendUrl -UseBasicParsing
```

Expected results:

- `/actuator/health` returns status information and should indicate healthy startup
- `/api/version` returns backend build and environment metadata
- the frontend root returns the Angular application HTML successfully

### Useful Manual Browser Checks

- `${frontendUrl}/`
- `${frontendUrl}/api/version`
- `${backendUrl}/actuator/health`
- `${backendUrl}/api/version`

Expected notes:

- `${frontendUrl}/` should become the main browser-facing entry point
- `${frontendUrl}/api/*` should proxy through to the backend Container App
- `${backendUrl}/` is not the final UI and should not be treated as the user-facing app URL
- `${backendUrl}/swagger-ui/index.html` should only work when `backend_swagger_enabled = true`

### Optional API Checks

If seed data loaded successfully, these should also be useful:

```powershell
Invoke-RestMethod -Uri "$backendUrl/api/v1/belts"
Invoke-RestMethod -Uri "$backendUrl/api/v1/menu-items"
Invoke-RestMethod -Uri "$frontendUrl/api/v1/belts"
Invoke-RestMethod -Uri "$frontendUrl/api/v1/menu-items"
```

## Troubleshooting

### Backend Container Starts Then Crashes During Flyway

Likely cause:

- Azure PostgreSQL extension allowlist is missing `uuid-ossp`

Current expected state:

- Terraform manages `azure.extensions`
- `uuid-ossp` is allowlisted by default

Recovery:

```powershell
cd infra/terraform/azure
terraform apply -var-file="terraform.tfvars"
```

### Backend URL Returns 500 For `/` Or `/swagger-ui/index.html`

Interpretation:

- this usually means the deployed backend image is an older revision that still maps missing resources to the generic 500 handler

What it means in practice:

- `/` is not a real homepage for the backend
- `/swagger-ui/index.html` is expected to be unavailable when Swagger is disabled
- both should return `404`, not `500`, after the fixed backend image is deployed

Recovery:

- build and push a new backend image
- apply Terraform with the new image tag

### Swagger UI Does Not Open In Azure

Check `backend_swagger_enabled` in `infra/terraform/azure/terraform.tfvars`.

If it is `false`, this is expected.

To enable it explicitly:

```hcl
backend_swagger_enabled = true
```

Then run:

```powershell
cd infra/terraform/azure
terraform apply -var-file="terraform.tfvars"
```

### Image Push Succeeds But App Still Runs Old Behavior

Most likely cause:

- the Container App is still pointing at the old image tag

Preferred fix:

- use a new explicit `backend_image_tag`
- push that exact tag
- run `terraform apply`

### Frontend Loads But API Calls Fail

Likely causes:

- frontend image is deployed, but backend is unhealthy
- frontend runtime proxy is pointing at the wrong backend host or scheme

Checks:

```powershell
$backendUrl = terraform -chdir=infra/terraform/azure output -raw backend_container_app_url
$frontendUrl = terraform -chdir=infra/terraform/azure output -raw frontend_container_app_url
Invoke-RestMethod -Uri "$backendUrl/actuator/health"
Invoke-RestMethod -Uri "$frontendUrl/api/version"
```

Interpretation:

- if the backend health call fails directly, fix the backend first
- if the backend health call succeeds but `${frontendUrl}/api/version` fails, inspect the frontend Container App revision and current runtime settings

### Terraform Says A Container App Already Exists

Example message:

- `a resource with the ID ... already exists - to be managed via Terraform this resource needs to be imported into the State`

Interpretation:

- Azure already has the resource
- Terraform state may be missing that resource entry, or the error may have come from an earlier partial apply while a later retry already fixed state

Check current state first:

```powershell
cd infra/terraform/azure
terraform state list
terraform plan -var-file="terraform.tfvars" -refresh=false
```

If the resource is already present in `terraform state list` and plan says `No changes`, do not import anything. The state is already correct.

If the resource exists in Azure but is missing from `terraform state list`, import it explicitly.

Frontend Container App example:

```powershell
terraform import azurerm_container_app.frontend /subscriptions/b5814eac-6c5c-417f-9649-fefe6f57f3a6/resourceGroups/sushitrain-dev-rg/providers/Microsoft.App/containerApps/sushitrain-dev-frontend
```

Then run:

```powershell
terraform plan -var-file="terraform.tfvars"
```

## Rollback Approach

The simplest rollback is to point Terraform back at the previous known-good backend image tag.

Example:

```hcl
backend_image_tag = "2026-04-06-previous-good"
```

Then:

```powershell
cd infra/terraform/azure
terraform apply -var-file="terraform.tfvars"
```

## Current Browser Entry Point

Once the frontend Container App is applied and the frontend image is pushed, the expected user-facing URL is:

- `frontend_container_app_url`

The backend Container App URL remains useful for health checks, debugging, and direct API inspection, but it is not the intended main entry point for normal browser use.
