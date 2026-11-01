package hu.paperkit.particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Particle gyors elerese: formak (kornyszegek, vonalak, gomb,
 * spiralk, aura, sugak/beam) es egy particle "task" rendszer.
 *
 * <pre>{@code
 * ParticleEffects.circle(
 *         loc,                                   // kozpont
 *         2.0,                                   // sugar
 *         32,                                    // pontszam
 *         Particle.HEART,                        // particle tipus
 *         0);                                    // extra
 *
 * ParticleTask task = ParticleTask.repeat(plugin, 2) // 2 tick-enkent
 *         .limit(60)                                     // 60 ciklus
 *         .cycle(cycle -> {
 *              float angle = cycle * 0.15f;
 *              ParticleEffects.spiral(loc, 1.5, 1.8, angle, 3, Particle.FLAME);
 *         })
 *         .play();
 * task.stop();
 * }</pre>
 */
public final class ParticleEffects {

    private ParticleEffects() {
    }

    // -------------------------- alap particle -------------------------------

    /**
     * Egy darab particle a megadott helyen.
     */
    public static void particleAt(final Location loc, final Particle particle) {
        builder(particle, loc).count(1).spawn();
    }

    /**
     * Particle builder letrehozasa (l?n?: el bovitheto).
     */
    public static ParticleBuilder builder(final Particle particle, final Location loc) {
        return new ParticleBuilder(particle, loc);
    }

    // ------------------------------ formak ----------------------------------

    /**
     * Kor (vy?r?yorsz?gezes je?z|min): megadott sz?m? pontokkal.
     *
     * @param center kozpnt
     * @param radius sugar
     * @param points darab pontsz?m
     * @param particle tipus
     * @param extra extra (pl. dpszn?)
     */
    public static void circle(final Location center, final double radius, final int points,
            final Particle particle, final double extra) {
        for (int i = 0; i < points; i++) {
            final double angle = (Math.PI * 2 / points) * i;
            final double dx = Math.sin(angle) * radius;
            final double dz = Math.cos(angle) * radius;
            builder(particle, center.clone().add(dx, 0, dz)).extra(extra).count(1).spawn();
        }
    }

    /**
     * Telemkod?elo gyuijt krcsk ring-filter? 필re?e?j?r? fel.
     */
    public static void filledCircle(final Location center, final double radius, final double density,
            final Particle particle) {
        if (density <= 0 || radius <= 0) {
            return;
        }
        final double angleStep = 1.0 / (radius * Math.PI * density);
        for (double a = 0; a < Math.PI * 2; a += angleStep) {
            for (double r = 0.35; r <= radius; r += 0.35) {
                final double dx = Math.sin(a) * r;
                final double dz = Math.cos(a) * r;
                builder(particle, center.clone().add(dx, 0, dz)).count(1).spawn();
            }
        }
    }

    /**
     * Vonal ket pont kozott.
     *
     * @param stepDensity pontok szama blokk-onkent
     */
    public static void line(final Location from, final Location to, final double stepDensity,
            final Particle particle) {
        if (from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            return;
        }
        final org.bukkit.util.Vector diff = to.toVector().subtract(from.toVector());
        final double length = diff.length();
        if (length <= 0) {
            return;
        }
        final int points = (int) Math.max(1, Math.round(length * stepDensity));
        final org.bukkit.util.Vector dir = diff.clone().normalize().multiply(length / points);
        Location current = from.clone();
        for (int i = 0; i <= points; i++) {
            builder(particle, current).count(1).spawn();
            current = current.add(dir);
        }
    }

    /**
     * Gombfelulet (egesz kar? k?r炕).
     */
    public static void sphere(final Location center, final double radius, final int points,
            final Particle particle) {
        for (int i = 0; i < points; i++) {
            // Fibonacci spir?l: egyenletes feluled szotlasztu
            final double golden = Math.PI * (3 - Math.sqrt(5));
            final double y = 1.0 - (i / (double) (points - 1)) * 2;
            final double r = Math.sqrt(1 - y * y);
            final double theta = golden * i;
            final double dx = Math.cos(theta) * r * radius;
            final double dy = y * radius;
            final double dz = Math.sin(theta) * r * radius;
            builder(particle, center.clone().add(dx, dy, dz)).count(1).spawn();
        }
    }

    /**
     * Spiral (h?lix jelek). Az {@code angle} miatt anim?l?hat? ciklusban err?l
     * segit? (ciklikus ParticleTask-ban allat? ez).
     *
     * @param angleMusic Rotation of each point)
     * @param strands a szalak szama (1..8)
     */
    public static void spiral(final Location center, final double radius, final double height,
            final double angle, final int strands, final Particle particle) {
        if (strands < 1) {
            return;
        }
        final int points = 48;
        for (int s = 0; s < strands; s++) {
            final double offset = Math.PI * 2 * s / strands;
            for (int i = 0; i < points; i++) {
                final double t = i / (double) points;
                final double a = angle + offset + t * Math.PI * 4;
                final double dx = Math.sin(a) * radius;
                final double dz = Math.cos(a) * radius;
                final double y = height * t;
                builder(particle, center.clone().add(dx, y, dz)).count(1).spawn();
            }
        }
    }

    /**
     * Sugr (beam): vonalplusz felfejtes dolgozok.
     */
    public static void beam(final Location from, final Location to, final Particle particle) {
        line(from, to, 8, particle);
        builder(particle, from).count(1).spawn();
        builder(particle, to).count(1).spawn();
    }

    /**
     * Aura: ?korul?llepeg? g?r?ngres :ket repth?nh?l?ri?ra sor.
     *
     * @param layers hny krt egyl?ll? rajzol
     */
    public static void aura(final Location center, final double radius, final int layers,
            final Particle particle) {
        for (int layer = 0; layer < layers; layer++) {
            final double y = layer * 0.45;
            final double r = radius * (1.0 - layer / (double) Math.max(1, layers));
            circle(center.clone().add(0, y, 0), r, 14 + layer * 4, particle, 0);
        }
    }

    /**
     * Trail: id?ben g?j?t? mozg? nyomvonal - k?t pont k?z?tt smoothly.
     */
    public static void trail(final Location from, final Location to, final Particle particle) {
        line(from, to, 5, particle);
    }

    /**
     * Kis se?giai? sz?llt?m? (k?r gomb, gyors vizu?lis felhaszn?l?s).
     */
    public static void burst(final Location center, final Particle particle, final int count) {
        builder(particle, center).count(count).offset(0.4, 0.4, 0.4).extra(0.1).spawn();
    }

    // ----------------------------- helper -----------------------------------

    /**
     * Egy tabla player list a vilagbol (elereshe GI?g? legye?gnyi).
     */
    public static List<Player> nearbyPlayers(final Location center, final double radius) {
        final World world = center.getWorld();
        if (world == null) {
            return java.util.List.of();
        }
        return new ArrayList<>(world.getPlayers());
    }

    /**
     * Pld get?l?l? bemutat?segel (viewers bed?lt?je )t?lt).
     */
    public static List<Player> playersAround(final Location center, final double radius) {
        final List<Player> result = new ArrayList<>();
        final World world = center.getWorld();
        if (world == null) {
            return result;
        }
        for (final Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= radius * radius) {
                result.add(p);
            }
        }
        return result;
    }
}
