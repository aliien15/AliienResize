package com.aliiensmp.aliienResize.data

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.core.utils.DebugUtils
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class PlayerDataService(private val plugin: AliienResize) {

    private val cache = ConcurrentHashMap<UUID, PlayerData>()

    fun getPlayerData(uuid: UUID): PlayerData {
        return cache.computeIfAbsent(uuid) {
            DebugUtils.send("Created new PlayerData instance for UUID: $uuid")
            PlayerData(it, 1.0)
        }
    }

    fun loadPlayer(player: Player) {
        val uuid = player.uniqueId
        val scheduler = player.scheduler

        DebugUtils.send("Fetching database scale for UUID: $uuid")
        plugin.databaseReady.thenCompose { ready ->
            if (ready) {
                plugin.databaseProvider.loadScale(uuid)
            } else {
                CompletableFuture.completedFuture<Double?>(null)
            }
        }.thenAccept { loadedScale ->
            val scale = loadedScale ?: 1.0

            scheduler.run(plugin, { _ ->
                DebugUtils.send("Database returned scale $scale for UUID: $uuid. Applying to player...")
                getPlayerData(uuid).applyScaleOnEntityThread(player, scale)
            }, null)
        }.exceptionally { ex ->
            DebugUtils.send(Level.SEVERE, "Failed to load scale for UUID: $uuid - ${ex.message}")
            null
        }
    }

    fun applyScale(player: Player, scale: Double, onSuccess: (() -> Unit)? = null) {
        val uuid = player.uniqueId

        player.scheduler.run(plugin, { _ ->
            getPlayerData(uuid).applyScaleOnEntityThread(player, scale, onSuccess)
        }, null)
    }

    fun resetScale(player: Player, onSuccess: (() -> Unit)? = null) {
        val uuid = player.uniqueId

        player.scheduler.run(plugin, { _ ->
            getPlayerData(uuid).resetScaleOnEntityThread(player, onSuccess)
        }, null)
    }

    fun savePlayer(uuid: UUID): CompletableFuture<Boolean> {
        val data = cache[uuid]
        if (data == null) {
            DebugUtils.send("Attempted to save player $uuid but they were not in the cache.")
            return CompletableFuture.completedFuture(true)
        }

        DebugUtils.send("Saving scale ${data.scale} to database for UUID: $uuid")
        return plugin.databaseReady.thenCompose { ready ->
            if (ready) {
                plugin.databaseProvider.saveScale(uuid, data.scale)
            } else {
                CompletableFuture.completedFuture(false)
            }
        }.thenApply { saved ->
            if (saved) {
                cache.remove(uuid, data)
            } else {
                DebugUtils.send(Level.WARNING, "Failed to save scale for UUID: $uuid")
            }

            saved
        }.exceptionally { ex ->
            DebugUtils.send(Level.SEVERE, "Failed to save scale for UUID: $uuid - ${ex.message}")
            false
        }
    }

    fun clearCache() {
        DebugUtils.send("Clearing PlayerDataService cache. (${cache.size} entries removed)")
        cache.clear()
    }
}