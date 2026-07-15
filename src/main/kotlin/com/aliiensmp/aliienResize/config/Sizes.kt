package com.aliiensmp.aliienResize.config

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.data.*
import com.aliiensmp.aliienResize.Menus.MenuAction
import com.aliiensmp.core.config.Key
import com.aliiensmp.core.items.ItemBuilder
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.lib.boostedyaml.block.implementation.Section
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.Locale
import kotlin.math.round

object Sizes {

    @Key("menu-settings.title")
    var MENU_TITLE: String = "<dark_gray>Select your Size</dark_gray>"

    @Key("menu-settings.rows")
    var MENU_ROWS: Int = 6

    @Key("menu-settings.locked-material")
    private var RAW_LOCKED_MATERIAL: String = "BARRIER"

    val MENU_LOCKED_MATERIAL: Material
        get() = Material.matchMaterial(RAW_LOCKED_MATERIAL.uppercase(Locale.ROOT)) ?: Material.BARRIER

    @Key("defaults.lore")
    var MENU_DEFAULT_LORE: List<String> = emptyList()

    @Key("defaults.lore-without-perm")
    var MENU_DEFAULT_LORE_NO_PERM: List<String> = emptyList()

    val SIZES_BY_ID = linkedMapOf<String, SizeNode>()
    val SIZE_ITEMS_BY_PAGE = linkedMapOf<Int, MutableList<CachedSizeItem>>()
    val ACTION_ITEMS_BY_PAGE = linkedMapOf<Int, MutableList<CachedActionItem>>()

    var MENU_MAX_PAGE: Int = 1
        private set

    private data class ParsedSizeEntry(val node: SizeNode, val cachedItem: CachedSizeItem, val page: Int)
    private data class RawActionItem(
        val action: MenuAction,
        val material: Material,
        val name: String,
        val slots: List<Int>,
        val lore: List<String>,
        val modelData: Int,
        val flags: List<ItemFlag>
    )

    /**
     * Rebuilds the runtime cache
     */
    fun loadFromConfigs(sizesConfig: YamlDocument, mainMenuConfig: YamlDocument, plugin: AliienResize) {
        SIZES_BY_ID.clear()
        SIZE_ITEMS_BY_PAGE.clear()
        ACTION_ITEMS_BY_PAGE.clear()
        MENU_MAX_PAGE = 1

        val maxSlots = MENU_ROWS * 9

        // Parse size nodes
        sizesConfig.getSection("sizes")?.let { section ->
            section.getRoutesAsStrings(false)
                .mapNotNull { rawKey -> parseSizeEntry(section.getSection(rawKey), rawKey, maxSlots, plugin) }
                .forEach { cacheSizeEntry(it) }
        } ?: plugin.logger.warning("No 'sizes' section found in sizes.yml.")

        // Parse action items
        mainMenuConfig.getSection("items")?.let { section ->
            section.getRoutesAsStrings(false)
                .map { rawKey -> parseRawActionItem(section.getSection(rawKey), rawKey, plugin) }
                .forEach { expandActionItem(it, maxSlots) }
        } ?: plugin.logger.info("No action items found in main-menu.yml.")

        // Sort lists
        SIZE_ITEMS_BY_PAGE.values.forEach { items -> items.sortBy { it.slot } }
        ACTION_ITEMS_BY_PAGE.values.forEach { items -> items.sortBy { it.slot } }

        plugin.logger.info("Loaded ${SIZES_BY_ID.size} sizes across $MENU_MAX_PAGE page(s).")
    }

    private fun parseSizeEntry(section: Section, rawKey: String, maxSlots: Int, plugin: AliienResize): ParsedSizeEntry? {
        val id = rawKey.lowercase(Locale.ROOT)
        val slot = section.getInt("gui.slot", 0)

        if (slot !in 0..<maxSlots) {
            plugin.logger.warning("Skipping size '$id': slot $slot is outside GUI bounds.")
            return null
        }

        val scale = section.getDouble("scale", 1.0)
        val permission = section.getString("permission", "")
        val page = section.getInt("gui.page", 1).coerceAtLeast(1)

        val specificLore = section.getStringList("gui.lore") ?: emptyList()
        val specificNoPermLore = section.getStringList("gui.lore-without-perm") ?: emptyList()

        // Fallbacks
        val rawLore = specificLore.ifEmpty { MENU_DEFAULT_LORE }
        val rawNoPermLore = specificNoPermLore.ifEmpty { MENU_DEFAULT_LORE_NO_PERM }

        val itemFlags = parseItemFlags(section.getStringList("gui.item-flags"))
        val material = parseMaterial(section.getString("gui.material", "STONE"), Material.STONE, id, plugin)
        val name = section.getString("gui.name", "<green>$rawKey")
        val modelData = section.getInt("gui.model-data", 0)

        val purchasable = section.getBoolean("price.enabled", false)
        val currency = section.getString("price.currency", "VAULT")
        val amount = section.getDouble("price.amount", 0.0)

        // String evaluations
        val priceText = if (round(amount) == amount) amount.toLong().toString() else amount.toString()
        val scaleText = scale.toString()
        val suffixText = if (purchasable) plugin.currencyManager.getSuffix(id) else ""

        val availableLore = replacePlaceholders(rawLore, priceText, scaleText, permission, suffixText)
        val noPermLore = replacePlaceholders(rawNoPermLore, priceText, scaleText, permission, suffixText)

        val flagsArray = itemFlags.toTypedArray()
        val availableItem = buildItem(material, name, availableLore, modelData, flagsArray, false)
        val selectedItem = buildItem(material, name, availableLore, modelData, flagsArray, true)
        val noPermItem = buildItem(MENU_LOCKED_MATERIAL, name, noPermLore, modelData, flagsArray, false)

        val guiData = GuiData(material.name, slot, page, name, specificLore, specificNoPermLore, modelData, itemFlags)
        val priceData = PriceData(purchasable, currency, amount)
        val sizeNode = SizeNode(id, scale, permission, guiData, priceData)
        val cachedItem = CachedSizeItem(slot, id, permission, scale, availableItem, selectedItem, noPermItem)

        return ParsedSizeEntry(sizeNode, cachedItem, page)
    }

