package com.aliiensmp.aliienResize.Hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VaultExpansion {

    private final Plugin plugin;
    private Economy economy = null;
    private Permission permissions = null;

    /**
     * Creates the Vault integration wrapper and attempts to resolve its services.
     *
     * @param plugin owning plugin instance
     */
    public VaultExpansion(Plugin plugin) {
        this.plugin = plugin;

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            setupEconomy();
            setupPermissions();
        }
    }

    /**
     * Resolves the Vault economy service when it is available.
     */
    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
            plugin.getLogger().info("Successfully hooked into Vault Economy!");
        }
    }

    /**
     * Resolves the Vault permissions service when it is available.
     */
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            this.permissions = rsp.getProvider();
            plugin.getLogger().info("Successfully hooked into Vault Permissions!");
        }
    }

    /**
     * Returns the hooked Vault economy service.
     *
     * @return economy provider, or {@code null} when unavailable
     */
    public Economy getEconomy() { return economy; }

    /**
     * Returns the hooked Vault permissions service.
     *
     * @return permissions provider, or {@code null} when unavailable
     */
    public Permission getPermissions() { return permissions; }

    /**
     * Checks whether a Vault economy provider is available.
     *
     * @return {@code true} when economy support is available
     */
    public boolean hasEconomy() { return economy != null; }

    /**
     * Checks whether a Vault permissions provider is available.
     *
     * @return {@code true} when permissions support is available
     */
    public boolean hasPermissions() { return permissions != null; }
}
