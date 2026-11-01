package hu.paperkit.hooks.integrations;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import hu.paperkit.hooks.Hooks;

/**
 * Tartalmi integrációk: Citizens, MythicMobs, ModelEngine,
 * DecentHolograms, ItemsAdder, Nexo, Geyser/Floodgate.
 */
public final class ContentHooks {

    private ContentHooks() {
    }

    /** Citizens integráció (NPC-felismerés). */
    public static final class CitizensHook {
        private CitizensHook() {
        }

        /**
         * Az entity Citizens-NPC-e.
         */
        public static boolean isNpc(final Entity entity) {
            if (!Hooks.isPluginEnabled("Citizens")) {
                return false;
            }
            try {
                final Object registry = Class.forName("net.citizensnpcs.api.CitizensAPI")
                        .getMethod("getNPCRegistry").invoke(null);
                final Object npc = registry.getClass().getMethod("getNPC", Entity.class)
                        .invoke(registry, entity);
                return npc != null;
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /** MythicMobs integráció (jelenlét + best-effort felismerés). */
    public static final class MythicMobsHook {
        private MythicMobsHook() {
        }

        /**
         * @return fent-e a MythicMobs
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("MythicMobs");
        }

        /**
         * Az entity MythicMobs-mob-e (best-effort).
         */
        public static boolean isMythicMob(final Entity entity) {
            if (!hooked()) {
                return false;
            }
            try {
                final Object api = Class.forName("io.lumine.mythic.bukkit.MythicBukkit")
                        .getMethod("inst").invoke(null);
                final Object helper = api.getClass().getMethod("getAPIHelper").invoke(api);
                final Object instance = helper.getClass()
                        .getMethod("getMythicMobInstance", Entity.class).invoke(helper, entity);
                return instance != null;
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /** ModelEngine integráció (jelenlét). */
    public static final class ModelEngineHook {
        private ModelEngineHook() {
        }

        /**
         * @return fent-e a ModelEngine
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("ModelEngine");
        }
    }

    /** DecentHolograms integráció (hologram létrehozás). */
    public static final class DecentHologramsHook {
        private DecentHologramsHook() {
        }

        /**
         * Hologram létrehozás DH API-val (sikertelenül false).
         *
         * @return true, ha létrejött
         */
        public static boolean spawnHologram(final String name, final Location loc,
                final List<String> lines) {
            if (!Hooks.isPluginEnabled("DecentHolograms")) {
                return false;
            }
            try {
                final Class<?> dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                try {
                    dhapi.getMethod("createHologram", String.class, Location.class, List.class)
                            .invoke(null, name, loc, lines);
                    return true;
                } catch (final NoSuchMethodException e) {
                    dhapi.getMethod("createHologram", String.class, List.class, Location.class)
                            .invoke(null, name, lines, loc);
                    return true;
                }
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /** ItemsAdder integráció (custom item felismerés). */
    public static final class ItemsAdderHook {
        private ItemsAdderHook() {
        }

        /**
         * @return fent-e az ItemsAdder
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("ItemsAdder");
        }

        /**
         * Az item ItemsAdder custom item-e (best-effort).
         */
        public static boolean isCustomItem(final ItemStack item) {
            if (!hooked()) {
                return false;
            }
            try {
                final Object result = Class.forName("dev.lone.itemsadder.api.ItemsAdder")
                        .getMethod("isCustomItem", ItemStack.class)
                        .invoke(null, item);
                return result instanceof final Boolean b && b;
            } catch (final Exception e) {
                return false;
            }
        }
    }

    /** Nexo integráció (jelenlét). */
    public static final class NexoHook {
        private NexoHook() {
        }

        /**
         * @return fent-e a Nexo
         */
        public static boolean hooked() {
            return Hooks.isPluginEnabled("Nexo");
        }
    }

    /** Geyser + Floodgate integráció (Bedrock-felismerés). */
    public static final class GeyserHook {
        private GeyserHook() {
        }

        /**
         * A játékos Bedrock-játékos-e (Floodgate/előszó).
         */
        public static boolean isBedrock(final Player player) {
            if (Hooks.isPluginEnabled("floodgate")) {
                try {
                    final Object api = Class.forName("org.geysermc.floodgate.api.FloodgateApi")
                            .getMethod("getInstance").invoke(null);
                    final Object result = api.getClass()
                            .getMethod("isFloodgatePlayer", UUID.class)
                            .invoke(api, player.getUniqueId());
                    return result instanceof final Boolean b && b;
                } catch (final Exception ignored) {
                    // tavalk tett晋江文学
                }
            }
            if (Hooks.isPluginEnabled("Geyser")) {
                try {
                    final Object manager = Class.forName("org.geysermc.geyser.GeyserImpl")
                            .getMethod("getInstance").invoke(null);
                    return manager != null && player.getName().startsWith(".");
                } catch (final Exception ignored) {
                    // probalkozas ok
                }
            }
            return false;
        }
    }
}
