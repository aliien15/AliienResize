package com.aliiensmp.aliienResize.commands

import co.aikar.commands.annotation.*
import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

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

        if (!checkAdminSpace(sender, target, sizeNode.scale, force)) return

        applyScale(target, sizeNode.scale) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN, "%player%", target.name, "%size_id%", sizeNode.id)
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER, "%size_id%", sizeNode.id)
            if (sender is Player) Settings.SUCCESS_SOUND?.play(sender, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("admin clear")
    @CommandPermission("aliien.resize.admin.clear")
    @CommandCompletion("@players -f")
    fun clearSize(sender: CommandSender, @Flags("other") target: Player, @Optional flag: String?) {
        val force = flag.equals("-f", ignoreCase = true)

        if (!checkAdminSpace(sender, target, 1.0, force)) return

        applyScale(target, 1.0) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN, "%player%", target.name)
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER)

            if (sender is Player) Settings.CLEAR_SOUND?.play(sender, Settings.SOUNDS_ENABLED)
            if (target != sender) Settings.CLEAR_SOUND?.play(target, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("admin scale")
    @CommandPermission("aliien.resize.admin.scale")
    @CommandCompletion("@players -f")
    fun onScale(sender: CommandSender, @Flags("other") target: Player, newScale: Double, @Optional flag: String?) {
        val force = flag.equals("-f", ignoreCase = true)

        if (!checkAdminSpace(sender, target, newScale, force)) return

        applyScale(target, newScale) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SCALE_ADMIN, "%player%", target.name, "%scale%", String.format("%.1f", newScale))
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SCALE_PLAYER, "%scale%", String.format("%.1f", newScale))

            if (sender is Player) Settings.CLEAR_SOUND?.play(sender, Settings.SOUNDS_ENABLED)
            if (target != sender) Settings.CLEAR_SOUND?.play(target, Settings.SOUNDS_ENABLED)
        }
    }

    @Subcommand("admin debug")
    @CommandPermission("aliien.resize.admin.debug")
    fun setDebug(sender: CommandSender) {
        val newState = DebugUtils.toggleDebug()

        CompletableFuture.runAsync {
            try {
                plugin.settingsFile.set("debug-mode", newState)
                plugin.settingsFile.save()

                plugin.reloadConfigurations(sender)

                val task = Runnable {
                    val message = if (newState) Messages.DEBUG_TOGGLED_ON else Messages.DEBUG_TOGGLED_OFF
                    MessageUtils.send(sender, Messages.PREFIX, message)
                    if (Settings.SOUNDS_ENABLED && sender is Player) {
                        Settings.SUCCESS_SOUND?.play(sender)
                    }
                }

                if (sender is Player) {
                    sender.scheduler.run(plugin, { _ -> task.run() }, null)
                } else {
                    plugin.server.globalRegionScheduler.run(plugin) { _ -> task.run() }
                }

            } catch (e: Exception) {
                DebugUtils.send(Level.SEVERE, "Failed to save debug mode to settings.yml: %error%", "%error%", e.message ?: "Unknown error")
            }
        }
    }
}