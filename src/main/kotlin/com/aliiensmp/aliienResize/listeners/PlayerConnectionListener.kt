package com.aliiensmp.aliienResize.listeners

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.data.PlayerDataService
import com.aliiensmp.core.utils.DebugUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerConnectionListener(
    private val plugin: AliienResize,
    private val dataService: PlayerDataService
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        DebugUtils.send("PlayerJoinEvent fired for ${event.player.name}. Initiating data load.")
        dataService.loadPlayer(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        DebugUtils.send("PlayerQuitEvent fired for ${event.player.name}. Initiating data save.")
        dataService.savePlayer(event.player.uniqueId)
    }
}