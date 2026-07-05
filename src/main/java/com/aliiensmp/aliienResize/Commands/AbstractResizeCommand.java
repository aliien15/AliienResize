package com.aliiensmp.aliienResize.Commands;

import co.aikar.commands.BaseCommand;
import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Listeners.PlayerConnectionListener;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Optional;

public abstract class AbstractResizeCommand extends BaseCommand {

    protected final AliienResize plugin;

    public AbstractResizeCommand(final AliienResize plugin) {
        this.plugin = plugin;
    }

    /**
     * Changes the {@code Attribute.GENERIC_SCALE} of the player safely on Folia.
     *
     * @param target the player whose scale is changing
     * @param scale the new scale value
     * @param onSuccess callback for messages/sounds after the scale is applied
     */
    protected void applyScale(final Player target, final double scale, final Runnable onSuccess) {
        target.getScheduler().run(plugin, scheduledTask -> {

            Optional.ofNullable(target.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(scale));

            PlayerConnectionListener.cache.put(target.getUniqueId(), scale);

            if (onSuccess != null) {
                onSuccess.run();
            }

        }, null);
    }
}