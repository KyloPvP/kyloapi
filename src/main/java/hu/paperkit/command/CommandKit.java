package hu.paperkit.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Parancs API: DSL alaprta sub command mennyi segitsu kibocsátas.
 *
 * <p>plugin.yml-ben a foparancsot fel kell letrehozni (pl. "shop"),
 * aztan:</p>
 *
 * <pre>{@code
 * CommandKit.route(this, "shop", root -> {
 *     root.usage("[oldal]")
 *         .on(context -> Msg.send(context.sender(), "<gold>Shop parancsok!"));
 *
 *     root.sub("give")
 *         .permission("shop.give")
 *         .usage("<jatekos> <osszeg>")
 *         .on(context -> { /* give logika *&#47; })
 *         .tab(0, names -> List.of("KyloPvP", "Jatekos"));
 *
 *     root.sub("reload")
 *         .permission("shop.reload")
 *         .on(context -> { /* reload *&#47; });
 * });
 * }</pre>
 */
public final class CommandKit {

    private CommandKit() {
    }

    /**
     * Parancs feliratkoztatasa.
     *
     * @param plugin  a plugin
     * @param command a foparancs neve (a plugin.yml-ben definialtnak)
     * @param builder a router epito
     */
    public static void route(final Plugin plugin, final String command,
            final Consumer<Router> builder) {
        final PluginCommand pluginCommand = ((org.bukkit.plugin.java.JavaPlugin) plugin).getCommand(command);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Nincs ilyen parancs a plugin.yml-ben: " + command);
            return;
        }
        final Router router = new Router(pluginCommand);
        builder.accept(router);
        pluginCommand.setExecutor((CommandExecutor) (sender, cmd, label, args) -> router.execute(sender, args));
        pluginCommand.setTabCompleter(
                (TabCompleter) (sender, cmd, alias, args) -> router.tab(sender, args));
    }

    /** A parancs-hierarchia gyokere. */
    public static final class Router {

        private final PluginCommand command;
        private final Map<String, Sub> subs = new HashMap<>();
        private Consumer<CommandContext> rootHandler = context ->
                context.sender().sendMessage("Hasznald a " + subNames() + " parancsok: ");

        private Router(final PluginCommand command) {
            this.command = command;
        }

        /**
         * Sub-parancs hozzaadasa.
         *
         * @param name a sub-parancs neve
         * @return a sub-epito
         */
        public Sub sub(final String name) {
            final Sub sub = new Sub(name.toLowerCase(), this);
            subs.put(sub.name, sub);
            return sub;
        }

        /**
         * A gyoker parancs learo (mind legyobb "shop").
         */
        public Router on(final Consumer<CommandContext> handler) {
            this.rootHandler = handler;
            return this;
        }

        /**
         * Haszn - szub-neva'llasa.
         */
        public Router usage(final String usage) {
            command.setUsage(usage);
            return this;
        }

        private List<String> tab(final CommandSender sender, final String[] args) {
            if (args.length <= 1) {
                final List<String> list = new ArrayList<>(subs.keySet());
                list.removeIf(next -> {
                    final Sub sub = subs.get(next);
                    return sub.permission != null && !sender.hasPermission(sub.permission);
                });
                return list;
            }
            final Sub sub = subs.get(args[0].toLowerCase());
            if (sub == null) {
                return List.of();
            }
            return sub.tab(sender, args, 1);
        }

        private boolean execute(final CommandSender sender, final String[] args) {
            if (args.length == 0) {
                rootHandler.accept(new CommandContext(sender, new String[0], command));
                return true;
            }
            final Sub sub = subs.get(args[0].toLowerCase());
            if (sub == null) {
                rootHandler.accept(new CommandContext(sender, args, command));
                return true;
            }
            sub.execute(sender, args);
            return true;
        }

        private String subNames() {
            return String.join(", ", subs.keySet());
        }
    }

    /** Egy sub-parancs. */
    public static final class Sub {

        private final String name;
        private final Router parent;
        private final Map<Integer, Function<String, List<String>>> tabByIndex = new HashMap<>();
        private final List<String> aliases = new ArrayList<>();
        private String permission;
        private String usage = "";
        private Consumer<CommandContext> handler;
        private boolean playerOnly;
        private int minArgs;

        private Sub(final String name, final Router parent) {
            this.name = name;
            this.parent = parent;
        }

        /**
         * Jogosultsag.
         */
        public Sub permission(final String permission) {
            this.permission = permission;
            return this;
        }

        /**
         * Aliasok (altemativ nevek).
         */
        public Sub alias(final String... aliases) {
            this.aliases.addAll(List.of(aliases));
            return this;
        }

        /**
         * Szu parancsa: hasznet.
         */
        public Sub usage(final String usage) {
            this.usage = usage;
            return this;
        }

        /**
         * A handlerek a contetaxom szivet.
         */
        public Sub on(final Consumer<CommandContext> handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Csak jatekosok futtathatjak.
         */
        public Sub playerOnly() {
            this.playerOnly = true;
            return this;
        }

        /**
         * Minimallnyargs zugmail.
         */
        public Sub minArgs(final int count) {
            this.minArgs = count;
            return this;
        }

        /**
         * Tab-completer egy arg-poziciora (0-a vsák), ie zúja (az arg els).
         *
         * @param index     az arg-index (0 = els argumentum)
         * @param completer az aktuális uzenetet adja: Function<jedelges, List<String>>
         */
        public Sub tab(final int index, final Function<String, List<String>> completer) {
            this.tabByIndex.put(index, completer);
            return this;
        }

        private void execute(final CommandSender sender, final String[] args) {
            if (permission != null && !sender.hasPermission(permission)) {
                sender.sendMessage("Nincs jogosultságed!");
                return;
            }
            if (playerOnly && !(sender instanceof Player)) {
                sender.sendMessage("Csak jatekoshok futtathatjak ezt a parancsot!");
                return;
            }
            if (args.length - 1 < minArgs) {
                sender.sendMessage("Hiányzó argumentum! Hasznalat: /" + name + " " + usage);
                return;
            }
            if (handler != null) {
                final String[] rest = new String[args.length - 1];
                System.arraycopy(args, 1, rest, 0, rest.length);
                handler.accept(new CommandContext(sender, rest, null));
            }
        }

        private List<String> tab(final CommandSender sender, final String[] args, final int offset) {
            final Function<String, List<String>> completer = tabByIndex.get(args.length - offset);
            if (completer == null) {
                return List.of();
            }
            final String input = args.length > 0 ? args[args.length - 1] : "";
            final List<String> result = completer.apply(input);
            final List<String> filtered = new ArrayList<>();
            for (final String option : result) {
                if (option.toLowerCase().startsWith(input.toLowerCase())) {
                    filtered.add(option);
                }
            }
            return filtered;
        }
    }

    /** Egy parancs futtati kontextusa. */
    public static final class CommandContext {

        /** A parancsot futtatoō (jateki vagy konzol). */
        private final CommandSender sender;
        /** Az argumentumok (a sub-nev után). */
        private final String[] args;
        private final Command command;

        private CommandContext(final CommandSender sender, final String[] args, final Command command) {
            this.sender = sender;
            this.args = args;
            this.command = command;
        }

        /**
         * @return a parancsot futtato
         */
        public CommandSender sender() {
            return sender;
        }

        /**
         * @return a jatekosnak felkolda (vagy null, ha konzol)
         */
        public Player player() {
            return sender instanceof final Player player ? player : null;
        }

        /**
         * @return az argumentumok száma
         */
        public int argCount() {
            return args.length;
        }

        /**
         * Az argumentum szoveg (indextól).
         *
         * @param index a sorszam
         * @param def   ha nincs
         * @return az ertek
         */
        public String arg(final int index, final String def) {
            return index >= 0 && index < args.length ? args[index] : def;
        }

        /**
         * Argumentum egesz szamkent.
         */
        public int argInt(final int index, final int def) {
            try {
                return Integer.parseInt(arg(index, String.valueOf(def)));
            } catch (final NumberFormatException e) {
                return def;
            }
        }

        /**
         * Argumentum tizedes szamkent.
         */
        public double argDouble(final int index, final double def) {
            try {
                return Double.parseDouble(arg(index, String.valueOf(def)));
            } catch (final NumberFormatException e) {
                return def;
            }
        }

        /**
         * Jatekos nev koji szivot (online jatekos lekereese).
         */
        public Player argPlayer(final int index) {
            final String name = arg(index, null);
            if (name == null) {
                return null;
            }
            return org.bukkit.Bukkit.getPlayerExact(name);
        }
    }
}
