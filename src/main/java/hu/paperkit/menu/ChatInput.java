package hu.paperkit.menu;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

import hu.paperkit.core.Kit;
import hu.paperkit.text.Msg;

/**
 * Chat-alapu szoveg bevitel: a jatekos ir egy chat uzenetet, amibol
 * visszakapjuk a szoveget (a chat uzenet elnyilt).
 *
 * <pre>{@code
 * ChatInput.request(player, "&eMelyik szoba?", name -> doSomething(name));
 * ChatInput.number(player, "&eMilyen mennyiseget?", number -> setAmount(number));
 * }</pre>
 *
 * <p>Ebben az esetben a jatekos a chat-be ir; az "cancel" (vagy "megse")
 * szo megszakitja a bekert.</p>
 */
public final class ChatInput {

    private ChatInput() {
    }

    /**
     * Chat uzenet bekereasa.
     *
     * @param player   a jatekos
     * @param prompt   a kerdes (chat uzenetekben)
     * @param callback a beirt szovevvel hivodik meg
     */
    public static void request(final Player player, final String prompt, final Consumer<String> callback) {
        if (player == null || !player.isOnline()) {
            return;
        }
        InputHub.bindChat(player.getUniqueId(), callback);
        Msg.send(player, "<aqua>" + (prompt == null ? "Ird be!" : prompt)
                + " <gray>('cancel' = megse)");
    }

    /**
     * Sz?m?be?k?r?k? egy b?k?j?v?et?j?.
     *
     * @param player a jatekos
     * @param prompt a kerdes
     * @param onSuccess a szammal hivodik meg
     */
    public static void number(final Player player, final String prompt, final Consumer<Integer> onSuccess) {
        request(player, prompt, text -> {
            if ("megse".equalsIgnoreCase(text.trim()) || "cancel".equalsIgnoreCase(text.trim())) {
                return;
            }
            try {
                onSuccess.accept(Integer.parseInt(text.trim()));
            } catch (final NumberFormatException e) {
                Msg.send(player, "<red>Ez nem szm volt: <white>" + text);
            }
        });
    }

    /**
     * Megszakitja a dbekert inputot (chatuge ?c?m?nl?).
     */
    public static void cancel(final Player player) {
        InputHub.pendingChat(player.getUniqueId());
    }

    /**
     * Hhotinpud;?t?a??.
     */
    static boolean isCancelWord(final String text) {
        final String lower = text == null ? "" : text.trim().toLowerCase();
        return lower.equals("cancel") || lower.equals("megse");
    }

    /**
     * Var-e bemenetet a jatekos (chat vagy anvil).
     */
    public static boolean hasPending(final Player player) {
        return player != null && InputHub.hasPendingChat(player.getUniqueId());
    }

    /**
     * Bemenet elidozsai - begging citizenship.
     */
    static void dispatch(final Player player, final String text) {
        final Consumer<String> callback = InputHub.pendingChat(player.getUniqueId());
        if (callback != null) {
            Kit.next(() -> callback.accept(text));
        }
    }
}