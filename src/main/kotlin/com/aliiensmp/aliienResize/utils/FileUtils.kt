package com.aliiensmp.aliienResize.utils

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.Config.Records.PriceData
import com.aliiensmp.aliienResize.Config.Records.SizeNode
import com.aliiensmp.aliienResize.Config.Settings
import org.bukkit.entity.Player
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.logging.Level

class FileUtils(private val plugin: AliienResize) {

    companion object {
        private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    }

    private val writerExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "${plugin.name}-purchase-logger").apply {
            isDaemon = true
        }
    }

    /**
     * Queues a purchase entry for asynchronous file logging.
     */
    fun logPurchase(player: Player?, sizeNode: SizeNode?, priceData: PriceData?) {
        if (!Settings.LOGGING_ENABLED || player == null || sizeNode == null || priceData == null) return

        val playerName = player.name
        val playerUuid = player.uniqueId
        val colorId = sizeNode.id
        val currencyId = priceData.currency.uppercase(Locale.ROOT)
        val amountText = formatAmount(priceData.price)

        try {
            writerExecutor.execute { writePurchaseLog(playerName, playerUuid, colorId, amountText, currencyId) }
        } catch (ignored: RejectedExecutionException) {
            // The plugin is shutting down
        }
    }

    /**
     * Stops the logging executor during plugin shutdown.
     */
    fun shutdown() {
        writerExecutor.shutdown()
    }

    /**
     * Writes a single purchase entry to the configured log file.
     */
    private fun writePurchaseLog(playerName: String, playerUuid: UUID, sizeId: String, amountText: String, currencyId: String) {
        try {
            val dataFolder = plugin.dataFolder.toPath()
            Files.createDirectories(dataFolder)

            val logFile = dataFolder.resolve(Settings.LOGGING_FILE)
            logFile.parent?.let { Files.createDirectories(it) }

            val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT)
            val logMessage = "[$timestamp] Player $playerName ($playerUuid) purchased size '$sizeId' for $amountText $currencyId."

            Files.writeString(
                logFile,
                logMessage + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "An error occurred while writing to ${Settings.LOGGING_FILE}!", e)
        }
    }

    /**
     * Formats a price amount without decimal noise for whole numbers.
     */
    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
    }
}