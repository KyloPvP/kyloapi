package hu.paperkit.combat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

import hu.paperkit.display.DisplayAnimation;
import hu.paperkit.display.TextDisplayBuilder;
import hu.paperkit.text.Text;

/**
 * Combat vizualizacio: uzemenoskc sea damage pocskava, kritikus talalg,
 * kombocounter.
 *
 * <p>A damage popup kis TextDisplay-ek, amik automatikusan torlodnek.</p>
 *
 * <pre>{@code
 * CombatVisuals.enable(this); // onBoot-ban
 *
 * // sajat popup (barmol, barmelyik helyre):
 * CombatVisuals.showDamage(loc, 12.5, false);
 * CombatVisuals.showText(loc, "<gold>SZINTLEPES!");
 * }</pre>
 */
public final class CombatVisuals implements Listener {

    /** Popup formatum sima talalatnal. */
    private static String popupFormat = "<red><bold>%amount%";
    /** Popup formatum kritikus talalatnal. */
    private static String critFormat = "<gold><bold>%amount%";
    /** Combo formatum (actionbara). */
    private static String comboFormat = "<yellow>%count%<gray>x <red>combo!";
    /** Kombonak a jarida (ms). */
    private static long comboWindowMillis = 3000;

    private static final Map<UUID, Combo> COMBOS = new ConcurrentHashMap<>();
    private static final AtomicInteger OFFSET = new AtomicInteger();

    private CombatVisuals() {
    }

    /**
     * Bekapcsolja a combat vizualokat (listener regisztracio).
     * Iidempotens: tobszori hivas nem tobb listener-t.
     *
     * @param plugin a hivo plugin
     * @return a listener (a disable-heoz a KitPlugin automatikusan
     *         feliratkoztatja a listener-t)
     */
    public static CombatVisuals enable(final Plugin plugin) {
        final CombatVisuals listener = new CombatVisuals();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        return listener;
    }

    // ---------- config ----------

    /**
     * Popup formatum atirasa (_simulation jelesled).
     * {@code %amount%} = a damage, pl. {@code "<red>❤ %amount%"}.
     */
    public static void popupFormat(final String format) {
        popupFormat = format;
    }

    /**
     * Kritikus popup formatuma.
     */
    public static void critFormat(final String format) {
        critFormat = format;
    }

    /**
     * Combo formatuma (actionbar); {@code %count%} = hany talalat.
     */
    public static void comboFormat(final String format) {
        comboFormat = format;
    }

    /**
     * Kombonak jarata milliszekben.
     */
    public static void comboWindow(final long millis) {
        comboWindowMillis = millis;
    }

    // ---------- esemenyek ----------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final LivingEntity victim)) {
            return;
        }
        final boolean crit = event.isCritical();
        showDamage(victim.getLocation(), event.getFinalDamage(), crit);
        if (event.getDamager() instanceof final Player attacker) {
            handleCombo(attacker);
        }
    }

    private static void handleCombo(final Player attacker) {
        final Combo combo = COMBOS.compute(attacker.getUniqueId(), (uuid, old) -> {
            final long now = System.currentTimeMillis();
            if (old != null && now < old.expireAt) {
                return new Combo(old.count + 1, now + comboWindowMillis);
            }
            return new Combo(1, now + comboWindowMillis);
        });
        if (combo.count >= 2) {
            Text.actionbar(attacker, comboFormat.replace("%count%", String.valueOf(combo.count)));
        }
    }

    // ---------- nativ (kulso) hasznalatra ----------

    /**
     * Damage popup kiiras egy helyre.
     *
     * @param loc    a hely (lovisvoked eye/loc, alapveto)
     * @param amount a damage osszeg
     * @param crit   kritikus-e
     */
    public static void showDamage(final Location loc, final double amount, final boolean crit) {
        showText(loc, (crit ? critFormat : popupFormat)
                .replace("%amount%", String.format(java.util.Locale.US, "%.1f", amount)));
    }

    /**
     * Szabad szoveges popup a megadott helyen.
     *
     * @param loc  a hely
     * @param text a szoveg (MiniMessage)
     */
    public static void showText(final Location loc, final String text) {
        final Location base = loc.clone();
        bounce(base);
        final var display = TextDisplayBuilder.at(base)
                .text(text)
                .billboard(org.bukkit.entity.Display.Billboard.CENTER)
                .defaultBackground()
                .lineWidth(220)
                .scale(1.15f)
                .spawn();
        DisplayAnimation.animate(display)
                .scale(1.0f, 3)
                .pause(14)
                .then(display::remove)
                .play();
    }

    // ---------- belso segitok ----------

    /** Popup scilux kiserleti (hogy ne fedjek egymast). */
    private static void bounce(final Location base) {
        final int n = OFFSET.getAndIncrement();
        final double side = ((n % 5) - 2) * 0.14;
        base.add(side, 0.4 + ((n % 3) * 0.08), (n % 2 == 0 ? side : -side) * 0.5);
    }

    private record Combo(int count, long expireAt) {
    }
}
