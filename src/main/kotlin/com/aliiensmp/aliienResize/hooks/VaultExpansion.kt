package com.aliiensmp.aliienResize.hooks

import com.aliiensmp.aliienResize.AliienResize
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit

class VaultExpansion(private val plugin: AliienResize) {

    private val isVaultLoaded: Boolean
        get() = Bukkit.getPluginManager().getPlugin("Vault") != null

    val economy: Economy? = if (isVaultLoaded) {
        plugin.server.servicesManager.getRegistration(Economy::class.java)?.provider?.also {
            plugin.logger.info("Successfully hooked into Vault Economy!")
        }
    } else null

    val permissions: Permission? = if (isVaultLoaded) {
        plugin.server.servicesManager.getRegistration(Permission::class.java)?.provider?.also {
            plugin.logger.info("Successfully hooked into Vault Permissions!")
        }
    } else null

    val hasEconomy: Boolean
        get() = economy != null

    val hasPermissions: Boolean
        get() = permissions != null
}