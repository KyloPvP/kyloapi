package hu.paperkit.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import io.papermc.paper.event.player.AsyncChatEvent;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import hu.paperkit.core.Kit;
import hu.paperkit.text.Msg;

/**
 * Chat engine: csatornakezel es konfiguralhato chat forma.
 *
 * <p>Muvelet:</p>
 * <pre>{@code
 * // onBoot-ban:
 * ChatEngine.start(plugin); // alap "global" csatorna + tok esemeny
 *
 * // sajat csatorna:
 * ChatEngine.registerChannel(
 *     Channel.global("<dark_gray>[<aqua>G<dark_gray>] {name}<gray>: </gray>{message}")
 *            .hover("<gray>Privi: <white>/msg {player}")
 *            .suggest("/msg {player} "));
 * ChatEngine.setDefaultChannel("global");
 *
 * // staff csatorna ( csak staff latja):
 * ChatEngine.registerChannel(
 *     Channel.create("staff", "<dark_red>[STAFF] {name}: {message}")
 *            .permission("paperkit.chat.staff"));
 *
 * // jatekos csatornaja:
 * ChatEngine.setChannel(player, "staff");
 *
 * // programatikus kuld:
 * ChatEngine.send(player, null, "Hello!"); // a jatekos csatornajaba
 * }</pre>
 *
 * <p>Ha az "tokolas" be van kapcsolva, minden nativ chat-uzenetet
 * az engine ol be es a csatorna formatumanak megfelel formban
 * kld ki (igy mukodik a prefix, hover, click, staff szures).</p>
 */
public final class ChatEngine implements Listener {

    private static ChatEngine current;

    private final Plugin plugin;
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerChannel = new ConcurrentHashMap<>();
    private String defaultChannelId = "global";
    private volatile boolean intercepting = true;

