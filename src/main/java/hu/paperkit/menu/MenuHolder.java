package hu.paperkit.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import hu.paperkit.text.Text;

/**
 * Inventory holder, ami osszefogja a menut az inventory-javal.
 */
public final class MenuHolder implements InventoryHolder {

    private final Menu menu;
    private Inventory inventory;

    MenuHolder(final Menu menu) {
        this.menu = menu;
    }

    void inventory(final Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return az osszefogott menu
     */
    public Menu menu() {
        return menu;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Hozzafased r? a cmjar: formazott cim.
     */
    @Override
    public String toString() {
        return "MenuHolder{" + Text.plain(menu.titleAsText()) + "}";
    }
}
