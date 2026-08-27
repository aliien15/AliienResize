package com.aliiensmp.aliienResize

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.PaperCommandManager
import com.aliiensmp.aliienResize.commands.AdminCommands
import com.aliiensmp.aliienResize.commands.PlayerCommands
import com.aliiensmp.aliienResize.config.Confirmation
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.Sizes
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.data.PlayerDataService
import com.aliiensmp.aliienResize.database.DatabaseProvider
import com.aliiensmp.aliienResize.database.options.H2
import com.aliiensmp.aliienResize.database.options.MariaDB
import com.aliiensmp.aliienResize.database.options.MySQL
import com.aliiensmp.aliienResize.database.options.None
import com.aliiensmp.aliienResize.database.options.SQLite
import com.aliiensmp.aliienResize.economy.CurrencyManager
import com.aliiensmp.aliienResize.hooks.PapiExpansion
import com.aliiensmp.aliienResize.hooks.VaultExpansion
import com.aliiensmp.aliienResize.listeners.PlayerConnectionListener
import com.aliiensmp.aliienResize.listeners.WorldListener
import com.aliiensmp.aliienResize.utils.FileUtils
import com.aliiensmp.core.AliienCore
import com.aliiensmp.core.config.ConfigManager
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.utils.ColorUtils
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
import com.aliiensmp.core.utils.updatechecker.UpdateChecker
import com.aliiensmp.core.utils.updatechecker.UpdateNotifyListener
import org.bstats.bukkit.Metrics
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class AliienResize : JavaPlugin() {

    private lateinit var messagesFile: YamlDocument
    private lateinit var sizesFile: YamlDocument
    private lateinit var mainMenuFile: YamlDocument
    private lateinit var confirmationMenuFile: YamlDocument

    lateinit var settingsFile: YamlDocument
        private set

    lateinit var currencyManager: CurrencyManager
        private set

    lateinit var vaultExpansion: VaultExpansion
        private set

    lateinit var databaseProvider: DatabaseProvider
        private set

    lateinit var databaseReady: CompletableFuture<Boolean>
        private set

    lateinit var playerDataService: PlayerDataService
        private set

    lateinit var fileUtils: FileUtils
        private set

    companion object {
        private const val GIST = "https://gist.githubusercontent.com/aliien15/ecb083083130349214c79c53f73913fa/raw/AliienResize-version.txt"
    }

    override fun onEnable() {
        AliienCore.init(this)
        DebugUtils.send("AliienCore initialized, starting AliienResize startup sequence.")

        vaultExpansion = VaultExpansion(this)
        if (!loadConfigurations()) {
            DebugUtils.send(Level.SEVERE, "Failed to load configurations. Disabling plugin.")
            server.pluginManager.disablePlugin(this)
            return
        }

        databaseReady = setupDatabase()

        playerDataService = PlayerDataService(this)
        DebugUtils.send("PlayerDataService initialized.")

        fileUtils = FileUtils(this)
        DebugUtils.send("FileUtils initialized.")

        setupCommands()
        setupListeners()

        setupUpdateChecker()
        setupPapiHook()
        setupBstats()

        logger.info("AliienResize enabled successfully!")
        DebugUtils.send("AliienResize startup sequence completed.")
    }

    override fun onDisable() {
        DebugUtils.send("AliienResize disable sequence initiated. Saving all online players...")
        if (::playerDataService.isInitialized) {
            val saveTasks = server.onlinePlayers.map { player ->
                playerDataService.savePlayer(player.uniqueId)
            }

            val allSaved = try {
                CompletableFuture.allOf(*saveTasks.toTypedArray()).get(5, TimeUnit.SECONDS)
                saveTasks.all { it.getNow(false) }
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Failed or timed out while saving player resize data during shutdown.", e)
                false
            }

            playerDataService.clearCache()
            if (allSaved) {
                DebugUtils.send("All online player data saved and cache cleared.")
            } else {
                DebugUtils.send(Level.WARNING, "Player data cache cleared after save flush failed or timed out.")
            }
        }

        if (::fileUtils.isInitialized) {
            fileUtils.shutdown()
        }

        logger.info("AliienResize disabled!")
    }

    private fun setupListeners() {
        DebugUtils.send("Registering Bukkit listeners.")
        server.pluginManager.registerEvents(WorldListener(this), this)
        server.pluginManager.registerEvents(PlayerConnectionListener(this, playerDataService), this)
    }

    private fun setupPapiHook() {
        if (Settings.HOOK_PAPI && server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            PapiExpansion(this).register()
            DebugUtils.send("PlaceholderAPI hook registered.")
        }
    }

    private fun setupDatabase(): CompletableFuture<Boolean> {
        val dbType = settingsFile.getString("database.type", "NONE").uppercase(Locale.ROOT)
        DebugUtils.send("Setting up database with provider type: $dbType")

        databaseProvider = when (dbType) {
            "MYSQL" -> {
                AliienCore.getDatabase().connectMySQL(
                    settingsFile.getString("database.settings.host", "localhost"),
                    settingsFile.getInt("database.settings.port", 3306),
                    settingsFile.getString("database.settings.database", "server"),
                    settingsFile.getString("database.settings.username", "root"),
                    settingsFile.getString("database.settings.password", "password"),
                    settingsFile.getInt("database.settings.advanced.max-pool-size", 10),
                    settingsFile.getInt("database.settings.advanced.min-idle", 10),
                    settingsFile.getLong("database.settings.advanced.connection-timeout", 10000L),
                    settingsFile.getLong("database.settings.advanced.max-lifetime", 1800000L)
                )
                MySQL()
            }
            "MARIADB" -> {
                AliienCore.getDatabase().connectMariaDB(
                    settingsFile.getString("database.settings.host", "localhost"),
                    settingsFile.getInt("database.settings.port", 3306),
                    settingsFile.getString("database.settings.database", "server"),
                    settingsFile.getString("database.settings.username", "root"),
                    settingsFile.getString("database.settings.password", "password"),
                    settingsFile.getInt("database.settings.advanced.max-pool-size", 10),
                    settingsFile.getInt("database.settings.advanced.min-idle", 10),
                    settingsFile.getLong("database.settings.advanced.connection-timeout", 10000L),
                    settingsFile.getLong("database.settings.advanced.max-lifetime", 1800000L)
                )
                MariaDB()
            }
            "NONE" -> None()
            "H2" -> {
                AliienCore.getDatabase().connectH2(this, "database")
                H2()
            }
            "SQLITE" -> {
                AliienCore.getDatabase().connectSQLite(this, "database")
                SQLite()
            }
            else -> {
                logger.warning("Invalid database type detected, therefore defaulting to NONE.")
                None()
            }
        }

        return databaseProvider.init()
            .thenApply { success ->
                if (success) {
                    DebugUtils.send("Database provider initialized successfully.")
                } else {
                    logger.severe("Database provider failed to initialize.")
                }

                success
            }.exceptionally { ex ->
                logger.log(Level.SEVERE, "Database provider failed to initialize.", ex)
                false
            }
    }

    private fun setupCommands() {
        DebugUtils.send("Setting up PaperCommandManager.")
        val commandManager = PaperCommandManager(this)

        commandManager.locales.addMessage(Locale.ENGLISH, MessageKeys.ERROR_PREFIX, Messages.PREFIX)
        commandManager.locales.addMessage(Locale.ENGLISH, MessageKeys.PERMISSION_DENIED, Messages.NO_PERM)

        commandManager.commandCompletions.registerCompletion("resize_ids") { Sizes.SIZES_BY_ID.keys }

        commandManager.commandCompletions.registerCompletion("accessible_resize_ids") { c ->
            val player = c.player ?: return@registerCompletion emptyList()
            Sizes.SIZES_BY_ID.values
                .filter { player.hasPermission(it.permission) }
                .map { it.id }
        }

        commandManager.commandContexts.registerContext(SizeNode::class.java) { c ->
            val sizeId = c.popFirstArg()
            val player = c.player

            val sizeNode = Sizes.SIZES_BY_ID[sizeId]
                ?: run {
                    if (Settings.SOUNDS_ENABLED) player?.let { Settings.ERROR_SOUND?.play(it) }
                    throw InvalidCommandArgument(Messages.NULL_ID, false)
                }

            sizeNode
        }

        commandManager.registerCommand(PlayerCommands(this))
        commandManager.registerCommand(AdminCommands(this))
        DebugUtils.send("Commands registered.")
    }

    private fun setupBstats() {
        val metric = Metrics(this, 31229)
        DebugUtils.send("bStats metrics initialized.")
    }

    private fun loadConfigurations(): Boolean {
        DebugUtils.send("Loading configuration files...")
        return try {
            messagesFile = ConfigManager.loadConfig(this, "messages.yml")
            ConfigManager.bindConfig(messagesFile, Messages)
            DebugUtils.send("Loading messages config file successfully.")

            mainMenuFile = ConfigManager.loadConfig(this, "main-menu.yml")
            ConfigManager.bindConfig(mainMenuFile, Sizes)
            DebugUtils.send("Loading main-menu config file successfully.")

            settingsFile = ConfigManager.loadConfig(this, "settings.yml")
            ConfigManager.bindConfig(settingsFile, Settings)
            Settings.loadDynamicData(settingsFile)
            DebugUtils.setDebug(Settings.DEBUG_MODE)
            DebugUtils.send("Loading settings config file successfully.")

            currencyManager = CurrencyManager(this)
            currencyManager.loadCurrencies()
            DebugUtils.send("Loading currencies successfully.")

            sizesFile = ConfigManager.loadConfig(this, "sizes.yml")
            Sizes.loadFromConfigs(sizesFile, mainMenuFile, this)
            DebugUtils.send("Loading sizes config file successfully.")

            confirmationMenuFile = ConfigManager.loadConfig(this, "confirmation-menu.yml")
            ConfigManager.bindConfig(confirmationMenuFile, Confirmation)
            Confirmation.loadFromConfig(confirmationMenuFile, this)
            DebugUtils.send("Loading confirmation-menu config file successfully.")

            DebugUtils.send("All configurations loaded and bound successfully.")
            true
        } catch (e: Exception) {
            DebugUtils.send(Level.SEVERE, "Configuration load failed: ${e.message}")
            logger.log(Level.SEVERE, "Failed to load or update configuration files!", e)
            false
        }
    }

    private fun setupUpdateChecker() {
        if (!Settings.CHECK_FOR_UPDATES) return

        UpdateChecker(this, GIST).getVersion { version ->
            if (this.pluginMeta.version == version) {
                logger.info("AliienResize is up to date!")
            } else {
                logger.warning("A new update is available for AliienResize!")
            }
        }

        server.pluginManager.registerEvents(
            UpdateNotifyListener(
                this,
                GIST,
                "aliien.resize.admin.version-notify"
            ) { ColorUtils.color(Messages.NEW_VERSION) },
            this
        )
    }

    fun reloadConfigurations(sender: CommandSender) {
        DebugUtils.send("Configuration reload triggered by ${sender.name}.")
        currencyManager.loadCurrencies()

        CompletableFuture.runAsync {
            val success = loadConfigurations()

            val task = Runnable {
                if (success) {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_SUCCESS)
                    DebugUtils.send("Configuration reload completed successfully.")
                } else {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_FAIL)
                    DebugUtils.send(Level.SEVERE, "Configuration reload failed.")
                }
            }

            if (sender is Player) {
                sender.scheduler.run(this, { _ -> task.run() }, null)
            } else {
                server.globalRegionScheduler.run(this) { _ -> task.run() }
            }
        }
    }
}