package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Records.CachedActionItem;
import com.aliiensmp.aliienResize.Config.Records.CachedSizeItem;
import com.aliiensmp.aliienResize.Config.Records.GuiData;
import com.aliiensmp.aliienResize.Config.Records.PriceData;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Menus.MenuAction;
import com.aliiensmp.core.items.ItemBuilder;
import com.aliiensmp.core.lib.boostedyaml.YamlDocument;
import com.aliiensmp.core.lib.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Sizes {

    // GUI settings cache
    public static String MENU_TITLE = "<dark_gray>Select your Size</dark_gray>";
    public static int MENU_ROWS = 6;
    public static Material MENU_LOCKED_MATERIAL = Material.BARRIER;
    public static List<String> MENU_DEFAULT_LORE = List.of();
    public static List<String> MENU_DEFAULT_LORE_NO_PERM = List.of();

    // Data caches
    public static final Map<String, SizeNode> SIZES_BY_ID = new LinkedHashMap<>();
    public static final Map<Integer, List<CachedSizeItem>> SIZE_ITEMS_BY_PAGE = new LinkedHashMap<>();
    public static final Map<Integer, List<CachedActionItem>> ACTION_ITEMS_BY_PAGE = new LinkedHashMap<>();

    public static int MENU_MAX_PAGE = 1;

    // Transport records for streams
    private record ParsedSizeEntry(SizeNode node, CachedSizeItem cachedItem, int page) {}
    private record RawActionItem(MenuAction action, Material material, String name, List<Integer> slots, List<String> lore, int modelData, List<ItemFlag> flags) {}

    /**
     * Rebuilds the runtime cache using functional streams, reading from both configs.
     */
    public static void loadFromConfigs(YamlDocument sizesConfig, YamlDocument mainMenuConfig, AliienResize plugin) {
        SIZES_BY_ID.clear();
        SIZE_ITEMS_BY_PAGE.clear();
        ACTION_ITEMS_BY_PAGE.clear();
        MENU_MAX_PAGE = 1;

        MENU_TITLE = mainMenuConfig.getString("menu-settings.title", "<dark_gray>Select your Size</dark_gray>");
        MENU_ROWS = mainMenuConfig.getInt("menu-settings.rows", 6);
        MENU_LOCKED_MATERIAL = parseMaterial(mainMenuConfig.getString("menu-settings.locked-material", "BARRIER"), Material.BARRIER, "locked-material", plugin);

        MENU_DEFAULT_LORE = Optional.ofNullable(sizesConfig.getStringList("defaults.lore")).orElse(List.of());
        MENU_DEFAULT_LORE_NO_PERM = Optional.ofNullable(sizesConfig.getStringList("defaults.lore-without-perm")).orElse(List.of());

        int maxSlots = MENU_ROWS * 9;

        // Parse size nodes
        Optional.ofNullable(sizesConfig.getSection("sizes")).ifPresentOrElse(
                section -> section.getRoutesAsStrings(false).stream()
                        .map(rawKey -> parseSizeEntry(section.getSection(rawKey), rawKey, maxSlots, plugin))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(Sizes::cacheSizeEntry),
                () -> plugin.getLogger().warning("No 'sizes' section found in sizes.yml.")
        );

        // Parse action items
        Optional.ofNullable(mainMenuConfig.getSection("items")).ifPresentOrElse(
                section -> section.getRoutesAsStrings(false).stream()
                        .map(rawKey -> parseRawActionItem(section.getSection(rawKey), rawKey, plugin))
                        .forEach(rawItem -> expandActionItem(rawItem, maxSlots)),
                () -> plugin.getLogger().info("No action items found in main-menu.yml.")
        );

        // Sort arrays for predictable rendering
        SIZE_ITEMS_BY_PAGE.values().forEach(items -> items.sort(Comparator.comparingInt(CachedSizeItem::slot)));
        ACTION_ITEMS_BY_PAGE.values().forEach(items -> items.sort(Comparator.comparingInt(CachedActionItem::slot)));

        plugin.getLogger().info("Loaded " + SIZES_BY_ID.size() + " sizes across " + MENU_MAX_PAGE + " page(s).");
    }

    private static Optional<ParsedSizeEntry> parseSizeEntry(Section section, String rawKey, int maxSlots, AliienResize plugin) {
        String id = rawKey.toLowerCase(Locale.ROOT);

        int slot = section.getInt("gui.slot", 0);
        if (slot < 0 || slot >= maxSlots) {
            plugin.getLogger().warning("Skipping size '" + id + "': slot " + slot + " is outside GUI bounds.");
            return Optional.empty();
        }

        double scale = section.getDouble("scale", 1.0);
        String permission = section.getString("permission", "");
        int page = Math.max(1, section.getInt("gui.page", 1));

        List<String> specificLore = Optional.ofNullable(section.getStringList("gui.lore")).orElse(List.of());
        List<String> specificNoPermLore = Optional.ofNullable(section.getStringList("gui.lore-without-perm")).orElse(List.of());

        // Fallback to defaults if specific lore is empty
        List<String> rawLore = specificLore.isEmpty() ? MENU_DEFAULT_LORE : specificLore;
        List<String> rawNoPermLore = specificNoPermLore.isEmpty() ? MENU_DEFAULT_LORE_NO_PERM : specificNoPermLore;

        List<ItemFlag> itemFlags = parseItemFlags(section.getStringList("gui.item-flags"), id);
        Material material = parseMaterial(section.getString("gui.material", "STONE"), Material.STONE, id, plugin);
        String name = section.getString("gui.name", "<green>" + rawKey);
        int modelData = section.getInt("gui.model-data", 0);

        boolean purchasable = section.getBoolean("price.enabled", false);
        String currency = section.getString("price.currency", "VAULT");
        double amount = section.getDouble("price.amount", 0.0);

        // Format placeholders
        String priceText = (Math.rint(amount) == amount) ? String.valueOf((long) amount) : String.valueOf(amount);
        String scaleText = String.valueOf(scale);
        String suffixText = purchasable ? plugin.getCurrencyManager().getSuffix(id) : "";

        List<String> availableLore = replacePlaceholders(rawLore, "%price%", priceText, "%scale%", scaleText, "%permission%", permission, "%suffix%", suffixText);
        List<String> noPermLore = replacePlaceholders(rawNoPermLore, "%price%", priceText, "%scale%", scaleText, "%permission%", permission, "%suffix%", suffixText);

        ItemFlag[] flagsArray = itemFlags.toArray(ItemFlag[]::new);

        ItemStack availableItem = buildItem(material, name, availableLore, modelData, flagsArray, false);
        ItemStack selectedItem = buildItem(material, name, availableLore, modelData, flagsArray, true);
        ItemStack noPermItem = buildItem(MENU_LOCKED_MATERIAL, name, noPermLore, modelData, flagsArray, false);

        GuiData guiData = new GuiData(material.name(), slot, page, name, specificLore, specificNoPermLore, modelData, itemFlags);
        PriceData priceData = new PriceData(purchasable, currency, amount);
        SizeNode sizeNode = new SizeNode(id, scale, permission, guiData, priceData);
        CachedSizeItem cachedItem = new CachedSizeItem(slot, id, permission, scale, availableItem, selectedItem, noPermItem);

        return Optional.of(new ParsedSizeEntry(sizeNode, cachedItem, page));
    }

    private static RawActionItem parseRawActionItem(Section section, String rawKey, AliienResize plugin) {
        MenuAction parsedAction;
        try {
            parsedAction = MenuAction.valueOf(section.getString("action", "NONE").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid action in main-menu.yml for item '" + rawKey + "'. Defaulting to NONE.");
            parsedAction = MenuAction.NONE;
        }

        return new RawActionItem(
                parsedAction,
                parseMaterial(section.getString("material", "STONE"), Material.STONE, rawKey, plugin),
                section.getString("name", " "),
                section.getIntList("slots"),
                Optional.ofNullable(section.getStringList("lore")).orElse(List.of()),
                section.getInt("model-data", 0),
                parseItemFlags(section.getStringList("item-flags"), rawKey)
        );
    }

    private static void expandActionItem(RawActionItem rawItem, int maxSlots) {
        ItemFlag[] flagsArray = rawItem.flags().toArray(new ItemFlag[0]);

        for (int page = 1; page <= MENU_MAX_PAGE; page++) {
            if (!shouldDisplayOnPage(rawItem.action(), page)) continue;

            int targetPage = resolveTargetPage(rawItem.action(), page);
            String targetPageText = String.valueOf(targetPage);

            String resolvedName = rawItem.name().replace("%target_page%", targetPageText);
            List<String> resolvedLore = replacePlaceholders(rawItem.lore(), "%target_page%", targetPageText, "", "", "", "", "", "");
            ItemStack cachedItem = buildItem(rawItem.material(), resolvedName, resolvedLore, rawItem.modelData(), flagsArray, false);

            for (int slot : rawItem.slots()) {
                if (slot >= 0 && slot < maxSlots) {
                    ACTION_ITEMS_BY_PAGE.computeIfAbsent(page, k -> new ArrayList<>()).add(
                            new CachedActionItem(slot, rawItem.action(), targetPage, cachedItem)
                    );
                }
            }
        }
    }

    private static void cacheSizeEntry(ParsedSizeEntry entry) {
        SIZES_BY_ID.put(entry.node().id(), entry.node());
        SIZE_ITEMS_BY_PAGE.computeIfAbsent(entry.page(), k -> new ArrayList<>()).add(entry.cachedItem());
        MENU_MAX_PAGE = Math.max(MENU_MAX_PAGE, entry.page());
    }

    private static boolean shouldDisplayOnPage(MenuAction action, int page) {
        return switch (action) {
            case NEXT_PAGE -> page < MENU_MAX_PAGE;
            case PREVIOUS_PAGE -> page > 1;
            default -> true;
        };
    }

    private static int resolveTargetPage(MenuAction action, int page) {
        return switch (action) {
            case NEXT_PAGE -> page + 1;
            case PREVIOUS_PAGE -> page - 1;
            default -> page;
        };
    }

    private static Material parseMaterial(String name, Material fallback, String id, AliienResize plugin) {
        return Optional.ofNullable(name)
                .filter(n -> !n.isBlank())
                .map(n -> Material.matchMaterial(n.toUpperCase(Locale.ROOT)))
                .orElseGet(() -> {
                    plugin.getLogger().warning("Invalid material '" + name + "' for '" + id + "'. Using " + fallback.name() + ".");
                    return fallback;
                });
    }

    private static List<ItemFlag> parseItemFlags(List<String> flags, String id) {
        return Optional.ofNullable(flags).orElse(List.of()).stream()
                .map(flag -> {
                    try {
                        return ItemFlag.valueOf(flag.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<String> replacePlaceholders(List<String> list, String p1, String v1, String p2, String v2, String p3, String v3, String p4, String v4) {
        return list.stream()
                .map(line -> line.replace(p1, v1).replace(p2, v2).replace(p3, v3).replace(p4, v4))
                .toList();
    }

    private static ItemStack buildItem(Material material, String name, List<String> lore, int modelData, ItemFlag[] flags, boolean glow) {
        ItemBuilder builder = new ItemBuilder(material)
                .name(name)
                .stringLore(lore)
                .customModelData(modelData)
                .addFlags(flags)
                .glow(glow);

        return builder.build();
    }
}