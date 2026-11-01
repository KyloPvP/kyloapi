package hu.paperkit.hooks.integrations;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import hu.paperkit.hooks.Hooks;

/**
 * Szerver-menedzsment integrációk: ProtocolLib, TAB, ViaVersion,
 * PlugManX, spark, Plan, CoreProtect.
 */
public final class ServerHooks {

    private ServerHooks() {
    }

    /** ProtocolLib: jelenlét-ellenőrzés. */
    public static final class ProtocolLibHook {
        private ProtocolLibHook() {
        }

        /**
         * @return fent-e a ProtocolLib
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("ProtocolLib");
        }
    }

    /** TAB: jelenlét-ellenőrzés. */
    public static final class TabHook {
        private TabHook() {
        }

        /**
         * @return fent-e a TAB
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("TAB");
        }
    }

    /** ViaVersion: player protocol lekerés. */
    public static final class ViaVersionHook {
        private ViaVersionHook() {
        }

        /**
         * A jatekos protokoll-verzioja (-1, ha nincs ViaVersion vagy hiba).
         */
        public static int protocolVersion(final Player player) {
            if (!Hooks.isPluginEnabled("ViaVersion")) {
                return -1;
            }
            try {
                final Object via = Class.forName("com.viaversion.viaversion.api.Via")
                        .getMethod("getAPI").invoke(null);
                final Object playerManager = via.getClass().getMethod("getPlayerManager").invoke(via);
                final Method version = playerManager.getClass()
                        .getMethod("getPlayerVersion", UUID.class);
                return (int) version.invoke(playerManager, player.getUniqueId());
            } catch (final Exception e) {
                return -1;
            }
        }
    }

    /** PlugManX: plugin be/kapcsolas (a Bukkit API-ival). */
    public static final class PlugManHook {
        private PlugManHook() {
        }

        /**
         * Plugin kikapcsolasa.
         *
         * @return true, ha sikerult
         */
        public static boolean disable(final String pluginName) {
            final Plugin target = Bukkit.getPluginManager().getPlugin(pluginName);
            if (target == null || !Hooks.getPlugin("PlugManX").isEnabled()
                    && !Hooks.isPluginEnabled("PlugManX")) {
                return false;
            }
            Bukkit.getPluginManager().disablePlugin(target);
            return true;
        }

        /**
         * Plugin bekapcsolasa.
         */
        public static boolean enable(final String pluginName) {
            final Plugin target = Bukkit.getPluginManager().getPlugin(pluginName);
            if (target == null) {
                return false;
            }
            if (!target.isEnabled()) {
                Bukkit.getPluginManager().enablePlugin(target);
            }
            return true;
        }
    }

    /** spark: jelenlét (a TPS-t a Bukkit API-tól kapjuk). */
    public static final class SparkHook {
        private SparkHook() {
        }

        /**
         * @return fent-e a spark
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("spark");
        }
    }

    /** Plan: jelenlét. */
    public static final class PlanHook {
        private PlanHook() {
        }

        /**
         * @return fent-e a Player Analytics (Plan)
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("Plan");
        }
    }

    /** CoreProtect: jelenlét. */
    public static final class CoreProtectHook {
        private CoreProtectHook() {
        }

        /**
         * @return fent-e a CoreProtect
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("CoreProtect");
        }
    }
}
