package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.annotation.*;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Menus.ResizeMenu;
import com.aliiensmp.aliienResize.Utils.ResizeUtils;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.entity.Player;

@CommandAlias("resize")
public class PlayerCommands extends AbstractResizeCommand {

    public PlayerCommands(AliienResize plugin) {
        super(plugin);
    }

    /**
     * Opens the main GUI for the player.
     */
    @Default
    @Subcommand("menu")
    @CommandPermission("aliien.resize.menu")
    public void openMenu(Player player) {
        if (!canUseInWorld(player)) return;

        new ResizeMenu(plugin).openMenu(player, 1);
        if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND.play(player);
    }

    /**
     * Sets a size on a target player
     */
    @Subcommand("set")
    @CommandPermission("aliien.resize.set")
    @CommandCompletion("@accessible_resize_ids")
    public void resize(Player player, SizeNode sizeNode) {
        if (!player.hasPermission(sizeNode.permission())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        if (!canUseInWorld(player)) return;

        if (!ResizeUtils.INSTANCE.hasEnoughSpace(player, sizeNode.scale())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        applyScale(player, sizeNode.scale(), () -> {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS.replace("%size_id%", sizeNode.id()));
            if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND.play(player);
        });
    }

    /**
     * Sets a player size back to default (1.0)
     */
    @Subcommand("clear")
    @CommandPermission("aliien.resize.clear")
    public void clearSize(Player player) {
        if (!ResizeUtils.INSTANCE.hasEnoughSpace(player, 1.0)) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        applyScale(player, 1.0, () -> {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT);
            if (Settings.SOUNDS_ENABLED) Settings.CLEAR_SOUND.play(player);
        });
    }

    /**
     * Helper method for world blacklist check
     */
    private boolean canUseInWorld(Player player) {
        if (!player.hasPermission("aliien.resize.bypass.worldblacklist") && Settings.BLACKLISTED_WORLDS.contains(player.getWorld().getName())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.IN_BLACKLISTED_WORLD);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return false;
        }
        return true;
    }
}