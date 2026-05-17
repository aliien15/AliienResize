package com.aliiensmp.aliienResize.Menus;

import com.aliiensmp.aliienResize.AliienResize;
import com.aliiensmp.aliienResize.Config.Messages;
import com.aliiensmp.aliienResize.Config.Records.CachedActionItem;
import com.aliiensmp.aliienResize.Config.Records.CachedSizeItem;
import com.aliiensmp.aliienResize.Config.Records.SizeNode;
import com.aliiensmp.aliienResize.Config.Settings;
import com.aliiensmp.aliienResize.Config.Sizes;
import com.aliiensmp.aliienResize.Economy.CurrencyProvider;
import com.aliiensmp.core.menu.AliienGUI;
import com.aliiensmp.core.menu.ClickableItem;
import com.aliiensmp.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.logging.Level;

import static com.aliiensmp.aliienResize.Menus.MenuAction.NONE;

public class ResizeMenu {

    private final AliienResize plugin;

    public ResizeMenu(AliienResize plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, int requestedPage) {
        int page = sanitizePage(requestedPage);

        double currentScale = Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                .map(AttributeInstance::getValue)
                .orElse(1.0);

        AliienGUI gui = new AliienGUI(Sizes.MENU_TITLE, Sizes.MENU_ROWS);

        populateSizes(gui, player, currentScale, page);
        populateActionItems(gui, player, page);

        gui.open(player, page);
    }

    private void populateSizes(AliienGUI gui, Player player, double currentScale, int page) {
        Sizes.SIZE_ITEMS_BY_PAGE.get(page).forEach(cachedItem -> {

            SizeNode sizeNode = Sizes.SIZES_BY_ID.get(cachedItem.id());
            boolean hasAccess = hasPermission(player, sizeNode.permission());
            ItemStack displayItem = selectSizeItem(cachedItem, hasAccess, currentScale);
            ItemStack confirmationItem = cachedItem.availableItem().clone();

            gui.setItem(
                    cachedItem.slot(),
                    ClickableItem.of(displayItem, event -> handleSizeClick(player, sizeNode, page, confirmationItem))
            );
        });
    }

    private void populateActionItems(AliienGUI gui, Player player, int page) {
        Sizes.ACTION_ITEMS_BY_PAGE.get(page).forEach(cachedItem -> {
            ItemStack item = cachedItem.item().clone();

            ClickableItem clickableItem = NONE.equals(cachedItem.action())
                    ? ClickableItem.empty(item)
                    : ClickableItem.of(item, event -> handleActionClick(player, cachedItem));

            gui.setItem(cachedItem.slot(), clickableItem);
        });
    }

    private void handleActionClick(Player player, CachedActionItem cachedItem) {
        switch (cachedItem.action()) {
            case NEXT_PAGE, PREVIOUS_PAGE -> {
                if (Settings.SOUNDS_ENABLED) Settings.CLICK_SOUND.play(player);
                openMenu(player, cachedItem.targetPage());
            }
            case CLEAR -> {
                player.closeInventory();

                runSync(player, () -> {
                    Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                            .ifPresent(attribute -> attribute.setBaseValue(1.0));

                    MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_DEFAULT);
                    if (Settings.SOUNDS_ENABLED) Settings.CLEAR_SOUND.play(player);
                });
            }
            case NONE -> {}
        }
    }

    private void handleSizeClick(Player player, SizeNode sizeNode, int currentPage, ItemStack confirmationItem) {
        boolean hasAccess = hasPermission(player, sizeNode.permission());;

        if (hasAccess) {
            applyScale(player, sizeNode);
            return;
        }

        if (sizeNode.price().isPurchasable()) {
            if (Settings.CONFIRMATION_MENU_ENABLED) {
                new ConfirmationMenu().openMenu(
                        player,
                        sizeNode,
                        confirmationItem,
                        () -> handlePurchase(player, sizeNode),
                        () -> openMenu(player, currentPage)
                );
            } else {
                handlePurchase(player, sizeNode);
            }
            return;
        }

        MessageUtils.send(player, Messages.PREFIX, Messages.NO_PERM);
    }

    private void handlePurchase(Player player, SizeNode sizeNode) {
        if (!plugin.getVaultExpansion().hasPermissions()){
            plugin.getLogger().log(Level.SEVERE, "Sizes purchase cancelled due to not finding any Vault permissions provider.");
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);

            Bukkit.getOnlinePlayers().stream()
                    .filter(admin -> admin.hasPermission("aliien.resize.admin"))
                    .forEach(admin -> {
                        MessageUtils.send(admin, Messages.PREFIX, "<red>Vault permissions provider is currently not setup properly, which just prevented a player from purchasing a size!");
                        if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(admin);
                    });
        }

        CurrencyProvider currency = plugin.getCurrencyManager().getCurrency(sizeNode.price().currency());

        if (currency == null || !currency.isValid()) {
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE);
            return;
        }

        double price = sizeNode.price().price();
        String formattedPrice = (Math.rint(price) == price) ? String.valueOf((long) price) : String.valueOf(price);

        if (!currency.hasBalance(player, price)) {
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_FAIL.replace("%price%", formattedPrice));
            return;
        }

        if (!currency.withdraw(player, price)) {
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_UNAVAILABLE);
            return;
        }

        grantPermission(player, sizeNode.permission());

        MessageUtils.send(player, Messages.PREFIX, Messages.PURCHASE_SUCCESS.replace("%price%", formattedPrice));

        applyScale(player, sizeNode);
    }

    /**
     * Executes the permission grant.
     *
     * @requires {@code plugin.getVaultExpansion().hasPermission()}
     */
    private void grantPermission(Player player, String permission) {
        plugin.getVaultExpansion().getPermissions().playerAdd(null, player, permission);
    }

    private void applyScale(Player player, SizeNode sizeNode) {
         if (!plugin.getResizeUtils().hasEnoughSpace(player, sizeNode.scale())) {
            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_FAIL);
            if (Settings.SOUNDS_ENABLED) Settings.ERROR_SOUND.play(player);
            return;
        }

        player.closeInventory();

        runSync(player, () -> {
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_SCALE))
                    .ifPresent(attribute -> attribute.setBaseValue(sizeNode.scale()));

            MessageUtils.send(player, Messages.PREFIX, Messages.RESIZE_SUCCESS);
            if (Settings.SOUNDS_ENABLED) Settings.SUCCESS_SOUND.play(player);
        });
    }

    private ItemStack selectSizeItem(CachedSizeItem cachedItem, boolean hasAccess, double currentScale) {
        if (!hasAccess) return cachedItem.noPermItem().clone();
        if (Double.compare(currentScale, cachedItem.scale()) == 0) return cachedItem.selectedItem().clone();
        return cachedItem.availableItem().clone();
    }

    private boolean hasPermission(Player player, String permission) {
        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    private int sanitizePage(int requestedPage) {
        int maxPage = Math.max(1, Sizes.MENU_MAX_PAGE);
        return Math.max(1, Math.min(requestedPage, maxPage));
    }

    private void runSync(Player player, Runnable task) {
        player.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }
}