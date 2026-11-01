package hu.paperkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import net.kyori.adventure.text.Component;

import hu.paperkit.text.Text;

/**
 * Inventory menu: sorokbol, slotokbol es gombokbol allo gui.
 *
 * <pre>{@code
 * Menu.create(3, "&8Beallitasok")
 *         .at(13, MenuButton.of(item).click(event -> task.run()))
 *         .border(MenuButton.fillof( glass ))
 *         .open(player);
 * }</pre>
 */
public final class Menu {

    /** A menu nel ?? sor tag: 1-6. */
    private int rows = 3;
    /** A menu cm szovege. */
    private String title = "";
    /** Slot -> gomb mappel? */
    private final MenuButton[] slots;
    /** Maradk slotok kitlsre. */
    private MenuButton filler;
    /** Bezaras esemeny. */
    private java.util.function.BiConsumer<InventoryCloseEvent, Menu> onClose;
    /** Az osszes klikks esemeny trolsa (shift/klik, jtkos inot). */
    private boolean cancelClicks = true;

    private Menu(final int rows) {
        this.rows = Math.min(6, Math.max(1, rows));
        this.slots = new MenuButton[this.rows * 9];
    }

    /**
     * Megadott sorokoli menut keszit.
     */
    public static Menu create(final int rows) {
        return new Menu(rows);
    }

    /**
     * Megadott sor es cmile meno es keszit (kodek vagy MiniMessage).
     */
    public static Menu create(final int rows, final String title) {
        final Menu menu = new Menu(rows);
        menu.title = title == null ? "" : title;
        return menu;
    }

    /**
     * Cim mdostsa kszul (fluent vgyl).
     */
    public Menu title(final String title) {
        this.title = title == null ? "" : title;
        return this;
    }

    /**
     * Elem elhelyezse egy sloton (0-based).
     */
    public Menu at(final int slot, final MenuButton button) {
        if (slot >= 0 && slot < slots.length) {
            slots[slot] = button;
        }
        return this;
    }

    /**
     * Elem elhelyezse sorko-s  ko-od.
     */
    public Menu at(final int row, final int column, final MenuButton button) {
        return at(row * 9 + column, button);
    }

    /**
     * Tldi elem elplmeny sor (slot-tt ketszet).
     */
    public Menu range(final int fromSlot, final int toSlot, final MenuButton button) {
        for (int i = Math.max(0, fromSlot); i <= Math.min(toSlot, slots.length - 1); i++) {
            slots[i] = button;
        }
        return this;
    }

    /**
     * A menu tel keret kitlts (gombszerpotaya).
     */
    public Menu border(final MenuButton button) {
        final int last = slots.length - 1;
        for (int i = 0; i < 9; i++) {
            slots[i] = button;
            slots[last - i] = button;
        }
        for (int row = 1; row < rows - 1; row++) {
            slots[row * 9] = button;
            slots[row * 9 + 8] = button;
        }
        return this;
    }

    /**
     * Az ss�zes mg res slot kit�lt�se.
     */
    public Menu fill(final MenuButton button) {
        this.filler = button;
        return this;
    }

    /**
     * Bezr es esemeny esemny (player bezrt a menut).
     */
    public Menu onClose(final java.util.function.BiConsumer<InventoryCloseEvent, Menu> consumer) {
        this.onClose = consumer;
        return this;
    }

    /**
     * Klikk trols (jelngegz?leges) ?rt?k?r?lj).
     */
    public boolean cancelClicks() {
        return cancelClicks;
    }

    /**
     * Klikk bax krb blkoltsi engedlj (shift/klik, jtkos inot).
     * Alapbl minden klikket leblokkol, hogy ne lophassak itemet.
     */
    public Menu cancelClicks(final boolean cancel) {
        this.cancelClicks = cancel;
        return this;
    }

    /**
     * A menut megjeleniti a jatekosnak.
     */
    public void open(final HumanEntity viewer) {
        viewer.openInventory(inventory());
    }

    /**
     * Az inventory osszeallitasa es visszaadasa (feliratkoztatva a holder-re).
     */
    public Inventory inventory() {
        final Component comp = title == null || title.isEmpty()
                ? Component.empty() : Text.parse(title);
        final MenuHolder holder = new MenuHolder(this);
        final Inventory inv = Bukkit.createInventory(holder, slots.length, comp);
        holder.inventory(inv);
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                inv.setItem(i, slots[i].display());
            } else if (filler != null) {
                inv.setItem(i, filler.display());
            }
        }
        return inv;
    }

    /**
     * Benzait slot gomb, vagy null.
     */
    MenuButton slot(final int slot) {
        if (slot < 0 || slot >= slots.length) {
            return null;
        }
        return slots[slot];
    }

    /**
     * Klikk esemeny levgse (a {@link MenuListener} hvja).
     */
    void fireClick(final InventoryClickEvent event) {
        final MenuButton button = slot(event.getRawSlot());
        if (cancelClicks) {
            event.setCancelled(true);
        }
        if (button != null && button.action() != null) {
            button.action().accept(event);
        }
    }

    /**
     * Bezrs esemeny kivltsa (a {@link MenuListener} hvja).
     */
    void fireClose(final InventoryCloseEvent event) {
        if (onClose != null) {
            onClose.accept(event, this);
        }
    }

    /**
     * Megnyit rszk tartalmi cmjelzs.
     */
    String titleAsText() {
        return title;
    }
}
