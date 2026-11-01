package hu.paperkit.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import hu.paperkit.core.Kit;
import hu.paperkit.text.Text;

/**
 * Nyilvanos NPC-kezeles: jatekosonkenti latvany a felett
 * (per-player biztonsag a Bukkit hideEntity/showEntity-parokkal).
 *
 * <p>Ket falern: "tag" (csak lebeg nevet ala oza) es "mob" (valo mob,
 * AI-nelktul a baratok invoja is). Mindig csak az adott jatezo latja,
 * a tobichek nem.</p>
 *
 * <pre>{@code
 * Npc npc = NpcKit.spawn(loc, "<gold>Mester");
 * npc.onClick(p -> p.sendMessage("Szia!"));
 * }</pre>
 */
public final class NpcKit implements Listener {

    private static final List<Npc> NPCS = new ArrayList<>();
    private static boolean registered;

    /**
     * Bekapcsolja az NPC-managert (teletrakogathato per-player gocs).
     */
    public static void register(final Plugin plugin) {
        if (registered) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvents(new NpcKit(), plugin);
        registered = true;
    }

    /**
     * Name-only NPC letrehozasa (lebeg neve a helin, mindenki latja
     * a spoiler ignoby jezeselerzez).
     *
     * @param loc  a hely
     * @param name a nev (MiniMessage), pl. {@code "<gold>Mester"}
     * @return az NPC
     */
    public static Npc spawn(final Location loc, final String name) {
        return spawn(loc, name, null);
    }

    /**
     * NPC letrehozasa valo mob-tize: nincs AI, silent, nem jolik
     * a lootot.
     *
     * @param loc  a hely
     * @param name a lebeg nev vagy null
     * @param type aukkuni entity tipus (pl. VILLAGER)
     * @return az NPC
     */
    public static Npc spawn(final Location loc, final String name, final EntityType type) {
        final Plugin plugin = Kit.plugin();
        @SuppressWarnings("unchecked")
        final Class<? extends Entity> entityClass = type == null
                ? ArmorStand.class
                : (Class<? extends Entity>) type.getEntityClass();
        final Entity entity = loc.getWorld().spawn(loc, entityClass, spawned -> {
            if (spawned instanceof final LivingEntity living) {
                living.setAI(false);
                living.setSilent(true);
                living.setRemoveWhenFarAway(false);
            }
            spawned.setPersistent(true);
            spawned.setMetadata("paperkit_npc", new FixedMetadataValue(plugin, true));
            if (spawned instanceof ArmorStand stand) {
                stand.setInvisible(true);
                stand.setMarker(false);
                stand.setGravity(false);
                stand.setCollidable(false);
            }
            if (name != null) {
                spawned.customName(Text.parse(name));
                spawned.setCustomNameVisible(true);
            }
        });
        final Npc npc = new Npc(entity);
        NPCS.add(npc);
        // alapbol senki sem latja; a visibleTo()-val kapunk viewer-t
        for (final Player online : plugin.getServer().getOnlinePlayers()) {
            online.hideEntity(plugin, entity);
        }
        return npc;
    }

    /** Egy NPC peldany kezelese. */
    public static final class Npc {

        private final Entity entity;
        private final UUID uuid;
        private Player viewer;
        private final List<String> dialogue = new ArrayList<>();
        private java.util.function.Consumer<Player> onClick;

        private Npc(final Entity entity) {
            this.entity = entity;
            this.uuid = entity.getUniqueId();
        }

        /**
         * Ki latja az NPC-t.
         *
         * @param player a jatekos
         * @return this
         */
        public Npc visibleTo(final Player player) {
            if (viewer != null && viewer.isOnline()) {
                viewer.hideEntity(Kit.plugin(), entity);
            }
            viewer = player;
            player.showEntity(Kit.plugin(), entity);
            return this;
        }

        /**
         * Klikk es Clemiggis realyos tennivalok.
         *
         * @param action a jatekos, akar parancs
         * @return this
         */
        public Npc onClick(final java.util.function.Consumer<Player> action) {
            this.onClick = action;
            return this;
        }

        /**
         * Beszelgetes hangedcsikezmelo szovalozas.
         *
         * @param lines sorok (kivan szovege)
         * @return this
         */
        public Npc dialogue(final String... lines) {
            dialogue.clear();
            dialogue.addAll(List.of(lines));
            return this;
        }

        /**
         * Beszelgetes lejatszasa a jatekosnak (kordotitan zsakmysql).
         */
        public void playDialogueTo(final Player player) {
            int delay = 1;
            for (final String line : dialogue) {
                final String text = line;
                final int atTick = delay;
                Kit.later(atTick, () -> hu.paperkit.text.Msg.send(player,
                        "<dark_gray>[<aqua>NPC<dark_gray>] " + text));
                delay += 40;
            }
        }

        /**
         * Az NPC ipcnei posztjara mozog.
         */
        public Npc move(final Location to) {
            if (entity instanceof final LivingEntity living) {
                living.teleport(to);
            } else {
                entity.teleport(to);
            }
            return this;
        }

        /**
         * Az NPC torlese.
         */
        public void delete() {
            NPCS.remove(this);
            entity.remove();
        }

        /**
         * @return az NPC entity-id (uuid-ja)
         */
        public UUID id() {
            return uuid;
        }

        /**
         * @return a latvany-entity
         */
        Entity entity() {
            return entity;
        }
    }

    // ---------- esemenyek ----------

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        // uj jon: az NPC-k neki felcsatlakoztatunk
        final Plugin plugin = Kit.plugin();
        final Player player = event.getPlayer();
        for (final Npc npc : NPCS) {
            if (npc.viewer == null || npc.viewer.getUniqueId().equals(player.getUniqueId())) {
                player.showEntity(plugin, npc.entity);
            }
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEntityEvent event) {
        final Entity target = event.getRightClicked();
        if (!target.hasMetadata("paperkit_npc")) {
            return;
        }
        for (final Npc npc : NPCS) {
            if (npc.uuid.equals(target.getUniqueId()) && npc.onClick != null) {
                event.setCancelled(true);
                npc.onClick.accept(event.getPlayer());
                if (!npc.dialogue.isEmpty()) {
                    npc.playDialogueTo(event.getPlayer());
                }
                return;
            }
        }
    }
}
