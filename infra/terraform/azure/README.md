# Terraform Azure Root

This directory contains the initial Terraform root configuration for deploying Sushi-Train to Azure.

The structure is intentionally small and explicit:

- one Terraform root
- local Terraform state only for now
- no internal Terraform modules yet
- Azure resources added incrementally in later steps

## Purpose

This scaffold exists to make local Terraform execution predictable and easy to understand before any real Azure resources are introduced.

At this stage, the directory is only responsible for:

- Terraform version and provider declarations
- provider configuration
- shared input variables
- shared locals and tags
- Azure Resource Group creation
- Azure Container Registry creation
- Azure Database for PostgreSQL Flexible Server creation
- local-state-friendly ignore rules

## Directory Layout

- `versions.tf`: Terraform and provider version requirements
- `providers.tf`: AzureRM provider configuration
- `variables.tf`: root input variables
- `locals.tf`: shared local values and tags
- `main.tf`: root resources
- `outputs.tf`: useful values from the current root resources
- `terraform.tfvars.example`: example local variable values
- `.gitignore`: ignores local Terraform working files and local state

## Local Workflow

Run Terraform from this directory:

```powershell
cd infra/terraform/azure
Copy-Item terraform.tfvars.example terraform.tfvars
terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

Recommended local secret file:

- create `secrets.auto.tfvars` for sensitive values
- Terraform loads `*.auto.tfvars` automatically
- keep secrets out of `terraform.tfvars.example`

Before running `plan` or `apply`:

1. Sign in with Azure CLI.
2. Confirm which subscription Azure CLI is currently using.
3. Put that same subscription ID into `terraform.tfvars`.

Useful commands:

```powershell
az login
az account show --output table
az account list --output table
az account set --subscription "<subscription-id-or-name>"
az account show --query id --output tsv
```

Important:

- The `subscription_id` passed to Terraform must match a subscription visible to the current Azure CLI login.
- If Terraform reports that the subscription ID is not known by Azure CLI, either the ID is wrong or Azure CLI is currently set to a different account or tenant.
- Running `terraform plan` without a local `terraform.tfvars` file will cause Terraform to prompt for required variables interactively.
- `container_registry_name` must be globally unique in Azure and use only lowercase letters and digits.
- `postgresql_server_name` must be globally unique in Azure.
- Set `postgresql_administrator_password` in a local-only way before planning or applying.

Recommended local file option for the PostgreSQL administrator password:

```hcl
# secrets.auto.tfvars
postgresql_administrator_password = "<your-strong-password>"
```

Terraform will load that file automatically.

Alternative PowerShell option:

```powershell
$env:TF_VAR_postgresql_administrator_password = "<your-strong-password>"
```

The `.gitignore` in this directory ignores `*.auto.tfvars`, so the secret file stays local by default.

## Local State

Terraform state is intentionally kept local in this phase.

That means:

- no remote backend is configured
- state files stay on the laptop
- the setup remains simple for a single-developer learning workflow

## Relationship To Docker Compose

This Terraform directory does not replace the existing local Docker Compose workflow.

The intended model is:

- Docker Compose stays available for local application execution
- Terraform is used only for Azure infrastructure and deployment preparation
- application containers should remain reusable across local and Azure environments where practical

## Incremental Delivery

Planned next infrastructure steps:

1. Prepare frontend runtime and API integration.
2. Capture deployment workflow and smoke-test guidance.

## Current Managed Resources

The Terraform root currently manages:

- one Azure Resource Group
- one Azure Container Registry
- one Azure Database for PostgreSQL Flexible Server
- one application database inside that PostgreSQL server

Current ACR defaults:

- SKU: `Basic`
- admin user: disabled

The registry name is kept as an explicit input instead of being auto-generated because Azure Container Registry names are globally unique and must remain readable during local learning and debugging.

Current PostgreSQL defaults:

- version: `17`
- SKU: `B_Standard_B1ms`
- storage: `32768` MB
- backup retention: `7` days
- public networking: enabled
- Azure-services firewall rule: enabled

This is intentionally a low-complexity first database setup. It avoids private networking for now and keeps the server reachable from Azure-hosted application components with a simple public-network rule.

Resources should be added directly in this root configuration until the structure becomes hard to read. Only then should modularization be reconsidered.
