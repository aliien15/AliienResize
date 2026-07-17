package com.aliiensmp.aliienResize.economy.currencyOptions

import AliienResize
import com.aliiensmp.aliienResize.economy.CurrencyProvider
import org.bukkit.entity.Player

class Vault(private val plugin: AliienResize, override val suffix: String) : CurrencyProvider {

    override val isValid: Boolean
        get() = plugin.vaultExpansion?.hasEconomy == true

    override fun hasBalance(player: Player, amount: Double): Boolean {
        if (!isValid) return false

        return plugin.vaultExpansion?.economy?.has(player, amount) == true
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        return plugin.vaultExpansion?.economy?.withdrawPlayer(player, amount)?.transactionSuccess() == true
    }
}