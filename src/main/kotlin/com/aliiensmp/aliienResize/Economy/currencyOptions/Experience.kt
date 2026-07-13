package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import org.bukkit.entity.Player
import kotlin.math.ceil

class Experience(override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean get() = true

    private fun getRequiredLevels(amount: Double): Int {
        return maxOf(0, ceil(amount).toInt())
    }

    override fun hasBalance(player: Player, amount: Double): Boolean {
        return player.level >= getRequiredLevels(amount)
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        player.level -= getRequiredLevels(amount)
        return true
    }
}