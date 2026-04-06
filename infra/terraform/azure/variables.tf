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

variable "tags" {
  description = "Additional tags applied to supported Azure resources."
  type        = map(string)
  default     = {}
}