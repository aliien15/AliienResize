package com.aliiensmp.aliienResize.economy.currencyOptions

import com.aliiensmp.aliienResize.economy.CurrencyProvider
import dev.unnm3d.rediseconomy.api.RedisEconomyAPI
import dev.unnm3d.rediseconomy.currency.Currency
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RedisEconomy(private val currencyName: String, override val suffix: String) : CurrencyProvider {

    private val currency: Currency? = if (Bukkit.getPluginManager().getPlugin("RedisEconomy") != null) {
        RedisEconomyAPI.getAPI()?.getCurrencyByName(currencyName)
    } else null

    override val isValid: Boolean
        get() = currency != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        return currency?.let { it.getBalance(player) >= amount } ?: false
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        val curr = currency ?: return false
        return curr.setPlayerBalance(player, curr.getBalance(player) - amount).transactionSuccess()
    }
}