package com.aliiensmp.aliienResize.config.data

import org.bukkit.inventory.ItemStack

data class CachedSizeItem(
    val slot: Int,
    val id: String,
    val permission: String,
    val scale: Double,
    val availableItem: ItemStack,
    val selectedItem: ItemStack,
    val noPermItem: ItemStack
)
