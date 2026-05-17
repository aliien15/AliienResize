package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.aliienResize.Menus.ResizeMenu;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("resize")
public class PlayerCommands extends BaseCommand {

    private final AliienResize plugin;

    public PlayerCommands(AliienResize plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main GUI for the player.
     */
    @Default
    @Subcommand("menu")
    @CommandPermission("aliien.resize.menu")
    public void openMenu(Player player) {
        if (!player.hasPermission("aliien.resize.bypass.worldblacklist") && Settings.BLACKLISTED_WORLDS.contains(player.getWorld().getName())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        new ResizeMenu(plugin).openMenu(player, 1);
        if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND.play(player);
    }

    /**
     * Sets a size on a target player
     */
    @Subcommand("set")
    @CommandPermission("aliien.resize.set")
    @CommandCompletion("accessible_resize_ids")
    public void resize(Player player, String sizeId) {
        SizeNode sizeNode = Sizes.SIZES_BY_ID.get(sizeId);

        if (!player.hasPermission(sizeNode.permission())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        if (!player.hasPermission("aliien.resize.bypass.worldblacklist") && Settings.BLACKLISTED_WORLDS.contains(player.getWorld().getName())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        if (!plugin.getResizeUtils().hasEnoughSpace(player, sizeNode.scale())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        player.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(sizeNode.scale()));

            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS.replace("%size_id%", sizeNode.id()));
            if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND.play(player);
        }, null);
    }

    /**
     * Sets a player size back to default (1.0)
     */
    @Subcommand("clear")
    @CommandPermission("aliien.resize.clear")
    public void clearSize(Player player) {
        if (!plugin.getResizeUtils().hasEnoughSpace(player, 1.0)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        player.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(1.0));

            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT);
            if (Settings.SOUNDS_ENABLED) Settings.CLEAR_SOUND.play(player);
        }, null);
    }
}