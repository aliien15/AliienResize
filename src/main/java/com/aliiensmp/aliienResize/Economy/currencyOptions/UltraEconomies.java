package com.aliiensmp.aliienResize.Economy.currencyOptions;

import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.objects.Account;
import me.TechsCode.UltraEconomy.objects.Currency;
import org.bukkit.entity.Player;

import java.util.Optional;

public class UltraEconomies implements CurrencyProvider {

    private final String currencyName;
    private final String suffix;

    /**
     * Creates an UltraEconomy adapter for one configured currency.
     *
     * @param currencyName UltraEconomy currency name
     */
    public UltraEconomies(String currencyName, String suffix) {
        this.currencyName = currencyName;
        this.suffix = suffix;
    }

    /**
     * Resolves the configured UltraEconomy currency, when available.
     *
     * @return optional currency definition
     */
    private Optional<Currency> getUECurrency() {
        if (UltraEconomy.getAPI() == null) return Optional.empty();
        return UltraEconomy.getAPI().getCurrencies().name(currencyName);
    }

    @Override
    public boolean isValid() {
        return getUECurrency().isPresent();
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        Optional<Currency> ueCurrencyOpt = getUECurrency();
        if (ueCurrencyOpt.isEmpty()) return false;

        Optional<Account> accountOpt = UltraEconomy.getAPI().getAccounts().uuid(player.getUniqueId());
        return accountOpt.filter(account -> account.getBalance(ueCurrencyOpt.get()).getSum() >= amount).isPresent();

    }

    @Override
    public boolean withdraw(Player player, double amount) {
        Optional<Currency> ueCurrencyOpt = getUECurrency();
        if (ueCurrencyOpt.isEmpty()) return false;

        Optional<Account> accountOpt = UltraEconomy.getAPI().getAccounts().uuid(player.getUniqueId());
        if (accountOpt.isEmpty()) return false;

        Currency ueCurrency = ueCurrencyOpt.get();
        Account account = accountOpt.get();

        if (account.getBalance(ueCurrency).getSum() < amount) return false;

        account.removeBalance(ueCurrency, amount);
        return true;
    }

    @Override
    public String suffix() {
        return suffix;
    }
}
