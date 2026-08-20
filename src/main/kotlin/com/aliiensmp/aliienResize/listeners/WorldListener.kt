package com.aliiensmp.aliienResize.listeners

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
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

        DebugUtils.send("Player ${player.name} changed world to $currentWorld. Blacklisted: $isBlacklisted")

        if (!isBlacklisted || player.hasPermission("aliien.resize.bypass.worldblacklist")) {
            return
        }

        DebugUtils.send("Player ${player.name} entered blacklisted world $currentWorld without bypass. Forcing scale reset.")
        val playerData = plugin.playerDataService.getPlayerData(player.uniqueId)
        playerData.resetScale(plugin)

        MessageUtils.send(player, Messages.PREFIX, Messages.CHANGE_TO_BLACKLISTED_WORLD)
        if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND?.play(player)
    }
}