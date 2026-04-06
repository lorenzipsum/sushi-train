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

variable "tags" {
  description = "Additional tags applied to supported Azure resources."
  type        = map(string)
  default     = {}
}