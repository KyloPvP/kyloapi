package hu.paperkit.hooks.packetevents;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

/**
 * PacketEvents hook (optionalis): packet figyeles es fake megjelenites.
 *
 * <p>Egyedul akkor hasznalhato, ha a PacketEvents plugin telepitve van:</p>
 *
 * <pre>{@code
 * if (Hooks.isPluginEnabled("packetevents")) {
 *     PacketEventsHook.engage(plugin);
 * }
 *
 * PacketEventsHook.addSendListener(event -> { ... });
 * PacketEventsHook.sendBlockChange(player, loc, blockState);
 * ...
 * PacketEventsHook.terminate();  // onDisable-ben
 * }</pre>
 */
public final class PacketEventsHook {

    private static boolean engaged;

    private PacketEventsHook() {
    }

    /**
     * PacketEvents inicializalasa.
     *
     * @param plugin a hivo plugin
     * @return true, ha sikeresen inicializalt ( vagy korabbben az volt)
     */
    public static boolean engage(final Plugin plugin) {
        if (!hu.paperkit.hooks.Hooks.isPluginEnabled("packetevents")) {
            return false;
        }
        if (!engaged) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(plugin));
            PacketEvents.getAPI().getSettings()
                    .reEncodeByDefault(false)
                    .debug(false)
                    .checkForUpdates(false);
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().init();
            engaged = true;
        }
        return true;
    }

    /**
     * Fogadott (kliens -> szerver) csomag listener.
     */
    public static PacketListenerCommon addReceiveListener(final Consumer<PacketReceiveEvent> handler) {
        requireEngaged();
        return addListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(final PacketReceiveEvent event) {
                handler.accept(event);
            }
        });
    }

    /**
     * Kimeno (szerver -> kliens) csomag listener, minden tipusra.
     */
    public static PacketListenerCommon addSendListener(final Consumer<PacketSendEvent> handler) {
        requireEngaged();
        return addListener(new PacketListenerAbstract() {
            @Override
            public void onPacketSend(final PacketSendEvent event) {
                handler.accept(event);
            }
        });
    }

    /**
     * Kimeno csomag listener csak egy meghat�arozott PacketType-ra.
     */
    public static PacketListenerCommon addSendListener(final PacketTypeCommon type,
            final Consumer<PacketSendEvent> handler) {
        requireEngaged();
        return addListener(new PacketListenerAbstract() {
            @Override
            public void onPacketSend(final PacketSendEvent event) {
                if (event.getPacketType() == type) {
                    handler.accept(event);
                }
            }
        });
    }

    /**
     * Prioritssal ellatott listener (pl. HIGH priority esetn? el?sz?r l?t).
     */
    public static PacketListenerCommon addSendListener(final PacketListenerPriority priority,
            final Consumer<PacketSendEvent> handler) {
        requireEngaged();
        final PacketListenerCommon listener = new PacketListenerAbstract(priority) {
            @Override
            public void onPacketSend(final PacketSendEvent event) {
                handler.accept(event);
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        return listener;
    }

    private static PacketListenerCommon addListener(final PacketListenerCommon listener) {
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        return listener;
    }

    /**
     * Listener leiratkoztatasa.
     */
    public static void removeListener(final PacketListenerCommon listener) {
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
    }

    /**
     * Fake block csomag kuldese egy jatekosnak (csak neki lathato blokk).
     *
     * @param player  a lazato jatekos
     * @param loc     a blokk helye
     * @param state   a fake block state, pl. {@code WrappedBlockState.getDefaultState(Material)}
     */
    public static void sendBlockChange(final Player player, final Location loc, final WrappedBlockState state) {
        requireEngaged();
        final PacketWrapper<WrapperPlayServerBlockChange> packet =
                new WrapperPlayServerBlockChange(new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), state);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    /**
     * PacketEvents leallitasa (onDisable-ben hivni).
     */
    public static void terminate() {
        if (engaged && PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
            engaged = false;
        }
    }

    private static void requireEngaged() {
        if (!engaged) {
            throw new IllegalStateException(
                    "PacketEvents nincs bekapcsolva! Hivd meg elobb: PacketEventsHook.engage(plugin)");
        }
    }
}
