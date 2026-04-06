locals {
  resource_group_name = "${var.resource_name_prefix}-${var.environment_name}-rg"
  log_analytics_workspace_name = "${var.resource_name_prefix}-${var.environment_name}-law"
  container_app_environment_name = "${var.resource_name_prefix}-${var.environment_name}-cae"
  backend_container_app_name = "${var.resource_name_prefix}-${var.environment_name}-backend"
  backend_acr_pull_identity_name = "${var.resource_name_prefix}-${var.environment_name}-backend-pull-id"
  frontend_container_app_name = "${var.resource_name_prefix}-${var.environment_name}-frontend"
  frontend_acr_pull_identity_name = "${var.resource_name_prefix}-${var.environment_name}-frontend-pull-id"
  backend_database_user = var.backend_database_user_override != "" ? var.backend_database_user_override : var.postgresql_administrator_login

  default_tags = {
    project     = var.project_name
    environment = var.environment_name
    managed-by  = "terraform"
  }

  common_tags = merge(local.default_tags, var.tags)
}