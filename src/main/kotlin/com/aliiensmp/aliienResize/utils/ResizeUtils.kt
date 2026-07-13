package com.aliiensmp.aliienResize.utils

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import kotlin.math.floor

object ResizeUtils {

    /**
     * Checks if a player has enough space to resize before actually resizing,
     * avoiding players glitching/clipping through blocks
     *
     * @param player player to check
     * @param newScale the size/scale they want to go to
     */
    fun hasEnoughSpace(player: Player, newScale: Double): Boolean {
        val currentPlayerScale = player.getAttribute(Attribute.GENERIC_SCALE)?.value ?: 1.0

        if (currentPlayerScale >= newScale || player.hasPermission("aliien.resize.bypass.collisioncheck")) {
            return true
        }

        val halfWidth = (0.6 * newScale) / 2.0
        val height = 1.8 * newScale
        val loc = player.location

        val playerBox = BoundingBox(
            loc.x - halfWidth, loc.y, loc.z - halfWidth,
            loc.x + halfWidth, loc.y + height, loc.z + halfWidth
        )

        val xMin = floor(playerBox.minX).toInt()
        val yMin = floor(playerBox.minY).toInt()
        val zMin = floor(playerBox.minZ).toInt()
        val xMax = floor(playerBox.maxX).toInt()
        val yMax = floor(playerBox.maxY).toInt()
        val zMax = floor(playerBox.maxZ).toInt()

        val world = loc.world

        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                for (z in zMin..zMax) {
                    val currentBlock = world.getBlockAt(x, y, z)

                    if (!currentBlock.isPassable && playerBox.overlaps(currentBlock.boundingBox)) {
                        return false
                    }
                }
            }
        }

        return true
    }
}