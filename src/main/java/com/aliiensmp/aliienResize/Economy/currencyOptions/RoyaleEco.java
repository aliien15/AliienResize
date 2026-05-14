package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import me.qKing12.RoyaleEconomy.RoyaleEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RoyaleEco implements CurrencyProvider {

    @Override
    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin("RoyaleEconomy") != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;
        return RoyaleEconomy.apiHandler.balance.getBalance(player.getUniqueId().toString()) >= amount;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        RoyaleEconomy.apiHandler.balance.removeBalance(player.getUniqueId().toString(), amount);
        return true;
    }
}
