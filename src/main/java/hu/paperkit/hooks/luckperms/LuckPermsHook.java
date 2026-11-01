package hu.paperkit.hooks.luckperms;

import java.lang.reflect.Method;
import java.util.UUID;

import hu.paperkit.hooks.Hooks;

/**
 * LuckPerms hook (reflection): prefix/suffix lekerese a
 * chat engine szamara.
 *
 * <pre>{@code
 * if (Hooks.isPluginEnabled("LuckPerms") && LuckPermsHook.hooked()) {
 *     String prefix = LuckPermsHook.prefix(player.getUniqueId());
 *     Msg.send(player, prefix + "<white>" + player.getName());
 * }
 * }</pre>
 */
public final class LuckPermsHook {

    private static Object luckPermsApi;
    private static boolean attempted;

    private LuckPermsHook() {
    }

    /**
     * Megprobalja elerni a LuckPerms API-t.
     *
     * @return true, ha sikerult
     */
    public static boolean hooked() {
        if (attempted) {
            return luckPermsApi != null;
        }
        attempted = true;
        if (!Hooks.isPluginEnabled("LuckPerms")) {
            return false;
        }
        try {
            final Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            final Method get = provider.getDeclaredMethod("get");
            get.setAccessible(true);
            luckPermsApi = get.invoke(null);
            return luckPermsApi != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * A jatekos prefixje (vagy ures) — pl. a chat-hez.
     */
    public static String prefix(final UUID player) {
        return meta(player, "getPrefix");
    }

    /**
     * A jatekos suffixje (vagy ures).
     */
    public static String suffix(final UUID player) {
        return meta(player, "getSuffix");
    }

    /**
     * A fo primaren group-neve (vagy "default").
     */
    public static String primaryGroup(final UUID player) {
        if (!hooked()) {
            return "default";
        }
        try {
            final Object user = invokeUser(player);
            if (user == null) {
                return "default";
            }
            final Object primary = invokeNoArg(user, "getPrimaryGroup");
            return primary == null ? "default" : String.valueOf(primary);
        } catch (final Exception e) {
            return "default";
        }
    }

    private static String meta(final UUID player, final String method) {
        if (!hooked()) {
            return "";
        }
        try {
            final Object user = invokeUser(player);
            if (user == null) {
                return "";
            }
            final Object cached = invokeNoArg(user, "getCachedData");
            final Object metaData = invokeNoArg(cached, "getMetaData");
            final Object value = invokeNoArg(metaData, method);
            return value == null ? "" : String.valueOf(value);
        } catch (final Exception e) {
            return "";
        }
    }

    private static Object invokeUser(final UUID player) {
        try {
            final Method getUser = luckPermsApi.getClass().getMethod("getUserManager");
            final Object userManager = getUser.invoke(luckPermsApi);
            final Method loadUser = userManager.getClass().getMethod("getUser", UUID.class);
            Object user = loadUser.invoke(userManager, player);
            if (user == null) {
                // nincs cache-ben, probalkozzunk fesusok (async betoltest keruljuk)
                final Method getIfLoaded = userManager.getClass().getMethod("getUser", UUID.class);
                getIfLoaded.setAccessible(true);
                user = getIfLoaded.invoke(userManager, player);
            }
            return user;
        } catch (final Exception e) {
            return null;
        }
    }

    private static Object invokeNoArg(final Object target, final String method) {
        try {
            final Method m = target.getClass().getMethod(method);
            m.setAccessible(true);
            return m.invoke(target);
        } catch (final NoSuchMethodException e) {
            // interface-en keresztul is probalkozunk
            try {
                outer:
                for (final Class<?> iface : target.getClass().getInterfaces()) {
                    for (final Method candidate : iface.getMethods()) {
                        if (candidate.getName().equals(method)) {
                            return candidate.invoke(target);
                        }
                    }
                }
            } catch (final Exception ignored) {
                // ocserejes csaptelep
            }
            return null;
        } catch (final Exception e) {
            return null;
        }
    }
}
