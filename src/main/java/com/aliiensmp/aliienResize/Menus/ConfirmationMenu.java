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

        // Format the price for the lore
        double price = sizeNode.price().price();
        String formattedPrice = (Math.rint(price) == price) ? String.valueOf((long) price) : String.valueOf(price);

        // Confirm buttons
        Confirmation.ButtonData confirmData = Confirmation.CONFIRMATION_MENU_CONFIRM_BUTTON;
        List<String> confirmLore = confirmData.lore().stream()
                .map(line -> line.replace("%price%", formattedPrice))
                .toList();

        ItemStack confirmItem = new ItemBuilder(confirmData.material())
                .name(confirmData.name())
                .stringLore(confirmLore)
                .customModelData(confirmData.modelData())
                .addFlags(confirmData.flags())
                .build();

        confirmData.slots().forEach(slot ->
                gui.setItem(slot, ClickableItem.of(confirmItem, event -> {
                    player.closeInventory();
                    onConfirm.run();
                }))
        );

        // Cancel buttons
        Confirmation.ButtonData cancelData = Confirmation.CONFIRMATION_MENU_CANCEL_BUTTON;
        ItemStack cancelItem = new ItemBuilder(cancelData.material())
                .name(cancelData.name())
                .stringLore(cancelData.lore())
                .customModelData(cancelData.modelData())
                .addFlags(cancelData.flags())
                .build();

        cancelData.slots().forEach(slot ->
                gui.setItem(slot, ClickableItem.of(cancelItem, event -> {
                    Settings.CLICK_SOUND.play(player);
                    onCancel.run();
                }))
        );

        gui.open(player, 1);
    }
}