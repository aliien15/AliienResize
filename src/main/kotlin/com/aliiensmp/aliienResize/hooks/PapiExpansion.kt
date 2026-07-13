package com.aliiensmp.aliienResize.Hooks

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.Config.Sizes
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import java.util.Locale

class PapiExpansion(private val plugin: AliienResize) : PlaceholderExpansion() {

    override fun getIdentifier() = "aliienresize"
    override fun getAuthor() = "Aliien15"
    override fun getVersion() = plugin.pluginMeta.version
    override fun persist() = true

    override fun onRequest(offlinePlayer: OfflinePlayer?, params: String): String? {
        val player = offlinePlayer?.player ?: return ""

        val currentScale = player.getAttribute(Attribute.GENERIC_SCALE)?.value ?: 1.0

        val normalizedParams = params.lowercase(Locale.ROOT)

        return when (normalizedParams) {
            "scale" -> currentScale.toString()
            "scale_formatted" -> "${currentScale}x"
            "id" -> Sizes.SIZES_BY_ID.values
                .firstOrNull { it.scale == currentScale }
                ?.id ?: if (currentScale == 1.0) "Normal" else "Unknown"
            "name" -> Sizes.SIZES_BY_ID.values
                .firstOrNull { it.scale == currentScale }
                ?.gui?.name ?: if (currentScale == 1.0) "Normal Size" else "Unknown"
            else -> null
        }
    }
}