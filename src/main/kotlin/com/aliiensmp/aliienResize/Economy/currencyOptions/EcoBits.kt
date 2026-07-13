package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import com.willfp.ecobits.currencies.Currencies
import com.willfp.ecobits.currencies.adjustBalance
import com.willfp.ecobits.currencies.getBalance
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal

class EcoBits(private val currencyName: String, override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = Bukkit.getPluginManager().getPlugin("EcoBits") != null && Currencies.getByID(currencyName) != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false

        val bitCurrency = Currencies.getByID(currencyName) ?: return false

        return player.getBalance(bitCurrency).toDouble() >= amount
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        val bitCurrency = Currencies.getByID(currencyName) ?: return false

        player.adjustBalance(bitCurrency, BigDecimal.valueOf(-amount))
        return true
    }
}