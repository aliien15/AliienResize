package com.aliiensmp.aliienResize.data

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.core.utils.DebugUtils
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.UUID

class PlayerData(val uuid: UUID, var scale: Double = 1.0) {

    fun getPlayer(): Player? {
        return Bukkit.getPlayer(uuid)
    }

    fun applyScale(plugin: AliienResize, newScale: Double, onSuccess: (() -> Unit)? = null) {
        DebugUtils.send("Updating internal scale to $newScale for UUID: $uuid")
        this.scale = newScale

        val player = getPlayer()
        if (player == null) {
            DebugUtils.send("Player with UUID $uuid is offline. Scale updated in cache only.")
            return
        }

        player.scheduler.run(plugin, { _ ->
            DebugUtils.send("Executing scale attribute application for ${player.name} ($newScale).")
            player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = newScale
            onSuccess?.invoke()
        }, null)
    }

    fun resetScale(plugin: AliienResize, onSuccess: (() -> Unit)? = null) {
        DebugUtils.send("Resetting scale to 1.0 for UUID: $uuid")
        applyScale(plugin, 1.0, onSuccess)
    }
}