package hu.paperkit.notify;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import hu.paperkit.text.Text;

/**
 * Notification Center: konzisztens, stilizert ertesitesek
 * (actionbar / title / hang) a pluginok szamara.
 *
 * <pre>{@code
 * Notifier.success(player, "<green>Ki mentve!");
 * Notifier.warn(player, "<red>Hibas szint!");
 * Notifier.notify(player, "<gold>Szintlepés!", 60); // 60 tick title
 * }</pre>
 */
public final class Notifier {

    /** Success hang. */
    private static Sound successSound = Sound.ENTITY_PLAYER_LEVELUP;
    /** Info hang. */
    private static Sound infoSound = Sound.UI_BUTTON_CLICK;
    /** Warn hang. */
    private static Sound warnSound = Sound.ENTITY_VILLAGER_NO;
    /** Prefix minden ertesites ele. */
    private static String prefix = "";

    private Notifier() {
    }

    /**
     * Siker-ertesites: actionbar + hang.
     *
     * @param player a jatekos
     * @param text   a szoveg (MiniMessage)
     */
    public static void success(final Player player, final String text) {
        send(player, text, successSound);
    }

    /**
     * Info-ertesites: actionbar + hang.
     */
    public static void info(final Player player, final String text) {
        send(player, text, infoSound);
    }

    /**
     * Figyelmeztetes: actionbar + hang.
     */
    public static void warn(final Player player, final String text) {
        send(player, text, warnSound);
    }

    /**
     * Title-ertesites (felso es also resz).
     *
     * @param player    a jatekos
     * @param main      a fo szoveg
     * @param sub       az also szoveg (lehet ures)
     * @param stayTicks mennyi ideig maradjon
     */
    public static void announce(final Player player, final String main, final String sub,
            final int stayTicks) {
        Text.title(player, main, sub, 5, stayTicks, 10);
        if (infoSound != null) {
            player.playSound(player.getLocation(), infoSound, 1f, 1f);
        }
    }

    private static void send(final Player player, final String text, final Sound sound) {
        Text.actionbar(player, prefix + text);
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

    /**
     * Minden ertesites ele rakodo prefix.
     *
     * @param newPrefix a prefix (MiniMessage)
     */
    public static void prefix(final String newPrefix) {
        prefix = newPrefix == null ? "" : newPrefix;
    }

    /**
     * Hang allitas (a levelupilachrl sor).
     */
    public static void sounds(final Sound success, final Sound info, final Sound warn) {
        successSound = success;
        infoSound = info;
        warnSound = warn;
    }

    /**
     * Hangok "kihagnyasa" (null- Ehhez, ne lasakok).
     */
    public static void silence() {
        successSound = null;
        infoSound = null;
        warnSound = null;
    }
}
