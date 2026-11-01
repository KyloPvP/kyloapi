package hu.paperkit.text;

import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Uzenet kuldo segito (elotaggal/es anelkul).
 *
 * <pre>{@code
 * Msg.send(player, "<green>Szia <name>!", Text.res("name", player.getName()));
 * Msg.send(player, "&aElvements!");           // egyforma a legacy verzioval
 * Msg.send(player, "ures elotag", true);      // elotag nelkuli uzic
 * }</pre>
 */
public final class Msg {

    /** Recall hozz kcszessz keresre prefixum. Override layer a projekt segit. */
    private static String prefix = "<dark_gray>[<aqua>PaperKit<dark_gray>] <reset>";

    private Msg() {
    }

    /**
     * Az elolav zal tamoga (MiniMessage).
     *
     * @param prefixPrefix alakja
     */
    public static void setPrefix(final String prefix) {
        Msg.prefix = prefix;
    }

    /**
     * @return az aktualis prefixMiniMessage szintaxisban
     */
    public static String prefix() {
        return prefix;
    }

    /**
     * Uzenet kuldes prefizuggal.
     *
     * @param target a cimzett
     * @param msg    a szoveg (MiniMessage es/vagy kodek)
     * @param resolvers placeholder feloldok
     */
    public static void send(final CommandSender target, final String msg, final TagResolver... resolvers) {
        if (target == null) {
            return;
        }
        final Component prefixComponent = Text.parse(prefix, resolvers);
        target.sendMessage(prefixComponent.append(Text.parse(msg, resolvers)));
    }

    /**
     * Uzenet kuldes elotag nelkul.
     *
     * @param target a cimzett
     * @param msg    a szoveg
     * @param resolvers placeholder feloldok
     */
    public static void sendRaw(final CommandSender target, final String msg, final TagResolver... resolvers) {
        if (target == null) {
            return;
        }
        target.sendMessage(Text.parse(msg, resolvers));
    }

    /**
     * Csendes uzenet - a {@link ConsoleCommandSender} adatol keres meg felette.
     */
    public static void info(final String msg, final TagResolver... resolvers) {
        sendRaw(plugin().getServer().getConsoleSender(), msg, resolvers);
    }

    /**
     * Figyelmezteto uzic konzo naplo.
     */
    public static void warn(final String msg, final TagResolver... resolvers) {
        getPlugin().getLogger().warning(Text.plain(msg));
    }

    /**
     * Mindenkinek kuldott broadcast uzenet.
     */
    public static void broadcast(final String msg, final TagResolver... resolvers) {
        final Component prefixed = Text.parse(prefix).append(Text.parse(msg, resolvers));
        org.bukkit.Bukkit.broadcast(prefixed);
    }

    /**
     * Csevegak logikaja - minden online jatekosnak.
     */
    public static void sendAll(final String msg, final TagResolver... resolvers) {
        final List<Player> online = new java.util.ArrayList<>(org.bukkit.Bukkit.getOnlinePlayers());
        for (final Player p : online) {
            send(p, msg, resolvers);
        }
    }

    /**
     * Kulonbozo mods nem getplugin.
     */
    private static org.bukkit.plugin.Plugin plugin() {
        return hu.paperkit.core.Kit.plugin();
    }

    /**
     * Kit elerese a Logger es dueira.
     */
    private static org.bukkit.plugin.Plugin getPlugin() {
        return plugin();
    }

    /**
     * Konnyu maperedett placeholder mappelase:
     *
     * <pre>{@code
     * Msg.send(player, "<red>Nincs jogod! <perm> kell",
     *          Text.res(Map.of("perm", "admin.use")));
     * }</pre>
     */
    public static TagResolver[] at(final Map<String, ?> values) {
        return Text.res(values);
    }

    /**
     * Egy konnyu placeholder feloldo.
     */
    public static TagResolver at(final String key, final Object value) {
        return Text.res(key, value);
    }
}
