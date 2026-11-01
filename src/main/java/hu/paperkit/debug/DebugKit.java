package hu.paperkit.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import hu.paperkit.core.Kit;
import hu.paperkit.task.TaskKit;
import hu.paperkit.task.TaskKit.Repeating;
import hu.paperkit.text.Msg;

/**
 * Performance es Debug eszkozonek: idometes, TPS-monitor.
 *
 * <pre>{@code
 * DebugKit.time("update", () -> { /* munka *&#47; });
 * DebugKit.tpsMonitor(this); // figyelmeztet ha tps alacsony
 * }</pre>
 */
public final class DebugKit {

    private DebugKit() {
    }

    /**
     * Muvelet idometese naploba irva.
     *
     * @param label a neve/srcmkoze
     * @param task  a futtatando muvelet
     * @return az eltelt milliszek
     */
    public static long time(final String label, final Runnable task) {
        final long start = System.nanoTime();
        try {
            task.run();
        } finally {
            final long millis = (System.nanoTime() - start) / 1_000_000;
            Msg.info("<gray>[DebugKit] <white>" + label + "<gray> idotartam: <aqua>" + millis + "ms");
            return millis;
        }
    }

    /**
     * Muvelet idometese visszaternivel (futtatasi visszat).
     *
     * @param label a megnevezes
     * @param task  a futtatando
     * @return az eltelt milliszek (a muvelet futasa mertem)
     */
    public static long record(final String label, final Runnable task) {
        final long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * TPS monitor: minden 10 mp-ben ellenorzi a szerver TPS-t
     * es figyelmeztet ha csoncsv latasa.
     *
     * @param plugin     a hivo plugin
     * @param minTps     minimum TPS (pl. 18)
     * @return a monitor task (cancel() toltik)
     */
    public static Repeating tpsMonitor(final Plugin plugin, final double minTps) {
        return TaskKit.repeat(200L, cycle -> {
            final double[] tps = Bukkit.getTPS();
            final double last = tps.length > 0 ? tps[0] : 20.0;
            if (last < minTps) {
                Msg.warn("TPS alacsony! (" + String.format("%.2f", last) + ")"
                        + " mem: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB
                        + MB_UNIT + " fs hasznalt");
            }
        }).start();
    }

    /**
     * Memory info naplozasa.
     */
    public static void memory() {
        final Runtime rt = Runtime.getRuntime();
        final long used = (rt.totalMemory() - rt.freeMemory()) / MB;
        final long total = rt.totalMemory() / MB;
        Msg.info("<gray>[DebugKit] mem: <aqua>" + used + "<gray>/" + total + MB_UNIT);
    }

    /**
     * Gyors statisztika: online jatekosok, load.
     */
    public static void snapshot() {
        memory();
        Msg.info("<gray>[DebugKit] online: <aqua>" + Bukkit.getOnlinePlayers().size()
                + "<gray>, loaded chunks: <aqua>" + Bukkit.getWorlds().size());
    }

    private static final long MB = 1024L * 1024;
    private static final String MB_UNIT = "MB";
}
