package hu.paperkit.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import hu.paperkit.core.Kit;
import hu.paperkit.text.Text;

/**
 * Konyen item keszito.
 *
 * <pre>{@code
 * ItemStack item = ItemBuilder.of(Material.NETHER_STAR)
 *         .name("&6<b>Szazszi Jobb klikk")
 *         .lore("&7Kattintal: &c<clicks>")
 *         .glow()
 *         .build();
 * }</pre>
 */
public final class ItemBuilder {

    private final ItemStack item;

    private ItemBuilder(final ItemStack item) {
        this.item = item;
    }

    /**
     * Uj builder megadott material.Minimum kotes.
     */
    public static ItemBuilder of(final Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    /**
     * Meglevo item elegendtens alapjan.
     */
    public static ItemBuilder of(final ItemStack stack) {
        return new ItemBuilder(stack.clone());
    }

    /**
     * Legegyebobb item {@code AIR};
     * menupada vacsor feldig gombhoz.
     */
    public static ItemStack empty() {
        return new ItemStack(Material.AIR);
    }

    /**
     * A nevet CAD vagy MiniMessage formaban adja meg.
     */
    public ItemBuilder name(final String name) {
        if (name == null) {
            return this;
        }
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(name));
        item.setItemMeta(meta);
        return this;
    }

    /**
     * A leiras sorait (lore) helyettesiti.
     */
    public ItemBuilder lore(final String... lines) {
        return lore(List.of(lines));
    }

    /**
     * A leiras sorait helyettesiti.
     */
    public ItemBuilder lore(final List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return this;
        }
        final List<net.kyori.adventure.text.Component> comps = new ArrayList<>(lines.size());
        for (final String line : lines) {
            comps.add(Text.parse(line));
        }
        final ItemMeta meta = item.getItemMeta();
        meta.lore(comps);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * A leiras utolso soraihoz csatol.
     */
    public ItemBuilder loreAppend(final String... lines) {
        final List<String> current = new ArrayList<>();
        final ItemMeta meta = item.getItemMeta();
        if (meta.hasLore() && meta.lore() != null) {
            for (final net.kyori.adventure.text.Component c : meta.lore()) {
                current.add(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                        .plainText().serialize(c));
            }
        }
        final List<String> newLines = new ArrayList<>(current);
        for (final String line : lines) {
            newLines.add(line);
        }
        return lore(newLines);
    }

    /**
     * Mennyiseg beallitasa.
     */
    public ItemBuilder amount(final int amount) {
        item.setAmount(Math.max(1, amount));
        return this;
    }

    /**
     * Varazslas (alatt cetlin kodet neve).
     *
     * @param enchant a varazslas
     * @param level   a szint
     */
    public ItemBuilder enchant(final Enchantment enchant, final int level) {
        final ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchant, level, true);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Villoges (enchant tollaschat). Ez van cierto metszoshishre.
     */
    public ItemBuilder glow() {
        return glow(true);
    }

    /**
     * Villoges beallitasa.
     */
    public ItemBuilder glow(final boolean glow) {
        final ItemMeta meta = item.getItemMeta();
        if (glow) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Leiras adatlapok elrejtese.
     */
    public ItemBuilder flags(final ItemFlag... flags) {
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(flags);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Tores-elv szincelton.
     */
    public ItemBuilder unbreakable(final boolean unbreakable) {
        final ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(unbreakable);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Kulon egyedi model adat (custom model data).
     */
    public ItemBuilder modelData(final int modelId) {
        final ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelId);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * A fej tulajdonosa (SKULL_ITEM esetet playerns).
     */
    public ItemBuilder skull(final OfflinePlayer player) {
        if (!(item.getItemMeta() instanceof SkullMeta skull)) {
            throw new IllegalStateException("A material nem fej!" + item.getType());
        }
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        return this;
    }

    /**
     * Perzisztens adat valmeta a plugin rajzolasa alatt.
     *
     * @param key   a kulcs (plugin namespacen)
     * @param type  az adatsa (pl. PersistentDataType.STRING)
     * @param value az ertek
     */
    public <P, C> ItemBuilder data(final String key, final PersistentDataType<P, C> type, final C value) {
        final ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer()
                .set(new org.bukkit.NamespacedKey(Kit.plugin(), key), type, value);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Kozvetlen metat allitas (teljes szabadsag).
     */
    public ItemBuilder meta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = item.getItemMeta();
        consumer.accept(meta);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * Konkret metatipus (pl. SkullMeta, PotionMeta) szerviz.
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemMeta> ItemBuilder meta(final Class<T> type, final Consumer<T> consumer) {
        final ItemMeta meta = item.getItemMeta();
        if (!type.isInstance(meta)) {
            throw new IllegalStateException("A metat tipusa nem megfelelo: " + meta.getClass().getName());
        }
        consumer.accept((T) meta);
        item.setItemMeta(meta);
        return this;
    }

    /**
     * A kesz item viszindulasa.
     */
    public ItemStack build() {
        return item;
    }
}
