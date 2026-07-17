package com.aliiensmp.aliienResize.config.data

import com.aliiensmp.aliienResize.menus.MenuAction
import org.bukkit.inventory.ItemStack

data class CachedActionItem(
    val slot: Int,
    val action: MenuAction,
    val targetPage: Int,
    val item: ItemStack
)
