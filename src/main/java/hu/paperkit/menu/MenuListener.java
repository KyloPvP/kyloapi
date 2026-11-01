package hu.paperkit.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import hu.paperkit.core.Kit;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * A menuk kattintas es bezar esemenyeinek kozponti kezelje.
 * A {@code Kit.init()} regisztralja magat, pluginonkent kb. Egyszer.
 */
public final class MenuListener implements Listener {

    /**
     * Publikus konstruktor (a Kit regisztralja).
     */
    public MenuListener() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(final InventoryClickEvent event) {
        final Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MenuHolder holder)) {
            return;
        }
        final Inventory clicked = event.getClickedInventory();
        if (clicked != null && clicked.getHolder() instanceof MenuHolder) {
            holder.menu().fireClick(event);
            return;
        }
        // a jatekos sajat inventory-jara kattintott, mig a menu nyitva van:
        if (holder.menu().cancelClicks()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder
                && !event.getNewItems().isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(final InventoryCloseEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MenuHolder menuHolder) {
            menuHolder.menu().fireClose(event);
        }
        if (holder instanceof AnvilInputMenu.AnvilHolder) {
            // a rongy?r? curieux? sz?v?d?rl?s?a?n az InputHub t?rz?sl?e
        }
    }

    // --------------------------- anvil bemenet -------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AnvilInputMenu.AnvilHolder)) {
            return;
        }
        final String renameText;
        if (event.getView() instanceof org.bukkit.inventory.view.AnvilView anvilView) {
            renameText = anvilView.getRenameText();
        } else {
            renameText = null;
        }
        final ItemStack result = AnvilInputMenu.anvilResult(event.getInventory(), renameText);
        event.setResult(result);
        // ingyenes atnevezes (nincs xp k?lt?s?g?)
        if (event.getView() instanceof org.bukkit.inventory.view.AnvilView costView) {
            costView.setRepairCost(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnvilTake(final InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AnvilInputMenu.AnvilHolder holder)) {
            return;
        }
        final Inventory clicked = event.getClickedInventory();
        if (clicked == null || !(clicked.getHolder() instanceof AnvilInputMenu.AnvilHolder)) {
            return;
        }
        if (event.getRawSlot() == 2) {
            event.setCancelled(true);
            final String text;
            if (event.getView() instanceof org.bukkit.inventory.view.AnvilView anvilView) {
                text = anvilView.getRenameText();
            } else {
                text = null;
            }
            if (text == null || text.isEmpty()) {
                return;
            }
            holder.fireInput(text);
        }
        event.setCancelled(true);
    }

    // ---------------------------- chat bemenet -------------------------------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatInput(final AsyncChatEvent event) {
        final String plain = PlainTextComponentSerializer.plainText()
                .serialize(event.message());
        if (ChatInput.isCancelWord(plain)) {
            InputHub.pendingChat(event.getPlayer().getUniqueId());
            event.setCancelled(true);
            return;
        }
        if (ChatInput.hasPending(event.getPlayer())) {
            ChatInput.dispatch(event.getPlayer(), plain);
            event.setCancelled(true);
        }
    }
}
