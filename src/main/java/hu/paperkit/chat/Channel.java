package hu.paperkit.chat;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import hu.paperkit.placeholder.Placeholders;

/**
 * Egy chat csatorna: MiniMessage formatum, jogosultsag szures,
 * helyi sugar (local), nev-re tett hover es kattintas parancs.
 *
 * <p>A formatumban helyeoorszeti jelek (tokenek) hasznalhatoak:</p>
 * <ul>
 *   <li>{@code {prefix}} - a csatorna prefixje ({@link #prefix(String)})</li>
 *   <li>{@code {name}}   - a jatekos neve, hover + click esellyel</li>
 *   <li>{@code {player}} - a nyers jatekosnev</li>
 *   <li>{@code {message}}- maga az uzenet (biztonsagos, nem MiniMessage)</li>
 *   <li>{@code %placeholder%} - PlaceholderAPI - kovyelo hu.paperkit.placeholder.Placeholders</li>
 * </ul>
 *
 * <pre>{@code
 * Channel.global("<dark_gray>[<aqua>G<dark_gray>] {prefix}{name}<gray>: </gray>{message}")
 *        .prefix("<rank_prefix>")
 *        .hover("<gray>Jatekos: <white>{player}\n<gray>Privi: <white>/msg {player}")
 *        .suggest("/msg {player} ")
 *        .build();
 * }</pre>
 */
public final class Channel {

    /** Csatorna azonosito (pl. "global", "staff"). */
    private final String id;
    /** A MiniMessage formatum. */
    private String format;
    /** A {name} szine (MiniMessage szin-tag, pl. aqua). */
    private String nameColor = "<aqua>";
    /** Szukseges jogosultsag a csatornaba irashoz (null = mindenki). */
    private String permission;
    /** Helyi sugar blokkban; 0 = mindenki latja. */
    private int range = 0;
    /** A {prefix} token erteke (mini, plakat plas). */
    private String prefix = "";
    /** Hover szoveg a nev felett (MiniMessage, {player} behelyettesites). */
    private String hover;
    /** Kattintas: parancs sugalasa (nem futtat le, csak beirja). */
    private String suggestCommand;
    /** Kattintas: parancs lefuttatasa. */
    private String runCommand;
    /** Kattintas a nevre: link megnyitasa (elso sorrend). */
    private String clickUrl;

    private Channel(final String id) {
        this.id = id == null ? "channel" : id.toLowerCase();
    }

    /**
     * Uj global csatorna (mindenki latja).
     *
     * @param format a MiniMessage formatum
     * @return a csatorna
     */
    public static Channel global(final String format) {
        final Channel channel = new Channel("global");
        channel.format = format;
        return channel;
    }

    /**
     * Uj csatorna adott azonositoval.
     *
     * @param id     az azonosito (pl. "staff")
     * @param format a MiniMessage formatum
     * @return a csatorna
     */
    public static Channel create(final String id, final String format) {
        final Channel channel = new Channel(id);
        channel.format = format;
        return channel;
    }

    /**
     * Helyi csatorna: csak adott sugaron beluli jatekosok latjak.
     *
     * @param format a MiniMessage formatum
     * @param blocks a sugar blokkban
     * @return a csatorna
     */
    public static Channel local(final String format, final int blocks) {
        final Channel channel = new Channel("local");
        channel.format = format;
        channel.range = Math.max(0, blocks);
        return channel;
    }

    /**
     * Helyi sugar allitasa blokban.
     *
     * @param blocks a sugar; 0 = mindenki latja
     * @return this
     */
    public Channel range(final int blocks) {
        this.range = Math.max(0, blocks);
        return this;
    }

