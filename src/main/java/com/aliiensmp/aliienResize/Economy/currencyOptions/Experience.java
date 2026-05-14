package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import org.bukkit.entity.Player;

public class Experience implements CurrencyProvider {

    /**
     * Rounds a configured price up to the minimum whole number of levels required.
     *
     * @param amount configured price
     * @return required player levels
     */
    private int getRequiredLevels(double amount) {
        return Math.max(0, (int) Math.ceil(amount));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        return player.getLevel() >= getRequiredLevels(amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;

        player.setLevel(player.getLevel() - getRequiredLevels(amount));
        return true;
    }
}
