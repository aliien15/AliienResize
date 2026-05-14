package com.aliiensmp.aliienResize.Config.Records;

import org.bukkit.inventory.ItemStack;

public record CachedActionItem(
        int slot,
        String action,
        int targetPage,
        ItemStack item
) {}