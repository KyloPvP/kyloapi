package hu.paperkit.particle;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Particle builder: darab, offset, gyorsgas (extra), ados megadashoz.
 *
 * <pre>{@code
 * ParticleEffects.builder(Particle.FLAME, loc)
 *         .count(3)
 *         .offset(0.2, 0.1, 0.2)
 *         .extra(0.05)
 *         .viewers(players)     // csak nekik szamolj?n (0n?p?gen?r?ss)
 *         .spawn();
 * }</pre>
 */
public final class ParticleBuilder {

    private final Particle particle;
    private final Location location;
    private int count = 1;
    private double offsetX, offsetY, offsetZ;
    private double extra;
    private boolean forceRender = true;
    private List<Player> viewers;

    ParticleBuilder(final Particle particle, final Location location) {
        this.particle = particle;
        this.location = location.clone();
    }

    /**
     * Darabszm egy spawn-ban.
     */
    public ParticleBuilder count(final int count) {
        this.count = Math.max(0, count);
        return this;
    }

    /**
     * Szros (x, y, z) pontatlansg (szrt?k?z?l).
     */
    public ParticleBuilder offset(final double x, final double y, final double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    /**
     * Sebessg vagy egyeb extra adat (particle tipustol fugg).
     */
    public ParticleBuilder extra(final double extraValue) {
        this.extra = extraValue;
        return this;
    }

    /**
     * Csak a megadott jatekosok latjak.
     */
    public ParticleBuilder viewers(final List<Player> players) {
        this.viewers = players;
        return this;
    }

    /**
     * Csak a megadott jatekosok latjak.
     */
    public ParticleBuilder viewers(final Player... players) {
        return viewers(java.util.Arrays.asList(players));
    }

    /**
     * Tlvolsgi? renderels engelyezse (nem lhtson ki?jt).
     */
    public ParticleBuilder noForceRender() {
        this.forceRender = false;
        return this;
    }

    /**
     * Particle megjelenitese a vilagban.
     */
    public void spawn() {
        final org.bukkit.World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (viewers == null || viewers.isEmpty()) {
            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, null);
        } else {
            world.spawnParticle(particle, viewers, null,
                    location.getX(), location.getY(), location.getZ(),
                    count, offsetX, offsetY, offsetZ, extra, null, forceRender);
        }
    }
}
