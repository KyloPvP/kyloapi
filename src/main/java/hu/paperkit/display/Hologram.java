package hu.paperkit.display;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import hu.paperkit.text.Text;

/**
 * Tobb soros hologram TextDisplay entitasokbol.
 *
 * <pre>{@code
 * Hologram holo = Hologram.create(loc("&8Vasarlo NPC", "&7Kattints a vasarlashoz)"));
 * holo.spawn();
 *
 * kesobbi:
 * holo.setLine(0, "&cVasarlasz!");
 * holo.move(newLocation);
 * holo.delete();
 * }</pre>
 */
public final class Hologram {

    /** Egy hologram sor: a szoveg es az entitas. */
    private static final class HoloLine {
        TextDisplay entity;
        String raw;
    }

    private final List<HoloLine> lines = new ArrayList<>();
    private final Location base;
    private double spacing = 0.28;
    private Color background = Color.fromARGB(0x00000000);
    private boolean shadowed;
    private DeleteListener deleteListener;

    /** Hologram torles слушатель (pl. plugin disable). */
    public interface DeleteListener {
        void onDelete(Hologram hologram);
    }

    private Hologram(final Location base, final String... lineTexts) {
        this.base = base.clone();
        for (final String raw : lineTexts) {
            final HoloLine line = new HoloLine();
            line.raw = raw == null ? "" : raw;
            lines.add(line);
        }
    }

    /**
     * Hologram letrehozasa: a base pozicio az ELSO sor helye,
     * a tobbi sor alatta van.
     *
     * @param base az elso sor pozicioja
     * @param lineTexts a sorok szovege
     */
    public static Hologram create(final Location base, final String... lineTexts) {
        return new Hologram(base, lineTexts);
    }

    /**
     * A sorok kozti tavolsag (0.25 = 25% blokkenyire magasabban alacsonyabban).
     */
    public Hologram spacing(final double spacing) {
        this.spacing = spacing;
        return this;
    }

    /**
     * Hatter szine (a teljes hologramra).
     */
    public Hologram background(final Color background) {
        this.background = background;
        return this;
    }

    /**
     * Arnyekos szoveg minden soron.
     */
    public Hologram shadowed() {
        this.shadowed = true;
        return this;
    }

    /**
     * Hologram letrehozo spawn sa a vilagban.
     */
    public Hologram spawn() {
        for (int i = 0; i < lines.size(); i++) {
            spawnLine(i);
        }
        return this;
    }

    private void spawnLine(final int index) {
        final Location loc = locationOf(index);
        final HoloLine line = lines.get(index);
        line.entity = TextDisplayBuilder.at(loc)
                .text(line.raw)
                .background(background)
                .shadow(shadowed)
                .billboard(Display.Billboard.CENTER)
                .spawn();
    }

    /**
     * Az i-edik sor pozicioja (fentről-le).
     */
    private Location locationOf(final int index) {
        final Location loc = base.clone();
        loc.setY(base.getY() - spacing * index);
        return loc;
    }

    /**
     * Sor szovegenek valtozcatasa.
     *
     * @param index line index (0 = legfelso)
     * @param raw   az ujj szoveg
     */
    public Hologram setLine(final int index, final String raw) {
        final HoloLine line = lines.get(index);
        if (line == null) {
            throw new IllegalArgumentException("Nincs ilyen sort: " + index);
        }
        line.raw = raw == null ? "" : raw;
        if (line.entity != null) {
            line.entity.text(Text.parse(line.raw));
        }
        return this;
    }

    /**
     * Uj sor hozzaadasa az aljara.
     */
    public Hologram addLine(final String raw) {
        final HoloLine line = new HoloLine();
        line.raw = raw == null ? "" : raw;
        lines.add(line);
        spawnLine(lines.size() - 1);
        return this;
    }

    /**
     * Sor torlese (0 = legfelso).
     */
    public Hologram removeLine(final int index) {
        final HoloLine line = lines.get(index);
        if (line == null) {
            return this;
        }
        if (line.entity != null) {
            line.entity.remove();
        }
        lines.remove(index);
        return this;
    }

    /**
     * A sorok szama.
     */
    public int lineCount() {
        return lines.size();
    }

    /**
     * A teljes hologram mozgatasa (uj elso pillanat).
     */
    public Hologram move(final Location newBase) {
        base.setWorld(newBase.getWorld());
        base.setX(newBase.getX());
        base.setY(newBase.getY());
        base.setZ(newBase.getZ());
        for (int i = 0; i < lines.size(); i++) {
            final HoloLine line = lines.get(i);
            if (line.entity != null && !line.entity.isDead()) {
                line.entity.teleport(locationOf(i));
            }
        }
        return this;
    }

    /**
     * Az osszes entitas torlese.
     */
    public void delete() {
        for (final HoloLine line : lines) {
            if (line.entity != null && !line.entity.isDead()) {
                line.entity.remove();
                line.entity = null;
            }
        }
        if (deleteListener != null) {
            deleteListener.onDelete(this);
        }
    }

    /**
     * Torles figyelo beallitasa.
     */
    public Hologram onDelete(final DeleteListener listener) {
        this.deleteListener = listener;
        return this;
    }

    /**
     * A spawn iar -ol a creature-es hrs ha eltunt-e.
     */
    public boolean isDeleted() {
        for (final HoloLine line : lines) {
            if (line.entity != null && !line.entity.isDead()) {
                return false;
            }
        }
        return true;
    }
}
