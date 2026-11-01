package hu.paperkit.compat;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Version Compatibility Layer: szerver-detektalas es szalbiztos
 * task-futtatas (Folia es vanili szerveren is mukodik).
 *
 * <pre>{@code
 * if (Compat.isFolia()) {
 *     hu.paperkit.text.Msg.info("<aqua>Folia mod");
 * }
 * Compat.atEntity(entity, () -> doWork());    // mindig a megfelelo szalon
 * Compat.global(() -> doWork());              // global szal
 * }</pre>
 */
public final class Compat {

    private static final boolean FOLIA = detectFolia();

    private Compat() {
    }

    /**
     * @return true, ha a szerver Folia (vagy Folia-kompatibilis fork)
     */
    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * @return a szerver nms verzioja, pl. "1.21.4-R0.1-SNAPSHOT"
     */
    public static String serverVersion() {
        return Bukkit.getBukkitVersion();
    }

    /**
     * Muvelet az entity-hez tartozo szalon (Folia: region szal,
     * vanilia: main thread).
     *
     * @param entity az entity
     * @param task   a task
     */
    public static void atEntity(final Entity entity, final Runnable task) {
        if (entity == null || !entity.isValid()) {
            KitGlobal.global(task);
            return;
        }
        if (FOLIA && foliaEntityScheduler(entity, task)) {
            return;
        }
        hu.paperkit.core.Kit.sync(task);
    }

    /**
     * Muvelet a global szalon (Folia: global region szal).
     *
     * @param task a task
     */
    public static void global(final Runnable task) {
        KitGlobal.global(task);
    }

    /**
     * Aszinkron igazat (a thread-poolon).
     *
     * @param task a task
     */
    public static void async(final Runnable task) {
        if (FOLIA) {
            runFoliaAsync(task);
        } else {
            hu.paperkit.task.TaskKit.async(task);
        }
    }

    // ---------- Folia reflection ----------

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean foliaEntityScheduler(final Entity entity, final Runnable task) {
        try {
            final Plugin plugin = hu.paperkit.core.Kit.plugin();
            final Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            final Method run = scheduler.getClass().getMethod("run",
                    Plugin.class, java.util.function.Consumer.class, Runnable.class);
            // a ScheduledTask-el nem kell zon (tuzes-elgend)
            run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run(), null);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void runFoliaAsync(final Runnable task) {
        try {
            final Plugin plugin = hu.paperkit.core.Kit.plugin();
            final Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            final Method runNow = scheduler.getClass().getMethod("runNow",
                    Plugin.class, java.util.function.Consumer.class);
            runNow.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run());
        } catch (final Exception e) {
            hu.paperkit.task.TaskKit.async(task);
        }
    }

    /** Global-Kit (a szerver szal biztositass) segito. */
    private static final class KitGlobal {
        static void global(final Runnable task) {
            if (FOLIA) {
                try {
                    final Plugin plugin = hu.paperkit.core.Kit.plugin();
                    final Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                    final Method run = scheduler.getClass().getMethod("run",
                            Plugin.class, java.util.function.Consumer.class);
                    run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) t -> task.run());
                    return;
                } catch (final Exception e) {
                    // fallback: bukkit szal
                }
            }
            hu.paperkit.core.Kit.sync(task);
        }
    }
}
