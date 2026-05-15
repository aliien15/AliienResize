package com.aliiensmp.aliienResize.Menus;

import com.aliiensmp.aliienResize.Config.Confirmation;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.core.items.ItemBuilder;
import com.aliiensmp.core.menu.AliienGUI;
import com.aliiensmp.core.menu.ClickableItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConfirmationMenu {

    public void openMenu(Player player, SizeNode sizeNode, ItemStack displayItem, Runnable onConfirm, Runnable onCancel) {
        AliienGUI gui = new AliienGUI(Confirmation.CONFIRMATION_MENU_TITLE, Confirmation.CONFIRMATION_MENU_ROWS);

        // Set the display item
        Confirmation.CONFIRMATION_MENU_DISPLAY_SLOTS.forEach(slot -> gui.setItem(slot, ClickableItem.empty(displayItem)));

        // Format the price for the lore price placeholder
        double price = sizeNode.price().price();
        String formattedPrice = (Math.rint(price) == price) ? String.valueOf((long) price) : String.valueOf(price);

        Confirmation.CONFIRMATION_MENU_ITEMS.forEach(buttonData -> {

            // Parse the %price% placeholder
            List<String> resolvedLore = buttonData.lore().stream()
                    .map(line -> line.replace("%price%", formattedPrice))
                    .toList();

            ItemStack item = new ItemBuilder(buttonData.material())
                    .name(buttonData.name())
                    .stringLore(resolvedLore)
                    .customModelData(buttonData.modelData())
                    .addFlags(buttonData.flags())
                    .build();

            ClickableItem clickableItem = switch (buttonData.action()) {
                case CONFIRM -> ClickableItem.of(item, event -> {
                    player.closeInventory();
                    onConfirm.run();
                });
                case CANCEL -> ClickableItem.of(item, event -> {
                    if (Settings.SOUNDS_ENABLED) Settings.CLICK_SOUND.play(player);
                    onCancel.run();
                });
                case NONE -> ClickableItem.empty(item);
            };

            // Set the item in all slots
            buttonData.slots().forEach(slot -> gui.setItem(slot, clickableItem));
        });

        gui.open(player, 1);
    }
}