package hu.paperkit.menu;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import hu.paperkit.item.ItemBuilder;

/**
 * Anvil-alapu szoveg bevitel: a jatekos egy itemet nevez at, es a
 * beirt szoveget kapjuk meg callbackben.
 *
 * <pre>{@code
 * AnvilInputMenu.of(player)
 *         .prompt("&eÍrd be az új nevet!")
 *         .callback(name -> renameHologram(name))
 *         .open();
 *
 * // szamot is konnyen parse-olhatsz:
 * AnvilInputMenu.of(player)
 *         .prompt("&eMennyi penze legyen?")
 *         .number(amount -> setBalance(amount))
 *         .open();
 * }</pre>
 */
public final class AnvilInputMenu {

    private final Player player;
    private String prompt = "&eÍrd be a szöveget!";
    private Material contentType = Material.PAPER;
    private Consumer<String> callback;
    private Consumer<Integer> numberCallback;

    private AnvilInputMenu(final Player player) {
        this.player = player;
    }

    /**
     * Uj anvil bemenet a jatekosnak.
     */
    public static AnvilInputMenu of(final Player player) {
        return new AnvilInputMenu(player);
    }

    /**
     * A bemutatott tooltip (mini szoveg).
     */
    public AnvilInputMenu prompt(final String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Az input item materialja (alap: PAPER).
     */
    public AnvilInputMenu item(final Material material) {
        this.contentType = material;
        return this;
    }

    /**
     * Szoveg callback.
     */
    public AnvilInputMenu callback(final Consumer<String> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Szam callback (ha nem szam az input, nem hivodik).
     */
    public AnvilInputMenu number(final Consumer<Integer> numberCallback) {
        this.numberCallback = numberCallback;
        return this;
    }

    /**
     * Az anvil megnyitasa a jatekosnak.
     */
    public void open() {
        if (player == null || !player.isOnline()) {
            return;
        }
        final AnvilHolder holder = new AnvilHolder(this);
        final Inventory inv = Bukkit.createInventory(holder, org.bukkit.event.inventory.InventoryType.ANVIL);
        holder.inventory(inv);
        final ItemStack baseItem = ItemBuilder.of(contentType)
                .name(prompt)
                .amount(1)
                .build();
        inv.setItem(0, baseItem);
        player.openInventory(inv);
    }

    /**
     * A telek? za? holder: az InputListener? b?v?s?tt ev?gar???nl?.
     */
    static final class AnvilHolder implements InventoryHolder {

        private final AnvilInputMenu menu;
        private Inventory inventory;

        AnvilHolder(final AnvilInputMenu menu) {
            this.menu = menu;
        }

        void inventory(final Inventory inventory) {
            this.inventory = inventory;
        }

        /**
         * Kattintas a "vevo" slotra az inputtal (a {@link MenuListener} hvja).
         */
        void fireInput(final String text) {
            if (text == null || text.isEmpty()) {
                return;
            }
            final Player p = menu.player;
            if (p != null && p.isOnline()) {
                p.closeInventory();
            }
            if (menu.numberCallback != null) {
                try {
                    final int number = Integer.parseInt(text.trim());
                    menu.numberCallback.accept(number);
                    return;
                } catch (final NumberFormatException ignored) {
                }
            }
            if (menu.callback != null) {
                menu.callback.accept(text);
            }
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        /**
         * Az anvil t?lmoz?s?es gyors?z?d?sa?nl.
         */
        Location location() {
            return menu.player == null ? null : menu.player.getLocation();
        }
    }

    /**
     * Az anvil item atnevezett eredmenyenek elkeszitese.
     */
    static ItemStack anvilResult(final AnvilInventory inv, final String renameText) {
        final ItemStack base = inv.getItem(0);
        if (base == null || renameText == null || renameText.isEmpty()) {
            return null;
        }
        final ItemStack result = base.clone();
        final var meta = result.getItemMeta();
        meta.setDisplayName(renameText.replace('&', '\u00a7'));
        result.setItemMeta(meta);
        return result;
    }
}
