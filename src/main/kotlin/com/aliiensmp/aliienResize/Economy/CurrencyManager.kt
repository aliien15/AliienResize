package com.aliiensmp.aliienResize.Economy

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.Economy.currencyOptions.*
import org.bukkit.Bukkit

class CurrencyManager(private val plugin: AliienResize) {

    private val settings = plugin.settingsFile
    private val activeCurrencies = mutableMapOf<String, CurrencyProvider>()

    companion object {
        private const val DEFAULT_CURRENCY = "VAULT"
    }

    fun loadCurrencies() {
        activeCurrencies.clear()

        registerStandaloneCurrency(
            "hooks.vault.enabled",
            DEFAULT_CURRENCY,
            "Vault Economy",
            { Vault(plugin, getConfiguredSuffix("hooks.vault.suffix", "$")) },
            "Vault Economy enabled in config, but no economy provider (like Essentials) was found!"
        )

        registerStandaloneCurrency(
            "hooks.player-points.enabled",
            "PLAYERPOINTS",
            "PlayerPoints",
            { PlayerPoint(getConfiguredSuffix("hooks.player-points.suffix", "Points")) },
            "PlayerPoints enabled in config, but plugin not found!"
        )

        registerStandaloneCurrency(
            "hooks.ax-quest-board.enabled",
            "AXQUESTBOARD",
            "AxQuestBoard",
            { AxQuestBoard(getConfiguredSuffix("hooks.ax-quest-board.suffix", "Quest Points")) },
            "AxQuestBoard enabled in config, but plugin not found!"
        )

        if (isEnabled("hooks.experience.enabled")) {
            registerCurrency("EXPERIENCE", Experience(getConfiguredSuffix("hooks.experience.suffix", "Experience")))
            plugin.logger.info("Experience currency enabled!")
        }

        registerNamedCurrencies(
            "hooks.ultra-economy.enabled",
            "UltraEconomy",
            "hooks.ultra-economy.currencies",
            "ULTRAECONOMY_",
            null,
            ::UltraEconomies,
            "UltraEconomy",
            "is in settings.yml but doesn't exist in UltraEconomy!"
        )

        registerNamedCurrencies(
            "hooks.coins-engine.enabled",
            "CoinsEngine",
            "hooks.coins-engine.currencies",
            "COINSENGINE_",
            null,
            ::CoinsEngine,
            "CoinsEngine",
            "is in settings.yml but doesn't exist in CoinsEngine!"
        )

        registerNamedCurrencies(
            "hooks.excellent-economy.enabled",
            "ExcellentEconomy",
            "hooks.excellent-economy.currencies",
            "EXCELLENTECONOMY_",
            null,
            ::ExcellentEconomy,
            "ExcellentEconomy",
            "is in settings.yml but doesn't exist in ExcellentEconomy!"
        )

        registerNamedCurrencies(
            "hooks.eco-bits.enabled",
            "EcoBits",
            "hooks.eco-bits.currencies",
            "ECOBITS_",
            "ECOBITS",
            ::EcoBits,
            "EcoBits",
            "is in settings.yml but doesn't exist in EcoBits!"
        )

        registerStandaloneCurrency(
            "hooks.royale-economy.enabled",
            "ROYALEECONOMY",
            "RoyaleEco",
            { RoyaleEco(getConfiguredSuffix("hooks.royale-economy.suffix", "RoyaleEco")) },
            "RoyaleEco enabled in config, but plugin not found!"
        )

        registerNamedCurrencies(
            "hooks.redis-economy.enabled",
            "RedisEconomy",
            "hooks.redis-economy.currencies",
            "REDISECONOMY_",
            null,
            ::RedisEconomy,
            "RedisEconomy",
            "is in settings.yml but doesn't exist in the plugin!"
        )

        registerCustomCurrencies()
    }

    fun getCurrency(identifier: String?): CurrencyProvider? {
        if (identifier.isNullOrBlank()) return null

        val normalizedIdentifier = normalizeIdentifier(identifier)

        activeCurrencies[normalizedIdentifier]?.let { return it }

        if (normalizedIdentifier.startsWith("ECOBITS_")) {
            return activeCurrencies[normalizedIdentifier.replace("ECOBITS_", "ECOBITS")]
        }

        return null
    }

    fun getSuffix(identifier: String?): String {
        if (identifier.isNullOrBlank()) return ""

        getCurrency(identifier)?.let { return it.suffix }

        return when (val normalizedIdentifier = normalizeIdentifier(identifier)) {
            DEFAULT_CURRENCY -> getConfiguredSuffix("hooks.vault.suffix", "$")
            "EXPERIENCE" -> getConfiguredSuffix("hooks.experience.suffix", "Experience")
            "PLAYERPOINTS" -> getConfiguredSuffix("hooks.player-points.suffix", "Points")
            "AXQUESTBOARD" -> getConfiguredSuffix("hooks.ax-quest-board.suffix", "Quest Points")
            "ROYALEECONOMY" -> getConfiguredSuffix("hooks.royale-economy.suffix", "RoyaleEco")
            else -> getDynamicSuffix(identifier, normalizedIdentifier)
        }
    }

