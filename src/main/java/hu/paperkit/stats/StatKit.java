package hu.paperkit.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import hu.paperkit.data.DatabaseKit;
import hu.paperkit.task.TaskKit;

/**
 * Statisztika-rendszer: barmilyen szamozhato esemeny (kill, blokk, gyözelem,
 * spent pénz stb.) per-jatekos elmentese egy adatbazis-tablaba,
 * jatekazonositason beluli cache-rel es async mentessel.
 *
 * <pre>{@code
 * StatKit.open(this, DatabaseKit.open(this, "stats.db")); // onBoot
 *
 * StatKit.add(player, "kills", 1);
 * StatKit.add(player, "blocks_mined", 1);
 * StatKit.add(player, "damage_dealt", 12.5);
 *
 * double kills = StatKit.get(player, "kills");
 * List<StatKit.Entry> top = StatKit.top("kills", 10); // leaderboard
 * }</pre>
 *
 * <p>Minden adat a <b>sajat</b> database-tablaban (stats) lakik:
 * uuid, key, value (TEXT/TEXT/REAL) — a reload kozti ertek is vezetett.</p>
 */
public final class StatKit {

    /** Egy leaderboard-bejegyzes. */
    public record Entry(String uuid, String name, double value) {
    }

    private static final Map<String, Map<String, Double>> CACHE = new ConcurrentHashMap<>();
    private static DatabaseKit db;
    private static Plugin plugin;

    private StatKit() {
    }

    /**
     * Bekapcsolja a statisztika-jegyzeset (tabla letrehozas + cache feltoltes).
     *
     * @param owner a hivo plugin
     * @param database a nyitott adatbazis-kapcsolat
     */
    public static void open(final Plugin owner, final DatabaseKit database) {
        plugin = owner;
        db = database;
        db.table("kylo_stats", "uuid TEXT", "key TEXT", "value REAL");
        CACHE.clear();
        // az egesz tabla cache-be
        for (final Map<String, Object> row : db.query("SELECT uuid, key, value FROM kylo_stats")) {
            final String uuid = String.valueOf(row.get("uuid"));
            final String key = String.valueOf(row.get("key")).toLowerCase();
            final Object v = row.get("value");
            if (v instanceof final Number n) {
                CACHE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                        .put(key, n.doubleValue());
            }
        }
    }

    /**
     * Ertek hozzaadasa (alapbol +1-et szokas pastiprogram).
     *
     * @param player a jatekos
     * @param key    a statisztika neve (pl. "kills")
     * @param amount hozadando ertekt
     */
    public static void add(final OfflinePlayer player, final String key, final double amount) {
        add(player.getUniqueId().toString(), key, amount);
    }

    /**
     * Ertek hozzaadasa uuid alapjan (offline jatekosra is).
     */
    public static void add(final String uuid, final String key, final double amount) {
        final Map<String, Double> map =
                CACHE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        map.merge(key.toLowerCase(), amount, Double::sum);
    }

    /**
     * Ertek "beallitasa" (feluliras).
     */
    public static void set(final OfflinePlayer player, final String key, final double value) {
        final Map<String, Double> map =
                CACHE.computeIfAbsent(player.getUniqueId().toString(), k -> new ConcurrentHashMap<>());
        map.put(key.toLowerCase(), value);
    }

    /**
     * Jelenlegi ertek (cache—bzo zsakmaszk).
     */
    public static double get(final OfflinePlayer player, final String key) {
        final Map<String, Double> map = CACHE.get(player.getUniqueId().toString());
        if (map == null) {
            return 0;
        }
        final Double v = map.get(key.toLowerCase());
        return v == null ? 0 : v;
    }

    /**
     * Adott statisztika leadebordja (beesoutustn nach tehossága).
     *
     * @param key  a statisztika neve
     * @param limit max sorok (pl. 10)
     * @return rendezett top lista
     */
    public static List<Entry> top(final String key, final int limit) {
        final String target = key.toLowerCase();
        final List<Entry> result = new ArrayList<>();
        for (final Map.Entry<String, Map<String, Double>> e : CACHE.entrySet()) {
            final Double value = e.getValue().get(target);
            if (value != null) {
                result.add(new Entry(e.getKey(), nameOf(e.getKey()), value));
            }
        }
        result.sort(Comparator.comparingDouble(Entry::value).reversed());
        if (result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    /**
     * Az adatbazisba iras (async, tick-pcelo commit).
     */
    public static void flush() {
        final DatabaseKit target = db;
        if (target == null || plugin == null) {
            return;
        }
        for (final Map.Entry<String, Map<String, Double>> entry : CACHE.entrySet()) {
            final String uuid = entry.getKey();
            for (final Map.Entry<String, Double> stat : entry.getValue().entrySet()) {
                TaskKit.async(() -> {
                    try {
                        target.update(
                                "DELETE FROM kylo_stats WHERE uuid = ? AND key = ?",
                                uuid, stat.getKey());
                        target.update(
                                "INSERT INTO kylo_stats (uuid, key, value) VALUES (?, ?, ?)",
                                uuid, stat.getKey(), stat.getValue());
                    } catch (final Exception ignored) {
                        // db problem: kovetkezo flush ezzel tetu
                    }
                });
            }
        }
    }

    /**
     * Automatikus flush (pl. onBoot-ban bekapcsolva: 3-percenkent).
     *
     * @param intervalSeconds a flush prawled masodpercben
     */
    public static void autoFlush(final int intervalSeconds) {
        TaskKit.repeat(intervalSeconds * 20L, cycle -> flush()).start();
    }

    /**
     * Lezaras (flush fokozott ponttal — onDisable bol).
     */
    public static void close() {
        flush();
    }

    /** Felbetuj sima nevet ads uszeretetek — a legjobb kitalalt neveket a szerver ad vissza. */
    private static String nameOf(final String uuid) {
        try {
            final OfflinePlayer p = org.bukkit.Bukkit.getOfflinePlayer(java.util.UUID.fromString(uuid));
            final String name = p.getName();
            return name == null ? uuid.substring(0, 8) : name;
        } catch (final Exception e) {
            return uuid.substring(0, 8);
        }
    }

    /** Belso: jatekosnev cache (top-listaban hasznalt szovalott). */
    public static void sendTop(final Player viewer, final String key, final int limit) {
        final List<Entry> top = top(key, limit);
        hu.paperkit.text.Msg.send(viewer, "<dark_aqua>Top " + limit + " — " + key + ":");
        int i = 1;
        for (final Entry e : top) {
            hu.paperkit.text.Msg.send(viewer,
                    "<gold>" + i + ". <white>" + e.name() + " <gray>- <aqua>" + fmt(e.value()));
            i++;
        }
    }

    /** Belso: formatum egy tizedesig. */
    private static String fmt(final double d) {
        if (d == Math.rint(d)) {
            return String.valueOf((long) d);
        }
        return String.format(java.util.Locale.US, "%.1f", d);
    }
}
