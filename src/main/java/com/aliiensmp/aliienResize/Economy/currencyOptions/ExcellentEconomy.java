package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;

public class ExcellentEconomy implements CurrencyProvider {

    private ExcellentEconomyAPI api = null;
    private final String currencyName;
    private final String suffix;

    /**
     * Creates an ExcellentEconomy adapter for one configured currency.
     *
     * @param currencyName ExcellentEconomy currency id
     */
    public ExcellentEconomy(String currencyName, String suffix) {
        this.currencyName = currencyName;
        this.suffix = suffix;

        if (Bukkit.getPluginManager().getPlugin("ExcellentEconomy") != null) {
            RegisteredServiceProvider<ExcellentEconomyAPI> rsp = Bukkit.getServicesManager().getRegistration(ExcellentEconomyAPI.class);
            if (rsp != null) {
                this.api = rsp.getProvider();
            }
        }
    }

    @Override
    public boolean isValid() {
        return api != null && api.hasCurrency(this.currencyName);
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;
        return this.api.getBalance(player, this.currencyName) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isValid()) return false;
        return this.api.withdraw(player, this.currencyName, amount);
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
