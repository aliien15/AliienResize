package com.aliiensmp.aliienResize.Economy;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Economy.currencyOptions.*;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CurrencyManager {

    private static final String DEFAULT_CURRENCY = "VAULT";

    private final AliienResize plugin;
    private final Map<String, CurrencyProvider> activeCurrencies = new LinkedHashMap<>();

    public CurrencyManager(AliienResize plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads every enabled economy hook and rebuilds the active currency map directly from RAM.
     */
    public void loadCurrencies() {
        activeCurrencies.clear();

        registerStandaloneCurrency(
                Settings.HOOK_VAULT,
                DEFAULT_CURRENCY,
                "Vault Economy",
                () -> new Vault(plugin),
                "Vault Economy enabled in config, but no economy provider (like Essentials) was found!"
        );

        registerStandaloneCurrency(
                Settings.HOOK_PLAYER_POINTS,
                "PLAYERPOINTS",
                "PlayerPoints",
                PlayerPoint::new,
                "PlayerPoints enabled in config, but plugin not found!"
        );

        if (Settings.HOOK_EXP) {
            registerCurrency("EXPERIENCE", new Experience());
            plugin.getLogger().info("Experience currency enabled!");
        }

        registerNamedCurrencies(
                Settings.HOOK_ULTRA_ECO,
                "UltraEconomy",
                Settings.ULTRA_ECO_CURRENCIES,
                "ULTRAECONOMY_",
                null,
                UltraEconomies::new,
                "UltraEconomy",
                "is in Settings.yml but doesn't exist in UltraEconomy!"
        );

        registerNamedCurrencies(
                Settings.HOOK_COINS_ENGINE,
                "CoinsEngine",
                Settings.COINS_ENGINE_CURRENCIES,
                "COINSENGINE_",
                null,
                CoinsEngine::new,
                "CoinsEngine",
                "is in Settings.yml but doesn't exist in CoinsEngine!"
        );

        registerNamedCurrencies(
                Settings.HOOK_EXCELLENT_ECO,
                "ExcellentEconomy",
                Settings.EXCELLENT_ECO_CURRENCIES,
                "EXCELLENTECONOMY_",
                null,
                ExcellentEconomy::new,
                "ExcellentEconomy",
                "is in Settings.yml but doesn't exist in ExcellentEconomy!"
        );

        registerNamedCurrencies(
                Settings.HOOK_ECO_BITS,
                "EcoBits",
                Settings.ECO_BITS_CURRENCIES,
                "ECOBITS_",
                "ECOBITS",
                EcoBits::new,
                "EcoBits",
                "is in Settings.yml but doesn't exist in EcoBits!"
        );

        registerStandaloneCurrency(
                Settings.HOOK_ROYALE_ECO,
                "ROYALEECONOMY",
                "RoyaleEco",
                RoyaleEco::new,
                "RoyaleEco enabled in config, but plugin not found!"
        );

        registerNamedCurrencies(
                Settings.HOOK_REDIS_ECO,
                "RedisEconomy",
                Settings.REDIS_ECO_CURRENCIES,
                "REDISECONOMY_",
                null,
                RedisEconomy::new,
                "RedisEconomy",
                "is in Settings.yml but doesn't exist in the plugin!"
        );

        registerCustomCurrencies();
    }

    public CurrencyProvider getCurrency(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;

        String normalizedIdentifier = normalizeIdentifier(identifier);
        CurrencyProvider provider = activeCurrencies.get(normalizedIdentifier);

        if (provider != null) return provider;

        if (normalizedIdentifier.startsWith("ECOBITS_")) {
            return activeCurrencies.get(normalizedIdentifier.replace("ECOBITS_", "ECOBITS"));
        }

        return null;
    }

    private void registerStandaloneCurrency(
            boolean isEnabled,
            String identifier,
            String successDisplayName,
            Supplier<? extends CurrencyProvider> providerSupplier,
            String invalidMessage
    ) {
        if (!isEnabled) return;

        CurrencyProvider provider = providerSupplier.get();
        if (!provider.isValid()) {
            plugin.getLogger().warning(invalidMessage);
            return;
        }

        registerCurrency(identifier, provider);
        plugin.getLogger().info("Successfully registered " + successDisplayName + " as a valid currency!");
    }

    private void registerNamedCurrencies(
            boolean isEnabled,
            String pluginName,
            List<String> configuredCurrencies,
            String identifierPrefix,
            String legacyIdentifierPrefix,
            Function<String, ? extends CurrencyProvider> providerFactory,
            String serviceName,
            String invalidCurrencySuffix
    ) {
        if (!isEnabled) return;

        if (!isPluginEnabled(pluginName)) {
            plugin.getLogger().warning(serviceName + " enabled in config, but plugin not found!");
            return;
        }

        if (configuredCurrencies == null || configuredCurrencies.isEmpty()) return;

        for (String configuredCurrency : configuredCurrencies) {
            if (configuredCurrency == null || configuredCurrency.isBlank()) continue;

            CurrencyProvider provider = providerFactory.apply(configuredCurrency);
            if (!provider.isValid()) {
                plugin.getLogger().warning(serviceName + " currency '" + configuredCurrency + "' " + invalidCurrencySuffix);
                continue;
            }

            String canonicalIdentifier = identifierPrefix + configuredCurrency;
            registerCurrency(canonicalIdentifier, provider);

            if (legacyIdentifierPrefix != null) {
                registerCurrencyAlias(legacyIdentifierPrefix + configuredCurrency, provider);
            }

            plugin.getLogger().info("Successfully registered " + serviceName + " as a valid currency: " + configuredCurrency);
        }
    }

    private void registerCustomCurrencies() {
        if (!Settings.HOOK_CUSTOM_ECO) return;

        if (!isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().warning("Custom economies are enabled, but PlaceholderAPI is not installed!");
            return;
        }

        Settings.CUSTOM_CURRENCIES.forEach((customId, currencyData) -> {
            String placeholder = currencyData.balancePlaceholder();
            String command = currencyData.withdrawCommand();

            if (placeholder == null || placeholder.isBlank() || command == null || command.isBlank()) {
                plugin.getLogger().warning("Custom currency '" + customId + "' is missing its placeholder or command!");
                return;
            }

            CurrencyProvider provider = new CustomEconomy(customId, placeholder, command);
            if (!provider.isValid()) {
                plugin.getLogger().warning("Custom economies are enabled, but PlaceholderAPI is not installed!");
                return;
            }

            registerCurrency("CUSTOM_" + customId, provider);
            plugin.getLogger().info("Successfully registered valid Custom Currency: " + customId);
        });
    }

    private void registerCurrency(String identifier, CurrencyProvider provider) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        CurrencyProvider previous = activeCurrencies.put(normalizedIdentifier, provider);

        if (previous != null && previous != provider) {
            plugin.getLogger().warning("Currency identifier '" + normalizedIdentifier + "' was already registered and has been replaced.");
        }
    }

    private void registerCurrencyAlias(String identifier, CurrencyProvider provider) {
        activeCurrencies.putIfAbsent(normalizeIdentifier(identifier), provider);
    }

    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.toUpperCase(Locale.ROOT);
    }
}