    private inline fun registerStandaloneCurrency(
        enabledPath: String,
        identifier: String,
        successDisplayName: String,
        providerSupplier: () -> CurrencyProvider,
        invalidMessage: String
    ) {
        if (!isEnabled(enabledPath)) return

        val provider = providerSupplier()
        if (!provider.isValid) {
            plugin.logger.warning(invalidMessage)
            return
        }

        registerCurrency(identifier, provider)
        plugin.logger.info("Successfully hooked into $successDisplayName!")
    }

    private inline fun registerNamedCurrencies(
        enabledPath: String,
        pluginName: String,
        currenciesPath: String,
        identifierPrefix: String,
        legacyIdentifierPrefix: String?,
        providerFactory: (String, String) -> CurrencyProvider,
        serviceName: String,
        invalidCurrencySuffix: String
    ) {
        if (!isEnabled(enabledPath)) return

        if (!isPluginEnabled(pluginName)) {
            plugin.logger.warning("$serviceName enabled in config, but plugin not found!")
            return
        }

        val configuredCurrencies = settings.getStringList(currenciesPath)
        if (configuredCurrencies.isEmpty()) return

        for (configuredCurrency in configuredCurrencies) {
            if (configuredCurrency.isNullOrBlank()) continue

            val suffix = getNamedCurrencySuffix(currenciesPath, configuredCurrency)
            val provider = providerFactory(configuredCurrency, suffix)

            if (!provider.isValid) {
                plugin.logger.warning("$serviceName currency '$configuredCurrency' $invalidCurrencySuffix")
                continue
            }

            registerCurrency("$identifierPrefix$configuredCurrency", provider)

            legacyIdentifierPrefix?.let { registerCurrencyAlias("$it$configuredCurrency", provider) }

            plugin.logger.info("Successfully hooked into $serviceName currency: $configuredCurrency")
        }
    }

    private fun registerCustomCurrencies() {
        if (!isEnabled("hooks.custom-economy.enabled")) return

        if (!isPluginEnabled("PlaceholderAPI")) {
            plugin.logger.warning("Custom economies are enabled, but PlaceholderAPI is not installed!")
            return
        }

        val customSection = settings.getSection("hooks.custom-economy.currencies") ?: return

        for (customId in customSection.getRoutesAsStrings(false)) {
            val placeholder = customSection.getString("$customId.balance-placeholder", "")
            val command = customSection.getString("$customId.withdraw-command", "")
            val suffix = customSection.getString("$customId.suffix", customId) ?: customId

            if (placeholder.isNullOrBlank() || command.isNullOrBlank()) {
                plugin.logger.warning("Custom currency '$customId' is missing its placeholder or command!")
                continue
            }

            val provider = CustomEconomy(customId, placeholder, command, suffix)
            registerCurrency("CUSTOM_$customId", provider)
            plugin.logger.info("Successfully hooked into Custom Currency: $customId")
        }
    }

    private fun registerCurrency(identifier: String, provider: CurrencyProvider) {
        val normalizedIdentifier = normalizeIdentifier(identifier)
        val previous = activeCurrencies.put(normalizedIdentifier, provider)

        if (previous != null && previous != provider) {
            plugin.logger.warning("Currency identifier '$normalizedIdentifier' was already registered and has been replaced.")
        }
    }

    private fun registerCurrencyAlias(identifier: String, provider: CurrencyProvider) {
        activeCurrencies.putIfAbsent(normalizeIdentifier(identifier), provider)
    }

    private fun isEnabled(path: String) = settings.getBoolean(path, false)

    private fun getConfiguredSuffix(path: String, fallback: String) = settings.getString(path, fallback) ?: fallback

    private fun isPluginEnabled(pluginName: String) = Bukkit.getPluginManager().getPlugin(pluginName) != null

    private fun normalizeIdentifier(identifier: String) = identifier.uppercase()

    private fun getDynamicSuffix(identifier: String, normalizedIdentifier: String): String {
        if (normalizedIdentifier.startsWith("CUSTOM_")) {
            val customId = identifier.substring("CUSTOM_".length)
            return getConfiguredSuffix("hooks.custom-economy.currencies.$customId.suffix", customId)
        }

        return configuredCurrencyName(identifier, normalizedIdentifier) ?: normalizedIdentifier
    }

    private fun getNamedCurrencySuffix(currenciesPath: String, configuredCurrency: String): String {
        val path = currenciesPath.replace(".currencies", ".currency-suffixes.") + configuredCurrency
        return getConfiguredSuffix(path, configuredCurrency)
    }

    private fun configuredCurrencyName(identifier: String, normalizedIdentifier: String): String? {
        val prefixes = arrayOf("ULTRAECONOMY_", "COINSENGINE_", "EXCELLENTECONOMY_", "ECOBITS_", "REDISECONOMY_")

        return prefixes.firstOrNull { normalizedIdentifier.startsWith(it) }?.let { identifier.substring(it.length) }
    }
}