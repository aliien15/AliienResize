package com.aliiensmp.aliienResize.Economy

import org.bukkit.entity.Player

interface CurrencyProvider {

    /**
     * @return `true` if the economy plugin is enabled, `false` otherwise
     */
    val isValid: Boolean

    /**
     * @return the suffix used to display prices in names/lores
     */
    val suffix: String

    /**
     * @param player player to check the balance
     * @param amount amount to check in the player's balance
     * @return `true` if [player] has [amount] in their balance, `false` otherwise
     */
    fun hasBalance(player: Player, amount: Double): Boolean

    /**
     * @param player player to withdraw the money
     * @param amount money to withdraw
     * @return `true` if the transaction is successful
     * * Requires hasBalance(player, amount)
     */
    fun withdraw(player: Player, amount: Double): Boolean
}