package hu.paperkit.display;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import hu.paperkit.text.Text;

/**
 * Text display entitas pLETESI builder (a megjelenitese a vilagban).
 *
 * <pre>{@code
 * TextDisplay disp = TextDisplayBuilder.at(loc)
 *         .text("&b&lHologram")
 *         .billboard(Display.Billboard.CENTER)
 *         .background(Color.fromARGB(0x66000000))
 *         .scale(1.2f)
 *         .spawn();
 *
 * kesobbi frissites: disp.setText(Text.parse("..."));
 * }</pre>
 */
public final class TextDisplayBuilder {

    private final Location location;
    private String text = "";
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private Color background = Color.fromARGB(0x40000000);
    private int lineWidth = 200;
    private boolean seeThrough;
    private boolean shadowed = true;
    private boolean defaultBackground;
    private boolean glow;
    private Color glowColor = Color.fromRGB(0, 181, 255);
    private float scaleX = 1f, scaleY = 1f, scaleZ = 1f;
    private float tx, ty, tz;
    private float rotationDegrees;

    private TextDisplayBuilder(final Location location) {
        this.location = location.clone();
    }

    /**
     * Plet megadott helyszin.
     */
    public static TextDisplayBuilder at(final Location location) {
        return new TextDisplayBuilder(location);
    }

    /**
     * A megjelenito szoveg (kodek es/vagy MiniMessage).
     */
    public TextDisplayBuilder text(final String text) {
        this.text = text == null ? "" : text;
        return this;
    }

    /**
     * Forgas modja (jatekos-hoz elforges vagy fix).
     */
    public TextDisplayBuilder billboard(final Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    /**
     * Hatter szine (ARGB atmenetben atmaszathato).
     */
    public TextDisplayBuilder background(final Color background) {
        this.background = background;
        this.defaultBackground = false;
        return this;
    }

    /**
     * Egyszeru hatter (szerver default).
     */
    public TextDisplayBuilder defaultBackground() {
        this.defaultBackground = true;
        return this;
    }

    /**
     * A szoveg margos szelessege (px). A sortores novelo.
     */
    public TextDisplayBuilder lineWidth(final int width) {
        this.lineWidth = width;
        return this;
    }

    /**
     * Falak attszuros megjelenitese.
     */
    public TextDisplayBuilder seeThrough() {
        this.seeThrough = true;
        return this;
    }

    /**
     * Arnyek beallitasa.
     */
    public TextDisplayBuilder shadow(final boolean shadowed) {
        this.shadowed = shadowed;
        return this;
    }

    /**
     * Villamos szine ("glow") bekapcsolasa.
     */
    public TextDisplayBuilder glow() {
        this.glow = true;
        return this;
    }

    /**
     * Glow szin megadasa.
     */
    public TextDisplayBuilder glowColor(final Color color) {
        this.glowColor = color;
        this.glow = true;
        return this;
    }

    /**
     * Egyseges skala.
     */
    public TextDisplayBuilder scale(final float scale) {
        return scale(scale, scale, scale);
    }

    /**
     * Skala tengelyenkent.
     */
    public TextDisplayBuilder scale(final float x, final float y, final float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    /**
     * Eltolasa (blokk, 1.0 = 1 blokk).
     */
    public TextDisplayBuilder translation(final float x, final float y, final float z) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
        return this;
    }

    /**
     * Y tengely koruli forgas (fok).
     */
    public TextDisplayBuilder rotation(final float degrees) {
        this.rotationDegrees = degrees;
        return this;
    }

    /**
     * Kepzelj belent (csak a meghívásra) a config vilagban.
     *
     * @return az elkeszult entitas
     */
    public TextDisplay spawn() {
        final TextDisplay display = this.location.getWorld().spawn(this.location, TextDisplay.class, entity -> {
            entity.text(Text.parse(text));
            entity.setBillboard(billboard);
            entity.setBackgroundColor(background);
            entity.setDefaultBackground(defaultBackground);
            entity.setLineWidth(lineWidth);
            entity.setSeeThrough(seeThrough);
            entity.setShadowed(shadowed);
            if (glow) {
                final Color glowCol = glowColor == null ? Color.fromRGB(0, 181, 255) : glowColor;
                entity.setGlowColorOverride(glowCol);
                entity.setGlowing(true);
            } else {
                entity.setGlowing(false);
            }
            final Vector3f scale = new Vector3f(scaleX, scaleY, scaleZ);
            final Vector3f translation = new Vector3f(tx, ty, tz);
            entity.setTransformation(new Transformation(
                    translation,
                    new AxisAngle4f((float) Math.toRadians(rotationDegrees), 0f, 1f, 0f),
                    scale,
                    new AxisAngle4f(0f, 0f, 1f, 0f)));
        });
        return display;
    }
}
