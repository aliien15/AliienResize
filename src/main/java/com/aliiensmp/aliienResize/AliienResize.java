package com.aliiensmp.aliienResize;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import com.aliiensmp.aliienResize.Commands.AdminCommands;
import com.aliiensmp.aliienResize.Config.Confirmation;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.aliienResize.database.DatabaseProvider;
import com.aliiensmp.aliienResize.database.options.MySQL;
import com.aliiensmp.aliienResize.database.options.MariaDB;
import com.aliiensmp.aliienResize.database.options.None;
import com.aliiensmp.aliienResize.economy.CurrencyManager;
import com.aliiensmp.aliienResize.Hooks.PapiExpansion;
import com.aliiensmp.aliienResize.hooks.VaultExpansion;
import com.aliiensmp.aliienResize.listeners.PlayerConnectionListener;
import com.aliiensmp.aliienResize.listeners.WorldListener;
import com.aliiensmp.core.AliienCore;
import com.aliiensmp.core.config.ConfigManager;
import com.aliiensmp.core.lib.boostedyaml.YamlDocument;
import com.aliiensmp.core.utils.ColorUtils;
import com.aliiensmp.core.utils.MessageUtils;
import com.aliiensmp.core.utils.updatechecker.UpdateChecker;
import com.aliiensmp.core.utils.updatechecker.UpdateNotifyListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.aliiensmp.aliienResize.Commands.PlayerCommands;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class AliienResize extends JavaPlugin {

    private YamlDocument messagesFile;
    private YamlDocument sizesFile;
    private YamlDocument mainMenuFile;
    private YamlDocument settingsFile;
    private YamlDocument confirmationMenuFile;

    private CurrencyManager currencyManager;

    private VaultExpansion vaultExpansion;

    private DatabaseProvider databaseProvider;

    private static final String GIST = "https://gist.githubusercontent.com/aliien15/ecb083083130349214c79c53f73913fa/raw/AliienResize-version.txt";

    @Override
    public void onEnable() {
        AliienCore.init(this);

        if (!loadConfigurations()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupDatabase();
        setupCommands();
        setupListeners();

        vaultExpansion = new VaultExpansion(this);

        setupUpdateChecker();
        setupPapiHook();
        setupBstats();

        getLogger().info("AliienResize enabled successfully!");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("AliienResize disabled!");
    }

    private void setupPapiHook() {
        if (Settings.HOOK_PAPI && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExpansion(this).register();
        }
    }

    private void setupDatabase() {
        databaseProvider = switch (settingsFile.getString("database.type").toUpperCase(Locale.ROOT)) {
            case "MYSQL" -> {
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
                );
                yield new MySQL();
            }
            case "MARIADB" -> {
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
                );
                yield new MariaDB();
            }
            case "NONE" -> new None();
            default -> {
                getLogger().warning("Invalid database type detected, therefore defaulting to NONE. If you are sure that you have typed your storage type correctly and this message is showing up, then this is a bug and must be reported!");
                yield new None();
            }
        };
    }

    private void setupCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        // Prefix
        commandManager.getLocales().addMessage(
                Locale.ENGLISH,
                MessageKeys.ERROR_PREFIX,
                Messages.PREFIX
        );

        // No perms msg
        commandManager.getLocales().addMessage(
                Locale.ENGLISH,
                MessageKeys.PERMISSION_DENIED,
                Messages.NO_PERM
        );

        // Tab completions
        commandManager.getCommandCompletions().registerCompletion("resize_ids", c -> Sizes.SIZES_BY_ID.keySet());

        commandManager.getCommandCompletions().registerCompletion("accessible_resize_ids", c -> {
            Player player = c.getPlayer();
            if (player == null) return List.of(); // Prevent console errors

            return Sizes.SIZES_BY_ID.values().stream()
                    .filter(node -> player.hasPermission(node.permission()))
                    .map(SizeNode::id)
                    .toList();

        });

        // Context resolver
        commandManager.getCommandContexts().registerContext(SizeNode.class, c -> {
            String sizeId = c.popFirstArg(); // Grabs the string the player typed
            Player player = c.getPlayer();

            SizeNode sizeNode = Sizes.SIZES_BY_ID.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(sizeId))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

            if (sizeNode == null) {
                if (Settings.SOUNDS_ENABLED && player != null) Settings.ERROR_SOUND.play(player);
                throw new InvalidCommandArgument(Messages.NULL_ID, false);
            }

            return sizeNode;
        });

        commandManager.registerCommand(new PlayerCommands(this));
        commandManager.registerCommand(new AdminCommands(this));
    }

    private void setupBstats() {
        Metrics metrics = new Metrics(this, 31229);
    }

    /**
     * Loads every managed YAML file used by the plugin.
     *
     * @return {@code true} when all configuration files are available and loaded successfully
     */
    private boolean loadConfigurations() {
        try {
            messagesFile = ConfigManager.loadConfig(this, "messages.yml");
            ConfigManager.bindConfig(messagesFile, Messages.class);

            mainMenuFile = ConfigManager.loadConfig(this, "main-menu.yml");

            settingsFile = ConfigManager.loadConfig(this, "settings.yml");
            ConfigManager.bindConfig(settingsFile, Settings.class);
            Settings.loadDynamicData(settingsFile);

            currencyManager = new CurrencyManager(this);
            currencyManager.loadCurrencies();

            sizesFile = ConfigManager.loadConfig(this, "sizes.yml");
            Sizes.loadFromConfigs(sizesFile, mainMenuFile, this);

            confirmationMenuFile = ConfigManager.loadConfig(this, "confirmation-menu.yml");
            ConfigManager.bindConfig(confirmationMenuFile, Confirmation.class);
            Confirmation.loadFromConfig(confirmationMenuFile, this);

            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load or update configuration files!", e);
            return false;
        }
    }

    /**
     * Starts the version check and join notification flow when enabled.
     */
    private void setupUpdateChecker() {
        if (!Settings.CHECK_FOR_UPDATES) return;

        new UpdateChecker(this, GIST).getVersion(version -> {
            if (this.getPluginMeta().getVersion().equals(version)) {
                getLogger().info("AliienResize is up to date!");
            } else {
                getLogger().warning("A new update is available for AliienResize!");
            }
        });

        getServer().getPluginManager().registerEvents(
                new UpdateNotifyListener(
                        this,
                        GIST,
                        "aliien.resize.admin.version-notify",
                        () -> ColorUtils.color(Messages.NEW_VERSION)
                ),
                this
        );
    }

    public void reloadConfigurations(CommandSender sender) {
        currencyManager.loadCurrencies();

        CompletableFuture.runAsync(() -> {
            boolean success = loadConfigurations();

            Runnable task = () -> {
                if (success) {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_SUCCESS);
                } else {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_FAIL);
                }
            };

            if (sender instanceof Player player) {
                player.getScheduler().run(this, scheduledTask -> task.run(), null);
            } else {
                getServer().getGlobalRegionScheduler().run(this, scheduledTask -> task.run());
            }
        });
    }

    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public VaultExpansion getVaultExpansion() { return vaultExpansion; }
    public DatabaseProvider getDatabaseProvider() { return databaseProvider; }

    public YamlDocument getSettingsFile() {
        return settingsFile;
    }
}