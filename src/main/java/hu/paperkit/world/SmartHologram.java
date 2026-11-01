package hu.paperkit.world;

import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import hu.paperkit.core.Kit;
import hu.paperkit.display.Hologram;
import hu.paperkit.task.TaskKit;
import hu.paperkit.task.TaskKit.Repeating;

/**
 * Smart Hologram / World UI: hologram ami egy entitat kovet
 * (pl. egy jatekos felett lebeg) es lassan ocsorgos.
 *
 * <pre>{@code
 * SmartHologram hud = SmartHologram.create(player.getLocation(),
 *         "<gold>" + player.getName());
 * hud.follow(player, 2.4);      // 2.4 blokkra a fej hoa
 * hud.bob(0.15);                // ocsego浩 ki amplitudé
 * ...
 * hud.remove();
 * }</pre>
 */
public final class SmartHologram {

    private static final long TICK_INTERVAL = 2;

    private Hologram hologram;
    private UUID followTarget;
    private double height;
    private double bobAmplitude;
    private Repeating ticker;
    private long cycles;

    private SmartHologram() {
    }

    /**
     * Letrehoz egy smart hologramot a megadott helyen.
     *
     * @param base a kezdohely
     * @param lines a szovegek (MiniMessage)
     * @return a hologram
     */
    public static SmartHologram create(final Location base, final String... lines) {
        final SmartHologram sh = new SmartHologram();
        sh.hologram = Hologram.create(base, lines).spawn();
        return sh;
    }

    /**
     * Kovetes egy entity utan (pl. Player, ArmorStand).
     *
     * @param target az entity-t (a szammu jatekos)
     * @param height magassago a fej a hoar (blokkban)
     */
    public SmartHologram follow(final Entity target, final double height) {
        this.followTarget = target.getUniqueId();
        this.height = height;
        startTicker();
        return this;
    }

    /**
     * Kovetes a dinamikus pozccioval (pl. kamera-alapek).
     *
     * @param supplier a hely szamitoja (folyamatosan hivodik)
     */
    public SmartHologram followDynamic(final Supplier<Location> supplier) {
        this.dynamicSupplier = supplier;
        startTicker();
        return this;
    }

    /**
     * Ocorgo (lebeg) amplitudo blokkban.
     */
    public SmartHologram bob(final double amplitude) {
        this.bobAmplitude = amplitude;
        return this;
    }

    /**
     * Szoveg allitasa (sororszinten).
     */
    public SmartHologram line(final int index, final String text) {
        hologram.setLine(index, text);
        return this;
    }

    /**
     * Azonnali mozgatas.
     */
    public SmartHologram move(final Location to) {
        hologram.move(to);
        return this;
    }

    /**
     * Torles es a task megallitasa.
     */
    public void remove() {
        stopTicker();
        hologram.delete();
    }

    private void startTicker() {
        if (ticker != null && ticker.isRunning()) {
            return;
        }
        cycles = 0;
        ticker = TaskKit.repeat(TICK_INTERVAL, cycle -> tick());
    }

    private void stopTicker() {
        if (ticker != null) {
            ticker.cancel();
        }
    }

    private void tick() {
        final Location location = currentLocation();
        if (location == null) {
            return;
        }
        double y = 0;
        if (bobAmplitude > 0) {
            y = mobileTick() * bobAmplitude;
        }
        final Location target = location.add(0, y, 0);
        hologram.move(target);
        cycles++;
    }

    private double mobileTick() {
        return Math.sin((cycleCount() % 40) / 40.0 * Math.PI * 2);
    }

    private long cycleCount() {
        return cycles;
    }

    private Location currentLocation() {
        if (dynamicSupplier != null) {
            return dynamicSupplier.get();
        }
        if (followTarget != null) {
            final Entity entity = entityById(followTarget);
            if (entity == null || entity.isDead()) {
                return null;
            }
            return entity.getLocation().add(0, height, 0);
        }
        return null;
    }

    private Entity entityById(final UUID id) {
        for (final var world : hu.paperkit.core.Kit.plugin().getServer().getWorlds()) {
            for (final Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(id)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private Supplier<Location> dynamicSupplier;

    /** Frissitjuk ezeresen a jatekos latok is - sajanlom adott. */
    public boolean isFollowing(final UUID id) {
        return followTarget != null && followTarget.equals(id);
    }
}
