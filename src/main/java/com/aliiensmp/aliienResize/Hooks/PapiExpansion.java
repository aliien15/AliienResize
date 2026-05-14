package com.aliiensmp.aliienResize.Hooks;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Sizes;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class PapiExpansion extends PlaceholderExpansion {

    private final AliienResize plugin;

    /**
     * Creates the PlaceholderAPI expansion for player scale values.
     *
     * @param p owning plugin instance
     */
    public PapiExpansion(AliienResize p) {
        this.plugin = p;
    }

    @Override
    public @NotNull String getIdentifier() { return "aliienresize"; }

    @Override
    public @NotNull String getAuthor() { return "Aliien15"; }

    @Override
    public @NotNull String getVersion() { return plugin.getPluginMeta().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) return "";

        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        double currentScale = Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                .map(AttributeInstance::getValue)
                .orElse(1.0);

        String normalizedParams = params.toLowerCase(Locale.ROOT);

        return switch (normalizedParams) {
            case "scale" -> String.valueOf(currentScale);

            case "scale_formatted" -> currentScale + "x";

            case "id" -> Sizes.SIZES_BY_ID.values().stream()
                    .filter(node -> Double.compare(node.scale(), currentScale) == 0)
                    .map(SizeNode::id)
                    .findFirst()
                    .orElse(Double.compare(currentScale, 1.0) == 0 ? "normal" : "unknown");

            case "name" -> Sizes.SIZES_BY_ID.values().stream()
                    .filter(node -> Double.compare(node.scale(), currentScale) == 0)
                    .map(node -> node.gui().name())
                    .findFirst()
                    .orElse(Double.compare(currentScale, 1.0) == 0 ? "Normal Size" : "Unknown");

            default -> null;
        };
    }
}