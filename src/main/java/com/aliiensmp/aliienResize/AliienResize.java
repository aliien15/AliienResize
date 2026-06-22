package com.aliiensmp.aliienResize;

import co.aikar.commands.MessageKeys;
import co.aikar.commands.PaperCommandManager;
import com.aliiensmp.aliienResize.Commands.AdminCommands;
import com.aliiensmp.aliienResize.Config.Confirmation;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.aliienResize.Economy.CurrencyManager;
import com.aliiensmp.aliienResize.Hooks.PapiExpansion;
import com.aliiensmp.aliienResize.Hooks.VaultExpansion;
import com.aliiensmp.aliienResize.Listeners.WorldListener;
import com.aliiensmp.aliienResize.Utils.ResizeUtils;
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
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class AliienResize extends JavaPlugin {

    private YamlDocument messagesFile;
    private YamlDocument sizesFile;
    private YamlDocument mainMenuFile;
    private YamlDocument settingsFile;
    private YamlDocument confirmationMenuFile;

    private ResizeUtils resizeUtils;
    private CurrencyManager currencyManager;

    private VaultExpansion vaultExpansion;

    private static final String GIST = "https://gist.githubusercontent.com/aliien15/ecb083083130349214c79c53f73913fa/raw/AliienResize-version.txt";

    @Override
    public void onEnable() {
        AliienCore.init(this);

        if (!loadConfigurations()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupCommands();
        setupListeners();

        resizeUtils = new ResizeUtils();
        vaultExpansion = new VaultExpansion(this);

        setupUpdateChecker();
        setupPapiHook();
        setupBstats();

        getLogger().info("AliienResize enabled successfully!");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
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

    private void setupCommands() {
        PaperCommandManager commandManager = new co.aikar.commands.PaperCommandManager(this);

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
    public ResizeUtils getResizeUtils() { return resizeUtils; }

    public YamlDocument getSettingsFile() {
        return settingsFile;
    }
}