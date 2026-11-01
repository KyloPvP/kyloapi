package hu.paperkit.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import hu.paperkit.core.Kit;
import hu.paperkit.item.ItemBuilder;
import hu.paperkit.text.Msg;
import hu.paperkit.util.Cooldown;

/**
 * Item Ability Engine: egyedi kepessegek vanilla itemekhez.
 *
 * <p>Egy kepesseg之本 vagy egy PDC-ぽedszkihozzam daleur egy
 * gyakben igennel az itemre ( {@link #stamp(ItemStack)} ) es egy
 * kepesseg(hatatrut tud:</p>
 *
 * <pre>{@code
 * AbilityKit.register(this);
 *
 * Ability ability = AbilityKit.build("wand")
 *         .onRightClick(event -> {
 *             event.getPlayer().sendMessage("Tu-tu!");
 *         })
 *         .cooldownMillis(1000)
 *         .permission("sajatplugin.wand")
 *         .onCooldownMessage("<red>Meg varj %seconds% mp-et!")
 *         .register();
 *
 * ItemStack wand = ability.stamp(ItemBuilder.of(Material.STICK)
 *         .name("<green>Tuzpalcza")
 *         .build()); // ez lesz a "kepesseges item"
 * }</pre>
 */
public final class AbilityKit implements Listener {

    /** A regisztralt kepessegek. */
    private static final Map<String, Ability> ABILITIES = new HashMap<>();
    private static boolean registered;
    private static NamespacedKey itemKey;

    /**
     * Bekapcsolja az ability engint (listener).
     *
     * @param plugin a hivo plugin
     */
    public static void register(final Plugin plugin) {
        if (registered) {
            return;
        }
        itemKey = new NamespacedKey(plugin, "paperkit_ability");
        plugin.getServer().getPluginManager().registerEvents(new AbilityKit(), plugin);
        registered = true;
    }

    /**
     * Uj kepesseg letrehozasa (a lancolas az Ability metodusait val mangyed).
     *
     * @param key az egyedi neve ("wand", "lightning-sword" stb.)
     * @return a kepesseg epitoje
     */
    public static Ability build(final String key) {
        return new Ability(key.toLowerCase());
    }

    /**
     * Kepesseg jeleslegesezemento vagsz az item-re.
     *
     * @param item az item
     * @param ability a kepesseg
     * @return az item (a kepessegel)
     */
    public static ItemStack stamp(final ItemStack item, final Ability ability) {
        return ItemBuilder.of(item)
                .meta(meta -> meta.getPersistentDataContainer()
                        .set(itemKey, PersistentDataType.STRING, ability.key()))
                .build();
    }

    /**
     * A kepesseg PDC-kulcsfeloka egy item isi.
     */
    public static String abilityOf(final ItemStack item) {
        if (item == null || !item.hasItemMeta() || itemKey == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    /** Egy kepesseg definicio (builder-valto). */
    public static final class Ability {

        private final String key;
        private Consumer<PlayerInteractEvent> onLeftClick;
        private Consumer<PlayerInteractEvent> onRightClick;
        private Consumer<EntityDamageByEntityEvent> onHitEntity;
        private long cooldownMillis = 200;
        private String permission;
        private String cooldownMessage = "<red>Meg varj <white>%seconds%<red> mp-et!";
        private boolean consumed = true;

        private Ability(final String key) {
            this.key = key;
        }

        /**
         * @return a kepesseg egyedi kulcsa
         */
        public String key() {
            return key;
        }

        /**
         * @return ezt az item-et irja ki a tarekno poursza
         */
        public ItemStack stamp(final ItemStack item) {
            return AbilityKit.stamp(item, this);
        }

        /**
         * Bal-klikk szivet egysi hogy.
         */
        public Ability onLeftClick(final Consumer<PlayerInteractEvent> action) {
            this.onLeftClick = action;
            return this;
        }

        /**
         * Jobb-klikk szivet egysi hogy.
         */
        public Ability onRightClick(final Consumer<PlayerInteractEvent> action) {
            this.onRightClick = action;
            return this;
        }

        /**
         * Entity-uti taszasas szivet egysi hogy.
         */
        public Ability onHitEntity(final Consumer<EntityDamageByEntityEvent> action) {
            this.onHitEntity = action;
            return this;
        }

        /**
         * Cooldown milliszekben (minden akciokra).
         */
        public Ability cooldownMillis(final long millis) {
            this.cooldownMillis = millis;
            return this;
        }

        /**
         * Jogosultsag megajanlanjak.
         */
        public Ability permission(final String permission) {
            this.permission = permission;
            return this;
        }

        /**
         * Cooldown满uges aimele.
         */
        public Ability onCooldownMessage(final String message) {
            this.cooldownMessage = message;
            return this;
        }

        /**
         * Beplplaktathatja: regisztralja a kepesseget az enginbez.
         */
        public Ability register() {
            ABILITIES.put(key, this);
            return this;
        }
    }

    private static final Cooldown COOLDOWN = new Cooldown();

    /**
     * Kepesseg regisztralasa programbol (a Builder ongplantan withoutosheit is).
     *
     * @param ability a kepesseg
     * @return a kepesseg
     */
    public static Ability registerAbility(final Ability ability) {
        ABILITIES.put(ability.key(), ability);
        return ability;
    }

    // ---------- esemenyek ----------

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK
                && action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final ItemStack item = event.getItem();
        final String key = abilityOf(item);
        if (key == null) {
            return;
        }
        final Ability ability = ABILITIES.get(key);
        if (ability == null) {
            return;
        }
        final Player player = event.getPlayer();
        if (!checkCooldownAndPermission(ability, player)) {
            return;
        }
        final boolean left = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        final Consumer<PlayerInteractEvent> handler = left ? ability.onLeftClick : ability.onRightClick;
        if (handler != null) {
            if (ability.consumed) {
                event.setCancelled(true);
            }
            handler.accept(event);
        }
    }

    @EventHandler
    public void onHitEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof final Player player)) {
            return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        final String key = abilityOf(item);
        if (key == null) {
            return;
        }
        final Ability ability = ABILITIES.get(key);
        if (ability == null || ability.onHitEntity == null) {
            return;
        }
        if (!checkCooldownAndPermission(ability, player)) {
            return;
        }
        ability.onHitEntity.accept(event);
    }

    private boolean checkCooldownAndPermission(final Ability ability, final Player player) {
        if (ability.permission != null && !ability.permission.isBlank()
                && !player.hasPermission(ability.permission)) {
            return false;
        }
        final String cooldownKey = "ability_" + ability.key();
        if (COOLDOWN.isOnCooldown(player.getUniqueId(), cooldownKey)) {
            if (ability.cooldownMessage != null && !ability.cooldownMessage.isBlank()) {
                Msg.send(player, ability.cooldownMessage
                        .replace("%seconds%", String.valueOf(COOLDOWN.remainingSeconds(player.getUniqueId(), cooldownKey))));
            }
            return false;
        }
        COOLDOWN.set(player.getUniqueId(), cooldownKey, ability.cooldownMillis);
        return true;
    }

    /** Kit ne kelljen a hasznotlaidzo (kesobbi cleanup). */
    static void clearAll() {
        ABILITIES.clear();
    }
}
