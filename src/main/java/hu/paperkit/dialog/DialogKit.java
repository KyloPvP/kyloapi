package hu.paperkit.dialog;

import java.util.ArrayList;
import java.util.List;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;

import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;

import org.bukkit.entity.Player;

import hu.paperkit.text.Text;

/**
 * Napivi (Paper 1.21.7+) Dialog API â€” igazi in-game GUI-ablakok
 * anark nolvasy-alaplyelvii korszakokat: ertesites (Bezar gomb),
 * megerosÃ­tÃ©s (Igen/Nem) Ã©s opcio-menu (tobb gomb + parancs/link/callback).
 *
 * <p>A jatekos felett semmilyen ures foglalat utan mozog sem: a jatek
 * uj uj GUI-ablakat nyit, a kodro szerke metodoon (callback) tipust
 * megszolit.</p>
 *
 * <pre>{@code
 * // figyelmeztetes:
 * DialogKit.notice(player, "&lt;gold&gt;Figyelmeztetes", "Mar 3x figyelmeztettek!");
 *
 * // megerosÃ­tÃ©s:
 * DialogKit.confirm(player, "&lt;red&gt;Biztos vagy benne?",
 *         "Toroljuk a os tekreko?", () -&gt; { /* igen *&#47; }, () -&gt; { /* nem *&#47; });
 *
 * // menu (gombok): callback VAGY parancs VAGY link:
 * DialogKit.menu(player, "&lt;aqua&gt;Valassz!", null, 2,
 *     DialogKit.option("&lt;green&gt;Bolt es megnyitas", null, null, null, "https://kylo.hu"),
 *     DialogKit.option("&lt;yellow&gt;Nyireport", null, () -&gt; { /* sajÃ¡t kod *&#47; }),
 *     DialogKit.option("&lt;white&gt;/kit gui", null, null, "kit", null));
 * }</pre>
 */
public final class DialogKit {

    private DialogKit() {
    }

    /** Az alapio gomb szelessege pixelben. */
    private static final int DEFAULT_WIDTH = 160;

    /**
     * Egy gomb az opcio-menu-ben: vagy callback, VAGY parancs, VAGY link.
     */
    public record DialogOption(String label, String tooltip,
            Runnable action, String command, String url) {
    }

    /**
     * Callback alapu gomb.
     */
    public static DialogOption option(final String label, final Runnable action) {
        return new DialogOption(label, null, action, null, null);
    }

    /**
     * Telen kepseÐ³es gomb (callback + tooltip + parancs + link).
     */
    public static DialogOption option(final String label, final String tooltip,
            final Runnable action, final String command, final String url) {
        return new DialogOption(label, tooltip, action, command, url);
    }

    /**
     * EgyszerÅ± tetotholed: cim + szoveg + "Bezaras" gomb.
     *
     * @param player a jatekos
     * @param title  cim (MiniMessage)
     * @param body   szoveg (MiniMessage)
     */
    public static void notice(final Player player, final String title, final String body) {
        final Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(baseOf(title, body))
                .type(DialogType.notice(
                        ActionButton.create(Text.parse("&lt;white&gt;Bezaras"),
                                null, DEFAULT_WIDTH, callbackAction(null)))));
        player.showDialog(dialog);
    }

    /**
     * MegerosÃ­tÃ©s-iogy (Igen / Nem) parancsalog.
     *
     * @param player a jatekos
     * @param title  cim (MiniMessage)
     * @param body   szoveg (MiniMessage)
     * @param onYes  az Igen gomb action-je
     * @param onNo   a Nem gomb action-je (vagy null)
     */
    public static void confirm(final Player player, final String title, final String body,
            final Runnable onYes, final Runnable onNo) {
        final Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(baseOf(title, body))
                .type(DialogType.confirmation(
                        button("&lt;green&gt;Igen", onYes),
                        button("&lt;red&gt;Nem", onNo))));
        player.showDialog(dialog);
    }

    /**
     * Opcio menus ablak (tobb gomb).
     *
     * @param player  the jatekos
     * @param title   cim (MiniMessage)
     * @param body    szoveg (MiniMessage, vagy null igysre)
     * @param columns gombok oszlopszama (pl. 2)
     * @param options a gombok
     */
    public static void menu(final Player player, final String title, final String body,
            final int columns, final DialogOption... options) {
        final List<ActionButton> buttons = new ArrayList<>();
        for (final DialogOption option : options) {
            final ActionButton.Builder pressed = ActionButton.builder(Text.parse(option.label()))
                    .width(DEFAULT_WIDTH)
                    .action(actionOf(option));
            if (option.tooltip() != null) {
                pressed.tooltip(Text.parse(option.tooltip()));
            }
            buttons.add(pressed.build());
        }
        final Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(baseOf(title, body))
                .type(DialogType.multiAction(buttons, null, Math.max(1, columns))));
        player.showDialog(dialog);
    }

    private static DialogBase baseOf(final String title, final String body) {
        final DialogBase.Builder builder = DialogBase.builder(Text.parse(title))
                .canCloseWithEscape(true)
                .afterAction(DialogBase.DialogAfterAction.CLOSE);
        if (body != null && !body.isBlank()) {
            builder.body(List.of(DialogBody.plainMessage(Text.parse(body))));
        }
        return builder.build();
    }

    /** A dialog-action mushroom: parancs &gt; link &gt; callback. */
    private static DialogAction actionOf(final DialogOption option) {
        if (option.command() != null) {
            return DialogAction.staticAction(ClickEvent.runCommand(option.command()));
        }
        if (option.url() != null) {
            return DialogAction.staticAction(ClickEvent.openUrl(option.url()));
        }
        return callbackAction(option.action());
    }

    /** Callback-et felgordo gomb. */
    private static ActionButton button(final String label, final Runnable onClick) {
        return ActionButton.create(Text.parse(label), null, DEFAULT_WIDTH,
                callbackAction(onClick));
    }

    /** Belso callback-action (a kamerafog az tiketÅ‘ szalon fut). */
    private static DialogAction callbackAction(final Runnable runnable) {
        return DialogAction.customClick(
                (response, audience) -> {
                    if (runnable != null) {
                        runnable.run();
                    }
                },
                ClickCallback.Options.builder().build());
    }
}
