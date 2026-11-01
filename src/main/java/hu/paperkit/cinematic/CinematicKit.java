package hu.paperkit.cinematic;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import hu.paperkit.core.Kit;
import hu.paperkit.task.TaskKit;
import hu.paperkit.task.TaskKit.Repeating;

/**
 * Cinematic Engine: smooth kameramu bavantatos soros pontok
 * kozott (cosine easing), teleport-alap; a jatekos nezni fog az
 * aktualisiranyba.
 *
 * <pre>{@code
 * CinematicKit.play(plugin, player, List.of(p1, p2, p3), 100, () -> {
 *     hu.paperkit.text.Msg.send(player, "<green>A cinematic veget ert!");
 * });
 * CinematicKit.cancel(player); // megszakitás
 * }</pre>
 */
public final class CinematicKit {

    /** Aktiv cinematic taskok (jatekos szerint). */
    private static final Map<UUID, Repeating> ACTIVE = new ConcurrentHashMap<>();

    private CinematicKit() {
    }

    /**
     * Cinematic lejatszasa: pontok kozotti smooth mozgas (cosine easing).
     *
     * @param plugin     a hivo plugin
     * @param player     a jatekos
     * @param points     a kamerapontok (min. 2)
     * @param totalTicks osszes ido tick-ben
     * @param onEnd      hivodik a vegebb, vagy null
     */
    public static void play(final Plugin plugin, final Player player, final List<Location> points,
            final long totalTicks, final Runnable onEnd) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Legalabb 2 kamerapont szukseges!");
        }
        cancel(player);
        final int segments = points.size() - 1;
        final long ticksPerSegment = Math.max(1, totalTicks / segments);
        final AtomicBoolean ended = new AtomicBoolean(false);

        final Repeating task = TaskKit.repeat(1, cycle ->
                tick(player, points, cycle, segments, ticksPerSegment, totalTicks, onEnd, ended))
                .limit(totalTicks + 1)
                .start();
        ACTIVE.put(player.getUniqueId(), task);
    }

    private static void tick(final Player player, final List<Location> points, final int cycle,
            final int segments, final long ticksPerSegment, final long totalTicks,
            final Runnable onEnd, final AtomicBoolean ended) {
        if (cycle >= totalTicks) {
            if (onEnd != null && ended.compareAndSet(false, true)) {
                Ended.get(player, onEnd);
                ended.set(true);
            }
            return;
        }
        final int index = Math.min(segments - 1, (int) ((long) cycle / ticksPerSegment));
        final double phase = ease(((double) cycle / ticksPerSegment) - index);
        final Location from = points.get(index);
        final Location to = points.get(index + 1);
        final Vector delta = to.clone().toVector().subtract(from.toVector());
        final Location interpolated = from.clone().add(delta.clone().multiply(phase));
        if (delta.lengthSquared() > 0.0001) {
            interpolated.setDirection(delta.clone().normalize());
        } else {
            interpolated.setDirection(from.getDirection());
        }
        player.teleport(interpolated);
    }

    /** Cosine easing: smooth eleje es vege. */
    static double ease(final double phase) {
        final double clamped = Math.min(1, Math.max(0, phase));
        return 0.5 - 0.5 * Math.cos(clamped * Math.PI);
    }

    /**
     * Megszakitja a jatekos aktiv cinematicjet.
     */
    public static void cancel(final Player player) {
        final Repeating task = ACTIVE.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    /** Vegsz hivashoz es menedjspik belszonal. */
    private static final class Ended {
        static void get(final Player player, final Runnable end) {
            Kit.next(end);
            ACTIVE.remove(player.getUniqueId());
        }
    }
}
