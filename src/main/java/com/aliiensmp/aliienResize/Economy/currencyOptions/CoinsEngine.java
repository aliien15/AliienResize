package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;

public class CoinsEngine implements CurrencyProvider {

    private final String currencyName;
    private final String suffix;

    /**
     * Creates a CoinsEngine currency adapter for one configured currency.
     *
     * @param currencyName CoinsEngine currency id
     */
    public CoinsEngine(String currencyName, String suffix) {
        this.currencyName = currencyName;
        this.suffix = suffix;
    }

    @Override
    public boolean isValid() {
        if (Bukkit.getPluginManager().getPlugin("CoinsEngine") == null) return false;
        return CoinsEngineAPI.getCurrency(currencyName) != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;

        var ceCurrency = CoinsEngineAPI.getCurrency(currencyName);
        if (ceCurrency == null) return false;

        return CoinsEngineAPI.getBalance(player, ceCurrency) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        var ceCurrency = CoinsEngineAPI.getCurrency(currencyName);
        if (ceCurrency == null) return false;

        CoinsEngineAPI.removeBalance(player, ceCurrency, amount);
        return true;
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
