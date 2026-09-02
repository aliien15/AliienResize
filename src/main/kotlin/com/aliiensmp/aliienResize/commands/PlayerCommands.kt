package com.aliiensmp.aliienResize.commands

import co.aikar.commands.annotation.*
import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.menus.ResizeMenu
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.entity.Player

@CommandAlias("resize")
class PlayerCommands(plugin: AliienResize) : AbstractResizeCommand(plugin) {

    @Default
    @Subcommand("menu")
    @CommandPermission("aliien.resize.menu")
    fun openMenu(player: Player) {
        if (cannotUseInWorld(player)) return

        ResizeMenu(plugin).openMenu(player, 1)

        Settings.SUCCESS_SOUND?.play(player, Settings.SOUNDS_ENABLED)
    }

    @Subcommand("set")
    @CommandPermission("aliien.resize.set")
    @CommandCompletion("@accessible_resize_ids")
    fun resize(player: Player, sizeNode: SizeNode) {
        if (sizeNode.permission.isNotBlank() && !player.hasPermission(sizeNode.permission)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            return
        }

        if (cannotUseInWorld(player) || doesNotHaveEnoughSpace(player, sizeNode.scale)) {
            return
        }

        applyScale(player, sizeNode.scale) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS)
            Settings.SUCCESS_SOUND?.play(player, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("clear")
    @CommandPermission("aliien.resize.clear")
    fun clearSize(player: Player) {
        if (cannotUseInWorld(player) || doesNotHaveEnoughSpace(player, 1.0)) {
            return
        }

        applyScale(player, 1.0) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT)
            Settings.CLEAR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("scale")
    @CommandPermission("aliien.resize.scale")
    fun onScale(player: Player, newScale: Double) {
        if ((newScale < Settings.SCALE_MIN || newScale > Settings.SCALE_MAX) && !player.hasPermission("aliien.resize.bypass.scale-limits")) {
            DebugUtils.send("Player ${player.name} could not resize themselves using the scale command due to the size being off of the limits set in settings.yml")
            MessageUtils.send(player, Messages.PREFIX, Messages.SCALE_NOT_IN_LIMITS, "%min%", String.format("%.1f", Settings.SCALE_MIN), "%max%", String.format("%.1f", Settings.SCALE_MAX))
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            return
        }

        if (cannotUseInWorld(player) || doesNotHaveEnoughSpace(player, newScale)) {
            return
        }

        applyScale(player, newScale) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS)
            Settings.SUCCESS_SOUND?.play(player, Settings.SOUNDS_ENABLED)
        }
    }
}