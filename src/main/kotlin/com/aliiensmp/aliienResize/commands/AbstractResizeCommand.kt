package com.aliiensmp.aliienResize.commands

import co.aikar.commands.BaseCommand
import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.utils.ResizeUtils
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class AbstractResizeCommand(protected val plugin: AliienResize) : BaseCommand() {

    protected fun applyScale(target: Player, scale: Double, onSuccess: (() -> Unit)? = null) {
        plugin.playerDataService.applyScale(target, scale, onSuccess)
    }

    protected fun checkAdminSpace(sender: CommandSender, target: Player, scale: Double, force: Boolean): Boolean {
        if (!force && !ResizeUtils.hasEnoughSpace(target, scale)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL, "%player%", target.name)
            Settings.ERROR_SOUND?.play(sender, Settings.SOUNDS_ENABLED)
            return false
        }
        return true
    }

    protected fun doesNotHaveEnoughSpace(player: Player, scale: Double): Boolean {
        if (!ResizeUtils.hasEnoughSpace(player, scale)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            DebugUtils.send("Player ${player.name} does not have enough space to resize to scale $scale and does not have permission to bypass this check")
            return true
        }
        return false
    }

    protected fun cannotUseInWorld(player: Player): Boolean {
        val isBlacklistedWorld = Settings.BLACKLISTED_WORLDS.any {
            it.equals(player.world.name, ignoreCase = true)
        }

        if (isBlacklistedWorld && !player.hasPermission("aliien.resize.bypass.worldblacklist")) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            DebugUtils.send("Player ${player.name} cannot resize due to being in a blacklisted world and not having the bypass permission")
            return true
        }
        return false
    }
}