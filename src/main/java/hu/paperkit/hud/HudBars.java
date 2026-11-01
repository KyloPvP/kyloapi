package hu.paperkit.hud;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Locale;

import net.kyori.adventure.bossbar.BossBar;

import org.bukkit.entity.Player;

/**
 * HUD widgetek (statusz savok): health, mana, stamina, barmilyen
 * progress — mindegyik egy sajat bossbar a jatekos felett.
 *
 * <pre>{@code
 * HudBars.Bar hp = HudBars.bar(player, "hp",
 *     "<red>[HP] %current&d/%max&d  %bar%", BossBar.Color.RED);
 * hp.value(player.getHealth()).max(20).push();
 *
 * HudBars.Bar mana = HudBars.bar(player, "mana", "<blue>Mana: %value%", BossBar.Color.BLUE);
 * mana.value(45).max(100).push();
 *
 * HudBars.clear(player); // mind eltuntetese
 * }</pre>
 *
 * <p>Elonk posix ta zhazban id-node-i poustatok (%px%%zavisp)</p>
 */
public final class HudBars {

    /** Egy statuszsav widget. */
    public static final class Bar {

        private final Player player;
        private final BossBarKit bossBar;
        private String titleFormat;
        private BossBar.Color color;
        private double value;
        private double max = 100;

        private Bar(final Player player, final String name, final String titleFormat, final BossBar.Color color) {
            this.player = player;
            this.titleFormat = titleFormat;
            this.color = color;
            this.bossBar = BossBarKit.create(name, color, 1f);
            this.bossBar.showTo(player);
        }

        /**
         * Beallitja az aktualis erteket.
         *
         * @param newValue az ertek
         * @return this
         */
        public Bar value(final double newValue) {
            this.value = newValue;
            return this;
        }

        /**
         * Beallitja a maximalis erteket.
         */
        public Bar max(final double newMax) {
            this.max = Math.max(0.01, newMax);
            return this;
        }

        /**
         * Cim formatum atirasa ({@code %current%}, {@code %max%}, {@code %bar%}).
         */
        public Bar format(final String newFormat) {
            this.titleFormat = newFormat;
            return this;
        }

        /**
         * Szin allitasa.
         */
        public Bar color(final BossBar.Color newColor) {
            this.color = newColor;
            return this;
        }

        /**
         * Elkuldi a widget allapotat a jatekosnak (bossbar frissites).
         *
         * @return this
         */
        public Bar push() {
            final double ratio = Math.clamp(value / max, 0.0, 1.0);
            final StringBuilder filled = new StringBuilder();
            final int length = 15;
            final int lit = (int) Math.round(ratio * length);
            for (int i = 0; i < length; i++) {
                filled.append(i < lit ? '\u25AE' : '\u25AF');
            }
            bossBar.color(color)
                    .progress((float) ratio)
                    .title(titleFormat
                            .replace("%current%", format(value))
                            .replace("%max%", format(max))
                            .replace("%bar%", filled.toString())
                            .replace("%value%", String.valueOf((int) Math.round(value))));
            return this;
        }

        /**
         * Widget eltuntetese.
         */
        public void hide() {
            bossBar.hide(player);
        }

        private static String format(final double number) {
            if (number == Math.rint(number)) {
                return String.valueOf((long) number);
            }
            return String.format(Locale.US, "%.1f", number);
        }
    }

    private static final Map<UUID, Map<String, Bar>> BARS = new ConcurrentHashMap<>();

    private HudBars() {
    }

    /**
     * Widget lekerese (letrehozza, ha eddig nem volt).
     *
     * @param player      a jatekos
     * @param name        a widget neve ("hp", "mana", "stamina" stb.)
     * @param titleFormat cim formatum (%current%/%max%/%bar%/%value%)
     * @param color       bossbar szin
     * @return a widget
     */
    public static Bar bar(final Player player, final String name, final String titleFormat,
            final BossBar.Color color) {
        final Bar existing = get(player, name);
        if (existing != null) {
            if (titleFormat != null) {
                existing.format(titleFormat);
            }
            if (color != null) {
                existing.color(color);
            }
            return existing;
        }
        final Bar created = new Bar(player, name.toLowerCase(Locale.ROOT),
                titleFormat == null ? "%current%" : titleFormat,
                color == null ? BossBar.Color.WHITE : color);
        BARS.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(name.toLowerCase(Locale.ROOT), created);
        return created;
    }

    /**
     * Widget lekerese letrehozas nelkul (vagy null).
     */
    public static Bar get(final Player player, final String name) {
        final Map<String, Bar> map = BARS.get(player.getUniqueId());
        return map == null ? null : map.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Minden widget eltuntetese a jatekosnal.
     */
    public static void clear(final Player player) {
        final Map<String, Bar> map = BARS.remove(player.getUniqueId());
        if (map != null) {
            for (final Bar bar : map.values()) {
                bar.bossBar.delete();
            }
        }
    }
}
