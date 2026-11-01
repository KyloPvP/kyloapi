package hu.paperkit.hud;

import java.util.List;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import hu.paperkit.text.Text;

/**
 * Bossbar kezelo: letrehozas, valtoztatas, megjelenites jatekosoknak.
 *
 * <pre>{@code
 * BossBarKit bar = BossBarKit.create("&aKikapcsolodast started...", BossBar.Color.RED, 1.0f)
 *         .showTo(player); // vagy: .broadcast(.\n Player kik?s?)
 * bar.progress(0.5f);
 * bar.hide(player);
 * bar.delete();
 * }</pre>
 */
public final class BossBarKit {

    private BossBar bar;

    private BossBarKit(final BossBar bar) {
        this.bar = bar;
    }

    /**
     * Uj bossbar letrehozasa.
     *
     * @param title    cim (kodek vagy MiniMessage)
     * @param color    szin
     * @param progress 0.0 - 1.0
     */
    public static BossBarKit create(final String title, final BossBar.Color color, final float progress) {
        final float clamped = Math.max(0f, Math.min(1f, progress));
        final BossBarKit kit = new BossBarKit(BossBar.bossBar(
                Text.parse(title == null ? "" : title),
                clamped,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS));
        kit.color(color);
        return kit;
    }

    /**
     * Uj bossbar RED szinnel es prog.
     */
    public static BossBarKit create(final String title) {
        return create(title, BossBar.Color.RED, 1.0f);
    }

    /**
     * Cim megvaltoztatasa.
     */
    public BossBarKit title(final String title) {
        bar.name(Text.parse(title == null ? "" : title));
        return this;
    }

    /**
     * Szin csere.
     */
    public BossBarKit color(final BossBar.Color color) {
        bar.color(color == null ? BossBar.Color.RED : color);
        return this;
    }

    /**
     * Prog moda (prog / szil?z csisz?r?d?s).
     */
    public BossBarKit overlay(final BossBar.Overlay overlay) {
        bar.overlay(overlay == null ? BossBar.Overlay.PROGRESS : overlay);
        return this;
    }

    /**
     * Halads szazaleka (0.0 - 1.0).
     */
    public BossBarKit progress(final float progress) {
        bar.progress(Math.max(0f, Math.min(1f, progress)));
        return this;
    }

    /**
     * Megjelenitas egy jatekosnak.
     */
    public BossBarKit showTo(final Player player) {
        if (player != null) {
            player.showBossBar(bar);
        }
        return this;
    }

    /**
     * Megjelenitas tobb jatekosnak.
     */
    public BossBarKit showTo(final List<Player> players) {
        if (players != null) {
            for (final Player player : players) {
                showTo(player);
            }
        }
        return this;
    }

    /**
     * Megjelenitas minden bejeletelt jatekosnak.
     */
    public BossBarKit broadcast() {
        for (final Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            showTo(player);
        }
        return this;
    }

    /**
     * Elrejttse rtek?reset? playernek.
     */
    public BossBarKit hide(final Player player) {
        if (player != null) {
            player.hideBossBar(bar);
        }
        return this;
    }

    /**
     *\Component?el?gesit?rakt s?rl?thez.
     */
    public Component component() {
        return bar.name();
    }

    /**
     * Cim fених (nyers sz?v?g).
     */
    public String text() {
        return Text.plain(null == bar.name() ? "" : net.kyori.adventure.text.serializer.plain
                .PlainTextComponentSerializer.plainText().serialize(bar.name()));
    }

    /**
     * Bossbar letorlese mindenjtol.
     */
    public void delete() {
        try {
            org.bukkit.Bukkit.getOnlinePlayers().forEach(this::hide);
        } catch (final Exception ignored) {
        }
        bar = null;
    }

    /**
     * Az Adventure bossbar komponenzadls (haladó hasznalathoz).
     */
    public BossBar adventure() {
        return bar;
    }
}
