package com.aliiensmp.aliienResize.Economy;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Economy.currencyOptions.*;
import com.aliiensmp.core.lib.boostedyaml.YamlDocument;
import com.aliiensmp.core.lib.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CurrencyManager {

    private static final String DEFAULT_CURRENCY = "VAULT";

    private final AliienResize plugin;
    private final YamlDocument settings;

    private final Map<String, CurrencyProvider> activeCurrencies = new LinkedHashMap<>();

    /**
     * Creates the currency registry for all supported economy hooks.
     *
     * @param plugin owning plugin instance
     */
    public CurrencyManager(AliienResize plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettingsFile();
    }

    /**
     * Reloads every enabled economy hook and rebuilds the active currency map.
     */
    public void loadCurrencies() {
        activeCurrencies.clear();

        registerStandaloneCurrency(
                "hooks.vault.enabled",
                DEFAULT_CURRENCY,
                "Vault Economy",
                () -> new Vault(plugin, getConfiguredSuffix("hooks.vault.suffix", "$")),
                "Vault Economy enabled in config, but no economy provider (like Essentials) was found!"
        );

        registerStandaloneCurrency(
                "hooks.player-points.enabled",
                "PLAYERPOINTS",
                "PlayerPoints",
                () -> new PlayerPoint(getConfiguredSuffix("hooks.player-points.suffix", "Points")),
                "PlayerPoints enabled in config, but plugin not found!"
        );

        registerStandaloneCurrency(
                "hooks.ax-quest-board.enabled",
                "AXQUESTBOARD",
                "AxQuestBoard",
                () -> new AxQuestBoard(getConfiguredSuffix("hooks.ax-quest-board.suffix", "Quest Points")),
                "AxQuestBoard enabled in config, but plugin not found!"
        );

        if (isEnabled("hooks.experience.enabled")) {
            registerCurrency("EXPERIENCE", new Experience(getConfiguredSuffix("hooks.experience.suffix", "Experience")));
            plugin.getLogger().info("Experience currency enabled!");
        }

        registerNamedCurrencies(
                "hooks.ultra-economy.enabled",
                "UltraEconomy",
                "hooks.ultra-economy.currencies",
                "ULTRAECONOMY_",
                null,
                UltraEconomies::new,
                "UltraEconomy",
                "is in settings.yml but doesn't exist in UltraEconomy!"
        );

        registerNamedCurrencies(
                "hooks.coins-engine.enabled",
                "CoinsEngine",
                "hooks.coins-engine.currencies",
                "COINSENGINE_",
                null,
                CoinsEngine::new,
                "CoinsEngine",
                "is in settings.yml but doesn't exist in CoinsEngine!"
        );

        registerNamedCurrencies(
                "hooks.excellent-economy.enabled",
                "ExcellentEconomy",
                "hooks.excellent-economy.currencies",
                "EXCELLENTECONOMY_",
                null,
                ExcellentEconomy::new,
                "ExcellentEconomy",
                "is in settings.yml but doesn't exist in ExcellentEconomy!"
        );

        registerNamedCurrencies(
                "hooks.eco-bits.enabled",
                "EcoBits",
                "hooks.eco-bits.currencies",
                "ECOBITS_",
                "ECOBITS",
                EcoBits::new,
                "EcoBits",
                "is in settings.yml but doesn't exist in EcoBits!"
        );

        registerStandaloneCurrency(
                "hooks.royale-economy.enabled",
                "ROYALEECONOMY",
                "RoyaleEco",
                () -> new RoyaleEco(getConfiguredSuffix("hooks.royale-economy.suffix", "RoyaleEco")),
                "RoyaleEco enabled in config, but plugin not found!"
        );

        registerNamedCurrencies(
                "hooks.redis-economy.enabled",
                "RedisEconomy",
                "hooks.redis-economy.currencies",
                "REDISECONOMY_",
                null,
                RedisEconomy::new,
                "RedisEconomy",
                "is in settings.yml but doesn't exist in the plugin!"
        );

        registerCustomCurrencies();
    }

    /**
     * Resolves a currency provider by configured identifier.
     *
     * @param identifier configured currency id
     * @return matching provider, or {@code null} when the identifier is unknown
     */
    public CurrencyProvider getCurrency(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        String normalizedIdentifier = normalizeIdentifier(identifier);
        CurrencyProvider provider = activeCurrencies.get(normalizedIdentifier);
        if (provider != null) {
            return provider;
        }

        if (normalizedIdentifier.startsWith("ECOBITS_")) {
            return activeCurrencies.get(normalizedIdentifier.replace("ECOBITS_", "ECOBITS"));
        }

        return null;
    }

    /**
     * Resolves the display suffix for a configured currency identifier.
     *
     * @param identifier configured currency id
     * @return suffix for price placeholders
     */
    public String getSuffix(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "";
        }

        CurrencyProvider provider = getCurrency(identifier);
        if (provider != null) {
            return provider.suffix();
        }

        String normalizedIdentifier = normalizeIdentifier(identifier);
        return switch (normalizedIdentifier) {
            case DEFAULT_CURRENCY -> getConfiguredSuffix("hooks.vault.suffix", "$");
            case "EXPERIENCE" -> getConfiguredSuffix("hooks.experience.suffix", "Experience");
            case "PLAYERPOINTS" -> getConfiguredSuffix("hooks.player-points.suffix", "Points");
            case "AXQUESTBOARD" -> getConfiguredSuffix("hooks.ax-quest-board.suffix", "Quest Points");
            case "ROYALEECONOMY" -> getConfiguredSuffix("hooks.royale-economy.suffix", "RoyaleEco");
            default -> getDynamicSuffix(identifier, normalizedIdentifier);
        };
    }

    /**
     * Registers a single-provider currency hook guarded by a settings toggle.
     */
    private void registerStandaloneCurrency(
            String enabledPath,
            String identifier,
            String successDisplayName,
            Supplier<? extends CurrencyProvider> providerSupplier,
            String invalidMessage
    ) {
        if (!isEnabled(enabledPath)) {
            return;
        }

        CurrencyProvider provider = providerSupplier.get();
        if (!provider.isValid()) {
            plugin.getLogger().warning(invalidMessage);
            return;
        }

        registerCurrency(identifier, provider);
        plugin.getLogger().info("Successfully hooked into " + successDisplayName + "!");
    }

    /**
     * Registers one provider per configured external currency name.
     */
    private void registerNamedCurrencies(
            String enabledPath,
            String pluginName,
            String currenciesPath,
            String identifierPrefix,
            String legacyIdentifierPrefix,
            BiFunction<String, String, ? extends CurrencyProvider> providerFactory,
            String serviceName,
            String invalidCurrencySuffix
    ) {
        if (!isEnabled(enabledPath)) {
            return;
        }

        if (!isPluginEnabled(pluginName)) {
            plugin.getLogger().warning(serviceName + " enabled in config, but plugin not found!");
            return;
        }

        List<String> configuredCurrencies = settings.getStringList(currenciesPath);
        if (configuredCurrencies.isEmpty()) {
            return;
        }

        for (String configuredCurrency : configuredCurrencies) {
            if (configuredCurrency == null || configuredCurrency.isBlank()) {
                continue;
            }

            String suffix = getNamedCurrencySuffix(currenciesPath, configuredCurrency);
            CurrencyProvider provider = providerFactory.apply(configuredCurrency, suffix);
            if (!provider.isValid()) {
                plugin.getLogger().warning(serviceName + " currency '" + configuredCurrency + "' " + invalidCurrencySuffix);
                continue;
            }

            String canonicalIdentifier = identifierPrefix + configuredCurrency;
            registerCurrency(canonicalIdentifier, provider);

            if (legacyIdentifierPrefix != null) {
                registerCurrencyAlias(legacyIdentifierPrefix + configuredCurrency, provider);
            }

            plugin.getLogger().info("Successfully hooked into " + serviceName + " currency: " + configuredCurrency);
        }
    }

    /**
     * Registers PlaceholderAPI-backed custom economies from settings.
     */
    private void registerCustomCurrencies() {
        if (!isEnabled("hooks.custom-economy.enabled")) {
            return;
        }

        if (!isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().warning("Custom economies are enabled, but PlaceholderAPI is not installed!");
            return;
        }

        Section customSection = settings.getSection("hooks.custom-economy.currencies");
        if (customSection == null) {
            return;
        }

        for (String customId : customSection.getRoutesAsStrings(false)) {
            String placeholder = customSection.getString(customId + ".balance-placeholder", "");
            String command = customSection.getString(customId + ".withdraw-command", "");
            String suffix = customSection.getString(customId + ".suffix", customId);

            if (placeholder == null || placeholder.isBlank() || command == null || command.isBlank()) {
                plugin.getLogger().warning("Custom currency '" + customId + "' is missing its placeholder or command!");
                continue;
            }

            CurrencyProvider provider = new CustomEconomy(customId, placeholder, command, suffix == null ? customId : suffix);

            registerCurrency("CUSTOM_" + customId, provider);
            plugin.getLogger().info("Successfully hooked into Custom Currency: " + customId);
        }
    }

    /**
     * Stores a provider under its canonical identifier.
     */
    private void registerCurrency(String identifier, CurrencyProvider provider) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        CurrencyProvider previous = activeCurrencies.put(normalizedIdentifier, provider);

        if (previous != null && previous != provider) {
            plugin.getLogger().warning("Currency identifier '" + normalizedIdentifier + "' was already registered and has been replaced.");
        }
    }

    /**
     * Stores a backwards-compatible alias without replacing an existing registration.
     */
    private void registerCurrencyAlias(String identifier, CurrencyProvider provider) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        activeCurrencies.putIfAbsent(normalizedIdentifier, provider);
    }

    /**
     * Reads a boolean toggle from settings.
     */
    private boolean isEnabled(String path) {
        return settings.getBoolean(path, false);
    }

    private String getConfiguredSuffix(String path, String fallback) {
        String suffix = settings.getString(path, fallback);
        return suffix == null ? fallback : suffix;
    }

    private String getDynamicSuffix(String identifier, String normalizedIdentifier) {
        if (normalizedIdentifier.startsWith("CUSTOM_")) {
            String customId = identifier.substring("CUSTOM_".length());
            return getConfiguredSuffix("hooks.custom-economy.currencies." + customId + ".suffix", customId);
        }

        String configuredCurrency = configuredCurrencyName(identifier, normalizedIdentifier);
        return configuredCurrency == null ? normalizedIdentifier : configuredCurrency;
    }

    private String getNamedCurrencySuffix(String currenciesPath, String configuredCurrency) {
        String path = currenciesPath.replace(".currencies", ".currency-suffixes.") + configuredCurrency;
        return getConfiguredSuffix(path, configuredCurrency);
    }

    private String configuredCurrencyName(String identifier, String normalizedIdentifier) {
        String[] prefixes = {
                "ULTRAECONOMY_",
                "COINSENGINE_",
                "EXCELLENTECONOMY_",
                "ECOBITS_",
                "REDISECONOMY_"
        };

        for (String prefix : prefixes) {
            if (normalizedIdentifier.startsWith(prefix)) {
                return identifier.substring(prefix.length());
            }
        }

        return null;
    }

    /**
     * Checks whether an external plugin is currently loaded.
     */
    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    /**
     * Normalizes currency identifiers for map lookups.
     */
    private String normalizeIdentifier(String identifier) {
        return identifier.toUpperCase(Locale.ROOT);
    }
}
