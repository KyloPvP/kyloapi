package hu.paperkit.preset;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import hu.paperkit.menu.Menu;
import hu.paperkit.menu.MenuButton;
import hu.paperkit.particle.ParticleEffects;
import hu.paperkit.particle.ParticleTask;
import hu.paperkit.item.ItemBuilder;

/**
 * Preset konyvtar: eltre keszitett particika-effektek, GUI-mintak,
 * lore-stilusok. Ebbol nem kell minden effekttet egetuzni.
 *
 * <pre>{@code
 * Presets.explosionRing(loc).play();      // falato robotulats
 * Presets.teleportSpiral(loc).play();     // torony spiral
 * Presets.shieldDome(loc).play();         // vedo bokor szférá
 * }</pre>
 */
public final class Presets {

    private static final Random RANDOM = new Random();

    private Presets() {
    }

    // ---------- particle presetek ----------

    /**
     * Fed店舗 robocationse: 20 egeszesben lekvon vonalaro.
     */
    public static ParticleTask explosionRing(final Location center) {
        return ParticleTask.every(1).limit(14)
                .cycleTick(cycle -> ring(center, Particle.FLAME, 0.25 + cycle * 0.3, 20));
    }

    /**
     * Teleport spiral: felfel es eso.
     */
    public static ParticleTask teleportSpiral(final Location center) {
        return ParticleTask.every(1).limit(24)
                .cycleTick(cycle -> {
                    final double angle = cycle * 0.5;
                    final double radius = 0.6;
                    final double y = cycle * 0.12;
                    ParticleEffects.builder(Particle.END_ROD, center.clone()
                                    .add(Math.cos(angle) * radius, y, Math.sin(angle) * radius))
                            .count(1).spawn();
                    ParticleEffects.builder(Particle.PORTAL, center.clone()
                                    .add(Math.cos(angle + Math.PI) * radius, y, Math.sin(angle + Math.PI) * radius))
                            .count(1).spawn();
                });
    }

    /**
     * Level-up aura: lebeg korok felfel.
     */
    public static ParticleTask levelUpAura(final Location center, final int durationTicks) {
        return ParticleTask.every(2).limit(Math.max(1, durationTicks / 2))
                .cycleTick(cycle -> {
                    final double y = (cycle % 10) / 10.0 * 1.6;
                    final List<Location> ring = ring(center, Particle.HAPPY_VILLAGER, 1.0, 12);
                    for (final Location at : ring) {
                        at.add(0, y, 0);
                        ParticleEffects.builder(Particle.HAPPY_VILLAGER, at).count(1).spawn();
                    }
                });
    }

    /**
     * Vedo bolkar (shield dome): forgok szfert szilvest ebercs.
     */
    public static ParticleTask shieldDome(final Location center, final int cycles) {
        return ParticleTask.every(3).limit(cycles)
                .cycleTick(cycle -> {
                    final double rotation = cycle * 0.35;
                    for (int i = 0; i < 26; i++) {
                        final double t = Math.PI * (i + (cycle % 2)) / 26.0;
                        final double radius = 1.9;
                        final double x = Math.cos(rotation) * radius * Math.sin(t);
                        final double z = Math.sin(rotation) * radius * Math.sin(t);
                        final double y = radius * Math.cos(t);
                        ParticleEffects.builder(Particle.END_ROD, center.clone().add(x, y, z))
                                .count(1).spawn();
                    }
                });
    }

    /**
     * Ego nyomok: latantha trail a ket pont kozott.
     */
    public static void fireTrail(final Location from, final Location to) {
        ParticleEffects.trail(from, to, Particle.FLAME);
    }

    /**
     * Pulzus beam: széles felé lenidozats a ket pont kozott.
     */
    public static void beamPulse(final Location from, final Location to, final Particle particle) {
        ParticleEffects.beam(from, to, particle);
        ParticleEffects.burst(to, particle, 12);
    }

    /**
     * Random sparkle a hely körül.
     */
    public static void sparkle(final Location center, final int count) {
        for (int i = 0; i < count; i++) {
            final Location at = center.clone().add(
                    (RANDOM.nextDouble() - 0.5) * 1.4,
                    RANDOM.nextDouble() * 1.2,
                    (RANDOM.nextDouble() - 0.5) * 1.4);
            ParticleEffects.builder(Particle.GLOW, at).count(1).spawn();
        }
    }

    private static List<Location> ring(final Location center, final Particle particle,
            final double radius, final int points) {
        final List<Location> spawned = new java.util.ArrayList<>();
        for (int i = 0; i < points; i++) {
            final double angle = Math.PI * 2 * i / points;
            final Location at = center.clone()
                    .add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius);
            ParticleEffects.builder(particle, at).count(1).spawn();
            spawned.add(at);
        }
        return spawned;
    }

    // ---------- GUI presetek ----------

    /**
     * Loading menu: soros fa k keretes loading ablak.
     */
    public static Menu loading(final String title) {
        return Menu.create(3, title)
                .border(MenuButton.display(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build()))
                .at(13, MenuButton.display(ItemBuilder.of(Material.CLOCK)
                        .name("<gold>Betoltes...")
                        .lore("<gray>Kerlek, varj par masodpercet!")
                        .glow()
                        .build()));
    }

    /**
     * Informacios menu: feher bali-frame es leiras.
     */
    public static Menu info(final String title, final Material icon, final String description) {
        return Menu.create(3, title)
                .border(MenuButton.display(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).name(" ").build()))
                .at(12, MenuButton.display(ItemBuilder.of(icon)
                        .name("<aqua>Informacio")
                        .glow()
                        .build()))
                .at(14, MenuButton.display(ItemBuilder.of(Material.PAPER)
                        .name("<white>Leiras")
                        .lore("<gray>" + description)
                        .build()));
    }

    /**
     * Shop-slot button: elo felége (ar, leiras) a shop menüimhez.
     */
    public static MenuButton shopEntry(final Material material, final String name,
            final int price, final Runnable onBuy) {
        final ItemStack stack = ItemBuilder.of(material)
                .name(name)
                .lore("<gray>Ar: <gold>" + price + " <yellow>$",
                        "",
                        "<yellow>Kattints a vasarla'tshoz!")
                .build();
        return MenuButton.of(stack).click(event -> {
            final Player player = (Player) event.getWhoClicked();
            onBuy.run();
            hu.paperkit.text.Msg.send(player, "<green>Vasarlas elvegezve!");
        });
    }

    // ---------- lore presetek ----------

    /**
     * Stats lore-format (a szerver Glossary tabbaval: kulcsertek parok).
     */
    public static List<String> statLore(final String key, final double value, final String suffix) {
        return List.of(
                " <gray>" + key + ": <white>" + String.format(java.util.Locale.US, "%,.1f", value) + suffix);
    }

// (nincs magasabb szintu tennivalo: a statLore-bol es GUI-presetretekbol all a konyvtar)
}
