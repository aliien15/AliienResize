package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

@CommandAlias("resize")
public class AdminCommands extends BaseCommand {

    private final AliienResize plugin;

    public AdminCommands(AliienResize plugin) {
        this.plugin = plugin;
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
    public void resizePlayer(CommandSender sender, @Flags("other") Player target, String sizeId, @Optional String flag) {

        SizeNode sizeNode = Sizes.SIZES_BY_ID.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(sizeId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        boolean force = flag != null && flag.equalsIgnoreCase("-f");

        if (sizeNode == null) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.NULL_ID);
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.ERROR_SOUND.play(p);
            return;
        }

        if (!force && !plugin.getResizeUtils().hasEnoughSpace(target, sizeNode.scale())) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.ERROR_SOUND.play(p);
            return;
        }

        target.getScheduler().run(plugin, scheduledTask -> {
            java.util.Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(sizeNode.scale()));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_ADMIN.replace("%player%", target.getName()).replace("%size_id%", sizeNode.id()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_SET_PLAYER.replace("%size_id%", sizeNode.id()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.SUCCESS_SOUND.play(p);
        }, null);
    }

    @Subcommand("admin clear")
    @CommandPermission("aliien.resize.admin.clear")
    @CommandCompletion("@players -f")
    public void clearSize(CommandSender sender, @Flags("other") Player target, @Optional String flag) {
        boolean force = flag != null && flag.equalsIgnoreCase("-f");

        if (!force && !plugin.getResizeUtils().hasEnoughSpace(target, 1.0)) {
            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_SET_FAIL.replace("%player%", target.getName()));
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.ERROR_SOUND.play(p);
            return;
        }

        target.getScheduler().run(plugin, scheduledTask -> {
            java.util.Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(1.0));

            MessageUtils.send(sender, Messages.PREFIX, Messages.FORCE_CLEAR_ADMIN.replace("%player%", target.getName()));
            MessageUtils.send(target, Messages.PREFIX, Messages.FORCE_CLEAR_PLAYER);
            if (Settings.SOUNDS_ENABLED && sender instanceof Player p) Settings.CLEAR_SOUND.play(p);
            if (Settings.SOUNDS_ENABLED && target != sender) Settings.CLEAR_SOUND.play(target);
        }, null);
    }
}