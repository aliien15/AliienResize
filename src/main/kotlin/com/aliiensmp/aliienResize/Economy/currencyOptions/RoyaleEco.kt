package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import me.qKing12.RoyaleEconomy.RoyaleEconomy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class RoyaleEco(override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = Bukkit.getPluginManager().getPlugin("RoyaleEconomy") != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false
        return RoyaleEconomy.apiHandler.balance.getBalance(player.uniqueId.toString()) >= amount
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false
        RoyaleEconomy.apiHandler.balance.removeBalance(player.uniqueId.toString(), amount)
        return true
    }
}