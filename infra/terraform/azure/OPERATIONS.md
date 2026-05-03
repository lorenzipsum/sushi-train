# Terraform Operations (Azure)

This file contains the operational Terraform commands for Sushi-Train on Azure.

Terraform root: `infra/terraform/azure`

## Prerequisites

- Azure CLI
- Terraform Community Edition
- Docker Desktop or another local Docker engine
- an Azure subscription that can create billable resources

After Azure free credits expire, use a pay-as-you-go or equivalent subscription that allows:

- Azure Container Apps
- Azure Container Registry
- Log Analytics
- Azure Database for PostgreSQL Flexible Server

## Inputs

Copy the example variables file first:

```powershell
Copy-Item terraform.tfvars.example terraform.tfvars
```

Then fill in these values in `terraform.tfvars`:

- `subscription_id`
- `location`
- `container_registry_name`
- `postgresql_server_name`
- optional image tags if you do not want `dev-latest`

Also provide `postgresql_administrator_password` locally, either in `secrets.auto.tfvars` or through `TF_VAR_postgresql_administrator_password`.

The subscription selected in Azure CLI must match `subscription_id` in Terraform.

Recommended checks:

```powershell
az login
az account list --output table
az account set --subscription "<subscription-id-or-name>"
az account show --output table
terraform version
```

## Deploy

The current Terraform layout expects application images to exist in Azure Container Registry before Container Apps are created. A clean deployment is a two-step apply.

1. Initialize Terraform and create registry first.

```powershell
terraform init
terraform apply -target=azurerm_container_registry.main -var-file="terraform.tfvars"
```

2. Build and push backend and frontend images.

```powershell
$acrName = terraform output -raw container_registry_name
$acrLoginServer = terraform output -raw container_registry_login_server

az acr login --name $acrName

docker build -t ${acrLoginServer}/sushi-train-backend:dev-latest ../../../backend
docker push ${acrLoginServer}/sushi-train-backend:dev-latest

docker build -t ${acrLoginServer}/sushi-train-frontend:dev-latest ../../../frontend
docker push ${acrLoginServer}/sushi-train-frontend:dev-latest
```

If you changed `backend_image_tag` or `frontend_image_tag` in `terraform.tfvars`, use the same tags in Docker commands.

3. Apply the full Azure stack.

```powershell
terraform apply -var-file="terraform.tfvars"
terraform output frontend_container_app_url
terraform output backend_container_app_url
```

## Update

Use this when Azure infrastructure already exists and you want to roll out Terraform changes or new container images.

Terraform-only changes:

```powershell
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

Backend/frontend image updates:

```powershell
$acrName = terraform output -raw container_registry_name
$acrLoginServer = terraform output -raw container_registry_login_server

az acr login --name $acrName

docker build -t ${acrLoginServer}/sushi-train-backend:<tag> ../../../backend
docker push ${acrLoginServer}/sushi-train-backend:<tag>

docker build -t ${acrLoginServer}/sushi-train-frontend:<tag> ../../../frontend
docker push ${acrLoginServer}/sushi-train-frontend:<tag>

terraform apply -var-file="terraform.tfvars"
```

Set the same `<tag>` values in `backend_image_tag` and `frontend_image_tag` when needed.

## Remove

If local Terraform state is present, destroy removes resources managed by this root and stops related Azure charges.

```powershell
terraform init
terraform destroy -var-file="terraform.tfvars"
```

After destroy, verify the Resource Group is gone or empty in Azure.

If local state is missing, Terraform cannot safely identify everything it created. Fallback:

```powershell
az group delete --name "<resource-group-name>" --yes --no-wait
```

Current default Resource Group naming pattern: `<resource_name_prefix>-<environment_name>-rg` (example: `sushitrain-dev-rg`).

## Moving Between Laptops

If you destroy from the old laptop (where local state exists), you can continue from a new laptop with a fresh local state.

Sequence:

1. On old laptop: run `terraform destroy`.
2. On new laptop: run `terraform init`.
3. Re-deploy with the `Deploy` steps above.

After first `terraform apply` on the new laptop, that machine becomes the new local state source.
