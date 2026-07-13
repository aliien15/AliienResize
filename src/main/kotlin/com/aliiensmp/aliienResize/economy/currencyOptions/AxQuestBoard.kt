package com.aliiensmp.aliienResize.economy.currencyOptions

import com.aliiensmp.aliienResize.economy.CurrencyProvider
import com.artillexstudios.axquestboard.api.AxQuestBoardAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.math.ceil

class AxQuestBoard(override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = Bukkit.getPluginManager().getPlugin("AxQuestBoard") != null

    /**
     * Rounds a configured price up to the minimum whole number of points required.
     */
    private fun getRequiredPoints(amount: Double): Int {
        return maxOf(0, ceil(amount).toInt())
    }

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false

        val balance = AxQuestBoardAPI.getQuestPoints(player.uniqueId).join()
        return balance >= getRequiredPoints(amount)
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        return AxQuestBoardAPI.takeQuestPoints(player.uniqueId, getRequiredPoints(amount)).join()
    }
}