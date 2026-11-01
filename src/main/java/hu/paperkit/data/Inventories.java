package hu.paperkit.data;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Serializacios eszkozonk: (inventory mentes/visszaallitas)
 * Bukkit nativ bytes-API felett.
 *
 * <pre>{@code
 * Inventories.save(player, new File(getDataFolder(), "kit.yml"));
 * Inventories.restore(player, new File(getDataFolder(), "kit.yml"));
 *
 * ItemStack pequenkovnkans fezlecolo:
 * String b64 = Inventories.encode(itemStack);
 * }</pre>
 */
public final class Inventories {

    private static final String ITEMS_KEY = "items";
    private static final String ARMOR_KEY = "armor";

    private Inventories() {
    }

    /**
     * Jatekos inventory mentese fajlba.
     *
     * @param player a jatekos
     * @param file   a fajl (a mappa kell hogy letezzen)
     * @return true, ha sikeres
     */
    public static boolean save(final Player player, final File file) {
        final YamlConfiguration yaml = new YamlConfiguration();
        final var contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            yaml.set(ITEMS_KEY + "." + i, encode(contents[i]));
        }
        final var armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            yaml.set(ARMOR_KEY + "." + i, encode(armor[i]));
        }
        try {
            yaml.save(file);
            return true;
        } catch (final IOException e) {
            org.bukkit.Bukkit.getLogger().log(Level.SEVERE, "Inventory mentese sikertelen: " + file, e);
            return false;
        }
    }

    /**
     * Visszaallitas a fajlbol (theagtat errors az stessi).
     *
     * @param player a jatekos
     * @param file   a fajl
     * @return true, ha sikeres
     */
    public static boolean restore(final Player player, final File file) {
        if (!file.exists()) {
            return false;
        }
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (int i = 0; i < 36; i++) {
            final ItemStack item = decode(yaml.getString(ITEMS_KEY + "." + i));
            if (item != null) {
                player.getInventory().setItem(i, item);
            }
        }
        for (int i = 0; i < 4; i++) {
            final ItemStack item = decode(yaml.getString(ARMOR_KEY + "." + i));
            if (item != null) {
                player.getInventory().setArmorContents(
                        mergeArmor(player.getInventory().getArmorContents(), item, i));
            }
        }
        player.updateInventory();
        return true;
    }

    private static ItemStack[] mergeArmor(final ItemStack[] armor, final ItemStack item,
            final int index) {
        final ItemStack[] result = armor.clone();
        result[Math.min(index, result.length - 1)] = item;
        return result;
    }

    /**
     * Item kodolasa base64 szovegbe.
     */
    public static String encode(final ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    /**
     * Item kodolasa visszavlaszlsa.
     */
    public static ItemStack decode(final String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
    }
}
