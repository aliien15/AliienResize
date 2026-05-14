package com.aliiensmp.aliienResize.Config.Records;

import org.bukkit.inventory.ItemFlag;
import java.util.List;

public record GuiData(
        String material,
        int slot,
        int page,
        String name,
        List<String> lore,
        List<String> loreWithoutPerm,
        int modelData,
        List<ItemFlag> itemFlags
) {}