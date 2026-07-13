package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import org.bukkit.entity.Player

class Vault(private val plugin: AliienResize, override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = plugin.vaultExpansion != null && plugin.vaultExpansion.hasEconomy()

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false
        return plugin.vaultExpansion.economy.has(player, amount)
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false
        return plugin.vaultExpansion.economy.withdrawPlayer(player, amount).transactionSuccess()
    }
}