locals {
  resource_group_name = "${var.resource_name_prefix}-${var.environment_name}-rg"

  default_tags = {
    project     = var.project_name
    environment = var.environment_name
    managed-by  = "terraform"
  }

  common_tags = merge(local.default_tags, var.tags)
}