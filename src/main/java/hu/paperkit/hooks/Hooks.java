package hu.paperkit.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Opcionalis plugin-hookok kozponti regisztracios pontja.
 *
 * <p>A fo PaperKit kod nem tamaszkodik egyetlen kulso pluginra sem;
 * a hookok kulon csomagokban (pl. {@code hooks.packetevents}) aktivolodnak,
 * ha a cel plugin rajta van a szerveren.</p>
 *
 * <pre>{@code
 * OnBoot-ban:
 * if (Hooks.isPluginEnabled("packetevents")) {
 *     PacketEventsHook.engage(plugin);
 *     Msg.info("<aqua>PacketEvents hook bekapcsolva!");
 * }
 * }</pre>
 */
public final class Hooks {

    private Hooks() {
    }

    /**
     * Telepitve es engedelyezve van-e a plugin.
     *
     * @param name a plugin neve (pl. "packetevents", "Vault", "LuckPerms")
     * @return true, ha fut
     */
    public static boolean isPluginEnabled(final String name) {
        final PluginManager pm = Bukkit.getPluginManager();
        return name != null && pm.isPluginEnabled(name);
    }

    /**
     * A telepitett plugin peldany lekerese.
     *
     * @param name a plugin neve
     * @return a Plugin peldany, vagy null
     */
    public static Plugin getPlugin(final String name) {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    /**
     * A telepitett plugin aktualis verzioszama szovegesen.
     *
     * @param name a plugin neve
     * @return a verzio vagy null, ha nincs fent
     */
    public static String version(final String name) {
        final Plugin plugin = getPlugin(name);
        return plugin == null ? null : plugin.getPluginMeta().getVersion();
    }
}
