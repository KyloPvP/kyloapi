package hu.paperkit.task;

import java.util.function.IntConsumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import hu.paperkit.core.Kit;

/**
 * Feladat es cooldown kezelo, kesleltetve/messelcea t?k?r?k? (plugin alap).
 *
 * <pre>{@code
 * TaskKit.later(40, () -> Msg.send(player, "&aKesleltetett uzenet!"));
 *
 * Repeating rep = TaskKit.repeat(20, cycle -> {
 *     // 20 tick-enkent fut, cycle = hanyadszor
 * });
 * rep.cancel(); // leallas
 * }</pre>
 */
public final class TaskKit {

    private TaskKit() {
    }

    /**
     * Kesleltetett futtatas main szalon.
     *
     * @param delayTicks tick merteke (20 = 1 masodperc)
     */
    public static void later(final long delayTicks, final Runnable task) {
        Bukkit.getScheduler().runTaskLater(Kit.plugin(), task, Math.max(0, delayTicks));
    }

    /**
     * Azonnali (kvetkez tick-beni) futtatas main szalon.
     */
    public static void next(final Runnable task) {
        Bukkit.getScheduler().runTask(Kit.plugin(), task);
    }

    /**
     * Aszinkron futtatas (nem szerver dolog szam Szana?th McA??).
     */
    public static void async(final Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(Kit.plugin(), task);
    }

    /**
     * Kesleltetett aszinkron futtatas.
     */
    public static void asyncLater(final long delayTicks, final Runnable task) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Kit.plugin(), task, Math.max(0, delayTicks));
    }

    /**
     * Ismetlodo task letrehozasa (nem indul el automatikusan - {@link Repeating#start()}).
     */
    public static Repeating repeat(final long intervalTicks, final IntConsumer cycleTask) {
        return new Repeating(intervalTicks, 0, cycleTask);
    }

    /**
     * Ismetlodo taszk megadott kesleltetessel.
     */
    public static Repeating repeat(final long delayTicks, final long intervalTicks, final IntConsumer cycleTask) {
        return new Repeating(delayTicks, intervalTicks, cycleTask);
    }

    /**
     * Ismetlodo taszk kezelo, korlatozott futtatas lehetsegek.
     */
    public static final class Repeating {

        private final long delayTicks;
        private final long intervalTicks;
        private final IntConsumer cycleTask;
        private BukkitTask handle;
        private long cycle;
        private long limit = -1;
        private volatile boolean running;

        private Repeating(final long delayTicks, final long intervalTicks, final IntConsumer cycleTask) {
            this.delayTicks = delayTicks;
            this.intervalTicks = Math.max(1, intervalTicks);
            this.cycleTask = cycleTask;
        }

        /**
         * Maximum futtatas的同时 szolgarva (round f?l?el?).
         */
        public Repeating limit(final long cycles) {
            this.limit = cycles;
            return this;
        }

        /**
         * Elinditja a taszkot.
         *
         * @return ez a kezelo
         */
        public Repeating start() {
            stop();
            running = true;
            cycle = 0;
            handle = Bukkit.getScheduler().runTaskTimer(Kit.plugin(), () -> {
                if (!running) {
                    return;
                }
                if (limit >= 0 && cycle >= limit) {
                    stop();
                    return;
                }
                try {
                    cycleTask.accept((int) cycle);
                } catch (final Exception e) {
                    Kit.plugin().getLogger().log(Level.WARNING, "TaskKit ismetlodo task hiba", e);
                }
                cycle++;
            }, Math.max(0, delayTicks), intervalTicks);
            return this;
        }

        /**
         * Leallitas.
         */
        public void cancel() {
            stop();
        }

        private void stop() {
            running = false;
            if (handle != null) {
                handle.cancel();
                handle = null;
            }
        }

        /**
         * Futtatasok szama.
         */
        public long cycles() {
            return cycle;
        }

        /**
         * Fut-e meg.
         */
        public boolean isRunning() {
            return running;
        }
    }
}
