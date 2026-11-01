package hu.paperkit.proxy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import hu.paperkit.core.Kit;

/**
 * Proxy support: BungeeCord / Velocity (compatible-kas) plugin
 * uzenet-csatornakeresztul.
 *
 * <p>Tamogatott: szerver status irok / portég-e (a Velocity-n is
 * mukodik, ha a BungeeCord kompatibilitas be van;kapcsolva).</p>
 *
 * <pre>{@code
 * ProxyKit.connect(player, "lobby");
 * ProxyKit.serverCount(player).thenAccept(count ->
 *     hu.paperkit.text.Msg.send(player, "<gray>Jatekosok: <white>" + count));
 * }</pre>
 */
public final class ProxyKit implements PluginMessageListener {

    private static final String CHANNEL = "BungeeCord";
    private static boolean registered;
    private static final Map<String, CompletableFuture<Integer>> COUNTS = new ConcurrentHashMap<>();

    private ProxyKit() {
    }

    /**
     * Bekapcsolja a proxy-csatornát.
     */
    public static void register(final Plugin plugin) {
        if (registered) {
            return;
        }
        final ProxyKit listener = new ProxyKit();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, listener);
        registered = true;
    }

    /**
     * Jatekos atpeszite egy masik szerverre.
     *
     * @param player a jatekos
     * @param server a szerver neve (a proxy configjabol)
     */
    public static void connect(final Player player, final String server) {
        final Plugin plugin = Kit.plugin();
        if (!registered) {
            register(plugin);
        }
        try (final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             final DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF("Connect");
            out.writeUTF(server);
            try {
                player.sendPluginMessage(plugin, CHANNEL, bytes.toByteArray());
            } catch (final IllegalStateException e) {
                // a sendPluginMessage nem altern alapj
            }
        } catch (final IOException e) {
            plugin.getLogger().warning("Proxy connect sikertelen: " + e.getMessage());
        }
    }

    /**
     * Online jatekosok szama megadass a proxy-bol.
     *
     * @param onAnyPlayer egy online jatekos, akur en a csatorna keresztul
     * @return a fut'szoros joleny (asza olvasata)
     */
    public static CompletableFuture<Integer> serverCount(final Player onAnyPlayer) {
        final Plugin plugin = Kit.plugin();
        if (!registered) {
            register(plugin);
        }
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        COUNTS.put("all", future);
        try (final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             final DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF("PlayerCount");
            out.writeUTF("ALL");
            onAnyPlayer.sendPluginMessage(plugin, CHANNEL, bytes.toByteArray());
        } catch (final IOException e) {
            future.complete(0);
        }
        return future;
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
        if (!CHANNEL.equals(channel)) {
            return;
        }
        try (final var in = new java.io.DataInputStream(new java.io.ByteArrayInputStream(message))) {
            final String command = in.readUTF();
            if ("PlayerCount".equals(command)) {
                final String target = in.readUTF();
                final int count = in.readInt();
                final CompletableFuture<Integer> future = COUNTS.remove(target);
                if (future != null) {
                    future.complete(count);
                }
            }
        } catch (final IOException e) {
            Kit.plugin().getLogger().warning("Proxy uzenet olvasasi hiba: " + e.getMessage());
        }
    }

    /** (belsone lejegyzett listener: peldany)
    */
}
