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
import com.aliiensmp.aliienResize.database.DatabaseProvider
import com.aliiensmp.aliienResize.database.options.MariaDB
import com.aliiensmp.aliienResize.database.options.MySQL
import com.aliiensmp.aliienResize.database.options.None
import com.aliiensmp.aliienResize.economy.CurrencyManager
import com.aliiensmp.aliienResize.hooks.PapiExpansion
import com.aliiensmp.aliienResize.hooks.VaultExpansion
import com.aliiensmp.aliienResize.listeners.PlayerConnectionListener
import com.aliiensmp.aliienResize.listeners.WorldListener
import com.aliiensmp.core.AliienCore
import com.aliiensmp.core.config.ConfigManager
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.utils.ColorUtils
import com.aliiensmp.core.utils.MessageUtils
import com.aliiensmp.core.utils.updatechecker.UpdateChecker
import com.aliiensmp.core.utils.updatechecker.UpdateNotifyListener
import org.bstats.bukkit.Metrics
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale
import java.util.concurrent.CompletableFuture
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

    companion object {
        private const val GIST = "https://gist.githubusercontent.com/aliien15/ecb083083130349214c79c53f73913fa/raw/AliienResize-version.txt"
    }

    override fun onEnable() {
        AliienCore.init(this)

        if (!loadConfigurations()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        setupDatabase()
        setupCommands()
        setupListeners()

        vaultExpansion = VaultExpansion(this)

        setupUpdateChecker()
        setupPapiHook()
        setupBstats()

        logger.info("AliienResize enabled successfully!")
    }

    override fun onDisable() {
        logger.info("AliienResize disabled!")
    }

    private fun setupListeners() {
        server.pluginManager.registerEvents(WorldListener(this), this)
        server.pluginManager.registerEvents(PlayerConnectionListener(this), this)
    }

    private fun setupPapiHook() {
        if (Settings.HOOK_PAPI && server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            PapiExpansion(this).register()
        }
    }

    private fun setupDatabase() {
        databaseProvider = when (settingsFile.getString("database.type").uppercase(Locale.ROOT)) {
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
            else -> {
                logger.warning("Invalid database type detected, therefore defaulting to NONE. If you are sure that you have typed your storage type correctly and this message is showing up, then this is a bug and must be reported!")
                None()
            }
        }
    }

    private fun setupCommands() {
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

            // Kotlin's gorgeous alternative to .stream().filter().findFirst().orElse(null)
            val sizeNode = Sizes.SIZES_BY_ID.values.firstOrNull { it.id.equals(sizeId, ignoreCase = true) }
                ?: run {
                    if (Settings.SOUNDS_ENABLED) player?.let { Settings.ERROR_SOUND?.play(it) }
                    throw InvalidCommandArgument(Messages.NULL_ID, false)
                }

            sizeNode
        }

        commandManager.registerCommand(PlayerCommands(this))
        commandManager.registerCommand(AdminCommands(this))
    }

    private fun setupBstats() {
        val metric = Metrics(this, 31229)
    }

    private fun loadConfigurations(): Boolean {
        return try {
            messagesFile = ConfigManager.loadConfig(this, "messages.yml")
            ConfigManager.bindConfig(messagesFile, Messages)

            mainMenuFile = ConfigManager.loadConfig(this, "main-menu.yml")

            settingsFile = ConfigManager.loadConfig(this, "settings.yml")
            ConfigManager.bindConfig(settingsFile, Settings)
            Settings.loadDynamicData(settingsFile)

            currencyManager = CurrencyManager(this)
            currencyManager.loadCurrencies()

            sizesFile = ConfigManager.loadConfig(this, "sizes.yml")
            Sizes.loadFromConfigs(sizesFile, mainMenuFile, this)

            confirmationMenuFile = ConfigManager.loadConfig(this, "confirmation-menu.yml")
            ConfigManager.bindConfig(confirmationMenuFile, Confirmation)
            Confirmation.loadFromConfig(confirmationMenuFile, this)

            true
        } catch (e: Exception) {
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
        currencyManager.loadCurrencies()

        CompletableFuture.runAsync {
            val success = loadConfigurations()

            val task = Runnable {
                if (success) {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_SUCCESS)
                } else {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_FAIL)
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