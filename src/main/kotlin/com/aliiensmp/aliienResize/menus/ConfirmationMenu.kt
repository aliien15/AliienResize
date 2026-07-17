package com.aliiensmp.aliienResize.menus

import com.aliiensmp.aliienResize.config.Confirmation
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.core.items.ItemBuilder
import com.aliiensmp.core.menu.AliienGUI
import com.aliiensmp.core.menu.ClickableItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.round

class ConfirmationMenu {

    fun openMenu(player: Player, sizeNode: SizeNode, displayItem: ItemStack, onConfirm: Runnable, onCancel: Runnable) {
        val gui = AliienGUI(Confirmation.CONFIRMATION_MENU_TITLE, Confirmation.CONFIRMATION_MENU_ROWS)

        Confirmation.CONFIRMATION_MENU_DISPLAY_SLOTS.forEach { slot ->
            gui.setItem(slot, ClickableItem.empty(displayItem))
        }

        val price = sizeNode.price.price
        val formattedPrice = if (round(price) == price) price.toLong().toString() else price.toString()

        Confirmation.CONFIRMATION_MENU_ITEMS.forEach { buttonData ->
            val resolvedLore = buttonData.lore
                .map { it.replace("%price%", formattedPrice) }

            val item = ItemBuilder(buttonData.material)
                .name(buttonData.name)
                .stringLore(resolvedLore)
                .customModelData(buttonData.modelData)
                .addFlags(*buttonData.flags)
                .build()

            val clickableItem = when (buttonData.action) {
                ConfirmationMenuAction.CONFIRM -> ClickableItem.of(item) { _: InventoryClickEvent? ->
                    player.closeInventory()
                    onConfirm.run()
                }

                ConfirmationMenuAction.CANCEL -> ClickableItem.of(item) { _: InventoryClickEvent? ->
                    if (Settings.SOUNDS_ENABLED)
                        Settings.CLICK_SOUND?.play(player)

                    onCancel.run()
                }

                ConfirmationMenuAction.NONE -> ClickableItem.empty(item)
            }

            buttonData.slots.forEach { gui.setItem(it, clickableItem) }
        }

        gui.open(player, 1)
    }
}