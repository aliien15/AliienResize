package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import com.artillexstudios.axquestboard.api.AxQuestBoardAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AxQuestBoard implements CurrencyProvider {

    private final String suffix;

    /**
     * Creates the AxQuestBoard adapter.
     *
     * @param suffix suffix displayed beside prices
     */
    public AxQuestBoard(String suffix) {
        this.suffix = suffix;
    }

    /**
     * Rounds a configured price up to the minimum whole number of points required.
     *
     * @param amount configured price
     * @return required point amount
     */
    private int getRequiredPoints(double amount) {
        return Math.max(0, (int) Math.ceil(amount));
    }

    @Override
    public boolean isValid() {
        return Bukkit.getPluginManager().getPlugin("AxQuestBoard") != null;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;

        int playerBalance = AxQuestBoardAPI.getQuestPoints(player.getUniqueId()).join();
        return playerBalance >= getRequiredPoints(amount);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        return AxQuestBoardAPI.takeQuestPoints(player.getUniqueId(), getRequiredPoints(amount)).join();
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
