package com.aliiensmp.aliienResize.menus.main

import com.aliiensmp.aliienResize.AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.Sizes
import com.aliiensmp.aliienResize.config.data.CachedActionItem
import com.aliiensmp.aliienResize.config.data.CachedSizeItem
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.economy.CurrencyProvider
import com.aliiensmp.aliienResize.menus.confirmation.ConfirmationMenu
import com.aliiensmp.aliienResize.utils.ResizeUtils
import com.aliiensmp.core.menu.AliienGUI
import com.aliiensmp.core.menu.ClickableItem
import com.aliiensmp.core.utils.DebugUtils
import com.aliiensmp.core.utils.MessageUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ResizeMenu(private val plugin: AliienResize) {

    fun openMenu(player: Player, requestedPage: Int) {
        openMenu(player, requestedPage, SizeFilter.ALL)
    }

    fun openMenu(player: Player, requestedPage: Int, sizeFilter: SizeFilter) {
        val page = sanitizePage(requestedPage)
        DebugUtils.send("Constructing ResizeMenu (Page: $page) for ${player.name}")

        val currentPlayerScale = plugin.playerDataService.getPlayerData(player.uniqueId).scale
        val gui = AliienGUI(Sizes.MENU_TITLE, Sizes.MENU_ROWS)
        val sizeItems = Sizes.SIZE_MENU_ITEMS.filter { item -> shouldDisplayItem(player, item, sizeFilter) }

        populateSizes(gui, player, currentPlayerScale, page, sizeItems)
        populateActionItems(gui, player, page, sizeFilter)
        gui.open(player, page)
    }

    private fun shouldDisplayItem(player: Player, item: CachedSizeItem, sizeFilter: SizeFilter): Boolean {
        return when (sizeFilter) {
            SizeFilter.ALL -> true
            SizeFilter.OWNED -> player.hasPermission(item.permission)
            SizeFilter.LOCKED -> !player.hasPermission(item.permission)
            SizeFilter.PURCHASABLE -> Sizes.SIZES_BY_ID[item.id]?.price?.isPurchasable
            SizeFilter.SMALL -> Sizes.SIZES_BY_ID[item.id]?.scale!! < 1.0
            SizeFilter.BIG -> Sizes.SIZES_BY_ID[item.id]?.scale!! > 1.0
        } == true
    }

    private fun populateSizes(gui: AliienGUI, player: Player, currentScale: Double, page: Int, items: List<CachedSizeItem>) {
        gui.setItems(
            Sizes.SIZES_SLOTS,
            items,
            page
        ) { cachedItem ->
            val sizeNode = Sizes.SIZES_BY_ID[cachedItem.id]!!
            val hasAccess = hasPermission(player, sizeNode.permission)
            val displayItem = selectSizeItem(cachedItem, hasAccess, currentScale)
            val confirmationItem = cachedItem.availableItem.clone()

            ClickableItem.of(displayItem) {
                handleSizeClick(player, sizeNode, page, confirmationItem)
            }
        }
    }

    private fun populateActionItems(gui: AliienGUI, player: Player, page: Int, sizeFilter: SizeFilter) {
        Sizes.ACTION_ITEMS_BY_PAGE[page]?.forEach { cachedItem ->
            val item = cachedItem.item.clone()
            if (MenuAction.FILTER == cachedItem.action) {
                item.itemMeta = item.itemMeta?.apply {
                    if (hasDisplayName()) {
                        val currentName = displayName()
                        if (currentName != null) {
                            displayName(currentName.replaceText { builder ->
                                builder.matchLiteral("%filter%").replacement(sizeFilter.displayName)
                            })
                        }
                    }

                    if (hasLore()) {
                        val currentLore = lore()
                        if (currentLore != null) {
                            lore(currentLore.map { line ->
                                line.replaceText { builder ->
                                    builder.matchLiteral("%filter%").replacement(sizeFilter.displayName)
                                }
                            })
                        }
                    }
                }
            }
            val clickableItem = if (MenuAction.NONE == cachedItem.action)
                ClickableItem.empty(item)
            else
                ClickableItem.of(item) { handleActionClick(player, cachedItem, page, sizeFilter) }
            gui.setItem(cachedItem.slot, clickableItem)
        }
    }

    private fun handleActionClick(player: Player, cachedItem: CachedActionItem, page: Int, sizeFilter: SizeFilter) {
        DebugUtils.send("Player ${player.name} clicked ActionItem: ${cachedItem.action}")
        when (cachedItem.action) {
            MenuAction.NEXT_PAGE, MenuAction.PREVIOUS_PAGE -> {
                Settings.CLICK_SOUND?.play(player, Settings.SOUNDS_ENABLED)
                openMenu(player, cachedItem.targetPage)
            }

            MenuAction.CLEAR -> {
                player.closeInventory()

                if (!ResizeUtils.hasEnoughSpace(player, 1.0)) {
                    DebugUtils.send("Player ${player.name} failed clear condition: Blocked by space.")
                    MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL)
                    Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
                    return
                }

                plugin.playerDataService.resetScale(player) {
                    MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT)
                    Settings.CLEAR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
                }
            }

            MenuAction.FILTER -> {
                openMenu(player, page, sizeFilter.getNext())
            }

            MenuAction.NONE -> {}
        }
    }

    private fun handleSizeClick(player: Player, sizeNode: SizeNode, currentPage: Int, confirmationItem: ItemStack) {
        DebugUtils.send("Player ${player.name} clicked SizeNode: ${sizeNode.id}")
        if (hasPermission(player, sizeNode.permission)) {
            player.applyScale(sizeNode)
            return
        }

        if (sizeNode.price.isPurchasable) {
            DebugUtils.send("Size ${sizeNode.id} is purchasable. Processing logic for ${player.name}")
            if (Settings.CONFIRMATION_MENU_ENABLED) {
                ConfirmationMenu().openMenu(player, sizeNode, confirmationItem, { handlePurchase(player, sizeNode) }, { openMenu(player, currentPage) })
            } else {
                handlePurchase(player, sizeNode)
            }
            return
        }

        DebugUtils.send("Player ${player.name} denied access to ${sizeNode.id}: No Permission")
        MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM)
    }

    private fun Player.applyScale(sizeNode: SizeNode) {
        if (!ResizeUtils.hasEnoughSpace(this, sizeNode.scale)) {
            DebugUtils.send("Player ${this.name} failed to resize to ${sizeNode.id}: Not enough space.")
            MessageUtils.send(this, Messages.PREFIX, Messages.RESIZE_FAIL)
            Settings.ERROR_SOUND?.play(this, Settings.SOUNDS_ENABLED)
            return
        }

        this.closeInventory()

        plugin.playerDataService.applyScale(this, sizeNode.scale) {
            MessageUtils.send(this, Messages.PREFIX, Messages.RESIZE_SUCCESS)
            Settings.SUCCESS_SOUND?.play(this, Settings.SOUNDS_ENABLED)
        }
    }

    private fun handlePurchase(player: Player, sizeNode: SizeNode) {
        DebugUtils.send("Initiating purchase transaction for ${player.name} (Size: ${sizeNode.id})")
        if (!plugin.vaultExpansion.hasPermissions) {
            DebugUtils.send(Level.SEVERE, "Purchase transaction failed: Vault permissions provider not found.")
            plugin.logger.log(Level.SEVERE, "Sizes purchase cancelled due to not finding any Vault permissions provider.")
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)

            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission("aliien.resize.admin") }
                .forEach {
                    MessageUtils.send(
                        it,
                        Messages.PREFIX,
                        "<red>Vault permissions provider is currently not setup properly, which just prevented a player from purchasing a size!"
                    )
                    Settings.ERROR_SOUND?.play(it, Settings.SOUNDS_ENABLED)
                }
            return
        }

        val currency: CurrencyProvider? = plugin.currencyManager.getCurrency(sizeNode.price.currency)

        if (currency == null || !currency.isValid) {
            DebugUtils.send(Level.SEVERE, "Purchase transaction failed: Invalid currency provider (${sizeNode.price.currency})")
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE)
            return
        }

        val price = sizeNode.price.price
        val formattedPrice = if (round(price) == price) price.toLong().toString() else price.toString()
        val suffixText = plugin.currencyManager.getSuffix(sizeNode.price.currency)

        if (!currency.hasBalance(player, price)) {
            DebugUtils.send("Purchase transaction failed for ${player.name}: Insufficient funds.")
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            MessageUtils.send(
                player,
                Messages.PREFIX,
                Messages.PURCHASE_FAIL.replace("%price%", formattedPrice).replace("%suffix%", suffixText)
            )
            return
        }

        if (!currency.withdraw(player, price)) {
            DebugUtils.send(Level.SEVERE, "Purchase transaction failed for ${player.name}: Withdraw error.")
            Settings.ERROR_SOUND?.play(player, Settings.SOUNDS_ENABLED)
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE)
            return
        }

        DebugUtils.send("Purchase transaction successful for ${player.name} (Size: ${sizeNode.id}). Granting permissions and applying scale.")
        player.grantPermission(sizeNode.permission)
        plugin.fileUtils.logPurchase(player, sizeNode, sizeNode.price)
        player.applyScale(sizeNode)

        MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_SUCCESS)
    }

    private fun selectSizeItem(cachedItem: CachedSizeItem, hasAccess: Boolean, currentScale: Double): ItemStack {
        if (!hasAccess)
            return cachedItem.noPermItem.clone()

        if (currentScale == cachedItem.scale)
            return cachedItem.selectedItem.clone()

        return cachedItem.availableItem.clone()
    }

    private fun sanitizePage(requestedPage: Int): Int {
        val maxPage = max(1, Sizes.MENU_MAX_PAGE)
        return max(1, min(requestedPage, maxPage))
    }

    private fun hasPermission(player: Player, permission: String?): Boolean {
        return (permission.isNullOrBlank() || player.hasPermission(permission))
    }

    private fun Player.grantPermission(permission: String) {
        plugin.vaultExpansion.permissions?.playerAdd(null, this, permission)
    }
}