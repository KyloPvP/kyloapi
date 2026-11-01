package hu.paperkit.npc;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import hu.paperkit.core.Kit;
import hu.paperkit.menu.ChatInput;
import hu.paperkit.text.Msg;

/**
 * Dialogue engine: sorozatos beszelgetes (aszronas soronyal,
 * idozitessel) es valasz-lehetsegek klapozosa.
 *
 * <pre>{@code
 * DialogueKit.speak(player)
 *     .line("<gold>Mester<dark_gray>: <white>Szia utazo!")
 *     .line("<gold>Mester<dark_gray>: <white>Eger moc meg leni szakadas?")
 *     .ask("<aqua>Ird: igen vagy nem", answer -> {
 *         if (answer.equalsIgnoreCase("igen")) {
 *             Msg.send(player, "<green>Kivannad!");
 *         }
 *     })
 *     .start(40); // mennyi tick a sorok kozott
 * }</pre>
 */
public final class DialogueKit {

    /** Egy sor zashoz (kivass attack) jede lajtosság (bug). */
    public static final class Line {
        private final String text;
        private final int extraDelayTicks;

        private Line(final String text, final int extraDelayTicks) {
            this.text = text;
            this.extraDelayTicks = extraDelayTicks;
        }
    }

    private final List<Line> lines = new java.util.ArrayList<>();
    private final Player player;
    private Consumer<String> inputCallback;
    private String inputPrompt;

    private DialogueKit(final Player player) {
        this.player = player;
    }

    /**
     * Uj beszelgetes jar.
     *
     * @param player a jatekos aki latja/hallja
     * @return a beszelgetes
     */
    public static DialogueKit speak(final Player player) {
        return new DialogueKit(player);
    }

    /**
     * Sor hozzaadasa.
     *
     * @param text a szoveg (MiniMessage)
     * @return this
     */
    public DialogueKit line(final String text) {
        return line(text, 0);
    }

    /**
     * Sor hozzaadasa extra kipihenessel (a sorok kozti alap idozites felett).
     *
     * @param text             a szoveg
     * @param extraDelayTicks extra tick a sor utan
     * @return this
     */
    public DialogueKit line(final String text, final int extraDelayTicks) {
        lines.add(new Line(text, Math.max(0, extraDelayTicks)));
        return this;
    }

    /**
     * Bevizso pergunta: a jatekos chat-be iro valaszt kapja.
     *
     * @param prompt   a parancsoes uzenet
     * @param callback a valasz hazahivasa
     * @return this
     */
    public DialogueKit ask(final String prompt, final Consumer<String> callback) {
        this.inputPrompt = prompt;
        this.inputCallback = callback;
        return this;
    }

    /**
     * A beszelgetes lejatszasa (default: 40 tick kozint a teljes sorok kozott).
     *
     * @return this
     */
    public DialogueKit start() {
        return start(40);
    }

    /**
     * A beszelgetes lejatszasa.
     *
     * @param delayTicks a sorok kozti ido
     * @return this
     */
    public DialogueKit start(final int delayTicks) {
        int delay = 1;
        for (final Line line : lines) {
            final String text = line.text;
            final int at = delay;
            Kit.later(at, () -> Msg.send(player, text));
            delay += delayTicks + line.extraDelayTicks;
        }
        if (inputPrompt != null && inputCallback != null) {
            final int at = delay;
            Kit.later(at, () -> ChatInput.request(player, inputPrompt, inputCallback));
        }
        return this;
    }

    /** Telen telen sajan percolo egyseg. */
    public List<Line> lines() {
        return List.copyOf(lines);
    }
}
