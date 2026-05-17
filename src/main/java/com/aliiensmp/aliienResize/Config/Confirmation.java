package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Menus.ConfirmationMenuAction;
import com.aliiensmp.core.config.Key;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.aliiensmp.aliienResize.Menus.ConfirmationMenuAction.NONE;

public class Confirmation {

    @Key("menu-settings.title")
    public static String CONFIRMATION_MENU_TITLE = "<dark_gray>Confirm Purchase?</dark_gray>";

    @Key("menu-settings.display-slots")
    public static List<Integer> CONFIRMATION_MENU_DISPLAY_SLOTS = List.of(13);

    public static int CONFIRMATION_MENU_ROWS;

    public static final List<ButtonData> CONFIRMATION_MENU_ITEMS = new ArrayList<>();

    public record ButtonData(ConfirmationMenuAction action, Material material, String name, List<String> lore, List<Integer> slots, int modelData, ItemFlag[] flags) {}

    public static void loadFromConfig(YamlDocument config, AliienResize plugin) {

        CONFIRMATION_MENU_ROWS = Math.max(1, Math.min(config.getInt("menu-settings.rows", 3), 6));

        CONFIRMATION_MENU_ITEMS.clear();

        Optional.ofNullable(config.getSection("items")).ifPresent(section -> {
            section.getRoutesAsStrings(false).forEach(key -> {
                CONFIRMATION_MENU_ITEMS.add(parseButton(section.getSection(key), key, plugin));
            });
        });
    }

    private static ButtonData parseButton(Section sec, String key, AliienResize plugin) {
        ConfirmationMenuAction action;
        try {
            action = ConfirmationMenuAction.valueOf(sec.getString("action", "NONE").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid action in confirmation-menu.yml for item '" + key + "'. Defaulting to NONE.");
            action = NONE;
        }

        Material mat = Optional.ofNullable(Material.matchMaterial(sec.getString("material", "")))
                .orElse(Material.STONE); // Fallback to STONE if invalid

        List<ItemFlag> flags = sec.getStringList("item-flags").stream()
                .map(flag -> {
                    try {
                        return ItemFlag.valueOf(flag.toUpperCase(Locale.ROOT));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        return new ButtonData(
                action,
                mat,
                sec.getString("name", " "),
                sec.getStringList("lore"),
                sec.getIntList("slots"),
                sec.getInt("model-data", 0),
                flags.toArray(ItemFlag[]::new)
        );
    }
}