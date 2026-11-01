package hu.paperkit.hooks.integrations;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import hu.paperkit.hooks.Hooks;

/**
 * Védelmi/plugin-határok integrációi: WorldGuard, WorldEdit,
 * GriefPrevention, LiteBans. Mindegyik reflexióval él, így a
 * foldítolás a CLASsPATH OUTPUT plugin nélkül is működik.
 */
public final class ProtectionHooks {

    private ProtectionHooks() {
    }

    /** WorldGuard integráció. */
    public static final class WorldGuardHook {
        private WorldGuardHook() {
        }

        /**
         * A helyen érvényes régiók nevei (ha a WG nincs fent: ures).
         */
        public static Set<String> regionIds(final Location loc) {
            final Set<String> ids = new HashSet<>();
            if (!Hooks.isPluginEnabled("WorldGuard")) {
                return ids;
            }
            try {
                final Object worldGuard = Class.forName("com.sk89q.worldguard.WorldGuard")
                        .getMethod("getInstance").invoke(null);
                final Object platform = worldGuard.getClass()
                        .getMethod("getPlatform").invoke(worldGuard);
                final Object container = platform.getClass()
                        .getMethod("getRegionContainer").invoke(platform);
                final Object query = container.getClass()
                        .getMethod("createQuery").invoke(container);
                final Object weLoc = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter")
                        .getMethod("adapt", org.bukkit.Location.class).invoke(null, loc);
                final Object regions = query.getClass()
                        .getMethod("getApplicableRegions", weLoc.getClass()).invoke(query, weLoc);
                final Object raw = regions.getClass().getMethod("getRegions").invoke(regions);
                for (final Object region : (Iterable<?>) raw) {
                    ids.add(String.valueOf(region.getClass().getMethod("getId").invoke(region)));
                }
            } catch (final Exception ignored) {
                // WG nincs fent/lustas
            }
            return ids;
        }

        /**
         * A hely bent van-e a megadott id-kú régióban.
         */
        public static boolean inRegion(final Location loc, final String regionId) {
            return regionIds(loc).contains(regionId.toLowerCase());
        }
    }

    /** WorldEdit integráció (jelenlét + segédfunkciók). */
    public static final class WorldEditHook {
        private WorldEditHook() {
        }

        /**
         * @return fent van-e a WorldEdit
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("WorldEdit");
        }
    }

    /** GriefPrevention integráció. */
    public static final class GriefPreventionHook {
        private GriefPreventionHook() {
        }

        /**
         * A hely claim-ben van-e.
         */
        public static boolean isClaimed(final Location loc) {
            return claimObject(loc) != null;
        }

        /**
         * A claim tulajdonosának neve (vagy null).
         */
        public static String claimOwner(final Location loc) {
            final Object claim = claimObject(loc);
            if (claim == null) {
                return null;
            }
            try {
                return String.valueOf(claim.getClass().getMethod("getOwnerName").invoke(claim));
            } catch (final Exception e) {
                return null;
            }
        }

        private static Object claimObject(final Location loc) {
            if (!Hooks.isPluginEnabled("GriefPrevention")) {
                return null;
            }
            try {
                final Object plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
                final Object dataStore = plugin.getClass().getField("dataStore").get(plugin);
                final java.lang.reflect.Method method = findGetClaimAt(dataStore.getClass());
                if (method == null) {
                    return null;
                }
                if (method.getParameterCount() == 3) {
                    final Class<?> third = method.getParameterTypes()[2];
                    final Object thirdArg = third.isAssignableFrom(org.bukkit.entity.Player.class) ? null : null;
                    return method.invoke(dataStore, loc, true, thirdArg);
                }
                return method.invoke(dataStore, loc, true);
            } catch (final Exception ignored) {
                return null;
            }
        }

        /** Megkeresi a getClaimAt egy mukodo valtozatat. */
        private static java.lang.reflect.Method findGetClaimAt(final Class<?> type) {
            for (final java.lang.reflect.Method method : type.getMethods()) {
                if (!method.getName().equals("getClaimAt")) {
                    continue;
                }
                final Class<?>[] params = method.getParameterTypes();
                if (params.length >= 2 && params[0] == org.bukkit.Location.class
                        && params[1] == boolean.class) {
                    return method;
                }
            }
            return null;
        }
    }

    /** LiteBans integráció (jelenlét + Bukkit-ban fallback). */
    public static final class LiteBansHook {
        private LiteBansHook() {
        }

        /**
         * @return fent van-e a LiteBans
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("LiteBans");
        }

        /**
         * A Bukkit-ban nevekalapú listán van-e (LiteBans epidei szerver
         * beépitett ban-listara kerul).
         */
        public static boolean isNameBanned(final Player player) {
            return Hooks.isPluginEnabled("LiteBans")
                    && Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(player.getName());
        }
    }
}
