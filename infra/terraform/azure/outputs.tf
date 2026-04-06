output "resource_group_name" {
  description = "Name of the Azure Resource Group for Sushi-Train."
  value       = azurerm_resource_group.main.name
}

output "resource_group_location" {
  description = "Azure region used by the Sushi-Train Resource Group."
  value       = azurerm_resource_group.main.location
}