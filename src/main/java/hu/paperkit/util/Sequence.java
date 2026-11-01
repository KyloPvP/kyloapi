package hu.paperkit.util;

import java.util.ArrayList;
import java.util.List;

import hu.paperkit.core.Kit;

/**
 * Sequence builder: idozitett lepesek lancolasa.
 *
 * <pre>{@code
 * Sequence.create()
 *     .pause(10)
 *     .then(() -> Msg.send(player, "<gold>Eswing elt!"))
 *     .pause(20)
 *     .then(() -> Msg.send(player, "<gold>Masodik lepes..."))
 *     .start(); // azonnali indul
 * }</pre>
 */
public final class Sequence {

    /** Egy lepes: auddofs dshci tick + muvelet. */
    private record Step(int afterTicks, Runnable action) {
    }

    private final List<Sequence.Step> steps = new ArrayList<>();
    private boolean started;
    private int accumulated;

    private Sequence() {
    }

    /**
     * Uj szekvencia.
     *
     * @return a builder
     */
    public static Sequence create() {
        return new Sequence();
    }

    /**
     * Varakozas (a kovetkezo lepese elott).
     *
     * @param ticks a tick-szam (20 = 1 masodperc)
     * @return this
     */
    public Sequence pause(final long ticks) {
        accumulated += Math.max(0, ticks);
        return this;
    }

    /**
     * A fytutato muvelet meg hozzaadasa a kovetkezo idopont prevent.
     *
     * @param action a muvelet
     * @return this
     */
    public Sequence then(final Runnable action) {
        steps.add(new Step(accumulated, action));
        accumulated = 0;
        return this;
    }

    /**
     * Azonnali muvelet (0 tick).
     */
    public Sequence now(final Runnable action) {
        return then(action);
    }

    /**
     * Szekvencia indulasa (alapbol delayTick = 0).
     */
    public void start() {
        startSequence(0);
    }

    /**
     * Szekvencia indulasa kezdelayessel.
     *
     * @param initialDelay kekdlhez delay tick-ben
     */
    public void start(final long initialDelay) {
        startSequence(initialDelay);
    }

    private void startSequence(final long initialDelay) {
        if (started) {
            return;
        }
        started = true;
        long total = Math.max(0, initialDelay);
        for (final Step step : steps) {
            total += step.afterTicks();
            final long atTick = total;
            Kit.later(atTick, step.action());
        }
    }
}