    private fun parseRawActionItem(section: Section, rawKey: String, plugin: AliienResize): RawActionItem {
        val actionStr = section.getString("action", "NONE").uppercase(Locale.ROOT)
        val parsedAction = runCatching { MenuAction.valueOf(actionStr) }.getOrElse {
            plugin.logger.warning("Invalid action in main-menu.yml for item '$rawKey'. Defaulting to NONE.")
            MenuAction.NONE
        }

        return RawActionItem(
            action = parsedAction,
            material = parseMaterial(section.getString("material", "STONE"), Material.STONE, rawKey, plugin),
            name = section.getString("name", " "),
            slots = section.getIntList("slots") ?: emptyList(),
            lore = section.getStringList("lore") ?: emptyList(),
            modelData = section.getInt("model-data", 0),
            flags = parseItemFlags(section.getStringList("item-flags"))
        )
    }

    private fun expandActionItem(rawItem: RawActionItem, maxSlots: Int) {
        val flagsArray = rawItem.flags.toTypedArray()

        for (page in 1..MENU_MAX_PAGE) {
            if (!shouldDisplayOnPage(rawItem.action, page)) continue

            val targetPage = resolveTargetPage(rawItem.action, page)
            val targetPageText = targetPage.toString()

            val resolvedName = rawItem.name.replace("%target_page%", targetPageText)
            val resolvedLore = replacePlaceholders(rawItem.lore, "", "", "", "", targetPageText)
            val cachedItem = buildItem(rawItem.material, resolvedName, resolvedLore, rawItem.modelData, flagsArray, false)

            for (slot in rawItem.slots) {
                if (slot in 0..<maxSlots) {
                    ACTION_ITEMS_BY_PAGE.computeIfAbsent(page) { mutableListOf() }.add(
                        CachedActionItem(slot, rawItem.action, targetPage, cachedItem)
                    )
                }
            }
        }
    }

    private fun cacheSizeEntry(entry: ParsedSizeEntry) {
        SIZES_BY_ID[entry.node.id] = entry.node
        SIZE_ITEMS_BY_PAGE.computeIfAbsent(entry.page) { mutableListOf() }.add(entry.cachedItem)
        MENU_MAX_PAGE = maxOf(MENU_MAX_PAGE, entry.page)
    }

    private fun shouldDisplayOnPage(action: MenuAction, page: Int): Boolean = when (action) {
        MenuAction.NEXT_PAGE -> page < MENU_MAX_PAGE
        MenuAction.PREVIOUS_PAGE -> page > 1
        else -> true
    }

    private fun resolveTargetPage(action: MenuAction, page: Int): Int = when (action) {
        MenuAction.NEXT_PAGE -> page + 1
        MenuAction.PREVIOUS_PAGE -> page - 1
        else -> page
    }

    private fun parseMaterial(name: String?, fallback: Material, id: String, plugin: AliienResize): Material {
        return name?.takeIf { it.isNotBlank() }
            ?.let { Material.matchMaterial(it.uppercase(Locale.ROOT)) }
            ?: run {
                plugin.logger.warning("Invalid material '$name' for '$id'. Using ${fallback.name}.")
                fallback
            }
    }

    private fun parseItemFlags(flags: List<String>?): List<ItemFlag> {
        return flags?.mapNotNull { flag ->
            runCatching { ItemFlag.valueOf(flag.uppercase(Locale.ROOT)) }.getOrNull()
        } ?: emptyList()
    }

    private fun replacePlaceholders(
        list: List<String>,
        price: String = "",
        scale: String = "",
        permission: String = "",
        suffix: String = "",
        targetPage: String = ""
    ): List<String> {
        return list.map { line ->
            line.replace("%price%", price)
                .replace("%scale%", scale)
                .replace("%permission%", permission)
                .replace("%suffix%", suffix)
                .replace("%target_page%", targetPage)
        }
    }

    private fun buildItem(material: Material, name: String, lore: List<String>, modelData: Int, flags: Array<ItemFlag>, glow: Boolean): ItemStack {
        return ItemBuilder(material)
            .name(name)
            .stringLore(lore)
            .customModelData(modelData)
            .addFlags(*flags)
            .glow(glow)
            .build()
    }
}