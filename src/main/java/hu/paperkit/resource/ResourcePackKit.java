package hu.paperkit.resource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Resource Pack support: pack kuldese a jatekosnak (Paper nativ API-t
 * hasznal, PackLoaderjeles szerver config):
 *
 * <pre>{@code
 * ResourcePackKit.send(player, "https://server.example.com/pack.zip",
 *         "sha1-hash-bytes");
 * }</pre>
 */
public final class ResourcePackKit {

    private static final Map<UUID, String> PENDING = new ConcurrentHashMap<>();

    private ResourcePackKit() {
    }

    /**
     * Resource pack kuldese a jatekosnak.
     *
     * @param player a jatekos
     * @param url    a pack leiro URL-je
     * @param sha1   a pack SHA-1 hash-e (vagy null)
     */
    public static void send(final Player player, final String url, final String sha1) {
        PENDING.put(player.getUniqueId(), url);
        if (sha1 == null || sha1.isBlank()) {
            player.setResourcePack(url);
        } else {
            try {
                final byte[] hash = hexToBytes(sha1);
                player.setResourcePack(url, hash);
            } catch (final IllegalArgumentException e) {
                player.setResourcePack(url);
            }
        }
    }

    /**
     * Pack torlese (reszlegesen? a beallitoje kiairast ertami).
     *
     * @param player a jatekos
     */
    public static void clearPrompt(final Player player) {
        PENDING.remove(player.getUniqueId());
    }

    /**
     * @return van-e le nem torolt pack-kompannsorom a jatekosn (opt)
     */
    public static boolean isPending(final Player player) {
        return PENDING.containsKey(player.getUniqueId());
    }

    /** Hexekes SHA-1 hash fajlbaldiaszte. */
    private static byte[] hexToBytes(final String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Ervenytelen SHA-1 hash hossz!");
        }
        final byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
}
