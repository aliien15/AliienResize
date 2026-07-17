package com.aliiensmp.aliienResize.commands

import co.aikar.commands.annotation.*
import AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.utils.ResizeUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("resize")
class AdminCommands(plugin: AliienResize) : AbstractResizeCommand(plugin) {

    @Subcommand("admin reload")
    @CommandPermission("aliien.resize.admin.reload")
    fun reloadConfigs(sender: CommandSender) {
        MessageUtils.send(sender, Messages.PREFIX, Messages.RELOADING)
        plugin.reloadConfigurations(sender)
    }

    @Subcommand("admin set")
    @CommandPermission("aliien.resize.admin.set")
    @CommandCompletion("@players @resize_ids -f")
    fun resizePlayer(sender: CommandSender, @Flags("other") target: Player, sizeNode: SizeNode, @Optional flag: String?) {
        val force = flag.equals("-f", ignoreCase = true)

        if (!force && !ResizeUtils.hasEnoughSpace(target, sizeNode.scale)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.name))
            if (Settings.SOUNDS_ENABLED && sender is Player) Settings.ERROR_SOUND?.play(sender)
            return
        }

        applyScale(target, sizeNode.scale) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN.replace("%player%", target.name).replace("%size_id%", sizeNode.id))
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER.replace("%size_id%", sizeNode.id))
            if (Settings.SOUNDS_ENABLED && sender is Player) Settings.SUCCESS_SOUND?.play(sender)
        }
    }

    @Subcommand("admin clear")
    @CommandPermission("aliien.resize.admin.clear")
    @CommandCompletion("@players -f")
    fun clearSize(sender: CommandSender, @Flags("other") target: Player, @Optional flag: String?) {
        val force = flag.equals("-f", ignoreCase = true)

        if (!force && !ResizeUtils.hasEnoughSpace(target, 1.0)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.name))
            if (Settings.SOUNDS_ENABLED && sender is Player) Settings.ERROR_SOUND?.play(sender)
            return
        }

        applyScale(target, 1.0) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN.replace("%player%", target.name))
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER)

            if (Settings.SOUNDS_ENABLED) {
                if (sender is Player) Settings.CLEAR_SOUND?.play(sender)
                if (target != sender) Settings.CLEAR_SOUND?.play(target)
            }
        }
    }
}