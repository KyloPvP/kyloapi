package hu.paperkit.placeholder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

/**
 * Placeholder engine: szovegek %kulcs% bazisert csereje.
 *
 * <p>Ketfele szolgaltato lehet:</p>
 * <ul>
 *   <li>plate (jatekfugetlen): {@code register("server_name", mapOf -> "Szerverem")}</li>
 *   <li>per player: {@code register("level", player -> String.valueOf(player.getLevel()))}</li>
 * </ul>
 * PlaceholderAPI es a full integracijat kesobbi lepesben eletel tanozsua (hook).</p>
 */
public final class Placeholders {

    private static final Pattern PATTERN = Pattern.compile("%([a-z0-9_]+(?:_[a-z0-9_]+)*)%");

    private static final Map<String, java.util.function.Supplier<String>> globals = new ConcurrentHashMap<>();
    private static final Map<String, Function<Player, String>> perPlayer = new ConcurrentHashMap<>();

    private Placeholders() {
    }

    /**
     * Jatek-fuggetlen placeholder regisztralasa.
     */
    public static void global(final String key, final java.util.function.Supplier<String> supplier) {
        globals.put(key.toLowerCase(), supplier);
    }

    /**
     * Jatek-fuggo placeholder regisztralasa (Player param).
     */
    public static void register(final String key, final Function<Player, String> supplier) {
        perPlayer.put(key.toLowerCase(), supplier);
    }

    /**
     * Egy placeholder lekerdezese.
     *
     * @return az ertek vagy null ha nincs ilyen
     */
    public static String get(final String key, final Player player) {
        final String lower = key.toLowerCase();
        final Function<Player, String> playerSupplier = perPlayer.get(lower);
        if (playerSupplier != null) {
            try {
                return playerSupplier.apply(player);
            } catch (final Exception e) {
                return null;
            }
        }
        final java.util.function.Supplier<String> globalSupplier = globals.get(lower);
        if (globalSupplier != null) {
            try {
                return globalSupplier.get();
            } catch (final Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Szovegban a %kulcs%-ok kicser?le?se.
     *
     * @param player jelenlegi jatekos (lehet null)
     */
    public static String apply(final String text, final Player player) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        final StringBuilder out = new StringBuilder(text.length() + 32);
        final Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            final String key = matcher.group(1);
            final String value = get(key, player);
            if (value != null) {
                matcher.appendReplacement(out, Matcher.quoteReplacement(value));
            } else {
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }

    /**
     * Placeholder csere + formazas egyfkk?ben.
     */
    public static net.kyori.adventure.text.Component parse(final String text, final Player player) {
        return hu.paperkit.text.Text.parse(apply(text, player));
    }

    /**
     * A regisztralt kulcsok listaja.
     */
    public static List<String> keys() {
        final List<String> all = new java.util.ArrayList<>();
        all.addAll(perPlayer.keySet());
        all.addAll(globals.keySet());
        return all;
    }

    /**
     * Minden placeholder torlese (plugin disable).
     */
    public static void clearAll() {
        perPlayer.clear();
        globals.clear();
    }
}
