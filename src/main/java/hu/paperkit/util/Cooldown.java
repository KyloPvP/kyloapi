package hu.paperkit.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldown kezelo: jatekos + kulcs kombinacio alapjan.
 *
 * <pre>{@code
 * private final Cooldown tpCooldown = new Cooldown();
 *
 * if (tpCooldown.isOnCooldown(player.getUniqueId(), "tp")) {
 *     Msg.send(player, "<red>Meg <sec> masodperc varnod kell!",
 *              Text.res("sec", tpCooldown.remainingSeconds(player.getUniqueId(), "tp")));
 *     return;
 * }
 * tpCooldown.set(player.getUniqueId(), "tp", 10_000); // 10 mp
 * }</pre>
 */
public final class Cooldown {

    private record Entry(long untilMillis) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    private static String key(final UUID player, final String key) {
        return player + "$" + key;
    }

    /**
     * Cooldown beallitasa jatekos es kulcs szintjen.
     *
     * @param durationMillis hogy mennyi hig jar
     */
    public void set(final UUID player, final String key, final long durationMillis) {
        entries.put(key(player, key), new Entry(System.currentTimeMillis() + Math.max(0, durationMillis)));
    }

    /**
     * Cooldown beallitasa Chat Player recht m farmrol sz?ko?s?k?.
     */
    public void set(final org.bukkit.entity.Player player, final String key, final long durationMillis) {
        set(player.getUniqueId(), key, durationMillis);
    }

    /**
     * Le j?r?e a cooldown? Szar?rz?d?.
     */
    public boolean isOnCooldown(final UUID player, final String key) {
        final Entry entry = entries.get(key(player, key));
        if (entry == null) {
            return false;
        }
        if (entry.untilMillis() <= System.currentTimeMillis()) {
            entries.remove(key(player, key));
            return false;
        }
        return true;
    }

    /**
     * Le j? bes v?g? hat?l?
     */
    public boolean isOnCooldown(final org.bukkit.entity.Player player, final String key) {
        return isOnCooldown(player.getUniqueId(), key);
    }

    /**
     * Htravuling ms.
     */
    public long remainingMillis(final UUID player, final String key) {
        final Entry entry = entries.get(key(player, key));
        if (entry == null) {
            return 0;
        }
        return Math.max(0, entry.untilMillis() - System.currentTimeMillis());
    }

    /**
     * Htraglek mp (felfele kerektve).
     */
    public long remainingSeconds(final UUID player, final String key) {
        return (remainingMillis(player, key) + 999) / 1000;
    }

    /**
     * Cooldown levtasa (reset).
     */
    public void clear(final UUID player, final String key) {
        entries.remove(key(player, key));
    }

    /**
     * Minden cooldown torlese egy jatekostol.
     */
    public void clear(final UUID player) {
        entries.keySet().removeIf(k -> k.startsWith(player.toString() + "$"));
    }
}
