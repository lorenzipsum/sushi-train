provider "azurerm" {
  resource_providers_to_register = [
    "Microsoft.App",
  ]

  features {}

  subscription_id = var.subscription_id
}