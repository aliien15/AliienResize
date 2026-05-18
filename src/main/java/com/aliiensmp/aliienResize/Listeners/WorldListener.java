package com.aliiensmp.aliienResize.Listeners;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Optional;

public class WorldListener implements Listener {

    private final AliienResize plugin;

    public WorldListener(AliienResize plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String currentWorld = player.getWorld().getName();

        boolean isBlacklisted = Settings.BLACKLISTED_WORLDS.stream()
                .anyMatch(world -> world.equalsIgnoreCase(currentWorld));

        if (!isBlacklisted) return;

        boolean hasBypass = player.hasPermission("aliien.resize.bypass.worldblacklist");

        if (hasBypass) return;

        player.getScheduler().run(plugin, scheduledTask -> {
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(scale -> scale.setBaseValue(1.0));
        }, null);

        MessageUtils.send(player, Messages.PREFIX, Messages.CHANGE_TO_BLACKLISTED_WORLD);
        Settings.ERROR_SOUND.play(player);
    }
}