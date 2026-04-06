locals {
  resource_group_name = "${var.resource_name_prefix}-${var.environment_name}-rg"
  log_analytics_workspace_name = "${var.resource_name_prefix}-${var.environment_name}-law"
  container_app_environment_name = "${var.resource_name_prefix}-${var.environment_name}-cae"

  default_tags = {
    project     = var.project_name
    environment = var.environment_name
    managed-by  = "terraform"
  }

  common_tags = merge(local.default_tags, var.tags)
}