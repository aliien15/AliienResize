package com.aliiensmp.aliienResize.Utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class ResizeUtils {

    /**
     * Checks if a player has enough space to resize before actually resizing,
     * avoiding players glitching/clipping through blocks
     *
     * @param player player to check
     * @param newScale the size/scale they want to go to
     * @requires {@code player != null && newScale > 0}
     */
    public boolean hasEnoughSpace (Player player, double newScale) {
        if (player.getAttribute(Attribute.GENERIC_SCALE).getValue() >= newScale) return true;

        double halfWidth = (0.6 * newScale) / 2.0;
        double height = 1.8 * newScale;
        Location loc = player.getLocation();

        BoundingBox playerBox = new BoundingBox(
                loc.getX() - halfWidth, loc.getY(), loc.getZ() - halfWidth,
                loc.getX() + halfWidth, loc.getY() + height, loc.getZ() + halfWidth
        );

        int minX = (int) Math.floor(playerBox.getMinX());
        int minY = (int) Math.floor(playerBox.getMinY());
        int minZ = (int) Math.floor(playerBox.getMinZ());
        int maxX = (int) Math.floor(playerBox.getMaxX());
        int maxY = (int) Math.floor(playerBox.getMaxY());
        int maxZ = (int) Math.floor(playerBox.getMaxZ());

        World world = loc.getWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block currentBlock = world.getBlockAt(x, y, z);

                    if (!currentBlock.isPassable() && playerBox.overlaps(currentBlock.getBoundingBox()))
                        return false;
                }
            }
        }

        return true;
    }
}
