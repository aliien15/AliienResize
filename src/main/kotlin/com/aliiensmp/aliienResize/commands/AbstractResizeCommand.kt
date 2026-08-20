package com.aliiensmp.aliienResize.commands

import co.aikar.commands.BaseCommand
import com.aliiensmp.aliienResize.AliienResize
import org.bukkit.entity.Player

abstract class AbstractResizeCommand(protected val plugin: AliienResize) : BaseCommand() {

    protected fun applyScale(target: Player, scale: Double, onSuccess: (() -> Unit)? = null) {
        val playerData = plugin.playerDataService.getPlayerData(target.uniqueId)
        playerData.applyScale(plugin, scale, onSuccess)
    }
}