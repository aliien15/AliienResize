package com.aliiensmp.aliienResize.data

import com.aliiensmp.core.utils.DebugUtils
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import java.util.UUID

class PlayerData(val uuid: UUID, var scale: Double = 1.0) {

    fun applyScaleOnEntityThread(player: Player, newScale: Double, onSuccess: (() -> Unit)? = null) {
        DebugUtils.send("Updating internal scale to $newScale for UUID: $uuid")
        scale = newScale

        DebugUtils.send("Executing scale attribute application for ${player.name} ($newScale).")
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = newScale
        onSuccess?.invoke()
    }

    fun resetScaleOnEntityThread(player: Player, onSuccess: (() -> Unit)? = null) {
        DebugUtils.send("Resetting scale to 1.0 for UUID: $uuid")
        applyScaleOnEntityThread(player, 1.0, onSuccess)
    }
}