package hu.paperkit.display;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import hu.paperkit.core.Kit;

/**
 * Simple, sorban allo (keyframe alapu) animacio display entitasokra.
 * A lpsinger a szerver beepitett interpolation-jat hasznalja, igy egy
 * lepe eszes correr a kovetkezo frame k?z?tt l?tsz?rdosan mozog.
 *
 * <pre>{@code
 * DisplayAnimation.animate(itemDisplay)
 *         .scale(1.5f, 15)          // 15 tick alatt nagyit 1.5-szeresre
 *         .rotateYLeft(90, 30)      // forogatra
 *         .pause(20)
 *         .reset(10)
 *         .loop()
 *         .play();
 * }</pre>
 */
public final class DisplayAnimation {

    private record Step(Runnable frame, int delayTicks) {
    }

    private final Display target;
    private final Deque<Step> baseSteps = new ArrayDeque<>();
    private final boolean loop;
    private BukkitTask task;
    private volatile boolean playing;
    private Deque<Step> currentQueue;
    private int pendingTicks;

    private DisplayAnimation(final Display target, final boolean loop) {
        this.target = target;
        this.loop = loop;
    }

    /**
     * Animacio a megadott display-re.
     */
    public static DisplayAnimation animate(final Display display) {
        return new DisplayAnimation(display, false);
    }

    /**
     * Animacio a megadott display-re, vegtelen ismetlessel.
     */
    public static DisplayAnimation looping(final Display display) {
        return new DisplayAnimation(display, true);
    }

    // ---------------------------- step gyartok -----------------------------

    /**
     * Egyseges (skala) lepes.
     *
     * @param scale  a cel skala
     * @param ticks  a lepes hossza (interpolacio)
     */
    public DisplayAnimation scale(final float scale, final int ticks) {
        return step(() -> transformTo(
                translationOf(),
                leftRotationOf(),
                new Vector3f(scale, scale, scale),
                rightRotationOf(), ticks), ticks);
    }

    /**
     * Eltolasi lepes (blokkban).
     */
    public DisplayAnimation translate(final float dx, final float dy, final float dz, final int ticks) {
        return step(() -> transformTo(
                new Vector3f(translationOf()).add(dx, dy, dz),
                leftRotationOf(),
                scaleOf(),
                rightRotationOf(), ticks), ticks);
    }

    /**
     * Veszend? (forgasi) lepes Y tengelyen, fokban.
     */
    public DisplayAnimation rotateYLeft(final float degrees, final int ticks) {
        return step(() -> {
            final float radians = (float) Math.toRadians(degrees);
            final Quaternionf current = new Quaternionf().set(leftRotationOf());
            current.rotateY(radians);
            transformTo(
                    translationOf(),
                    new AxisAngle4f().set(current),
                    scaleOf(),
                    rightRotationOf(), ticks);
        }, ticks);
    }

    /**
     * Kezdeti allapot visszaallitasa.
     */
    public DisplayAnimation reset(final int ticks) {
        return step(() -> transformTo(
                new Vector3f(0f, 0f, 0f),
                new AxisAngle4f(0f, 0f, 1f, 0f),
                new Vector3f(1f, 1f, 1f),
                new AxisAngle4f(0f, 0f, 1f, 0f), ticks), ticks);
    }

    /**
     * Pausing (tick szcsol? kiad?sa) a k?vetkez? l?p?ig.
     */
    public DisplayAnimation pause(final int ticks) {
        return step(() -> {
        }, Math.max(0, ticks));
    }

    /**
     * Tetsz?leges muvelet futtatsa l?pe?sk?k?zben (pl. hangj?r?ts?k).
     */
    public DisplayAnimation then(final Runnable action) {
        return step(action, 0);
    }

    private DisplayAnimation step(final Runnable frame, final int delayTicks) {
        baseSteps.addLast(new Step(frame, Math.max(0, delayTicks)));
        return this;
    }

    // ---------------------------- k?l?n?bz? er?forr?sok ----------------------------

    /**
     * Az animacio elinditasa.
     */
    public void play() {
        stop();
        currentQueue = new ArrayDeque<>(baseSteps);
        playing = true;
        nextStep();
    }

    /**
     * Az animacio megallitasa.
     */
    public void stop() {
        playing = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @return fut-e az animacio
     */
    public boolean isPlaying() {
        return playing;
    }

    private void nextStep() {
        if (!playing || target == null) {
            return;
        }
        if (target.isDead()) {
            stop();
            return;
        }
        final Step step = currentQueue.pollFirst();
        if (step == null) {
            if (loop && !baseSteps.isEmpty()) {
                currentQueue = new ArrayDeque<>(baseSteps);
                nextStep();
            } else {
                stop();
            }
            return;
        }
        try {
            step.frame().run();
        } catch (final Exception e) {
            Kit.plugin().getLogger().log(Level.WARNING, "Display animacio hiba", e);
        }
        scheduleNext(step.delayTicks());
    }

    private void scheduleNext(final int delayTicks) {
        final long delay = Math.max(1, delayTicks);
        task = Bukkit.getScheduler().runTaskLater(Kit.plugin(), this::nextStep, delay);
    }

    private void transformTo(
            final Vector3f translation,
            final AxisAngle4f leftRotation,
            final Vector3f scale,
            final AxisAngle4f rightRotation,
            final int ticks) {
        final Transformation t = new Transformation(translation, leftRotation, scale, rightRotation);
        target.setTransformation(t);
        target.setInterpolationDelay(0);
        target.setInterpolationDuration(Math.max(1, ticks));
    }

    private Vector3f translationOf() {
        final Transformation t = target.getTransformation();
        return new Vector3f(t.getTranslation());
    }

    private AxisAngle4f leftRotationOf() {
        return new AxisAngle4f(target.getTransformation().getLeftRotation());
    }

    private AxisAngle4f rightRotationOf() {
        return new AxisAngle4f(target.getTransformation().getRightRotation());
    }

    private Vector3f scaleOf() {
        return new Vector3f(target.getTransformation().getScale());
    }

    /**
     * A display billboard ha lehzall -kd regalo klty kesztuds.
     */
    public DisplayAnimation billboard(Billboard billboard) {
        target.setBillboard(billboard);
        return this;
    }
}
