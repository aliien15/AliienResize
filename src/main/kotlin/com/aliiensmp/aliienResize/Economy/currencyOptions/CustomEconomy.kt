package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CustomEconomy(private val id: String, private val balancePlaceholder: String, private val withdrawCommand: String, override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false

        val parsed = PlaceholderAPI.setPlaceholders(player, balancePlaceholder)
            .replace(".", "")
            .replace(",", "")
            .trim()

        val balance = parsed.toDoubleOrNull() ?: return false
        return balance >= amount
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        val amountString = if (amount % 1 == 0.0) amount.toInt().toString() else amount.toString()
        val finalCommand = withdrawCommand
            .replace("%player%", player.name)
            .replace("%amount%", amountString)

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)
        return true
    }
}