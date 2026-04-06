variable "subscription_id" {
  description = "Azure subscription ID used by the AzureRM provider."
  type        = string
  sensitive   = true
}

variable "location" {
  description = "Azure region for shared project resources."
  type        = string
}

variable "project_name" {
  description = "Short project name used in tags and resource naming."
  type        = string
  default     = "sushi-train"
}

variable "environment_name" {
  description = "Short environment label for this deployment iteration."
  type        = string
  default     = "dev"
}

variable "resource_name_prefix" {
  description = "Prefix used when constructing Azure resource names."
  type        = string
  default     = "sushitrain"
}

variable "container_registry_name" {
  description = "Globally unique Azure Container Registry name. Must be 5-50 lowercase alphanumeric characters."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9]{5,50}$", var.container_registry_name))
    error_message = "container_registry_name must be 5-50 characters long and use lowercase letters and digits only."
  }
}

variable "container_registry_sku" {
  description = "SKU for Azure Container Registry. Basic is the intended low-cost default for this project."
  type        = string
  default     = "Basic"

  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.container_registry_sku)
    error_message = "container_registry_sku must be one of: Basic, Standard, Premium."
  }
}

variable "container_registry_admin_enabled" {
  description = "Whether the admin user is enabled on Azure Container Registry. Keep false unless there is a concrete need."
  type        = bool
  default     = false
}

variable "postgresql_server_name" {
  description = "Globally unique Azure Database for PostgreSQL Flexible Server name. Use lowercase letters, digits, and hyphens."
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$", var.postgresql_server_name))
    error_message = "postgresql_server_name must be 3-63 characters, use lowercase letters, digits, or hyphens, and start and end with a letter or digit."
  }
}

variable "postgresql_version" {
  description = "PostgreSQL major version for Azure Database for PostgreSQL Flexible Server."
  type        = string
  default     = "17"
}

variable "postgresql_administrator_login" {
  description = "Administrator login name for Azure Database for PostgreSQL Flexible Server."
  type        = string
  default     = "sushitrainadmin"
}

variable "postgresql_administrator_password" {
  description = "Administrator password for Azure Database for PostgreSQL Flexible Server. Set in local terraform.tfvars or TF_VAR_postgresql_administrator_password."
  type        = string
  sensitive   = true
}

variable "postgresql_database_name" {
  description = "Application database name created inside the PostgreSQL server."
  type        = string
  default     = "sushitrain"
}

variable "postgresql_sku_name" {
  description = "SKU name for Azure Database for PostgreSQL Flexible Server."
  type        = string
  default     = "B_Standard_B1ms"
}

variable "postgresql_storage_mb" {
  description = "Storage size in MB for Azure Database for PostgreSQL Flexible Server."
  type        = number
  default     = 32768
}

variable "postgresql_backup_retention_days" {
  description = "Backup retention in days for Azure Database for PostgreSQL Flexible Server."
  type        = number
  default     = 7
}

variable "postgresql_public_network_access_enabled" {
  description = "Whether public network access is enabled for Azure Database for PostgreSQL Flexible Server."
  type        = bool
  default     = true
}

variable "postgresql_allow_azure_services" {
  description = "Whether to add a firewall rule that allows Azure services to reach the PostgreSQL server over public networking."
  type        = bool
  default     = true
}

variable "postgresql_allowed_extensions" {
  description = "PostgreSQL extensions to allowlist on Azure Flexible Server through the azure.extensions server parameter."
  type        = list(string)
  default     = ["uuid-ossp"]
}

variable "log_analytics_workspace_sku" {
  description = "SKU for the Log Analytics workspace used by Azure Container Apps environment diagnostics."
  type        = string
  default     = "PerGB2018"
}

variable "log_analytics_retention_days" {
  description = "Retention period in days for the Log Analytics workspace."
  type        = number
  default     = 30
}

variable "backend_image_repository" {
  description = "Repository name for the backend image stored in Azure Container Registry."
  type        = string
  default     = "sushi-train-backend"
}

variable "backend_image_tag" {
  description = "Image tag for the backend container image in Azure Container Registry."
  type        = string
  default     = "dev-latest"
}

variable "backend_container_cpu" {
  description = "CPU allocation for the backend Azure Container App."
  type        = number
  default     = 0.5
}

variable "backend_container_memory" {
  description = "Memory allocation for the backend Azure Container App."
  type        = string
  default     = "1Gi"
}

variable "backend_min_replicas" {
  description = "Minimum number of replicas for the backend Azure Container App."
  type        = number
  default     = 1
}

variable "backend_max_replicas" {
  description = "Maximum number of replicas for the backend Azure Container App."
  type        = number
  default     = 1
}

variable "backend_ingress_external_enabled" {
  description = "Whether the backend Azure Container App ingress is publicly reachable."
  type        = bool
  default     = true
}

variable "backend_target_port" {
  description = "Container port exposed by the backend Azure Container App."
  type        = number
  default     = 8080
}

variable "backend_swagger_enabled" {
  description = "Whether Swagger UI and API docs are enabled for the backend Azure runtime. Keep false by default."
  type        = bool
  default     = false
}

variable "backend_database_user_override" {
  description = "Optional override for the backend database username used at runtime. Leave empty to use the PostgreSQL administrator login."
  type        = string
  default     = ""
}

variable "frontend_image_repository" {
  description = "Repository name for the frontend image stored in Azure Container Registry."
  type        = string
  default     = "sushi-train-frontend"
}

variable "frontend_image_tag" {
  description = "Image tag for the frontend container image in Azure Container Registry."
  type        = string
  default     = "dev-latest"
}

variable "frontend_container_cpu" {
  description = "CPU allocation for the frontend Azure Container App."
  type        = number
  default     = 0.25
}

variable "frontend_container_memory" {
  description = "Memory allocation for the frontend Azure Container App."
  type        = string
  default     = "0.5Gi"
}

variable "frontend_min_replicas" {
  description = "Minimum number of replicas for the frontend Azure Container App."
  type        = number
  default     = 1
}

variable "frontend_max_replicas" {
  description = "Maximum number of replicas for the frontend Azure Container App."
  type        = number
  default     = 1
}

variable "frontend_ingress_external_enabled" {
  description = "Whether the frontend Azure Container App ingress is publicly reachable."
  type        = bool
  default     = true
}

variable "frontend_target_port" {
  description = "Container port exposed by the frontend Azure Container App."
  type        = number
  default     = 80
}

variable "frontend_api_upstream_scheme" {
  description = "URL scheme used by the frontend runtime proxy when forwarding /api requests to the backend."
  type        = string
  default     = "https"

  validation {
    condition     = contains(["http", "https"], var.frontend_api_upstream_scheme)
    error_message = "frontend_api_upstream_scheme must be either http or https."
  }
}

variable "tags" {
  description = "Additional tags applied to supported Azure resources."
  type        = map(string)
  default     = {}
}