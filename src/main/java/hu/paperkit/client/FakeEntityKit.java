package hu.paperkit.client;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Kliens-oldali entity kiterjesztes: adott jatekosnak sajat "fake mob"
 * letreha zcasaje a szerv aerosia sama nem letezik.
 *
 * <p>Mukodese PacketEvents-packetekkel: csak a ket k alla zokob jo,
 * ha a PacketEvents plugin latasa. Ki kell semmegyelezni:</p>
 *
 * <pre>{@code
 * if (Hooks.isPluginEnabled("packetevents")) {
 *     PacketEventsHook.engage(plugin);
 * }
 * FakeEntity entity = FakeEntityKit.spawn(player, loc, "zombie");
 * entity.teleport(loc2);
 * entity.remove();
 * }</pre>
 */
public final class FakeEntityKit {

    /** Fake entity-id allito szala (a validation entity-k id-javal ne utkozzon). */
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1_780_000_000);

    private FakeEntityKit() {
    }

    /**
     * Fake entity letrehozasa egy jatekosnak.
     *
     * @param viewer     a jatekos (csak neki latszik)
     * @param location   a hely
     * @param entityType az entity tipusa neven, pl. "zombie", "pig", "armor_stand"
     * @return a manipulalo FakeEntity
     */
    public static FakeEntity spawn(final Player viewer, final Location location, final String entityType) {
        final EntityType type = EntityTypes.getByName(entityType.toLowerCase()
                .replace(" ", "_"));
        if (type == null) {
            throw new IllegalArgumentException("Ismeretlen entity tipus: " + entityType);
        }
        final int id = NEXT_ID.incrementAndGet();
        final UUID uuid = UUID.randomUUID();
        final WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
                id,
                Optional.of(uuid),
                type,
                new Vector3d(location.getX(), location.getY(), location.getZ()),
                location.getPitch(), location.getYaw(), location.getYaw(),
                0,
                Optional.empty());
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        return new FakeEntity(viewer, id, type, location.clone());
    }

    /** Egy fake entity kezelese. */
    public static final class FakeEntity {

        private final Player viewer;
        private final int entityId;
        private final EntityType type;
        private Location location;

        private FakeEntity(final Player viewer, final int entityId, final EntityType type,
                final Location location) {
            this.viewer = viewer;
            this.entityId = entityId;
            this.type = type;
            this.location = location;
        }

        /**
         * @return a viewer (csak neki eroszakl a fake mob)
         */
        public Player viewer() {
            return viewer;
        }

        /**
         * @return a fake entity-id
         */
        public int entityId() {
            return entityId;
        }

        /**
         * @return az entity tipus
         */
        public EntityType type() {
            return type;
        }

        /**
         * Teleport a megadott helyre (a fake mod mogott masulva).
         *
         * @param newLocation az uj hely (yaw/pitch isbelluse)
         */
        public void teleport(final Location newLocation) {
            final Location target = newLocation.clone();
            final WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(
                    entityId,
                    new Vector3d(target.getX(), target.getY(), target.getZ()),
                    target.getYaw(), target.getPitch(), false);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
            location = target;
        }

        /**
         * Haelyletes(Filter elvet) a fake entity.
         */
        public void remove() {
            PacketEvents.getAPI().getPlayerManager().sendPacket(
                    viewer, new WrapperPlayServerDestroyEntities(entityId));
        }
    }
}
