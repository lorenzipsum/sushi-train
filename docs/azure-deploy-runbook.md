# Azure Deploy Runbook

This document is the operational reference for updating the current Azure deployment of Sushi-Train.

Current scope:

- Terraform-managed Azure infrastructure
- backend container image rollout
- backend smoke tests and common checks

Not yet covered:

- frontend Azure Container App rollout, because that resource is not implemented yet

## Current Deployment Shape

The current Azure deployment consists of:

- one Terraform root in `infra/terraform/azure`
- one Azure Resource Group
- one Azure Container Registry
- one Azure Database for PostgreSQL Flexible Server
- one Azure Container Apps environment
- one backend Azure Container App

The backend is API-only. It is not the browser entry point for the final application UI.

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

## Smoke Tests

Run these checks after any backend rollout.

First fetch the current backend URL:

```powershell
$backendUrl = terraform -chdir=infra/terraform/azure output -raw backend_container_app_url
```

### Minimum Checks

```powershell
Invoke-RestMethod -Uri "$backendUrl/actuator/health"
Invoke-RestMethod -Uri "$backendUrl/api/version"
```

Expected results:

- `/actuator/health` returns status information and should indicate healthy startup
- `/api/version` returns backend build and environment metadata

### Useful Manual Browser Checks

- `${backendUrl}/actuator/health`
- `${backendUrl}/api/version`

Expected notes:

- `${backendUrl}/` is not the final UI and should not be treated as the user-facing app URL
- `${backendUrl}/swagger-ui/index.html` should only work when `backend_swagger_enabled = true`

### Optional API Checks

If seed data loaded successfully, these should also be useful:

```powershell
Invoke-RestMethod -Uri "$backendUrl/api/v1/belts"
Invoke-RestMethod -Uri "$backendUrl/api/v1/menu-items"
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

## Pending Frontend Runbook Section

Frontend deployment commands are intentionally not documented here yet because the frontend Azure Container App does not exist in Terraform at the moment.

Once step 11 is implemented, extend this runbook with:

- frontend image build and push commands
- frontend Terraform rollout commands
- end-to-end browser smoke tests using the frontend URL
