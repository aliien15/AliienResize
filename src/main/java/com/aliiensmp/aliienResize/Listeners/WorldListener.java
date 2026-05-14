package com.aliiensmp.aliienResize.Listeners;

import com.aliiensmp.aliienResize.Config.Settings;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Optional;

public class WorldListener implements Listener {

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (!Settings.BLACKLISTED_WORLDS.contains(event.getFrom().getName())) return;

        Optional.ofNullable(event.getPlayer().getAttribute(Attribute.GENERIC_SCALE))
                .ifPresent(scale -> scale.setBaseValue(1.0));
    }
}
