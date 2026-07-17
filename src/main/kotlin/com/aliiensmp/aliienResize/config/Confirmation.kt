package com.aliiensmp.aliienResize.config

import AliienResize
import com.aliiensmp.aliienResize.menus.ConfirmationMenuAction
import com.aliiensmp.core.config.Key
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.lib.boostedyaml.block.implementation.Section
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import java.util.Locale

object Confirmation {

    @Key("menu-settings.title")
    var CONFIRMATION_MENU_TITLE: String = "<dark_gray>Confirm Purchase?</dark_gray>"

    @Key("menu-settings.display-slots")
    var CONFIRMATION_MENU_DISPLAY_SLOTS: List<Int> = listOf(13)

    var CONFIRMATION_MENU_ROWS: Int = 3
        private set

    val CONFIRMATION_MENU_ITEMS = mutableListOf<ButtonData>()

    data class ButtonData(
        val action: ConfirmationMenuAction,
        val material: Material,
        val name: String,
        val lore: List<String>,
        val slots: List<Int>,
        val modelData: Int,
        val flags: Array<ItemFlag>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ButtonData

            if (modelData != other.modelData) return false
            if (action != other.action) return false
            if (material != other.material) return false
            if (name != other.name) return false
            if (lore != other.lore) return false
            if (slots != other.slots) return false
            if (!flags.contentEquals(other.flags)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = modelData
            result = 31 * result + action.hashCode()
            result = 31 * result + material.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + lore.hashCode()
            result = 31 * result + slots.hashCode()
            result = 31 * result + flags.contentHashCode()
            return result
        }
    }

    fun loadFromConfig(config: YamlDocument, plugin: AliienResize) {
        CONFIRMATION_MENU_ROWS = config.getInt("menu-settings.rows", 3).coerceIn(1, 6)

        CONFIRMATION_MENU_ITEMS.clear()

        config.getSection("items")?.let { section ->
            section.getRoutesAsStrings(false).forEach { key ->
                CONFIRMATION_MENU_ITEMS.add(parseButton(section.getSection(key), key, plugin))
            }
        }
    }

    private fun parseButton(sec: Section, key: String, plugin: AliienResize): ButtonData {
        val actionStr = sec.getString("action", "NONE").uppercase(Locale.ROOT)

        // Parse action
        val action = runCatching { ConfirmationMenuAction.valueOf(actionStr) }.getOrElse {
            plugin.logger.warning("Invalid action in confirmation-menu.yml for item '$key'. Defaulting to NONE.")
            ConfirmationMenuAction.NONE
        }

        // Checks the string for materials, attempts a match falling back to STONE if invalid
        val matStr = sec.getString("material", "")
        val mat = matStr.takeIf { it.isNotBlank() }?.let { Material.matchMaterial(it.uppercase(Locale.ROOT)) } ?: Material.STONE

        // Map string flags directly to ItemFlags
        val flags = sec.getStringList("item-flags")?.mapNotNull { flag ->
            runCatching { ItemFlag.valueOf(flag.uppercase(Locale.ROOT)) }.getOrNull()
        }?.toTypedArray() ?: emptyArray()

        return ButtonData(
            action = action,
            material = mat,
            name = sec.getString("name", " "),
            lore = sec.getStringList("lore") ?: emptyList(),
            slots = sec.getIntList("slots") ?: emptyList(),
            modelData = sec.getInt("model-data", 0),
            flags = flags
        )
    }
}