package com.aliiensmp.aliienResize.Listeners

import com.aliiensmp.aliienResize.AliienResize
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerConnectionListener(private val plugin: AliienResize) : Listener {

    companion object {
        @JvmField
        val cache = ConcurrentHashMap<UUID, Double>()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId

        plugin.databaseProvider.loadScale(uuid).thenAccept { scale ->
            val finalScale = scale ?: 1.0
            cache[uuid] = finalScale

            player.scheduler.run(plugin, { _ ->
                player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = finalScale
            }, null)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId

        val cachedScale = cache.remove(uuid)
        if (cachedScale != null) {
            plugin.databaseProvider.saveScale(uuid, cachedScale)
        }
    }
}