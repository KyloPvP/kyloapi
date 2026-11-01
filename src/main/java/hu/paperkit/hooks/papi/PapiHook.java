package hu.paperkit.hooks.papi;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import hu.paperkit.hooks.Hooks;

/**
 * PlaceholderAPI hook (reflection): %spellout_placeholder% kenus
 * belso token-kenelation nezvia a PaperKit ki bo.
 *
 * <pre>{@code
 * if (Hooks.isPluginEnabled("PlaceholderAPI") && PapiHook.hooked()) {
 *     String clean = PapiHook.apply(player, "%player_name%");
 * }
 * }</pre>
 */
public final class PapiHook {

    private static Method setPlaceholders;
    private static boolean attempted;
    private static boolean hooked;

    private PapiHook() {
    }

    /**
     * Megprobalja elerni a PAPI-t.
     *
     * @return true, ha sikerult
     */
    public static boolean hooked() {
        if (attempted) {
            return hooked;
        }
        attempted = true;
        if (!Hooks.isPluginEnabled("PlaceholderAPI")) {
            return false;
        }
        try {
            final Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            setPlaceholders = papi.getMethod("setPlaceholders", Player.class, String.class);
            hooked = true;
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Placeholder-ek feloldasa (ha a PAPI nincs fent, a szoveg marad).
     *
     * @param player a jatekos
     * @param text   a szoveg
     * @return a feloldott szoveg
     */
    public static String apply(final Player player, final String text) {
        if (!hooked()) {
            return text;
        }
        try {
            return (String) setPlaceholders.invoke(null, player, text);
        } catch (final Exception e) {
            return text;
        }
    }
}
