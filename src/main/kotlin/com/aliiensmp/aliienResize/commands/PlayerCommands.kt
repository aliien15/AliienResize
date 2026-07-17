package com.aliiensmp.aliienResize.commands

import co.aikar.commands.annotation.*
import AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.menus.ResizeMenu
import com.aliiensmp.aliienResize.utils.ResizeUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.entity.Player

@CommandAlias("resize")
class PlayerCommands(plugin: AliienResize) : AbstractResizeCommand(plugin) {

    @Default
    @Subcommand("menu")
    @CommandPermission("aliien.resize.menu")
    fun openMenu(player: Player) {
        if (!canUseInWorld(player)) return

        ResizeMenu(plugin).openMainMenu(player, 1)

        if (Settings.SOUNDS_ENABLED)
            Settings.SUCCESS_SOUND?.play(player)
    }

    @Subcommand("set")
    @CommandPermission("aliien.resize.set")
    @CommandCompletion("@accessible_resize_ids")
    fun resize(player: Player, sizeNode: SizeNode) {
        if (sizeNode.permission.isNotBlank() && !player.hasPermission(sizeNode.permission)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM)
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)

            return
        }

        if (!canUseInWorld(player)) return

        if (!ResizeUtils.hasEnoughSpace(player, sizeNode.scale)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)

            return
        }

        applyScale(player, sizeNode.scale) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS.replace("%size_id%", sizeNode.id))
            if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND?.play(player)
        }
    }

    @Subcommand("clear")
    @CommandPermission("aliien.resize.clear")
    fun clearSize(player: Player) {
        if (!ResizeUtils.hasEnoughSpace(player, 1.0)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)

            return
        }

        applyScale(player, 1.0) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT)
            if (Settings.SOUNDS_ENABLED)
                Settings.CLEAR_SOUND?.play(player)
        }
    }

    private fun canUseInWorld(player: Player): Boolean {
        if (!player.hasPermission("aliien.resize.bypass.worldblacklist") && Settings.BLACKLISTED_WORLDS.contains(player.world.name)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD)
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)

            return false
        }
        return true
    }
}