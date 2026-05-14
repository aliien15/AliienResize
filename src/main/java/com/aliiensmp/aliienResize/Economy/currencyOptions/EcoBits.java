package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import com.willfp.ecobits.currencies.Currencies;
import com.willfp.ecobits.currencies.CurrencyUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class EcoBits implements CurrencyProvider {

    private final String currencyName;

    /**
     * Creates an EcoBits currency adapter for one configured currency.
     *
     * @param currencyName EcoBits currency id
     */
    public EcoBits(String currencyName) {
        this.currencyName = currencyName;
    }

    @Override
    public boolean isValid() {
        if (Bukkit.getPluginManager().getPlugin("EcoBits") == null) return false;

        return Currencies.getByID(currencyName) != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;

        var bitCurrency = Currencies.getByID(currencyName);
        if (bitCurrency == null) return false;

        BigDecimal balance = CurrencyUtils.getBalance(player, bitCurrency);

        return balance.doubleValue() >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        var bitCurrency = Currencies.getByID(currencyName);
        if (bitCurrency == null) return false;

        CurrencyUtils.adjustBalance(player, bitCurrency, BigDecimal.valueOf(-amount));
        return true;
    }
}
