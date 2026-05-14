package com.aliiensmp.aliienResize.Economy;

import org.bukkit.entity.Player;

/**
 * Defines a withdraw-only currency hook used for color purchases.
 */
public interface CurrencyProvider {

    /**
     * @return {@code true} if the economy plugin is enabled, {@code false} otherwise
     */
    boolean isValid();

    /**
     * @param player player to check the balance
     * @param amount amount to check in the player's balance
     * @return {@code true} if {@code player} has {@code amount} in their balance, {@code false} otherwise
     */
    boolean hasBalance(Player player, double amount);

    /**
     * @param player player to withdraw the money
     * @param amount money to withdraw
     * @return {@code true} if the transaction is successful, {@code}
     * @requires hasBalance(player, amount)
     */
    boolean withdraw(Player player, double amount);
}
