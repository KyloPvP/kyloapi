package hu.paperkit.text;

/**
 * Vanilla (alap Minecraft font) ikonok — kis jelek, amiket barmelyik
 * uzenetben hasznalhatsz (MiniMessage-ben is eloall a normal szoveggel).
 *
 * <p>Ezek unicode-karakterek, nem resource-pack fejlec: az alap jatekfont
 * egy reszet ban sort (a legtobb) es mindent megjelenit a jatekbeli
 * felulet (chat, lore, bossbar, score).</p>
 *
 * <p>Pelda:</p>
 * <pre>{@code
 * Msg.send(player, Icons.SWORD + " <white>Kezd a harcot!");
 * }</pre>
 */
public final class Icons {

    private Icons() {
    }

    /* ---- fegyverek, harc ---- */
    public static final String SWORD = "⚔";
    public static final String BOW = "🏹";
    public static final String SHIELD = "🛡";
    public static final String SKULL = "☠";
    public static final String HEART = "❤";

    /* ---- UI nyilak ---- */
    public static final String RIGHT = "»";
    public static final String LEFT = "«";
    public static final String UP = "▲";
    public static final String DOWN = "▼";
    public static final String DOT_RIGHT = "▸";

    /* ---- alakzatok ---- */
    public static final String DOT = "●";
    public static final String SQUARE = "■";
    public static final String SQUARE_EMPTY = "□";
    public static final String SMALL_SQUARE = "▪";
    public static final String SMALL_SQUARE_EMPTY = "▫";
    public static final String DIAMOND = "◆";
    public static final String BAR_FULL = "▮";
    public static final String BAR_EMPTY = "▯";
    public static final String LINE = "─";
    public static final String DOUBLE_LINE = "═";

    /* ---- statusz ---- */
    public static final String CHECK = "✓";
    public static final String CROSS = "✕";
    public static final String STAR = "★";
    public static final String STAR_EMPTY = "☆";
    public static final String NOTE = "♪";
    public static final String PIN = "⚑";
    public static final String ANCHOR = "⚓";
    public static final String WARNING = "⚠";
    public static final String FLOWER = "✿";
}
