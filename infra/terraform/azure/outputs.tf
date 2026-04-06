output "resource_group_name" {
  description = "Name of the Azure Resource Group for Sushi-Train."
  value       = azurerm_resource_group.main.name
}

output "resource_group_location" {
  description = "Azure region used by the Sushi-Train Resource Group."
  value       = azurerm_resource_group.main.location
}

output "container_registry_id" {
  description = "Resource ID of the Azure Container Registry."
  value       = azurerm_container_registry.main.id
}

output "container_registry_name" {
  description = "Name of the Azure Container Registry."
  value       = azurerm_container_registry.main.name
}

output "container_registry_login_server" {
  description = "Login server hostname for the Azure Container Registry."
  value       = azurerm_container_registry.main.login_server
}

output "postgresql_server_id" {
  description = "Resource ID of the Azure Database for PostgreSQL Flexible Server."
  value       = azurerm_postgresql_flexible_server.main.id
}

output "postgresql_server_name" {
  description = "Name of the Azure Database for PostgreSQL Flexible Server."
  value       = azurerm_postgresql_flexible_server.main.name
}

output "postgresql_server_fqdn" {
  description = "Fully qualified domain name of the Azure Database for PostgreSQL Flexible Server."
  value       = azurerm_postgresql_flexible_server.main.fqdn
}

output "postgresql_database_name" {
  description = "Application database name created in Azure Database for PostgreSQL Flexible Server."
  value       = azurerm_postgresql_flexible_server_database.main.name
}

output "log_analytics_workspace_name" {
  description = "Name of the Log Analytics workspace used by the Azure Container Apps environment."
  value       = azurerm_log_analytics_workspace.main.name
}

output "container_app_environment_id" {
  description = "Resource ID of the Azure Container Apps environment."
  value       = azurerm_container_app_environment.main.id
}

output "container_app_environment_name" {
  description = "Name of the Azure Container Apps environment."
  value       = azurerm_container_app_environment.main.name
}

output "container_app_environment_default_domain" {
  description = "Default domain of the Azure Container Apps environment."
  value       = azurerm_container_app_environment.main.default_domain
}

output "container_app_environment_static_ip_address" {
  description = "Static public IP address of the Azure Container Apps environment, when assigned by Azure."
  value       = azurerm_container_app_environment.main.static_ip_address
}

output "backend_container_app_name" {
  description = "Name of the backend Azure Container App."
  value       = azurerm_container_app.backend.name
}

output "backend_container_app_latest_revision_fqdn" {
  description = "Latest revision FQDN of the backend Azure Container App."
  value       = azurerm_container_app.backend.latest_revision_fqdn
}

output "backend_container_app_url" {
  description = "Public URL of the backend Azure Container App when external ingress is enabled."
  value       = var.backend_ingress_external_enabled ? "https://${azurerm_container_app.backend.ingress[0].fqdn}" : null
}