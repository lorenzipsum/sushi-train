locals {
  default_tags = {
    project     = var.project_name
    environment = var.environment_name
    managed-by  = "terraform"
  }

  common_tags = merge(local.default_tags, var.tags)
}