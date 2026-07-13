package com.aliiensmp.aliienResize.Economy.currencyOptions

import com.aliiensmp.aliienResize.Economy.CurrencyProvider
import me.TechsCode.UltraEconomy.UltraEconomy
import me.TechsCode.UltraEconomy.objects.Account
import me.TechsCode.UltraEconomy.objects.Currency
import org.bukkit.entity.Player

class UltraEconomies(private val currencyName: String, override val suffix: String) : CurrencyProvider {

    private val ueCurrency: Currency?
        get() = UltraEconomy.getAPI()?.currencies?.name(currencyName)?.orElse(null)

    private fun getAccount(player: Player): Account? {
        return UltraEconomy.getAPI()?.accounts?.uuid(player.uniqueId)?.orElse(null)
    }

    override val isValid: Boolean
        get() = ueCurrency != null

    override fun hasBalance(player: Player, amount: Double): Boolean {
        val currency = ueCurrency ?: return false
        val account = getAccount(player) ?: return false

        return account.getBalance(currency).sum >= amount
    }

    override fun withdraw(player: Player, amount: Double): Boolean {
        if (!hasBalance(player, amount)) return false

        val currency = ueCurrency ?: return false
        val account = getAccount(player) ?: return false

        account.removeBalance(currency, amount)
        return true
    }
}