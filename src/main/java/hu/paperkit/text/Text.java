package hu.paperkit.text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;

import org.bukkit.entity.Player;

/**
 * Szoveg formazas kozpontja.
 *
 * <p>Egy formatarti szintaxist tamogat, keverhetoen:</p>
 */
public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Legacy kodek -> MiniMessage tag mappeloseg. */
    private static final Map<Character, String> CODES = new HashMap<>();

    private static final String HEX_CHARS = "0123456789abcdefABCDEF";

    static {
        CODES.put('0', "<black>");
        CODES.put('1', "<dark_blue>");
        CODES.put('2', "<dark_green>");
        CODES.put('3', "<dark_aqua>");
        CODES.put('4', "<dark_red>");
        CODES.put('5', "<dark_purple>");
        CODES.put('6', "<gold>");
        CODES.put('7', "<gray>");
        CODES.put('8', "<dark_gray>");
        CODES.put('9', "<blue>");
        CODES.put('a', "<green>");
        CODES.put('b', "<aqua>");
        CODES.put('c', "<red>");
        CODES.put('d', "<light_purple>");
        CODES.put('e', "<yellow>");
        CODES.put('f', "<white>");
        CODES.put('k', "<obfuscated>");
        CODES.put('l', "<bold>");
        CODES.put('m', "<strikethrough>");
        CODES.put('n', "<underlined>");
        CODES.put('o', "<italic>");
        CODES.put('r', "<reset>");
    }

    private Text() {
    }

    /**
     * Hanyszveg kirakasa: a legacy kodeket leforditva, MiniMessage
     * parsl-ol alakkal. Apost�rt? A keparovasz nem fut, de formatart
     * nem kell szureni, vagy hibas sorrendek meg faggioi nem zorog.
     *
     * @param input    a szoveg keveredve (MiniMessage es/vagy legacy)
     * @param resolvers placeholder feloldok
     * @return a formazott komponens
     */
    public static Component parse(final String input, final TagResolver... resolvers) {
        if (input == null) {
            return Component.empty();
        }
        return MM.deserialize(translate(input), resolvers);
    }

    /**
     * Csak MiniMessage parsl-ol legacy n lekuni.
     *
     * @param input a MiniMessage szoveg
     * @return a komponens
     */
    public static Component mini(final String input) {
        if (input == null) {
            return Component.empty();
        }
        return MM.deserialize(input);
    }

    /**
     * Csak legacy (kodeknel: &a, &#FEA000) fordit, MiniMessage nelkul.
     */
    public static Component legacy(final String input) {
        return parse(input);
    }

    /**
     * A legacy kodeket MiniMessage tagre fordito transzformator.
     * A MiniMessage szintaxis visszat kitit (amit a parse nem torog).
     *
     * @param input a fordaszoveg
     * @return a forditott szoveg MiniMessage szintaxisba
     */
    public static String translate(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        final StringBuilder out = new StringBuilder(input.length() + 16);
        int i = 0;
        while (i < input.length()) {
            final char c = input.charAt(i);
            if ((c == '&' || c == '\u00a7') && i + 1 < input.length()) {
                final char code = Character.toLowerCase(input.charAt(i + 1));
                // &#RRGGBB forma
                if (code == '#' && isHexAt(input, i + 2, 6)) {
                    out.append("<#").append(input, i + 2, i + 8).append('>');
                    i += 8;
                    continue;
                }
                // &x&F&F&8&8&0&0 forma (Bukkit legaci hex)
                if (code == 'x' && i + 13 < input.length()
                        && isHexDelimiterAt(input, i + 2)) {
                    final String hex = legacyHexAt(input, i + 2);
                    if (hex != null) {
                        out.append("<#").append(hex).append('>');
                        i += 14;
                        continue;
                    }
                }
                final String tag = CODES.get(code);
                if (tag != null) {
                    out.append(tag);
                    i += 2;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static boolean isHexAt(final String s, final int from, final int len) {
        if (from < 0 || from + len > s.length()) {
            return false;
        }
        for (int i = from; i < from + len; i++) {
            if (HEX_CHARS.indexOf(s.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHexDelimiterAt(final String s, final int from) {
        for (int i = from; i < from + 12; i += 2) {
            if (i + 1 >= s.length()) {
                return false;
            }
            final char d = s.charAt(i);
            final char h = s.charAt(i + 1);
            if ((d != '&' && d != '\u00a7') || HEX_CHARS.indexOf(h) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String legacyHexAt(final String s, final int from) {
        final StringBuilder hex = new StringBuilder(6);
        for (int i = from; i < from + 12; i += 2) {
            hex.append(s.charAt(i + 1));
        }
        return hex.toString();
    }

    /**
     * Placeholder feloldo egyeve szoru (konnyu listaban):
     *
     * <pre>{@code
     * Text.parse("<green>Szia, <name>!", Text.res("name", "Pete"))
     * }</pre>
     *
     * @return a TagResolver feloldo
     */
    public static TagResolver res(final String key, final Object value) {
        return net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
                .unparsed(key, String.valueOf(value));
    }

    /**
     * Tobbplaceholder megadasa mappelosegebbol.
     */
    public static TagResolver[] res(final Map<String, ?> values) {
        final TagResolver[] array = new TagResolver[values.size()];
        int i = 0;
        for (final Map.Entry<String, ?> e : values.entrySet()) {
            array[i++] = res(e.getKey(), e.getValue());
        }
        return array;
    }

    /**
     * Cim es also cim megjelenitese dialgus formaban (kis pulzfeladat
     * atmenetben idealisa 20/60/20 vagy az emelnk).
     *
     * @param player a cimzett jatekos
     * @param main   a fo cim szovege, formazva
     * @param sub    az alcim szovege, formazva
     */
    public static void title(final Player player, final String main, final String sub) {
        title(player, main, sub, 5, 40, 5);
    }

    /**
     * Cim egyeni tartammal (tick egysgekben, 20 tick masodperc).
     */
    public static void title(final Player player, final String main, final String sub,
            final int inTicks, final int stayTicks, final int outTicks) {
        player.showTitle(Title.title(
                parse(main),
                parse(sub),
                Title.Times.times(
                        java.time.Duration.ofMillis(inTicks * 50L),
                        java.time.Duration.ofMillis(stayTicks * 50L),
                        java.time.Duration.ofMillis(outTicks * 50L))));
    }

    /**
     * Akciobar (hotbat felett szoveg) megjelenitese.
     */
    public static void actionbar(final Player player, final String input) {
        player.sendActionBar(parse(input));
    }

    /**
     * A formazas elvet: csupasz szoveg visszaadasa (nem tag, nem szinek).
     */
    public static String plain(final String input) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(parse(input));
    }

}