    /**
     * Irasi jogosultsag kovetelese.
     *
     * @param permission a permission node; null = mindenki
     * @return this
     */
    public Channel permission(final String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * A {prefix} token erteke (MiniMessage, pl plas prefix).
     *
     * @param prefix a prefix
     * @return this
     */
    public Channel prefix(final String prefix) {
        this.prefix = prefix == null ? "" : prefix;
        return this;
    }

    /**
     * Hover szoveg a nev felett ({@code {player}} helyeoorszu).
     *
     * @param hover a MiniMessage szoveg
     * @return this
     */
    public Channel hover(final String hover) {
        this.hover = hover;
        return this;
    }

    /**
     * Kattintas: parancs sugalasa a chat-mezbe (nem fut le).
     *
     * @param command a parancs, pl. {@code "/msg {player} "}
     * @return this
     */
    public Channel suggest(final String command) {
        this.suggestCommand = command;
        return this;
    }

    /**
     * Kattintas: parancs lefuttatasa.
     *
     * @param command a parancs
     * @return this
     */
    public Channel run(final String command) {
        this.runCommand = command;
        return this;
    }

    /**
     * A {name} szine (MiniMessage tag).
     *
     * @param color a szin-tag, pl. {@code "<aqua>"}
     * @return this
     */
    public Channel nameColor(final String color) {
        this.nameColor = color;
        return this;
    }

    /**
     * Kattintas a nevre: link megnyitasa (felulirja a run/suggest-et).
     *
     * @param url a megnyitando URL, pl. "https://kylo.hu"
     * @return this
     */
    public Channel url(final String url) {
        this.clickUrl = url;
        return this;
    }

    /**
     * @return a csatorna azonosito (kisbetus)
     */
    public String id() {
        return id;
    }

    /**
     * @return a formatum
     */
    public String format() {
        return format;
    }

    /**
     * @return a helyi sugar blokkban (0 = mindenki latja)
     */
    public int range() {
        return range;
    }

    /**
     * @return a kovetelt jogosultsag (vagy null)
     */
    public String permission() {
        return permission;
    }

    /**
     * Ez a jatekos tud-e irni a csatornaba.
     *
     * @param player a jatekos
     * @return true, ha jogosult
     */
    public boolean canTalk(final Player player) {
        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    /**
     * A csatorna favevoi: azok a jatekosok, akik latjak az uzenetet.
     *
     * @param sender a felado
     * @return favevok listaja
     */
    public List<Player> receivers(final Player sender) {
        final List<Player> result = new ArrayList<>();
        final Location from = sender.getLocation();
        final World world = sender.getWorld();
        for (final Player online : Bukkit.getOnlinePlayers()) {
            if (range > 0) {
                if (!online.getWorld().equals(world)
                        || online.getLocation().distanceSquared(from) > (double) range * range) {
                    continue;
                }
            }
            result.add(online);
        }
        return result;
    }

    /**
     * Az uzenet megformazasa a formatum alapjan.
     * (Package-private: a {@link ChatEngine} hasznalja.)
     *
     * @param player  a felado
     * @param message a nyers uzenet
     * @return a kesz Component
     */
    Component buildMessage(final Player player, final String message) {
        final TagResolver resolvers = TagResolver.resolver(
                Placeholder.parsed("prefix", Placeholders.apply(prefix, player)),
                Placeholder.component("name", nameComponent(player)),
                Placeholder.unparsed("player", player.getName()),
                Placeholder.unparsed("message", message));
        return MiniMessage.miniMessage().deserialize(Placeholders.apply(format, player), resolvers);
    }

    private Component nameComponent(final Player player) {
        final String plainName = PlainTextComponentSerializer.plainText().serialize(player.displayName());
        Component name = MiniMessage.miniMessage()
                .deserialize((nameColor == null ? "<white>" : nameColor) + plainName);
        if (hover != null) {
            name = name.hoverEvent(HoverEvent.showText(MiniMessage.miniMessage()
                    .deserialize(Placeholders.apply(hover, player))));
        }
        if (clickUrl != null) {
            name = name.clickEvent(ClickEvent.openUrl(clickUrl));
        } else if (runCommand != null) {
            name = name.clickEvent(ClickEvent.runCommand(runCommand.replace("{player}", player.getName())));
        } else if (suggestCommand != null) {
            name = name.clickEvent(ClickEvent.suggestCommand(suggestCommand.replace("{player}", player.getName())));
        }
        return name;
    }
}
