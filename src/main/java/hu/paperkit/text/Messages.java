package hu.paperkit.text;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.paperkit.config.ConfigFile;
import hu.paperkit.placeholder.Placeholders;

/**
 * Konfiguralhato uzenet konyvtar (axh lang.yml stilus): minden
 * szoveg egy yaml fajlban, helyettesitheto adatokkal.
 *
 * <p>A fajl elso soraban allo {@code prefix} automatikusan
 * ele kerul minden uzenet elott (kihagyahato).</p>
 *
 * <p>messages.yml pelda:</p>
 *
 * <pre>{@code
 * prefix: "<dark_gray>[<aqua>SajatPlugin<dark_gray>] "
 * messages:
 *   level-up: "<green>Szintlepes! Uj szint: <white>%level%"
 *   no-money:
 *     - "<red>Nincs eleg penzed!"
 *     - "<gray>Hianyzo osszeg: <white>%missing%"
 * }</pre>
 *
 * <pre>{@code
 * Messages msg = Messages.load(plugin, "messages.yml");
 * msg.send(player, "level-up", "<green>Szintlepes!", "level", 5);
 * msg.send(player, "no-money", "<red>Nincs eleg penzed!", "missing", 120);
 * msg.broadcast("welcome", "<gold>Udv a szerveren!");
 * }</pre>
 *
 * <p>A %kulcs% tokenek a parameterekbol tollenek ki; emellett
 * a Player-celzott kuldes a PaperKit Placeholders-t is lefuttatja
 * (%papi-placholderek, %level% stb.), es a %receiver% mindig a
 * celzat neve.</p>
 */
public final class Messages {

    /** Az alapul szolgalo config. */
    private final ConfigFile config;
    /** Minden uzenet ele rakodo elotag. */
    private String prefix = "";

    private Messages(final ConfigFile config) {
        this.config = config;
    }

    /**
     * Uzenet fajl betoltese (a jar-beli alap automatikusan lemásolodik,
     * ha meg nem letezik a plugin mappjaban).
     *
     * @param plugin   a hivo plugin
     * @param fileName a fajl neve, pl. {@code "messages.yml"}
     * @return a kezelo
     */
    public static Messages load(final org.bukkit.plugin.java.JavaPlugin plugin, final String fileName) {
        return from(ConfigFile.load(plugin, fileName));
    }

    /**
     * Mar meglevo ConfigFile-ra epites (pl. ha osszevontad a config.yml-be).
     *
     * @param config a config kanyelo
     * @return a kezelo
     */
    public static Messages from(final ConfigFile config) {
        final Messages messages = new Messages(config);
        messages.prefix = config.getString("prefix", "");
        return messages;
    }

    /**
     * Elotag allitasa (alapertelmezetten a fajl "prefix" kulcsarből).
     *
     * @param prefix az uzenet szoveg, pl. {@code "<gray>[<aqua>Plugin<gray>] "}
     * @return this
     */
    public Messages prefix(final String prefix) {
        this.prefix = prefix == null ? "" : prefix;
        return this;
    }

    /**
     * @return az alap config (reload/save hasznalhato)
     */
    public ConfigFile config() {
        return config;
    }

    /**
     * A nyers (ki nem formazott) szoveg egy utvonalrol,
     * az elotaggal egyutt.
     *
     * @param path az uzenet utvonala
     * @param def  alapertelmezett szoveg, ha nincs meg
     * @return a szoveg
     */
    public String raw(final String path, final String def) {
        return prefix + config.getString(path, def);
    }

    /**
     * Uzenet kuldese (%kulcs% tokenekkel + elotaggal).
     *
     * @param target a cimzett (jatekos vа konzol)
     * @param path   az uzenet utvonala
     * @param def    alapertelmezett szoveg, ha a kulcs nincs meg
     * @param keyValues páronként: kulcs, ertek → pl. "level", 5
     */
    public void send(final CommandSender target, final String path, final String def,
            final Object... keyValues) {
        if (target == null) {
            return;
        }
        Msg.sendRaw(target, substitute(raw(path, def), keyValues));
    }

