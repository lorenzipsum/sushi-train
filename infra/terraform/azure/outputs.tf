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