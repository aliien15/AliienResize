package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

public class PlayerPoint implements CurrencyProvider {

    private final PlayerPointsAPI playerPointsAPI;

    /**
     * Creates the PlayerPoints adapter.
     */
    public PlayerPoint() {
        PlayerPoints instance = PlayerPoints.getInstance();
        this.playerPointsAPI = instance != null ? instance.getAPI() : null;
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
        return playerPointsAPI != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (!isValid()) return false;
        return playerPointsAPI.look(player.getUniqueId()) >= getRequiredPoints(amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!hasBalance(player, amount)) return false;
        return playerPointsAPI.take(player.getUniqueId(), getRequiredPoints(amount));
    }
}
