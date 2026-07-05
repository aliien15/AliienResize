package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.annotation.*;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Utils.ResizeUtils;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("resize")
public class AdminCommands extends AbstractResizeCommand {

    public AdminCommands(AliienResize plugin) {
        super(plugin);
    }

    @Subcommand("admin reload")
    @CommandPermission("aliien.resize.admin.reload")
    public void reloadConfigs(CommandSender sender) {
        MessageUtils.send(sender, Messages.PREFIX, Messages.RELOADING);
        plugin.reloadConfigurations(sender);
    }

    @Subcommand("admin set")
    @CommandPermission("aliien.resize.admin.set")
    @CommandCompletion("@players @resize_ids -f")
    public void resizePlayer(CommandSender sender, @Flags("other") Player target, SizeNode sizeNode, @Optional String flag) {
        boolean force = flag != null && flag.equalsIgnoreCase("-f");

        if (!force && !ResizeUtils.INSTANCE.hasEnoughSpace(target, sizeNode.scale())) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.ERROR_SOUND.play(p);
            return;
        }

        applyScale(target, sizeNode.scale(), () -> {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN.replace("%player%", target.getName()).replace("%size_id%", sizeNode.id()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER.replace("%size_id%", sizeNode.id()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.SUCCESS_SOUND.play(p);
        });
    }

    @Subcommand("admin clear")
    @CommandPermission("aliien.resize.admin.clear")
    @CommandCompletion("@players -f")
    public void clearSize(CommandSender sender, @Flags("other") Player target, @Optional String flag) {
        boolean force = flag != null && flag.equalsIgnoreCase("-f");

        if (!force && !ResizeUtils.INSTANCE.hasEnoughSpace(target, 1.0)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.ERROR_SOUND.play(p);
            return;
        }

        applyScale(target, 1.0, () -> {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN.replace("%player%", target.getName()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER);
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.CLEAR_SOUND.play(p);
            if (Settings.SOUNDS_ENABLED && target != sender) Settings.CLEAR_SOUND.play(target);
        });
    }
}