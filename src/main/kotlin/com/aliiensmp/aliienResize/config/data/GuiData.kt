package com.aliiensmp.aliienResize.config.data

import org.bukkit.inventory.ItemFlag

data class GuiData(
    val material: String,
    val slot: Int,
    val page: Int,
    val name: String,
    val lore: List<String>,
    val loreWithoutPerm: List<String>,
    val modelData: Int,
    val itemFlags: List<ItemFlag>
)