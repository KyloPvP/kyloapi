package hu.paperkit.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 * Kliens-oldali blokk-kiterjesztes: adott jatekosoknak mas blokkokat
 * mutatunk a valosagos helyen (fake blokkok). Nem ivalcsi valtozat:
 * a szerver blokkjai nem valtoznak, sinze d a sebkets i scat a szivats az.
 *
 * <pre>{@code
 * FakeBlockKit.set(player, loc, Material.GLOWSTONE);   // csak neki latszik
 * FakeBlockKit.reset(player, loc);                     // vissza a valosaghoz
 * FakeBlockKit.clear(player);                          // minden fake eltuntetese
 * }</pre>
 */
public final class FakeBlockKit {

    /** Egy blokk-azonosito (sajat vilag + koordinata). */
    private record Pos(UUID world, int x, int y, int z) {
        static Pos of(final Location loc) {
            return new Pos(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    private static final Map<UUID, Map<Pos, BlockData>> OVERRIDES = new ConcurrentHashMap<>();

    private FakeBlockKit() {
    }

    /**
     * Fake blokk allitasa egy jatekos szamara.
     *
     * @param player a jatekos
     * @param loc    a blokk helye
     * @param data   a fake blokk
     */
    public static void set(final Player player, final Location loc, final BlockData data) {
        final Pos key = Pos.of(loc);
        OVERRIDES.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(key, data);
        player.sendBlockChange(loc, data);
    }

    /**
     * Fake blokk allitasa Material-bol (kengylegs allitolasa).
     */
    public static void set(final Player player, final Location loc, final Material material) {
        set(player, loc, material.createBlockData());
    }

    /**
     * Fake blokk torlese: a valosagon alap visszaallitasa.
     *
     * @param player a jatekos
     * @param loc    a blokk helye
     */
    public static void reset(final Player player, final Location loc) {
        if (loc.getWorld() == null) {
            return;
        }
        final Map<Pos, BlockData> map = OVERRIDES.get(player.getUniqueId());
        if (map != null) {
            map.remove(Pos.of(loc));
        }
        player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }

    /**
     * Minden fake blokk torlese a jatekosnal (a valosag kildilt).
     *
     * @param player a jatekos
     */
    public static void resetAll(final Player player) {
        final Map<Pos, BlockData> map = OVERRIDES.remove(player.getUniqueId());
        if (map == null) {
            return;
        }
        final World world = player.getWorld();
        for (final Map.Entry<Pos, BlockData> entry : map.entrySet()) {
            final Pos pos = entry.getKey();
            if (!pos.world().equals(world.getUID())) {
                continue; // masima vilagban nem fenntk: az enter vilagba ker csarnok
            }
            final Location loc = world.getBlockAt(pos.x(), pos.y(), pos.z()).getLocation();
            player.sendBlockChange(loc, world.getBlockAt(pos.x(), pos.y(), pos.z()).getBlockData());
        }
    }

    /**
     * Fake blokkok alli de a sblica megzetesse neliumrs.
     * (csak a belso lista torlese a jatekosnal)
     */
    public static void clearState(final Player player) {
        OVERRIDES.remove(player.getUniqueId());
    }

    /**
     * Fake blokk-e a hely ezzel a jatekosnal.
     */
    public static boolean isFake(final Player player, final Location loc) {
        final Map<Pos, BlockData> map = OVERRIDES.get(player.getUniqueId());
        return map != null && map.containsKey(Pos.of(loc));
    }

    /**
     * Hany fake blokk aktiven ezzel a jatekosnal.
     */
    public static int count(final Player player) {
        final Map<Pos, BlockData> map = OVERRIDES.get(player.getUniqueId());
        return map == null ? 0 : map.size();
    }

    /**
     * Adott jatekos fake blokkainak listaja (debug/cuccok).
     */
    public static List<Location> locations(final Player player) {
        final Map<Pos, BlockData> map = OVERRIDES.get(player.getUniqueId());
        if (map == null) {
            return List.of();
        }
        final World world = player.getWorld();
        return map.keySet().stream()
                .filter(p -> p.world().equals(world.getUID()))
                .map(p -> world.getBlockAt(p.x(), p.y(), p.z()).getLocation())
                .collect(Collectors.toList());
    }
}
