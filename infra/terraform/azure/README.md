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
- local-state-friendly ignore rules

## Directory Layout

- `versions.tf`: Terraform and provider version requirements
- `providers.tf`: AzureRM provider configuration
- `variables.tf`: root input variables
- `locals.tf`: shared local values and tags
- `main.tf`: placeholder root file for incremental resource additions
- `terraform.tfvars.example`: example local variable values
- `.gitignore`: ignores local Terraform working files and local state

## Local Workflow

Run Terraform from this directory:

```powershell
cd infra/terraform/azure
terraform init
terraform plan -var-file="terraform.tfvars"
```

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

1. Add the Azure Resource Group.
2. Add Azure Container Registry.
3. Add Azure Database for PostgreSQL Flexible Server.
4. Prepare backend and frontend deployment integration.

Resources should be added directly in this root configuration until the structure becomes hard to read. Only then should modularization be reconsidered.