    /**
     * Uzenet kuldese jatekosnak: a %receiver% a nev, es a PaperKit
     * Placeholders (%papi-placeholder%-ek) is feloldodnak rajta.
     *
     * @param target a jatekos
     * @param path   az uzenet utvonala
     * @param def    alapertelmezett szoveg
     * @param keyValues paronkent: kulcs, ertek
     */
    public void send(final Player target, final String path, final String def,
            final Object... keyValues) {
        if (target == null) {
            return;
        }
        final Object[] args = appendReceiver(target, keyValues);
        Msg.sendRaw(target, Placeholders.apply(
                substitute(raw(path, def), args), target));
    }

    /**
     * Tobb soros uzenet (lista) kuldese.
     *
     * @param target    a jatekos
     * @param path      az uzenet utvonala (lista)
     * @param def       alapertelmezett sorok (mind soronkent)
     * @param keyValues paronkent: kulcs, ertek
     */
    public void sendLines(final Player target, final String path, final List<String> def,
            final Object... keyValues) {
        if (target == null) {
            return;
        }
        final List<String> lines = config.getStringList(path);
        final List<String> source = (lines == null || lines.isEmpty())
                ? (def == null ? List.<String>of() : def)
                : lines;
        for (final String line : source) {
            final Object[] args = appendReceiver(target, keyValues);
            Msg.sendRaw(target, Placeholders.apply(
                    substitute(prefix + line, args), target));
        }
    }

    /**
     * Minden online jatekos es a konzol ha meghear broadcast.
     *
     * @param path      az uzenet utvonala
     * @param def       alapertelmezett szoveg
     * @param keyValues paronkent: kulcs, ertek
     */
    public void broadcast(final String path, final String def, final Object... keyValues) {
        final String text = substitute(raw(path, def), keyValues);
        final Component component = Text.parse(text);
        for (final Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            online.sendMessage(component);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(component);
    }

    /**
     * A kiszamitott (de ki nem kuldott) Component - pl. menu neveknek.
     *
     * @param player    a kontextual jatekos (Placeholders egyletek thesis)
     * @param path      az uzenet utvonala
     * @param def       alapertelmezett szoveg
     * @param keyValues paronkent: kulcs, ertek
     * @return a formazott Component
     */
    public Component component(final Player player, final String path, final String def,
            final Object... keyValues) {
        final Object[] args = appendReceiver(player, keyValues);
        final String expanded = substitute(raw(path, def), args);
        if (player == null) {
            return Text.parse(expanded);
        }
        return Text.parse(Placeholders.apply(expanded, player), args != null && args.length > 0
                ? resolve(args) : new TagResolver[0]);
    }

    /**
     * Az uzenet elolvasasa kis szovegesen (pl. configokba, lore-ba).
     */
    public List<String> lines(final String path, final List<String> def, final Object... keyValues) {
        final List<String> source = config.getStringList(path);
        final List<String> base = (source == null || source.isEmpty())
                ? (def == null ? List.<String>of() : def)
                : source;
        final List<String> out = new ArrayList<>(base.size());
        for (final String line : base) {
            out.add(substitute(prefix + line, keyValues));
        }
        return out;
    }

    private Object[] appendReceiver(final Player target, final Object[] keyValues) {
        final Object[] result = new Object[keyValues.length + 2];
        System.arraycopy(keyValues, 0, result, 0, keyValues.length);
        result[keyValues.length] = "receiver";
        result[keyValues.length + 1] = target.getName();
        return result;
    }

    private TagResolver[] resolve(final Object[] keyValues) {
        final List<TagResolver> list = new ArrayList<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            list.add(Text.res(String.valueOf(keyValues[i]), keyValues[i + 1]));
        }
        return list.toArray(new TagResolver[0]);
    }

    private static String substitute(final String text, final Object[] keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return text;
        }
        String result = text;
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            result = result.replace("%" + keyValues[i] + "%", String.valueOf(keyValues[i + 1]));
        }
        return result;
    }
}
