package com.aliiensmp.aliienResize.economy.currencyOptions

import com.aliiensmp.aliienResize.economy.CurrencyProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI

class ExcellentEconomy(private val currencyName: String, override val suffix: String) : CurrencyProvider {

    private val api: ExcellentEconomyAPI? = if (Bukkit.getPluginManager().getPlugin("ExcellentEconomy") != null) {
        Bukkit.getServicesManager().getRegistration(ExcellentEconomyAPI::class.java)?.provider
    } else null

    override val isValid: Boolean
        get() = api?.hasCurrency(currencyName) == true

    override fun hasBalance(player: Player, amount: Double): Boolean {
        return api?.let {
            it.hasCurrency(currencyName) && it.getBalance(player, currencyName) >= amount
        } ?: false
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false
        return api?.withdraw(player, currencyName, amount) ?: false 
    }
}