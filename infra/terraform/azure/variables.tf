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

variable "tags" {
  description = "Additional tags applied to supported Azure resources."
  type        = map(string)
  default     = {}
}