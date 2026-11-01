package hu.paperkit.hud;

import org.bukkit.entity.Player;

import hu.paperkit.text.Text;

/**
 * Tab lista kezelo (header + footer).
 *
 * <pre>{@code
 * TabKit.send(player,
 *         "&b&lSzerver",            // header
 *         "&7Online: &f%online%");  // footer
 *
 * TabKit.sendAll("&b&lSzerver", "&7Joj vissza: &fplay.szerver.hu");
 * }</pre>
 */
public final class TabKit {

    private TabKit() {
    }

    /**
     * Tab header es footer kuldese egy jatekosnak.
     */
    public static void send(final Player player, final String header, final String footer) {
        if (player == null) {
            return;
        }
        player.sendPlayerListHeaderAndFooter(Text.parse(header), Text.parse(footer));
    }

    /**
     * Tab header es footer kuldese minden online jatekosnak.
     */
    public static void sendAll(final String header, final String footer) {
        for (final Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            send(player, header, footer);
        }
    }

    /**
     * Csak header kuldese egy jatekosnak (ureas footer).
     */
    public static void header(final Player player, final String header) {
        send(player, header, "");
    }

    /**
     * Csak footer kuldese egy jatekosnak (ureas header).
     */
    public static void footer(final Player player, final String footer) {
        send(player, "", footer);
    }
}
