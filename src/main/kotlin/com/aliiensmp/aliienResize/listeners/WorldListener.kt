package com.aliiensmp.aliienResize.listeners

import AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class WorldListener(private val plugin: AliienResize) : Listener {

    @EventHandler
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        val currentWorld = player.world.name

        val isBlacklisted = Settings.BLACKLISTED_WORLDS.any {
            it.equals(currentWorld, ignoreCase = true)
        }

        if (!isBlacklisted || player.hasPermission("aliien.resize.bypass.worldblacklist")) {
            return
        }

        player.scheduler.run(plugin, { _ ->
            player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.0

            PlayerConnectionListener.cache[player.uniqueId] = 1.0
        }, null)

        MessageUtils.send(player, Messages.PREFIX, Messages.CHANGE_TO_BLACKLISTED_WORLD)
        if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND?.play(player)
    }
}