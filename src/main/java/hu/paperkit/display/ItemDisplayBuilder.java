package hu.paperkit.display;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Item display entitas pLETESI builder (lebeg? itemek, 3D itemekhez).
 *
 * <pre>{@code
 * ItemDisplay disp = ItemDisplayBuilder.at(loc)
 *         .item(ItemBuilder.of(Material.DIAMOND_PICKAXE).build())
 *         .transform(ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND)
 *         .scale(2.0f)
 *         .spawn();
 * }</pre>
 */
public final class ItemDisplayBuilder {

    private final Location location;
    private ItemStack item = new ItemStack(Material.STONE);
    private ItemDisplay.ItemDisplayTransform transformMode =
            ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND;
    private Display.Billboard billboard = Display.Billboard.FIXED;
    private boolean glow;
    private Color glowColor = Color.fromRGB(0, 181, 255);
    private float scaleX = 1f, scaleY = 1f, scaleZ = 1f;
    private float tx, ty, tz;
    private float rotationDegrees;

    private ItemDisplayBuilder(final Location location) {
        this.location = location.clone();
    }

    /**
     * Plet megadott helyszin.
     */
    public static ItemDisplayBuilder at(final Location location) {
        return new ItemDisplayBuilder(location);
    }

    /**
     * A megjelenitendo item.
     */
    public ItemDisplayBuilder item(final ItemStack item) {
        this.item = item == null ? new ItemStack(Material.STONE) : item;
        return this;
    }

    /**
     * A "kez" poz?ci?, ahogy az itemet moteSzaz d?l.
     */
    public ItemDisplayBuilder transform(final ItemDisplay.ItemDisplayTransform mode) {
        this.transformMode = mode;
        return this;
    }

    /**
     * Forgs modja.
     */
    public ItemDisplayBuilder billboard(final Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    /**
     * Glow bekapcsolasa.
     */
    public ItemDisplayBuilder glow() {
        this.glow = true;
        return this;
    }

    /**
     * Glow szin megadasa.
     */
    public ItemDisplayBuilder glowColor(final Color color) {
        this.glowColor = color;
        this.glow = true;
        return this;
    }

    /**
     * Egyseges skala.
     */
    public ItemDisplayBuilder scale(final float scale) {
        return scale(scale, scale, scale);
    }

    /**
     * Skala tengelyenkent.
     */
    public ItemDisplayBuilder scale(final float x, final float y, final float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
        return this;
    }

    /**
     * Eltolasa (blokk).
     */
    public ItemDisplayBuilder translation(final float x, final float y, final float z) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
        return this;
    }

    /**
     * Y tengely koruli forgas (fok).
     */
    public ItemDisplayBuilder rotation(final float degrees) {
        this.rotationDegrees = degrees;
        return this;
    }

    /**
     * Az entitas letrehozasa a vilagban.
     *
     * @return az elkeszult item display
     */
    public ItemDisplay spawn() {
        final ItemDisplay display = this.location.getWorld().spawn(this.location, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            entity.setItemDisplayTransform(transformMode);
            entity.setBillboard(billboard);
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
