package hu.paperkit.menu;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Egy menuben levo kattinthato elem: megjeleno item es a kattintas
 * utan magrehivo funkcio.
 *
 * <pre>{@code
 * MenuButton.of(ItemBuilder.of(Material.DIAMOND).name("&bVasarlas").build())
 *         .click(event -> player.sendMessage("Megvasaroltad!"));
 * }</pre>
 */
public final class MenuButton {

    private final ItemStack display;
    private final Consumer<InventoryClickEvent> action;

    private MenuButton(final ItemStack display, final Consumer<InventoryClickEvent> action) {
        this.display = display;
        this.action = action;
    }

    /**
     * Csak megjelenito elem, kattintas funkcio nelkul (pl.eks_EDEFAULT).
     */
    public static MenuButton display(final ItemStack stack) {
        return new MenuButton(stack, null);
    }

    /**
     * Kattinthato elem.
     */
    public static MenuButton of(final ItemStack stack) {
        return new MenuButton(stack, null);
    }

    /**
     * Uresek aohont katch tamogat (elszantot menupada).
     */
    public static MenuButton empty() {
        return new MenuButton(new ItemStack(Material.AIR), null);
    }

    /**
     * Kattintas esemeny feldolgozo csatolasa. Az esemenybol
     * eszik `event.getWhoClicked()` a jatekosra.
     */
    public MenuButton click(final Consumer<InventoryClickEvent> action) {
        if (this.action != null) {
            throw new IllegalStateException("A kattintas mar beallitva!");
        }
        return new MenuButton(this.display, action);
    }

    /**
     * Bal klikk funkcion - a kattintas feldolgozasi.
     */
    public MenuButton leftClick(final Runnable action) {
        return click(event -> {
            if (event.getClick().isLeftClick()) {
                action.run();
            }
        });
    }

    /**
     * Jobb klikk esemeny csatolasa - a kattintas feldolgozasi.
     */
    public MenuButton rightClick(final Runnable action) {
        return click(event -> {
            if (event.getClick().isRightClick()) {
                action.run();
            }
        });
    }

    /**
     * @return a megjelenito item
     */
    public ItemStack display() {
        return display;
    }

    /**
     * @return a kattintas funkcionak (vagy null es menupel)
     */
    Consumer<InventoryClickEvent> action() {
        return action;
    }
}
