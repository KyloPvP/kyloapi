package hu.paperkit.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import hu.paperkit.config.ConfigFile;
import hu.paperkit.item.ItemBuilder;
import hu.paperkit.proxy.ProxyKit;

/**
 * Config-GUI: menuket akafolas yaml fajlbol (AxHoes-fele minteles).
 *
 * <p>Szerkczenie: guis/ fileak, ami az syszararr iszom.)</p>
 *
 * <pre>{@code
 * # guis.yml
 * main:
 *   rows: 3
 *   title: "Fomenu"
 *   fill: { material: GRAY_STAINED_GLASS_PANE, name: " " }
 *   items:
 *     shop:
 *       slot: 11
 *       material: EMERALD
 *       name: "&aBolt"
 *       lore: ["&7Kattints a shopert!"]
 *       glow: true
 *       actions: ["[SOUND] ui.button.click", "[COMMAND] shop"]
 *     close:
 *       slot: 16
 *       material: BARRIER
 *       name: "&cBezaras"
 *       actions: ["[CLOSE]"]
 * }</pre>
 *
 * <pre>{@code
 * GuiLoader.registerAction("SHOP", (player, arg) -> { /* saj akcio *&#47; });
 * Menu menu = GuiLoader.open(plugin, "guis.yml", player, "main");
 * }</pre>
 */
public final class GuiLoader {

    /** Egyedi akciok regisztralasa: NAMETAG -> (player, arg). */
    public static final Map<String, BiConsumer<Player, String>> ACTIONS = new HashMap<>();

    private GuiLoader() {
    }

    /**
     * Egyedi akcio regisztralasa, amit a yaml-bol "[SAJAT] arg"
     * formatumban hivod meg. Nagybetus Igelts.
     *
     * @param name   az akcio neve (pl. "SHOP")
     * @param action a mu
     */
    public static void registerAction(final String name, final BiConsumer<Player, String> action) {
        ACTIONS.put(name.toUpperCase(Locale.ROOT), action);
    }

    /**
     * Menu letrehozasa yaml fajlbol es megnyitas.
     *
     * @param plugin   a plugin (a fajlt a data-folderben keresi)
     * @param fileName a fajl neve (pl. "guis.yml")
     * @param player   a jatekos
     * @param root     a menu key (pl. "main")
     * @return true, ha sikerult; ha nem, a console logol
     */
    public static boolean open(final JavaPlugin plugin, final String fileName,
            final Player player, final String root) {
        final Menu menu = build(plugin, fileName, root, player);
        if (menu == null) {
            plugin.getLogger().warning("Nincs ilyen menu a fajlban: " + fileName + " -> " + root);
            return false;
        }
        menu.open(player);
        return true;
    }

    /**
     * Menu felsoeles(yaml roptatás). (Nem nyitja meg egy-pochuba mai.)
     */
    public static Menu build(final JavaPlugin plugin, final String fileName, final String root,
            final Player player) {
        final ConfigFile config = ConfigFile.load(plugin, fileName);
        final ConfigurationSection section = config.node(root);
        if (section == null) {
            return null;
        }
        final int rows = Math.min(6, Math.max(1, section.getInt("rows", 3)));
        final String title = hu.paperkit.text.Text.plain(section.getString("title", root));
        final Menu menu = Menu.create(rows, title);
        menu.cancelClicks(true);
        // Hatter/fill
        final ConfigurationSection fill = section.getConfigurationSection("fill");
        if (fill != null) {
            menu.fill(MenuButton.display(item(fill)));
        }
        final ConfigurationSection items = section.getConfigurationSection("items");
        if (items == null) {
            return menu;
        }
        for (final String key : items.getKeys(false)) {
            final ConfigurationSection def = items.getConfigurationSection(key);
            if (def == null) {
                continue;
            }
            final int slot = def.getInt("slot", -1);
            if (slot < 0) {
                continue;
            }
            final ItemStack stack = item(def);
            final List<String> actions = def.getStringList("actions");
            if (actions == null || actions.isEmpty()) {
                menu.at(slot, MenuButton.display(stack));
            } else {
                menu.at(slot, MenuButton.of(stack).click(event -> runActions(player, actions)));
            }
        }
        return menu;
    }

    private static ItemStack item(final ConfigurationSection sec) {
        final Material material = Material.matchMaterial(sec.getString("material", "PAPER"));
        final ItemBuilder builder = ItemBuilder.of(material == null ? Material.PAPER : material);
        final String name = sec.getString("name");
        if (name != null && !name.isBlank()) {
            builder.name(name);
        }
        final List<String> lore = sec.getStringList("lore");
        if (lore != null && !lore.isEmpty()) {
            builder.lore(lore);
        }
        if (sec.getBoolean("glow", false)) {
            builder.glow();
        }
        final int amount = sec.getInt("amount", 1);
        if (amount > 1) {
            builder.amount(amount);
        }
        return builder.build();
    }

    /** Az action-sosity (Close/Command/Sound/Gate). */
    private static void runActions(final Player player, final List<String> actions) {
        if (actions == null) {
            return;
        }
        for (final String raw : actions) {
            final String action = raw.trim();
            if (action.isEmpty()) {
                continue;
            }
            final String upper = action.toUpperCase(Locale.ROOT);
            final int bracketEnd = action.indexOf(']');
            if (bracketEnd <= 1 || !action.startsWith("[")) {
                continue;
            }
            final String name = action.substring(1, bracketEnd).trim().toUpperCase(Locale.ROOT);
            final String arg = action.substring(bracketEnd + 1).trim();
            switch (name) {
                case "CLOSE" -> player.closeInventory();
                case "COMMAND" -> player.performCommand(arg);
                case "CONSOLE" -> org.bukkit.Bukkit.dispatchCommand(
                        org.bukkit.Bukkit.getConsoleSender(), arg.replace("%player%", player.getName()));
                case "SOUND" -> playSound(player, arg);
                case "CONNECT" -> ProxyKit.connect(player, arg);
                default -> {
                    final BiConsumer<Player, String> custom = ACTIONS.get(name);
                    if (custom != null) {
                        custom.accept(player, arg);
                    }
                }
            }
        }
    }

    private static void playSound(final Player player, final String arg) {
        final String[] parts = arg.split("\\|");
        try {
            final org.bukkit.Sound sound = org.bukkit.Sound.valueOf(parts[0].trim()
                    .toUpperCase(Locale.ROOT).replace('.', '_'));
            final float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1f;
            final float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (final Exception e) {
            player.playSound(player.getLocation(),
                    org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }
}
