package com.aliiensmp.aliienResize.commands

import co.aikar.commands.annotation.*
import com.aliiensmp.aliienResize.AliienResize
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

        if (!canUseInWorld(player)) return

        if (!ResizeUtils.hasEnoughSpace(player, sizeNode.scale)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)

            return
        }

        applyScale(player, sizeNode.scale) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS.replace("%size_id%", sizeNode.id))
            Settings.SUCCESS_SOUND?.play(player, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("clear")
    @CommandPermission("aliien.resize.clear")
    fun clearSize(player: Player) {
        if (!ResizeUtils.hasEnoughSpace(player, 1.0)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)

            return
        }

        applyScale(player, 1.0) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT)
            Settings.CLEAR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
        }
    }

    private fun canUseInWorld(player: Player): Boolean {
        val isBlacklistedWorld = Settings.BLACKLISTED_WORLDS.any {
            it.equals(player.world.name, ignoreCase = true)
        }

        if (isBlacklistedWorld && !player.hasPermission("aliien.resize.bypass.worldblacklist")) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD)
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)

            return false
        }
        return true
    }
}