    private ChatEngine(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Elinditja a chat engint alapertelmezett "global" csatornaval
     * es bekapcsolja a nativ chat tokolasat.
     *
     * @param plugin a hivo plugin
     * @return az engine (idempotens: meglevo instance-t ad vissza)
     */
    public static ChatEngine start(final Plugin plugin) {
        if (current != null) {
            return current;
        }
        final ChatEngine engine = new ChatEngine(plugin);
        engine.channels.put("global",
                Channel.global("<dark_gray>[<aqua>G<dark_gray>] {name}<gray>: </gray>{message}"));
        Bukkit.getPluginManager().registerEvents(engine, plugin);
        current = engine;
        Msg.info("<aqua>ChatEngine elindult!");
        return engine;
    }

    /**
     * Uj csatorna regisztralasa.
     *
     * @param channel a csatorna
     */
    public static void registerChannel(final Channel channel) {
        if (channel != null) {
            instance().channels.put(channel.id(), channel);
        }
    }

    /**
     * Csatorna torlese.
     *
     * @param id a csatorna azonosito
     */
    public static void removeChannel(final String id) {
        final ChatEngine engine = current;
        if (engine != null && id != null) {
            engine.channels.remove(id.toLowerCase());
        }
    }

    /**
     * Alapertelmezett csatorna allitasa (ami a nativ chat-be ert uzeneteket kap).
     *
     * @param id a csatorna azonosito
     */
    public static void setDefaultChannel(final String id) {
        if (current != null && id != null) {
            current.defaultChannelId = id.toLowerCase();
        }
    }

    /**
     * Nativ chat token be/kapcsolasa.
     * Ha ki van kapcsolva, a nativ uzenetek az eredeti utjukon mennek tovabb,
     * de a {@link #send(Player, String, String)} programatikus kuld mukodik.
     *
     * @param intercept true = engine veszi at a nativ chat-et
     */
    public static void setIntercepting(final boolean intercept) {
        final ChatEngine engine = current;
        if (engine != null) {
            engine.intercepting = intercept;
        }
    }

    /**
     * Csatorna lekerese azonosito szerint.
     *
     * @param id a csatorna azonosito
     * @return a csatorna vagy null
     */
    public static Channel channel(final String id) {
        final ChatEngine engine = current;
        return engine == null || id == null ? null : engine.channels.get(id.toLowerCase());
    }

    /**
     * Regisztralt csatorna id-k listaja.
     *
     * @return az id-k
     */
    public static List<String> channelIds() {
        final ChatEngine engine = current;
        return engine == null ? List.of() : new ArrayList<>(engine.channels.keySet());
    }

    /**
     * A jatekos aktualis csatornaja (vagy a csatorna Jsito, ha nincs sajat).
     *
     * @param player a jatekos
     * @return a csatorna, vagy null ha a default nem letezik
     */
    public static Channel channelOf(final Player player) {
        final ChatEngine engine = current;
        if (engine == null) {
            return null;
        }
        final String override = engine.playerChannel.get(player.getUniqueId());
        String id = override != null ? override : engine.defaultChannelId;
        Channel channel = engine.channels.get(id);
        // nem letezo csatornakra visszaesunk a default-ra
        if (channel == null && override != null) {
            channel = engine.channels.get(engine.defaultChannelId);
        }
        return channel;
    }

    /**
     * Jatekos csatornajanak allitasa.
     *
     * @param player a jatekos
     * @param channelId a csatorna id, vagy null a reset-hez
     */
    public static void setChannel(final Player player, final String channelId) {
        final ChatEngine engine = current;
        if (engine == null || player == null) {
            return;
        }
        if (channelId == null) {
            engine.playerChannel.remove(player.getUniqueId());
        } else {
            engine.playerChannel.put(player.getUniqueId(), channelId.toLowerCase());
        }
    }

    /**
     * Uzenet kuldese egy jatekos csatornajaba (vagy megadott csatornaba).
     * Nem fut le, ha a jatekos nem jogosult a csatornra.
     *
     * @param player a felado
     * @param channelId a csatorna azonosito, vagy null = a jatekos aktiv csatornja
     * @param message az uzenet
     */
    public static void send(final Player player, final String channelId, final String message) {
        final ChatEngine engine = current;
        if (engine == null || player == null || message == null || message.isBlank()) {
            return;
        }
        final Channel channel = channelId == null
                ? channelOf(player)
                : engine.channels.get(channelId.toLowerCase());
        if (channel == null) {
            return;
        }
        if (!channel.canTalk(player)) {
            Msg.send(player, "<red>Nincs jogosultsagod ehhez a csatornahoz!");
            return;
        }
        broadcast(channel, player, message);
    }

    private static void broadcast(final Channel channel, final Player player, final String message) {
        final var component = channel.buildMessage(player, message);
        for (final Player viewer : channel.receivers(player)) {
            viewer.sendMessage(component);
        }
    }

    /**
     * Nativ chat esemeny foglalasa: ha a tokolas aktiv, az uzenetet
     * a jatekos csatornajanak formatumaval kuldjuk ki.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        final ChatEngine engine = current;
        if (engine == null || !engine.intercepting) {
            return;
        }
        event.setCancelled(true);
        final String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        final Player player = event.getPlayer();
        // az async esemenybol visszaltunk main thread-re
        Kit.sync(() -> {
            try {
                send(player, null, plainMessage);
            } catch (final Exception e) {
                engine.plugin.getLogger().log(Level.WARNING, "ChatEngine hiba: chat kuldese nem sikerult", e);
            }
        });
    }

    /**
     * A chat engine leallitasa (listener leiratkoztatas).
     * KitPlugin onShutdown-bol hivhato.
     */
    public static void cleanup() {
        final ChatEngine engine = current;
        if (engine != null) {
            HandlerList.unregisterAll(engine);
            engine.channels.clear();
            engine.playerChannel.clear();
            current = null;
        }
    }

    private static ChatEngine instance() {
        if (current == null) {
            throw new IllegalStateException(
                    "A ChatEngine nincs elinditva! Hivd meg eloszor: ChatEngine.start(plugin)");
        }
        return current;
    }
}
