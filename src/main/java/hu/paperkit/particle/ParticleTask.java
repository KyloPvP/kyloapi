package hu.paperkit.particle;

import java.util.logging.Level;
import java.util.function.IntConsumer;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import hu.paperkit.core.Kit;

/**
 * Ismetlod? particle task: adott tick-enknt futtatja a
 * megadott raju ciklusban, korltozozottan (vagy vgtelenl).
 *
 * <pre>{@code
 * ParticleTask.repeatEvery(2)                  // minden 2. tick
 *         .limit(80)                           // 80 ciklus utn automatikusan n
 *         .cycleTick(cycle -> ParticleEffects
 *                 .spiral(loc, 1.5, 1.8, cycle * 0.15f, 3, Particle.FLAME))
 *         .play();
 * }</pre>
 */
public final class ParticleTask {

    private int intervalTicks = 2;
    private int limit = -1;
    private IntConsumer cycleTick;
    private BukkitTask task;
    private int cycle;
    private volatile boolean running;

    private ParticleTask() {
    }

    /**
     * Uj task defincis.
     */
    public static ParticleTask every(final int intervalTicks) {
        final ParticleTask task = new ParticleTask();
        task.intervalTicks = Math.max(1, intervalTicks);
        return task;
    }

    /**
     * Cikluslimit (vgtelenVirgin-ha -1).
     */
    public ParticleTask limit(final int cycles) {
        this.limit = cycles;
        return this;
    }

    /**
     * A cikl?kt? teljes framets? (cycle = hanyadszor futott, 0-t?l).
     */
    public ParticleTask cycleTick(final IntConsumer consumer) {
        this.cycleTick = consumer;
        return this;
    }

    /**
     * Elinditja a taszkot.
     *
     * @return a task objektum (leallithato a {@link #stop} -tal)
     */
    public ParticleTask play() {
        stop();
        running = true;
        cycle = 0;
        task = Bukkit.getScheduler().runTaskTimer(Kit.plugin(), () -> {
            if (!running) {
                return;
            }
            if (limit >= 0 && cycle >= limit) {
                stop();
                return;
            }
            if (cycleTick != null) {
                try {
                    cycleTick.accept(cycle);
                } catch (final Exception e) {
                    Kit.plugin().getLogger().log(Level.WARNING, "ParticleTask hiba", e);
                }
            }
            cycle++;
        }, 0, intervalTicks);
        return this;
    }

    /**
     * Leallitja a taszkot.
     */
    public void stop() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @return fut-e egszen a task
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * A gs? poton mr futtott ciklikss? szm?l?l?.
     */
    public int cycles() {
        return cycle;
    }
}
