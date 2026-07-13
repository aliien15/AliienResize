package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import su.nightexpress.coinsengine.api.CoinsEngineAPI

class CoinsEngine(private val currencyName: String, override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = Bukkit.getPluginManager().getPlugin("CoinsEngine") != null &&
                CoinsEngineAPI.getCurrency(currencyName) != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false

        val ceCurrency = CoinsEngineAPI.getCurrency(currencyName) ?: return false

        return CoinsEngineAPI.getBalance(player, ceCurrency) >= amount
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        val ceCurrency = CoinsEngineAPI.getCurrency(currencyName) ?: return false
        CoinsEngineAPI.removeBalance(player, ceCurrency, amount)
        return true
    }
}