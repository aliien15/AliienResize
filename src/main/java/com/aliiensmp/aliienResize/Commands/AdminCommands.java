package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("resize|size")
public class AdminCommands extends BaseCommand {

    private final AliienResize plugin;

    public AdminCommands(AliienResize plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads all configuration files and caches.
     */
    @Subcommand("admin reload|a reload")
    @CommandPermission("aliien.resize.admin.reload")
    public void reloadConfigs(Player player) {
        MessageUtils.send(player, Messages.PREFIX, Messages.RELOADING);
        plugin.reloadConfigurations(player);
    }

    /**
     * Force-sets a size on a target player.
     */
    @Subcommand("admin set|a set")
    @CommandPermission("aliien.resize.admin.set")
    @CommandCompletion("resize_ids")
    public void resizePlayer(Player sender, Player target, String sizeId) {
        SizeNode sizeNode = Sizes.SIZES_BY_ID.get(sizeId);

        if (!plugin.getResizeUtils().hasEnoughSpace(target, sizeNode.scale())) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            Settings.ERROR_SOUND.play(sender);
            return;
        }

        target.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(sizeNode.scale()));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN.replace("%player%", target.getName()).replace("size_id", sizeId));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER.replace("%size_id%", sizeId));
            Settings.SUCCESS_SOUND.play(sender);
        }, null);
    }

    /**
     * Force-sets a size on a target player, bypassing size restrictions.
     */
    @Subcommand("admin set -f|a set -f")
    @CommandPermission("aliien.resize.admin.set")
    @CommandCompletion("resize_ids")
    public void forceSizeForce(Player sender, Player target, String sizeId) {
        SizeNode sizeNode = Sizes.SIZES_BY_ID.get(sizeId);

        target.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(sizeNode.scale()));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN.replace("%player%", target.getName()).replace("size_id", sizeId));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER.replace("%size_id%", sizeId));
            Settings.SUCCESS_SOUND.play(sender);
        }, null);
    }

    /**
     * Sets a player size back to default (1.0)
     */
    @Subcommand("admin clear|a clear")
    @CommandPermission("aliien.resize.admin.clear")
    public void clearSize(Player sender, Player target) {
        if (!plugin.getResizeUtils().hasEnoughSpace(target, 1.0)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            Settings.ERROR_SOUND.play(sender);
            return;
        }

        target.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(1.0));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN.replace("%player%", target.getName()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER);
            Settings.SUCCESS_SOUND.play(sender);
            }, null);
    }

    /**
     * Sets a player size back to default (1.0), bypassing size restrictions.
     */
    @Subcommand("admin clear -f|a clear -f")
    @CommandPermission("aliien.resize.admin.clear")
    public void clearSizeForce(Player sender, Player target) {
        target.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(1.0));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN.replace("%player%", target.getName()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER);
            Settings.SUCCESS_SOUND.play(sender);
        }, null);
    }
}