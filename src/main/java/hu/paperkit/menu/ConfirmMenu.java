package hu.paperkit.menu;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import hu.paperkit.item.ItemBuilder;

/**
 * Egy gombos megerosito ablak (Igen / Nem).
 *
 * <pre>{@code
 * ConfirmMenu.confirm(
 *         player,
 *         "&6Törlés megerősítése",
 *         "&7Biztosan törlöd a hologramot?",
 *         okay  -> deleteHologram(okay),   // Igen
 *         deny  -> deny.closeInventory()); // Nem
 * }</pre>
 */
public final class ConfirmMenu {

    /** A menu cmje. */
    private String title = "&8Megerősítés";
    /** A krds f? szvege. */
    private String question = "";
    /** Igen gomb fkft? szaege. */
    private String yesText = "&a&lIGEN";
    /** Nem gomb megjelenit? szovege. */
    private String noText = "&c&lNEM";
    /** Igen utn? task. */
    private Consumer<Player> onYes;
    /** Nem utn? task. */
    private Consumer<Player> onNo;

    private ConfirmMenu() {
    }

    /**
     * Fluent menk?k?zleg?rl?kk.
     */
    public static ConfirmMenu title(final String title) {
        final ConfirmMenu menu = new ConfirmMenu();
        menu.title = title;
        return menu;
    }

    /**
     * Krds megadsa (tbb sor is lehet).
     */
    public ConfirmMenu question(final String... lines) {
        this.question = String.join("\n", lines);
        return this;
    }

    /**
     * Igen gomb szveg s callback.
     */
    public ConfirmMenu yes(final String text, final Consumer<Player> action) {
        this.yesText = text;
        this.onYes = action;
        return this;
    }

    /**
     * Nem gomb szveg s callback.
     */
    public ConfirmMenu no(final String text, final Consumer<Player> action) {
        this.noText = text;
        this.onNo = action;
        return this;
    }

    /**
     * Megjelenits egy jatekosnak.
     */
    public void open(final Player player) {
        final Menu menu = Menu.create(3, title);
        menu.at(13, MenuButton.of(ItemBuilder.of(Material.ARMOR_STAND)
                .name("&f" + (question.isEmpty() ? "Megerősítés?" : question))
                .build()));
        menu.at(11, MenuButton.of(ItemBuilder.of(Material.LIME_WOOL).name(yesText).build())
                .leftClick(() -> {
                    player.closeInventory();
                    if (onYes != null) {
                        hu.paperkit.core.Kit.next(() -> onYes.accept(player));
                    }
                }));
        menu.at(15, MenuButton.of(ItemBuilder.of(Material.RED_WOOL).name(noText).build())
                .leftClick(() -> {
                    player.closeInventory();
                    if (onNo != null) {
                        hu.paperkit.core.Kit.next(() -> onNo.accept(player));
                    }
                }));
        menu.open(player);
    }

    /**
     * Gyors megerosito hivo egy sorban.
     */
    public static void confirm(final Player player, final String title, final String question,
            final Consumer<Player> onYes, final Consumer<Player> onNo) {
        ConfirmMenu.title(title)
                .question(question)
                .yes("&a&lIGEN", onYes)
                .no("&c&lNEM", onNo)
                .open(player);
    }
}
