package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.core.config.Key;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class Confirmation {

    @Key("menu-settings.title")
    public static String CONFIRMATION_MENU_TITLE = "<dark_gray>Confirm Purchase?</dark_gray>";

    @Key("menu-settings.display-slots")
    public static List<Integer> CONFIRMATION_MENU_DISPLAY_SLOTS = List.of(13);

    public static int CONFIRMATION_MENU_ROWS;
    public static ButtonData CONFIRMATION_MENU_CONFIRM_BUTTON;
    public static ButtonData CONFIRMATION_MENU_CANCEL_BUTTON;

    // Internal record to cache raw button data
    public record ButtonData(Material material, String name, List<String> lore, List<Integer> slots, int modelData, ItemFlag[] flags) {}

    public void loadFromConfig(YamlDocument config) {

        CONFIRMATION_MENU_ROWS = Math.max(1, Math.min(config.getInt("menu-settings.rows", 3), 6));

        CONFIRMATION_MENU_CONFIRM_BUTTON = parseButton(config.getSection("items.confirm"), Material.LIME_STAINED_GLASS_PANE);
        CONFIRMATION_MENU_CANCEL_BUTTON = parseButton(config.getSection("items.cancel"), Material.RED_STAINED_GLASS_PANE);
    }

    private ButtonData parseButton(Section section, Material fallback) {
        return Optional.ofNullable(section).map(sec -> {
            Material mat = Optional.ofNullable(Material.matchMaterial(sec.getString("material", "")))
                    .orElse(fallback);

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
                    mat,
                    sec.getString("name", " "),
                    sec.getStringList("lore"),
                    sec.getIntList("slots"),
                    sec.getInt("model-data", 0),
                    flags.toArray(ItemFlag[]::new)
            );
        }).orElse(new ButtonData(fallback, "Error", List.of(), List.of(), 0, new ItemFlag[0]));
    }
}