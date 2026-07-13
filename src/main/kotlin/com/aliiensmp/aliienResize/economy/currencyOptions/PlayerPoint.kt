package com.aliiensmp.aliienResize.economy.currencyOptions

import com.aliiensmp.aliienResize.economy.CurrencyProvider
import org.black_ixx.playerpoints.PlayerPoints
import org.black_ixx.playerpoints.PlayerPointsAPI
import org.bukkit.entity.Player
import kotlin.math.ceil

class PlayerPoint(override val suffix: String) : CurrencyProvider {

    private val playerPointsAPI: PlayerPointsAPI?
        get() = PlayerPoints.getInstance()?.api

    override val isValid: Boolean
        get() = playerPointsAPI != null

    private fun getRequiredPoints(amount: Double): Int {
        return maxOf(0, ceil(amount).toInt())
    }

    override fun hasBalance(player: Player, amount: Double): Boolean {
        val api = playerPointsAPI ?: return false
        return api.look(player.uniqueId) >= getRequiredPoints(amount)
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false
        return playerPointsAPI?.take(player.uniqueId, getRequiredPoints(amount)) ?: false
    }
}