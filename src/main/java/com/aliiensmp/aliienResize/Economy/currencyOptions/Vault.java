package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import org.bukkit.entity.Player;

public class Vault implements CurrencyProvider {

    private final AliienResize plugin;
    private final String suffix;

    /**
     * Creates the Vault currency adapter.
     *
     * @param plugin owning plugin instance
     */
    public Vault(AliienResize plugin, String suffix) {
        this.plugin = plugin;
        this.suffix = suffix;
    }

    @Override
    public boolean isValid() {
        return plugin.getVaultExpansion() != null && plugin.getVaultExpansion().hasEconomy();
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;
        return plugin.getVaultExpansion().getEconomy().has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;
        return plugin.getVaultExpansion().getEconomy().withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
