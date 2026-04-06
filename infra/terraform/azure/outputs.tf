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