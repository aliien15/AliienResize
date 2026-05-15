package com.aliiensmp.aliienResize.Config.Records;

import com.aliiensmp.aliienResize.Menus.MenuAction;
import org.bukkit.inventory.ItemStack;

public record CachedActionItem(
        int slot,
        MenuAction action,
        int targetPage,
        ItemStack item
) {}