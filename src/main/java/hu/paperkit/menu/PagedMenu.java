package hu.paperkit.menu;

import java.util.List;
import java.util.function.BiFunction;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import hu.paperkit.item.ItemBuilder;

/**
 * Lapos (paginalis) menu: az elemek tobb oldalon van tehatoak.
 *
 * <pre>{@code
 * PagedMenu.of("&8Teleport pontok", points, (point, index) ->
 *         MenuButton.of(point.iconOf()).click(() -> use(point)))
 *     .rows(6)
 *     .open(player);
 * }</pre>
 *
 * <p>Az utolso sor minden oldalon navigacios sor lesz
 * (elozo oldal, oldal kijelzo, kovetkezo oldal).</p>
 */
public final class PagedMenu<T> {

    private final String title;
    private final List<T> items;
    private final BiFunction<T, Integer, MenuButton> renderer;

    private int rows = 6;
    private MenuButton prevIcon;
    private MenuButton nextIcon;
    private MenuButton filler;
    private java.util.function.BiConsumer<InventoryCloseEvent, PagedMenu<T>> onClose;

    private PagedMenu(final String title, final List<T> items,
            final BiFunction<T, Integer, MenuButton> renderer) {
        this.title = title;
        this.items = items;
        this.renderer = renderer;
    }

    /**
     * Uj lapozhato menu.
     *
     * @param title a menu cime
     * @param items az elemek
     * @param renderer elem es globalis index alapjan a megjelenito gomb
     */
    public static <T> PagedMenu<T> of(final String title, final List<T> items,
            final BiFunction<T, Integer, MenuButton> renderer) {
        return new PagedMenu<>(title, items, renderer);
    }

    /**
     * Sorok beallitasa (2-6). Az utolso sor navigacios.
     */
    public PagedMenu<T> rows(final int rows) {
        this.rows = Math.min(6, Math.max(2, rows));
        return this;
    }

    /**
     * Az ures slotok kitoltese.
     */
    public PagedMenu<T> fill(final MenuButton button) {
        this.filler = button;
        return this;
    }

    /**
     * Bezaras esemeny.
     */
    public PagedMenu<T> onClose(
            final java.util.function.BiConsumer<InventoryCloseEvent, PagedMenu<T>> consumer) {
        this.onClose = consumer;
        return this;
    }

    /**
     * A menu megjelenitese az elso oldaltol.
     */
    public void open(final HumanEntity viewer) {
        openAt(viewer, 0);
    }

    private void openAt(final HumanEntity viewer, final int page) {
        final int contentSlots = (this.rows - 1) * 9;
        final int pages = Math.max(1, (int) Math.ceil(items.size() / (double) contentSlots));
        final int current = Math.max(0, Math.min(page, pages - 1));

        final Menu menu = Menu.create(this.rows, this.title);
        if (this.filler != null) {
            menu.fill(this.filler);
        }
        final int from = current * contentSlots;
        final int to = Math.min(this.items.size(), from + contentSlots);
        for (int slot = 0; slot < to - from; slot++) {
            final T item = this.items.get(from + slot);
            final MenuButton button = this.renderer == null ? null : this.renderer.apply(item, from + slot);
            if (button != null) {
                menu.at(slot, button);
            }
        }
        final int navRow = this.rows - 1;
        final ItemStack info = ItemBuilder.of(Material.PAPER)
                .name("&eOldal: &f" + (current + 1) + "&7/&f" + pages)
                .build();
        menu.at(navRow, 2, MenuButton.of(prevDisplay(current - 1)).leftClick(() -> openAt(viewer, current - 1)));
        menu.at(navRow, 4, MenuButton.display(info));
        menu.at(navRow, 6, MenuButton.of(nextDisplay(current + 1, pages)).leftClick(() -> openAt(viewer, current + 1)));
        if (this.onClose != null) {
            menu.onClose((event, closedMenu) -> this.onClose.accept(event, this));
        }
        menu.open(viewer);
    }

    private ItemStack prevDisplay(final int targetPage) {
        if (this.prevIcon != null) {
            return this.prevIcon.display();
        }
        final boolean disabled = targetPage < 0;
        return ItemBuilder.of(Material.ARROW)
                .name(disabled ? "&8" : "&6" + "Elozo oldal")
                .build();
    }

    private ItemStack nextDisplay(final int targetPage, final int pages) {
        if (this.nextIcon != null) {
            return this.nextIcon.display();
        }
        final boolean disabled = targetPage >= pages;
        return ItemBuilder.of(Material.ARROW)
                .name(disabled ? "&8" : "&6" + "Kovetkezo oldal")
                .build();
    }

    /**
     * Az elemek darabszama.
     */
    public int size() {
        return this.items.size();
    }
}
