package com.aliiensmp.aliienResize.commands

import co.aikar.commands.BaseCommand
import AliienResize
import com.aliiensmp.aliienResize.listeners.PlayerConnectionListener
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

abstract class AbstractResizeCommand(protected val plugin: AliienResize) : BaseCommand() {

    protected fun applyScale(target: Player, scale: Double, onSuccess: (() -> Unit)? = null) {
        target.scheduler.run(plugin, { _ ->
            target.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = scale
            PlayerConnectionListener.cache[target.uniqueId] = scale

            onSuccess?.invoke()
        }, null)
    }
}