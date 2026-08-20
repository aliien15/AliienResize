package com.aliiensmp.aliienResize.data

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.core.utils.DebugUtils
import java.util.UUID
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

    fun loadPlayer(uuid: UUID) {
        DebugUtils.send("Fetching database scale for UUID: $uuid")
        plugin.databaseProvider.loadScale(uuid).thenAccept { loadedScale ->
            val scale = loadedScale ?: 1.0
            DebugUtils.send("Database returned scale $scale for UUID: $uuid. Applying to player...")

            val data = getPlayerData(uuid)
            data.applyScale(plugin, scale)
        }.exceptionally { ex ->
            DebugUtils.send(Level.SEVERE, "Failed to load scale for UUID: $uuid - ${ex.message}")
            null
        }
    }

    fun savePlayer(uuid: UUID) {
        val data = cache.remove(uuid)
        if (data != null) {
            DebugUtils.send("Saving scale ${data.scale} to database for UUID: $uuid")
            plugin.databaseProvider.saveScale(uuid, data.scale)
        } else {
            DebugUtils.send("Attempted to save player $uuid but they were not in the cache.")
        }
    }

    fun clearCache() {
        DebugUtils.send("Clearing PlayerDataService cache. (${cache.size} entries removed)")
        cache.clear()
    }
}