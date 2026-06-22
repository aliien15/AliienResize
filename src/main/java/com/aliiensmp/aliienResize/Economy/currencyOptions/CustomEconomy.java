package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CustomEconomy implements CurrencyProvider {

    private final String id;
    private final String balancePlaceholder;
    private final String withdrawCommand;
    private final String suffix;

    /**
     * Creates a PlaceholderAPI-driven custom currency adapter.
     *
     * @param id configured custom currency id
     * @param balancePlaceholder placeholder used to resolve the player's balance
     * @param withdrawCommand console command used to withdraw funds
     */
    public CustomEconomy(String id, String balancePlaceholder, String withdrawCommand, String suffix) {
        this.id = id;
        this.balancePlaceholder = balancePlaceholder;
        this.withdrawCommand = withdrawCommand;
        this.suffix = suffix;
    }

    @Override
    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;

        try {
            String parsed = PlaceholderAPI.setPlaceholders(player, this.balancePlaceholder);
            String normalizedBalance = parsed.replace(",", "").replace(".", "").trim();
            double balance = Double.parseDouble(normalizedBalance);
            return balance >= amount;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        String amountString = (amount % 1 == 0) ? String.valueOf((int) amount) : String.valueOf(amount);
        String finalCommand = this.withdrawCommand
                .replace("%player%", player.getName())
                .replace("%amount%", amountString);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        return true;
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
