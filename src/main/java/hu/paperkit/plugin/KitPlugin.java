package hu.paperkit.plugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import hu.paperkit.core.Kit;
import hu.paperkit.text.Msg;

/**
 * Az ajanlos plugin alap osztaly: automatikusan elvegzi az API
 * inicializalast es lekapcsolast.
 *
 * <p>Hasznalat: ebbol extends-olod a te pluginodot, es az
 * {@link #onBoot()} (beallitas) es {@link #onShutdown()} (elekeresztek)
 * metodust irtad hozza. Az {@code onEnable}/{@code onDisable} nincs bovitva,
 * mert azt az alap kezeli.</p>
 *
 * <pre>{@code
 * public final class SajatPlugin extends KitPlugin {
 *     &#64;Override
 *     protected void onBoot() {
 *         getCommand("sajat").setExecutor(new SajatParancs());
 *     }
 *     &#64;Override
 *     protected void onShutdown() { }
 * }
 * }</pre>
 */
public abstract class KitPlugin extends JavaPlugin {

    /** Az initinalizalt config kezeles cache-elt peldanya. */
    private hu.paperkit.config.ConfigFile configuration;

    @Override
    public final void onEnable() {
        final hu.paperkit.config.ConfigFile cfg = hu.paperkit.config.ConfigFile.load(this, "config.yml");
        configuration = cfg;
        Kit.init(this);
        Msg.send(getServer().getConsoleSender(),
                "<green><bold>" + getName() + "</bold> <gray>betoltott a(z) <aqua>\""
                        + getPluginMeta().getVersion() + "\" <gray>verzioval.");
        this.onBoot();
    }

    @Override
    public final void onDisable() {
        this.onShutdown();
        hu.paperkit.chat.ChatEngine.cleanup();
        hu.paperkit.hooks.packetevents.PacketEventsHook.terminate();
        if (configuration != null) {
            configuration.saveIfChanged();
        }
        hu.paperkit.config.ConfigFile.saveAllRegistered();
        Kit.shutdown();
    }

    /**
     * A plugin engedelyezes utan fut (config mar kesz, API mar initinalizalt).
     */
    protected abstract void onBoot();

    /**
     * A plugin lekapcsolasa elott fut.
     */
    protected void onShutdown() {
    }

    /**
     * A config.yml -t adja vissza, automatikusan betoltve
     * (a defaults a jar-beli config受了 boundols).
     *
     * @return a fo config
     */
    public final hu.paperkit.config.ConfigFile configuration() {
        return configuration;
    }

    /**
     * A temamapeloen config fajlt adja vissza (pl. uzenet konyvtar,
     * gui-fajl), masolva a jar-beli alap adataimtol, ha az meg nem letezik.
     *
     * @param name a fajl neve a jar-n belul es a plugin-databanb (pl. "messages.yml")
     * @return a config kezelo
     */
    public final hu.paperkit.config.ConfigFile configOf(final String name) {
        return hu.paperkit.config.ConfigFile.load(this, name);
    }

    /**
     * Parancs lekerese biztonsagos elwith checkedSRAZzak nelkul.
     *
     * @param name a command neve
     * @return a parancs, vagy null ha nem letezik
     */
    public final PluginCommand commandOf(final String name) {
        return getCommand(name);
    }
}
