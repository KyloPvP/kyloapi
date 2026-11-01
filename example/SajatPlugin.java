import java.util.List;

import net.kyori.adventure.bossbar.BossBar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import hu.paperkit.ability.AbilityKit;
import hu.paperkit.chat.ChatEngine;
import hu.paperkit.command.CommandKit;
import hu.paperkit.combat.CombatVisuals;
import hu.paperkit.config.ConfigFile;
import hu.paperkit.core.Kit;
import hu.paperkit.hooks.Hooks;
import hu.paperkit.hooks.packetevents.PacketEventsHook;
import hu.paperkit.hooks.vault.VaultHook;
import hu.paperkit.hud.HudBars;
import hu.paperkit.item.ItemBuilder;
import hu.paperkit.menu.GuiLoader;
import hu.paperkit.notify.Notifier;
import hu.paperkit.npc.DialogueKit;
import hu.paperkit.npc.NpcKit;
import hu.paperkit.preset.Presets;
import hu.paperkit.text.Messages;
import hu.paperkit.text.Msg;

/**
 * DEMO plugin: megmutatja, milyen gyorsan megy az elet a PaperKit-tel.
 * Olyesl konfigural iniciativa: ez egy "reader" peldapan
 * (nem artics plugin; kitano tanuloeszvens szumma).
 *
 * <p>Nem a maven fordul rajta (peldakod) — a kiindulopontnak szol:
 * masold a metodust azokra a reszletekre, amik kellenek.</p>
 */
public final class SajatPlugin extends JavaPlugin {

    private Messages msg;
    private ConfigFile config;
    private final Cooldown cooldown = new Cooldown();

    @Override
    public void onEnable() {
        // 1. API init (MenuListener + esemenyek)
        Kit.init(this);

        // 2. Config + uzenetek
        config = ConfigFile.load(this, "config.yml");
        msg = Messages.load(this, "messages.yml");

        // 3. Combat + chat + notifier + ability + npcs
        CombatVisuals.enable(this);
        ChatEngine.start(this);
        NpcKit.register(this);
        AbilityKit.register(this);

        // 4. Ha PacketEvents fent van: hook
        if (Hooks.isPluginEnabled("packetevents")) {
            PacketEventsHook.engage(this);
        }

        // 5. Parancsok
        CommandKit.route(this, "demo", root -> {
            root.sub("menu")
                    .playerOnly()
                    .on(context -> GuiLoader.open(this, "guis.yml", context.player(), "main"));

            root.sub("devices")
                    .playerOnly()
                    .on(context -> {
                        final Player player = context.player();
                        // Bossbar HP
                        HudBars.bar(player, "hp",
                                "<red>[HP] %bar% %current%/%max%", BossBar.Color.RED)
                                .value(player.getHealth()).max(20).push();
                        // Damage popup demo + combo
                        Notifier.success(player, "<green>Kezdet!");
                    });

            root.sub("npc")
                    .playerOnly()
                    .on(context -> {
                        final Player player = context.player();
                        final var npc = NpcKit.spawn(player.getLocation(), "<gold>Mester");
                        npc.visibleTo(player)
                                .dialogue("Szia utazo!",
                                        "Itt vagyunk eleg regi idok.")
                                .onClick(p -> DialogueKit.speak(p)
                                        .line("<gold>Mester: <white>Szia!")
                                        .line("<gold>Mester: <white>Kozdozed eleg.")
                                        .start(40));
                    });

            root.sub("ability")
                    .playerOnly()
                    .on(context -> {
                        final var wand = AbilityKit.build("demo-wand")
                                .cooldownMillis(2000)
                                .onCooldownMessage("<red>Varj chondr: <white>%seconds%s")
                                .onRightClick(event ->
                                        Presets.teleportSpiral(event.getPlayer().getLocation()).play())
                                .register();
                        context.player().getInventory().addItem(wand.stamp(
                                ItemBuilder.of(Material.STICK).name("<green>Demo vastarako").glow().build()));
                    });

            root.sub("sell")
                    .playerOnly()
                    .minArgs(1)
                    .on(context -> {
                        final Player player = context.player();
                        double price = 0;
                        for (final ItemStack item : player.getInventory().getContents()) {
                            if (item == null || item.getType() == Material.AIR) {
                                continue;
                            }
                            price += count(item) * 5.0;
                            item.setAmount(0);
                        }
                        if (VaultHook.hooked()) {
                            VaultHook.give(player, price);
                        }
                        msg.send(player, "sell-done", "<green>Eladva! Penz: <white>%amount%", "amount", price);
                    });

            root.sub("cinematic")
                    .playerOnly()
                    .on(context -> {
                        final Player player = context.player();
                        cmdCinematic(player);
                    });
        });
    }

    private void cmdCinematic(final Player player) {
        final Location base = player.getLocation();
        final List<Location> points = List.of(
                base.clone().add(5, 3, 0),
                base.clone().add(0, 3, 5),
                base.clone().add(-5, 3, 0),
                base.clone().add(0, 3, -5));
        hu.paperkit.cinematic.CinematicKit.play(this, player, points, 120, () -> {
            Msg.send(player, "<green>Cinematic vege!");
            player.teleport(base);
        });
    }

    private int count(final ItemStack item) {
        return item.getAmount();
    }

    @Override
    public void onDisable() {
        Kit.shutdown();
    }
}
