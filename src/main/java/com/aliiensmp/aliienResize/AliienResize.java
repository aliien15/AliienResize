package com.aliiensmp.aliienResize;

import co.aikar.commands.PaperCommandManager;
import com.aliiensmp.aliienResize.Config.Confirmation;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.aliienResize.Economy.CurrencyManager;
import com.aliiensmp.aliienResize.Hooks.PapiExpansion;
import com.aliiensmp.aliienResize.Hooks.VaultExpansion;
import com.aliiensmp.aliienResize.Utils.ResizeUtils;
import com.aliiensmp.core.AliienCore;
import com.aliiensmp.core.config.ConfigManager;
import com.aliiensmp.core.utils.ColorUtils;
import com.aliiensmp.core.utils.MessageUtils;
import com.aliiensmp.core.utils.updatechecker.UpdateChecker;
import com.aliiensmp.core.utils.updatechecker.UpdateNotifyListener;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.classloader.URLClassLoaderWrapper;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;
import com.aliiensmp.aliienResize.Commands.PlayerCommands;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.URLClassLoader;
import java.util.logging.Level;

public final class AliienResize extends JavaPlugin {

    // messages.yml
    private YamlDocument messagesFile;
    private Messages messages;

    // sizes.yml
    private YamlDocument sizesFile;
    private Sizes sizes;

    // main-menu.yml
    private YamlDocument mainMenuFile;

    // settings.yml
    private YamlDocument settingsFile;
    private Settings settings;

    // confirmation-menu.yml
    private YamlDocument confirmationMenuFile;
    private Confirmation confirmationMenu;

    private ResizeUtils resizeUtils;
    private CurrencyManager currencyManager;

    private VaultExpansion vaultExpansion;

    private static final String GIST = "https://gist.githubusercontent.com/aliien15/ecb083083130349214c79c53f73913fa/raw/AliienResize-version.txt";

    @Override
    public void onLoad() {
        File librariesFolder = new File(getDataFolder().getParentFile(), "AliienCore");

        DependencyManager dependencyManager = new DependencyManager(
                librariesFolder,
                URLClassLoaderWrapper.wrap((URLClassLoader) getClassLoader())
        );

        dependencyManager.repository(Repository.mavenCentral());
        dependencyManager.repository(Repository.maven("https://jitpack.io"));

        dependencyManager.dependency("com.zaxxer:HikariCP:5.1.0");
        dependencyManager.dependency("com.mysql:mysql-connector-j:9.6.0");
        dependencyManager.dependency("org.xerial:sqlite-jdbc:3.45.1.0");
        dependencyManager.dependency("dev.dejvokep:boosted-yaml:1.3.7");

        dependencyManager.relocate(new Relocation(
                "com{}zaxxer{}hikari".replace("{}", "."),
                "com.aliiensmp.core.lib.hikari"
        ));

        dependencyManager.load();
    }

    @Override
    public void onEnable() {
        AliienCore.init(this);

        if (!loadConfigurations()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupCommands();

        this.resizeUtils = new ResizeUtils();
        vaultExpansion = new VaultExpansion(this);
        setupUpdateChecker();
        setupPapiHook();
        setupBstats();

        getLogger().info("AliienResize enabled successfully!");
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

        // Tab completions
        commandManager.getCommandCompletions().registerCompletion("resize_ids", c -> Sizes.SIZES_BY_ID.keySet());

        commandManager.getCommandCompletions().registerCompletion("accessible_resize_ids", c -> {
            Player player = c.getPlayer();
            if (player == null) return java.util.List.of(); // Prevent console errors

            return Sizes.SIZES_BY_ID.values().stream()
                    .filter(node -> player.hasPermission(node.permission()))
                    .map(SizeNode::id)
                    .toList();

        });

        commandManager.registerCommand(new PlayerCommands(this));
    }

    private void setupBstats() {
        Metrics metrics = new Metrics(this, 31229);
    }

    private boolean loadConfigurations() {
        try {
            messagesFile = ConfigManager.loadConfig(this, "messages.yml");
            messages = new Messages();
            ConfigManager.bindConfig(messagesFile, messages);

            sizesFile = ConfigManager.loadConfig(this, "sizes.yml");
            mainMenuFile = ConfigManager.loadConfig(this, "main-menu.yml");

            sizes = new Sizes(this);
            sizes.loadFromConfigs(sizesFile, mainMenuFile);

            settingsFile = ConfigManager.loadConfig(this, "settings.yml");
            settings = new Settings();
            ConfigManager.bindConfig(settingsFile, settings);
            settings.loadDynamicData(settingsFile);

            currencyManager = new CurrencyManager(this);
            currencyManager.loadCurrencies();

            confirmationMenuFile = ConfigManager.loadConfig(this, "confirmation-menu.yml");
            confirmationMenu = new Confirmation();
            ConfigManager.bindConfig(confirmationMenuFile, "confirmation-menu.yml");
            confirmationMenu.loadFromConfig(confirmationMenuFile);

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

    public void reloadConfigurations(Player sender) {
        CompletableFuture.runAsync(() -> {
            boolean success = loadConfigurations();

            sender.getScheduler().run(this, scheduledTask -> {
                if (success) {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_SUCCESS);
                } else {
                    MessageUtils.send(sender, Messages.PREFIX, Messages.RELOAD_FAIL);
                }
            }, null);
        });
    }

    public CurrencyManager getCurrencyManager() { return currencyManager; }
    public VaultExpansion getVaultExpansion() { return vaultExpansion; }
    public ResizeUtils getResizeUtils() { return resizeUtils; }
}