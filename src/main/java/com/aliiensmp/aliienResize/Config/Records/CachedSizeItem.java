package com.aliiensmp.aliienResize.Config.Records;

import org.bukkit.inventory.ItemStack;

public record CachedSizeItem(
        int slot,
        String id,
        String permission,
        double scale,
        ItemStack availableItem,
        ItemStack selectedItem,
        ItemStack noPermItem
) {}