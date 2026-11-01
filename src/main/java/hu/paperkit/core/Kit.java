package hu.paperkit.core;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import hu.paperkit.menu.MenuListener;

/**
 * Az API "kapcsoloja".
 *
 * <p>Pluginok az onEnable-ben hivjak meg: {@code Kit.init(this);} es az
 * onDisable-ben: {@code Kit.shutdown();}</p>
 *
 * <p>A {@link hu.paperkit.plugin.KitPlugin} ki Lesziras az osztaly magatol
 * elvegzi ezt, ezert az ajanlos hasznalni.</p>
 */
public final class Kit {

    private static Plugin plugin;
    private static MenuListener menuListener;

    private Kit() {
    }

    /**
     * Beallitja az API-t a megadott pluginra es feliratkoztatja a
     * menu esemeny kezeloket. Egy plugin eleteben egyszer hivdheto meg.
     *
     * @param plugin a hivo plugin
     */
    public static void init(final Plugin plugin) {
        if (Kit.plugin != null) {
            throw new IllegalStateException("Kit mar inicializalva lett: " + plugin.getName());
        }
        Kit.plugin = plugin;
        menuListener = new MenuListener();
        plugin.getServer().getPluginManager().registerEvents(menuListener, plugin);
    }

    /**
     * Leiratkoztatja a menu esemeny kezeloket es kiadja az API-t.
     * Az onDisable-ben kell meghivni.
     */
    public static void shutdown() {
        if (menuListener != null) {
            HandlerList.unregisterAll(menuListener);
            menuListener = null;
        }
        plugin = null;
    }

    /**
     * @return az inicializalo plugin
     * @throws IllegalStateException ha az API nem lett inicializalva
     */
    public static Plugin plugin() {
        if (plugin == null) {
            throw new IllegalStateException(
                    "Az API nincs inicializalva! Hivd meg az onEnable-ben: Kit.init(plugin)");
        }
        return plugin;
    }

    /**
     * Adot teendo a fo szalon (biztos mutta pending szinkron futtatashoz).
     *
     * @param task a futtatando muvelet
     */
    public static void sync(final Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin(), task);
        }
    }

    /**
     * Azonnali (kovetkezo tick-beli) futtatas main szalon.
     *
     * @param task a futtatando muvelet
     */
    public static void next(final Runnable task) {
        Bukkit.getScheduler().runTask(plugin(), task);
    }

    /**
     * Kesleltetett feladat a szerver fo szalan.
     *
     * @param delayTicks tick merteke (20 = 1 masodperc)
     * @param task a futtatando muvelet
     */
    public static void later(final long delayTicks, final Runnable task) {
        Bukkit.getScheduler().runTaskLater(plugin(), task, Math.max(0, delayTicks));
    }
}
