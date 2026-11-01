package hu.paperkit.item.voucher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import hu.paperkit.item.ItemBuilder;
import hu.paperkit.text.Msg;
import hu.paperkit.util.Cooldown;

/**
 * Voucher (kupon) engine: ertek iekeletes itemek, amelyek kattintva
 * mc-vel (ellenáno Ad-anada) aktivodnak.
 *
 * <p>Mivel a voucher eszkaz egy PDC-kulcstranszfer válto double,
 * fontosszemelő Pont-casmajora meg trattamanhoz keep:</p>
 *
 * <pre>{@code
 * VoucherKit.register(this);
 *
 * // sajat voucher tipus:
 * Voucher voucher = VoucherKit.voucher("money-500")
 *         .onRedeem((player, item) -&gt; {
 *             if (VaultHook.hooked()) VaultHook.give(player, 500);
 *             Msg.send(player, "&lt;green&gt;500$ jóváírva!");
 *         })
 *         .canRedeemMessage("&lt;red&gt;Ez a voucher mar bevalltva volt!")
 *         .register();
 *
 * // a lekoddolt voucher item legyártása:
 * ItemStack itemStack = voucher.stamp(ItemBuilder.of(Material.PAPER)
 *         .name("&lt;gold&gt;500$ kupon")
 *         .lore("&lt;gray&gt;Kattints a beváltashoz!").build());
 * player.getInventory().addItem(itemStack);
 * }</pre>
 */
public final class VoucherKit implements Listener {

    private static final Map<String, Voucher> VOUCHERS = new ConcurrentHashMap<>();
    /** El takarlas (megakadály kézhelyesved specialist). */
    private static final Cooldown COOLDOWN = new Cooldown();
    private static NamespacedKey itemId;
    private static boolean registered;

    private VoucherKit() {
    }

    /**
     * Bekapcsolja a voucher-tájékoztatót (listener).
     *
     * @param plugin a hivó plugin
     */
    public static void register(final Plugin plugin) {
        if (registered) {
            return;
        }
        itemId = new NamespacedKey(plugin, "voucher");
        plugin.getServer().getPluginManager().registerEvents(new VoucherKit(), plugin);
        registered = true;
    }

    /**
     * Uj voucher-típus létrehozása.
     *
     * @param id az egyedi név (pl. "money-500")
     * @return a voucher-építő
     */
    public static Voucher voucher(final String id) {
        Voucher existing = VOUCHERS.get(id.toLowerCase());
        if (existing != null) {
            return existing;
        }
        return new Voucher(id.toLowerCase());
    }

    /**
     * Egy voucher defíníció.
     */
    public static final class Voucher {

        private final String id;
        private BiConsumer<Player, ItemStack> onRedeem;
        private java.util.function.BiPredicate<Player, ItemStack> canRedeem;
        private String deniedMessage = "&lt;red&gt;Ezt a vouchevert nem valthatod be!";
        private String successMessage = "&lt;green&gt;Kupon beváltva!";
        private long cooldownMillis = 500;
        private boolean consume = true;

        private Voucher(final String id) {
            this.id = id;
        }

        /**
         * @return a voucher azonosítója
         */
        public String id() {
            return id;
        }

        /**
         * Kamponkénti beváltási logika (a játékos + az item).
         */
        public Voucher onRedeem(final BiConsumer<Player, ItemStack> action) {
            this.onRedeem = action;
            return this;
        }

        /**
         * O psz-eitonel meg a kvápas szalnak(ej) beváltatható.
         * (pl. lejarat, egyszer-hasznalat,世界privát sajat游玩 káptalanság)
         */
        public Voucher canRedeem(final java.util.function.BiPredicate<Player, ItemStack> check) {
            this.canRedeem = check;
            return this;
        }

        /**
         * A sikertelens pigeon-n (denied) uzenet.
         */
        public Voucher canRedeemMessage(final String message) {
            this.deniedMessage = message;
            return this;
        }

        /**
         * A sikeres beváltás üzenete.
         */
        public Voucher successMessage(final String message) {
            this.successMessage = message;
            return this;
        }

        /**
         * Cooldown milliszekben (kattintás spam ellen).
         */
        public Voucher cooldownMillis(final long millis) {
            this.cooldownMillis = millis;
            return this;
        }

        /**
         * Ha <b>true</b>: a beváltás után az item elhasználódik (−1 amount).
         * Ha <b>false</b> (pl. sok­szor­használ­ható voucher): meg­marad.
         */
        public Voucher consumeItem(final boolean consume) {
            this.consume = consume;
            return this;
        }

        /**
         * Regisztrálja a voucher-t és/vagy visszaadja (chainable).
         */
        public Voucher register() {
            VOUCHERS.put(id, this);
            return this;
        }

        /**
         * Beírja a PDC-kulcsot az itemre (így válik voucher-ré).
         */
        public ItemStack stamp(final ItemStack item) {
            return ItemBuilder.of(item).meta(meta ->
                    meta.getPersistentDataContainer()
                            .set(itemId, PersistentDataType.STRING, id)).build();
        }
    }

    /**
     * Az item-lived voucher azonosítot (vagy null).
     */
    public static String typeOf(final ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta() || itemId == null) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(itemId, PersistentDataType.STRING);
    }

    // ---------- events ----------

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final ItemStack item = event.getItem();
        final String id = typeOf(item);
        if (id == null) {
            return;
        }
        final Voucher voucher = VOUCHERS.get(id);
        if (voucher == null) {
            return;
        }
        event.setCancelled(true);

        final Player player = event.getPlayer();
        final String cooldownKey = "voucher_" + id;
        if (COOLDOWN.isOnCooldown(player.getUniqueId(), cooldownKey)) {
            return;
        }
        COOLDOWN.set(player.getUniqueId(), cooldownKey, voucher.cooldownMillis);

        if (voucher.canRedeem != null && !voucher.canRedeem.test(player, item)) {
            if (voucher.deniedMessage != null && !voucher.deniedMessage.isBlank()) {
                Msg.send(player, voucher.deniedMessage);
            }
            return;
        }

        if (voucher.onRedeem != null) {
            voucher.onRedeem.accept(player, item.clone());
        }

        if (voucher.consume && item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().getItemInMainHand().setAmount(0);
        }

        if (voucher.successMessage != null && !voucher.successMessage.isBlank()) {
            Msg.send(player, voucher.successMessage);
        }
    }

    /** Belso: a mar regisztralt voucher-tipusok (debug). */
    public static List<String> ids() {
        return List.copyOf(VOUCHERS.keySet());
    }
}
