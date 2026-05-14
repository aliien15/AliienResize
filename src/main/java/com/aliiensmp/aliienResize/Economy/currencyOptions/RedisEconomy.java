package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import dev.unnm3d.rediseconomy.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RedisEconomy implements CurrencyProvider {

    private RedisEconomyAPI api;
    private Currency currency;

    /**
     * Creates a RedisEconomy adapter for one configured currency.
     *
     * @param currencyName RedisEconomy currency name
     */
    public RedisEconomy(String currencyName) {

        if (Bukkit.getPluginManager().getPlugin("RedisEconomy") != null) {
            this.api = RedisEconomyAPI.getAPI();

            if (this.api != null)
                this.currency = this.api.getCurrencyByName(currencyName);
        }
    }

    @Override
    public boolean isValid() {
        return this.currency != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;

        return this.currency.getBalance(player) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isValid()) return false;

        return this.currency.setPlayerBalance(player, this.currency.getBalance(player) - amount).transactionSuccess();
    }
}
