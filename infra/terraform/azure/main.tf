resource "azurerm_resource_group" "main" {
  name     = local.resource_group_name
  location = var.location
  tags     = local.common_tags
}

resource "azurerm_container_registry" "main" {
  name                = var.container_registry_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = var.container_registry_sku
  admin_enabled       = var.container_registry_admin_enabled
  tags                = local.common_tags
}

resource "azurerm_postgresql_flexible_server" "main" {
  name                          = var.postgresql_server_name
  resource_group_name           = azurerm_resource_group.main.name
  location                      = azurerm_resource_group.main.location
  version                       = var.postgresql_version
  administrator_login           = var.postgresql_administrator_login
  administrator_password        = var.postgresql_administrator_password
  sku_name                      = var.postgresql_sku_name
  storage_mb                    = var.postgresql_storage_mb
  backup_retention_days         = var.postgresql_backup_retention_days
  geo_redundant_backup_enabled  = false
  public_network_access_enabled = var.postgresql_public_network_access_enabled
  tags                          = local.common_tags

  # Azure may assign or later report the effective primary zone even when we do
  # not manage zoning explicitly. Ignore that drift for this low-complexity setup.
  lifecycle {
    ignore_changes = [zone]
  }
}

resource "azurerm_postgresql_flexible_server_database" "main" {
  name      = var.postgresql_database_name
  server_id = azurerm_postgresql_flexible_server.main.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure_services" {
  count = var.postgresql_public_network_access_enabled && var.postgresql_allow_azure_services ? 1 : 0

  name             = "allow-azure-services"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

resource "azurerm_postgresql_flexible_server_configuration" "allowed_extensions" {
  name      = "azure.extensions"
  server_id = azurerm_postgresql_flexible_server.main.id
  value     = join(",", var.postgresql_allowed_extensions)
}

resource "azurerm_log_analytics_workspace" "main" {
  name                = local.log_analytics_workspace_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  sku                 = var.log_analytics_workspace_sku
  retention_in_days   = var.log_analytics_retention_days
  tags                = local.common_tags
}

resource "azurerm_container_app_environment" "main" {
  name                       = local.container_app_environment_name
  location                   = azurerm_resource_group.main.location
  resource_group_name        = azurerm_resource_group.main.name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  tags                       = local.common_tags
}

resource "azurerm_user_assigned_identity" "backend_acr_pull" {
  name                = local.backend_acr_pull_identity_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  tags                = local.common_tags
}

resource "azurerm_role_assignment" "backend_acr_pull" {
  scope                            = azurerm_container_registry.main.id
  role_definition_name             = "AcrPull"
  principal_id                     = azurerm_user_assigned_identity.backend_acr_pull.principal_id
  principal_type                   = "ServicePrincipal"
  skip_service_principal_aad_check = true
}

resource "azurerm_container_app" "backend" {
  name                         = local.backend_container_app_name
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"
  tags                         = local.common_tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.backend_acr_pull.id]
  }

  registry {
    server   = azurerm_container_registry.main.login_server
    identity = azurerm_user_assigned_identity.backend_acr_pull.id
  }

  secret {
    name  = "db-password"
    value = var.postgresql_administrator_password
  }

  ingress {
    allow_insecure_connections = false
    external_enabled           = var.backend_ingress_external_enabled
    target_port                = var.backend_target_port
    transport                  = "http"

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  template {
    min_replicas = var.backend_min_replicas
    max_replicas = var.backend_max_replicas

    container {
      name   = "backend"
      image  = "${azurerm_container_registry.main.login_server}/${var.backend_image_repository}:${var.backend_image_tag}"
      cpu    = var.backend_container_cpu
      memory = var.backend_container_memory

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "azure"
      }

      env {
        name  = "APP_ENVIRONMENT"
        value = "azure"
      }

      env {
        name  = "DB_HOST"
        value = azurerm_postgresql_flexible_server.main.fqdn
      }

      env {
        name  = "DB_PORT"
        value = "5432"
      }

      env {
        name  = "DB_NAME"
        value = var.postgresql_database_name
      }

      env {
        name  = "DB_USER"
        value = local.backend_database_user
      }

      env {
        name        = "DB_PASSWORD"
        secret_name = "db-password"
      }

      env {
        name  = "DB_SSL_MODE"
        value = "require"
      }

      env {
        name  = "SPRINGDOC_API_DOCS_ENABLED"
        value = var.backend_swagger_enabled ? "true" : "false"
      }

      env {
        name  = "SPRINGDOC_SWAGGER_UI_ENABLED"
        value = var.backend_swagger_enabled ? "true" : "false"
      }

      liveness_probe {
        transport               = "HTTP"
        port                    = var.backend_target_port
        path                    = "/actuator/health"
        interval_seconds        = 15
        timeout                 = 5
        failure_count_threshold = 3
      }

      readiness_probe {
        transport               = "HTTP"
        port                    = var.backend_target_port
        path                    = "/actuator/health"
        interval_seconds        = 15
        timeout                 = 5
        failure_count_threshold = 3
        success_count_threshold = 1
      }

      startup_probe {
        transport               = "HTTP"
        port                    = var.backend_target_port
        path                    = "/actuator/health"
        interval_seconds        = 10
        timeout                 = 5
        failure_count_threshold = 30
      }
    }
  }

  depends_on = [
    azurerm_role_assignment.backend_acr_pull,
    azurerm_postgresql_flexible_server_configuration.allowed_extensions,
  ]
}