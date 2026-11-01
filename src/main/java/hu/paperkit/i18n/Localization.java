package hu.paperkit.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import hu.paperkit.text.Messages;
import hu.paperkit.text.Msg;

/**
 * Localization Engine: jatekos-nelkuli nyelvi valasztas.
 *
 * <p>Olvasas rend (jatekos locale alapjan):</p>
 * <pre>{@code
 * messages_hu.yml  /  messages_en_us.yml / messages_hu_hu.yml
 * messages_hu.yml  -> nyelv prefix (elso 2 karakter)
 * messages.yml     -> fallback
 * }</pre>
 *
 * <pre>{@code
 * Localization loc = Localization.load(this, "messages");
 * loc.send(player, "welcome", "<green>Udv!");
 * }</pre>
 */
public final class Localization {

    private final JavaPlugin plugin;
    private final String baseName;
    private final Map<String, Messages> cache = new ConcurrentHashMap<>();
    private final Messages fallback;

    private Localization(final JavaPlugin plugin, final String baseName) {
        this.plugin = plugin;
        this.baseName = baseName;
        this.fallback = Messages.load(plugin, baseName + ".yml");
        cache.put("default", fallback);
    }

    /**
     * Betoltes.
     *
     * @param plugin   a plugin
     * @param baseName a fajl alap-neve (pl. "messages")
     * @return a localization
     */
    public static Localization load(final JavaPlugin plugin, final String baseName) {
        return new Localization(plugin, baseName);
    }

    /**
     * A jatekos nyelvehez tartozo Messages.
     *
     * @param player a jatekos (locale alapjan, pl. "hu-hu")
     * @return a messages (fallback, ha nincs nyelvi fajl)
     */
    public Messages messages(final Player player) {
        final String locale = player == null ? null : player.getLocale();
        if (locale == null || locale.isBlank()) {
            return fallback;
        }
        final String language = locale.toLowerCase(Locale.ROOT)
                .replace('-', '_').split("_")[0];
        return cache.computeIfAbsent(language, key -> Messages.load(plugin, baseName + "_" + key + ".yml"));
    }

    /**
     * Uzenet kuldese a jatekos nyelven.
     *
     * @param player    a jatekos
     * @param path      az uzenet utvonala
     * @param def       alapertelmezett szoveg
     * @param keyValues %kulcs% ertekek
     */
    public void send(final Player player, final String path, final String def,
            final Object... keyValues) {
        messages(player).send(player, path, def, keyValues);
    }

    /**
     * Nyelvi Messages cache vont a csik csarnokok. Avai ✅ {@link Msg}
     */
    public void clearCache() {
        cache.clear();
    }
}
