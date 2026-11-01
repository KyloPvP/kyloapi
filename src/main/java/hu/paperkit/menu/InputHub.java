package hu.paperkit.menu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A bemenet-kapolshoz (anvil es chat adatok) k?zponti? nyilv?ntart?s.
 * Az {@code Kit.init()} regisztrlja az {@link InputListener}-t, ami ezt
 * hasznlja az esemnykezelhet? k?tszer.
 */
public final class InputHub {

    /** Chat altal varo callbackek (szoveg iras). */
    private static final Map<UUID, Consumer<String>> CHAT = new ConcurrentHashMap<>();
    /** Anvil (ItemAtnevezo) callbackek. */
    private static final Map<UUID, Consumer<String>> ANVIL = new ConcurrentHashMap<>();

    private InputHub() {
    }

    /**
     * Chat-alapu bemenet beJegyz?se.
     */
    static void bindChat(final UUID player, final Consumer<String> callback) {
        CHAT.put(player, callback);
    }

    /**
     * Anvil-alapu bemenet bejegyzese.
     */
    static void bindAnvil(final UUID player, final Consumer<String> callback) {
        ANVIL.put(player, callback);
    }

    /**
     * Chat bemenet lekerese (es torles).
     *
     * @return a callback vagy null ha nincs fuggo bemenet
     */
    static Consumer<String> pendingChat(final UUID player) {
        return CHAT.remove(player);
    }

    /**
     * Van-e fgg? helt?l?sz?meg?ad?as (chat bemenet) a j?tkosnak.
     */
    static boolean hasPendingChat(final UUID player) {
        return CHAT.containsKey(player);
    }

    /**
     * Anvil bemenet lekerdese (es torles).
     */
    static Consumer<String> pendingAnvil(final UUID player) {
        return ANVIL.remove(player);
    }

    /**
     * Minden fele?eg?bb?n? bon?j?v? j�t?kos? (pl. disconnect) f?lab?ra?rl?.
     */
    public static void clear(final UUID player) {
        CHAT.remove(player);
        ANVIL.remove(player);
    }
}
