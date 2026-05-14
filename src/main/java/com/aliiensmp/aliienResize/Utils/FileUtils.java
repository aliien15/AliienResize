package com.aliiensmp.aliienResize.Utils;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Records.PriceData;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;

public class FileUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final AliienResize plugin;
    private final ExecutorService writerExecutor;

    /**
     * Creates the asynchronous purchase logger.
     *
     * @param plugin owning plugin instance
     */
    public FileUtils(AliienResize plugin) {
        this.plugin = plugin;
        this.writerExecutor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, plugin.getName() + "-purchase-logger");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Queues a purchase entry for asynchronous file logging.
     */
    public void logPurchase(Player player, SizeNode sizeNode, PriceData priceData) {
        if (!Settings.LOGGING_ENABLED || player == null || sizeNode == null || priceData == null) {
            return;
        }

        String playerName = player.getName();
        UUID playerUuid = player.getUniqueId();
        String colorId = sizeNode.id();
        String currencyId = priceData.currency().toUpperCase(Locale.ROOT);
        String amountText = formatAmount(priceData.price());

        try {
            writerExecutor.execute(() -> writePurchaseLog(playerName, playerUuid, colorId, amountText, currencyId));
        } catch (RejectedExecutionException ignored) {
            // The plugin is shutting down
        }
    }

    /**
     * Stops the logging executor during plugin shutdown.
     */
    public void shutdown() {
        writerExecutor.shutdown();
    }

    /**
     * Writes a single purchase entry to the configured log file.
     */
    private void writePurchaseLog(String playerName, UUID playerUuid, String sizeId, String amountText, String currencyId) {
        try {
            Path dataFolder = plugin.getDataFolder().toPath();
            Files.createDirectories(dataFolder);

            Path logFile = dataFolder.resolve(Settings.LOGGING_FILE);
            Path parent = logFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("[")
                    .append(timestamp)
                    .append("] Player ")
                    .append(playerName)
                    .append(" (")
                    .append(playerUuid)
                    .append(") purchased size '")
                    .append(sizeId)
                    .append("' for ")
                    .append(amountText)
                    .append(" ")
                    .append(currencyId)
                    .append(".");

            Files.writeString(
                    logFile,
                    logMessage + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while writing to " + Settings.LOGGING_FILE + "!", e);
        }
    }

    /**
     * Formats a price amount without decimal noise for whole numbers.
     */
    private String formatAmount(double amount) {
        return (Math.rint(amount) == amount) ? Long.toString((long) amount) : Double.toString(amount);
    }
}
