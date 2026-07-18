package com.aliiensmp.aliienResize.menus

import AliienResize
import com.aliiensmp.aliienResize.config.Messages
import com.aliiensmp.aliienResize.config.Settings
import com.aliiensmp.aliienResize.config.Sizes
import com.aliiensmp.aliienResize.config.data.CachedActionItem
import com.aliiensmp.aliienResize.config.data.CachedSizeItem
import com.aliiensmp.aliienResize.config.data.SizeNode
import com.aliiensmp.aliienResize.economy.CurrencyProvider
import com.aliiensmp.aliienResize.listeners.PlayerConnectionListener
import com.aliiensmp.aliienResize.utils.ResizeUtils
import com.aliiensmp.core.menu.AliienGUI
import com.aliiensmp.core.menu.ClickableItem
import com.aliiensmp.core.utils.MessageUtils
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Level
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ResizeMenu(private val plugin: AliienResize) {

    fun openMenu(player: Player, requestedPage: Int) {
        val page = sanitizePage(requestedPage)
        val currentPlayerScale = player.getAttribute(Attribute.GENERIC_SCALE)?.value ?: 1.0
        val gui = AliienGUI(Sizes.MENU_TITLE, Sizes.MENU_ROWS)

        populateSizes(gui, player, currentPlayerScale, page)
        populateActionItems(gui, player, page)
        gui.open(player, page)
    }

    private fun populateSizes(gui: AliienGUI, player: Player, currentScale: Double, page: Int) {
        Sizes.SIZE_ITEMS_BY_PAGE[page]?.forEach {
            val sizeNode = Sizes.SIZES_BY_ID[it.id]!!
            val hasAccess = hasPermission(player, sizeNode.permission)
            val displayItem = selectSizeItem(it, hasAccess, currentScale)
            val confirmationItem = it.availableItem.clone()

            gui.setItem(it.slot, ClickableItem.of(displayItem
            ) { handleSizeClick(player, sizeNode, page, confirmationItem) })
        }
    }

    private fun populateActionItems(gui: AliienGUI, player: Player, page: Int) {
        Sizes.ACTION_ITEMS_BY_PAGE[page]!!.forEach { cachedItem ->
            val item = cachedItem.item.clone()
            val clickableItem = if (MenuAction.NONE == cachedItem.action)
                ClickableItem.empty(item)
            else
                ClickableItem.of(
                    item
                ) { handleActionClick(player, cachedItem) }
            gui.setItem(cachedItem.slot, clickableItem)
        }
    }

    private fun handleActionClick(player: Player, cachedItem: CachedActionItem) {
        when (cachedItem.action) {
            MenuAction.NEXT_PAGE, MenuAction.PREVIOUS_PAGE -> {
                if (Settings.SOUNDS_ENABLED) Settings.CLICK_SOUND!!.play(player)
                openMenu(player, cachedItem.targetPage)
            }

            MenuAction.CLEAR -> {
                player.closeInventory()

                player.runSync {
                    player.getAttribute(Attribute.GENERIC_SCALE)?.let { attribute ->
                        attribute.baseValue = 1.0
                        PlayerConnectionListener.cache[player.uniqueId] = 1.0
                    }

                    MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT)
                    if (Settings.SOUNDS_ENABLED) Settings.CLEAR_SOUND!!.play(player)
                }
            }

            MenuAction.NONE -> {}
        }
    }

    private fun handleSizeClick(player: Player, sizeNode: SizeNode, currentPage: Int, confirmationItem: ItemStack) {
        if (hasPermission(player, sizeNode.permission)) {
            player.applyScale(sizeNode)
            return
        }

        if (sizeNode.price.isPurchasable) {
            if (Settings.CONFIRMATION_MENU_ENABLED) {
                ConfirmationMenu().openMenu(player, sizeNode, confirmationItem, { handlePurchase(player, sizeNode)}, { openMenu(player, currentPage)} )
            } else {
                handlePurchase(player, sizeNode)
            }
            return
        }

        MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM)
    }

    private fun Player.applyScale(sizeNode: SizeNode) {
        if (!ResizeUtils.hasEnoughSpace(this, sizeNode.scale)) {
            MessageUtils.send(this, Messages.PREFIX, Messages.RESIZE_FAIL)

            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(this)

            return
        }

        this.closeInventory()

        this.runSync {
            this.getAttribute(Attribute.GENERIC_SCALE)?.let {
                it.baseValue = sizeNode.scale
            }

            PlayerConnectionListener.cache[this.uniqueId] = sizeNode.scale

            MessageUtils.send(this, Messages.PREFIX, Messages.RESIZE_SUCCESS)
            if (Settings.SOUNDS_ENABLED)
                Settings.SUCCESS_SOUND?.play(this)
        }
    }

    private fun handlePurchase(player: Player, sizeNode: SizeNode) {
        if (!plugin.vaultExpansion.hasPermissions) {
            plugin.logger.log(
                Level.SEVERE,
                "Sizes purchase cancelled due to not finding any Vault permissions provider."
            )
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)

            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission("aliien.resize.admin") }
                .forEach {
                    MessageUtils.send(
                        it,
                        Messages.PREFIX,
                        "<red>Vault permissions provider is currently not setup properly, which just prevented a player from purchasing a size!"
                    )
                    if (Settings.SOUNDS_ENABLED)
                        Settings.ERROR_SOUND?.play(it)
                }
        }

        val currency: CurrencyProvider? = plugin.currencyManager.getCurrency(sizeNode.price.currency)

        if (currency == null || !currency.isValid) {
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND?.play(player)
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE)
            return
        }

        val price = sizeNode.price.price
        val formattedPrice = if (round(price) == price) price.toLong().toString() else price.toString()
        val suffixText = plugin.currencyManager.getSuffix(sizeNode.price.currency)

        if (!currency.hasBalance(player, price)) {
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND!!.play(player)

            MessageUtils.send(
                player,
                Messages.PREFIX,
                Messages.PURCHASE_FAIL.replace("%price%", formattedPrice).replace("%suffix%", suffixText)
            )
            return
        }

        if (!currency.withdraw(player, price)) {
            if (Settings.SOUNDS_ENABLED)
                Settings.ERROR_SOUND!!.play(player)

            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE)
            return
        }

        player.grantPermission(sizeNode.permission)
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

    private fun Player.runSync(task: Runnable) {
        this.scheduler.run(plugin, { _: ScheduledTask? -> task.run() }, null)
    }

    private fun Player.grantPermission(permission: String) {
        plugin.vaultExpansion.permissions?.playerAdd(null, this, permission)
    }
}