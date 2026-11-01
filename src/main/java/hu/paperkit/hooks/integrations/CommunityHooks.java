package hu.paperkit.hooks.integrations;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.World;

import hu.paperkit.hooks.Hooks;

/**
 * Közösségi/minőség integrációk: EssentialsX, Multiverse-Core,
 * DiscordSRV, SuperVanish, SkinsRestorer.
 */
public final class CommunityHooks {

    private CommunityHooks() {
    }

    /** EssentialsX integráció (egyensúly, nickname). */
    public static final class EssentialsHook {
        private EssentialsHook() {
        }

        /**
         * A játékos Essentials egyenlege (0, ha van-e hiba).
         */
        public static double money(final Player player) {
            if (!Hooks.isPluginEnabled("Essentials")) {
                return 0;
            }
            try {
                final Object essentials = Hooks.getPlugin("Essentials");
                final Object user = essentials.getClass()
                        .getMethod("getUser", Player.class).invoke(essentials, player);
                final Object money = user.getClass().getMethod("getMoney").invoke(user);
                if (money instanceof final BigDecimal big) {
                    return big.doubleValue();
                }
                return ((Number) money).doubleValue();
            } catch (final Exception e) {
                return 0;
            }
        }

        /**
         * A játékos Essentials-nickname-je (vagy a normál név).
         */
        public static String nickname(final Player player) {
            if (!Hooks.isPluginEnabled("Essentials")) {
                return player.getName();
            }
            try {
                final Object essentials = Hooks.getPlugin("Essentials");
                final Object user = essentials.getClass()
                        .getMethod("getUser", Player.class).invoke(essentials, player);
                final Object nick = user.getClass().getMethod("getNickname").invoke(user);
                return nick == null ? player.getName() : String.valueOf(nick);
            } catch (final Exception e) {
                return player.getName();
            }
        }
    }

    /** Multiverse-Core integráció (világ-lista). */
    public static final class MultiverseHook {
        private MultiverseHook() {
        }

        /**
         * @return fent-e a Multiverse-Core
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("Multiverse-Core");
        }

        /**
         * A világ (neve) Multiverse-ben menedzelt-e.
         */
        public static boolean isManagedWorld(final World world) {
            if (!hooked()) {
                return false;
            }
            try {
                final Object plugin = Hooks.getPlugin("Multiverse-Core");
                final Object manager = plugin.getClass()
                        .getMethod("getMVWorldManager").invoke(plugin);
                final Object mvWorld = manager.getClass()
                        .getMethod("getMVWorld", String.class).invoke(manager, world.getName());
                return mvWorld != null;
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /** DiscordSRV integráció (Discord üzenet). */
    public static final class DiscordSRVHook {
        private DiscordSRVHook() {
        }

        /**
         * Üzenet küldése a fő chat-csatornára (ha a DiscordSRV-nincs fent: no-op).
         */
        public static void chat(final String message) {
            if (!Hooks.isPluginEnabled("DiscordSRV")) {
                return;
            }
            try {
                final Object plugin = Hooks.getPlugin("DiscordSRV");
                final Object channel = plugin.getClass().getMethod("getMainChatChannel")
                        .invoke(plugin);
                if (channel == null) {
                    return;
                }
                final Class<?> util = Class.forName("github.scarsz.discordsrv.util.DiscordUtil");
                util.getMethod("sendMessage", String.class, String.class)
                        .invoke(null, channel, message);
            } catch (final Exception e) {
                // elnyomva: a Discord res zenet nem kritikus
            }
        }
    }

    /** SuperVanish / PremiumVanish integráció (voltász). */
    public static final class SuperVanishHook {
        private SuperVanishHook() {
        }

        /**
         * A játékos „tűntetve”-e (SuperVanish/PremiumVanish).
         */
        public static boolean isVanished(final Player player) {
            for (final String provider : new String[] { "SuperVanish", "PremiumVanish" }) {
                if (!Hooks.isPluginEnabled(provider)) {
                    continue;
                }
                try {
                    final Class<?> api = Class.forName("de.myzelyam.api.vanish.VanishAPI");
                    final Method isInvisible = api.getMethod("isInvisible", Player.class);
                    return (boolean) isInvisible.invoke(null, player);
                } catch (final Exception ignored) {
                    // kovetkezo szerver: nem gond
                }
            }
            return player.hasMetadata("vanished");
        }
    }

    /** SkinsRestorer integráció (bőr). */
    public static final class SkinsRestorerHook {
        private SkinsRestorerHook() {
        }

        /**
         * @return fent-e a SkinsRestorer
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("SkinsRestorer");
        }
    }
}
