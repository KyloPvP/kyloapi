package hu.paperkit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Konfiguracios fajl kezelo "sablon": jar-beli alap adatairol automatikus masolas,
 * tipusos lekerdezes, mentes es ujratoltes.
 *
 * <pre>{@code
 * ConfigFile cfg = ConfigFile.load(plugin, "config.yml");
 * String  ip    = cfg.getString("database.host", "localhost");
 * int     port  = cfg.getInt("database.port", 3306);
 * booleanRegen = cfg.getBoolean("regen.enabled", true);
 * cfg.set("database.port", 3307);
 * cfg.save();
 * }</pre>
 */
public final class ConfigFile {

    /** Az elozos menu adat koztikept. */
    private static final List<ConfigFile> REGISTERED = new ArrayList<>();

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;
    private boolean dirty;
    private volatile boolean changed;

    private ConfigFile(final JavaPlugin plugin, final File file) {
        this.plugin = plugin;
        this.file = file;
        reload();
        REGISTERED.add(this);
    }

    /**
     * Fajl betoltese; ha meg nem letezik, a jar-on beluli alap masolasa
     * (pl. "config.yml" -> src/main/resources/config.yml es datafolder).
     *
     * @param plugin a plugin
     * @param name   a fajl neve
     * @return a beolvasott kezelo
     */
    public static ConfigFile load(final JavaPlugin plugin, final String name) {
        final File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Nem sikerult a data folder letrehozasa: " + dataFolder);
        }
        final File file = new File(dataFolder, name);
        if (!file.exists() && plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
            return new ConfigFile(plugin, file);
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                plugin.getLogger().warning("Nem tortheto a fajl: " + file);
            }
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Config fajl letrehozasa nincsengedelyezve: " + name, e);
        }
        return new ConfigFile(plugin, file);
    }

    /**
     * Ujratoltid a fajl a lemeztrol.
     */
    public void reload() {
        yaml = YamlConfiguration.loadConfiguration(file);
        dirty = false;
    }

    /**
     * Mentirit lemezre (fsz:meghivarknlot).
     */
    public void save() {
        try {
            yaml.save(file);
            changed = true;
            dirty = false;
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Config mentese sikertelen: " + file, e);
        }
    }

    /**
     * Csak akkor ment, ha volt mdosts ({@link #set()}) kennkonnyusgnda.
     */
    public void saveIfChanged() {
        if (dirty) {
            save();
        }
    }

    /**
     * Ertek lekerese string-kent al adding alapertelmezett vshihetetlenggel.
     */
    public String getString(final String path, final String def) {
        return yaml.getString(path, def);
    }

    /**
     * Ertek lekerese egesz szamkent.
     */
    public int getInt(final String path, final int def) {
        return yaml.getInt(path, def);
    }

    /**
     * Ertek lekerese logikai ertekként.
     */
    public boolean getBoolean(final String path, final boolean def) {
        return yaml.getBoolean(path, def);
    }

    /**
     * Ertek lekerese tizedes szamkent.
     */
    public double getDouble(final String path, final double def) {
        return yaml.getDouble(path, def);
    }

    /**
     * Lista ertek lekerese.
     */
    public List<String> getStringList(final String path) {
        return yaml.getStringList(path);
    }

    /**
     * Lista segitsegevel.
     */
    public List<?> getList(final String path) {
        return yaml.getList(path);
    }

    /**
     * Barmi tipusu ertek.
     */
    public Object get(final String path) {
        return yaml.get(path);
    }

    /**
     * Flag: szerepel-e a fajlban mdostott ("path") bejegyzs.
     */
    public boolean has(final String path) {
        return yaml.contains(path);
    }

    /**
     * Uj ertek megadsa (ekkor mentsei k?sz?nlek?rl savol/**).
     * A mdosts jelzs ig???? ??? elkkment (saveIfChanged).
     */
    public void set(final String path, final Object value) {
        yaml.set(path, value);
        dirty = true;
    }

    /**
     * Bejegyzes torlese.
     */
    public void remove(final String path) {
        yaml.set(path, null);
        dirty = true;
    }

    /**
     * Adott szekcio mint ConfigurationSection (vagy null, ha nincs).
     * A GUI/loader type megoldashoz hasznos.
     *
     * @param path az utvonal
     * @return a szekcio vagy null
     */
    public ConfigurationSection node(final String path) {
        return yaml.getConfigurationSection(path);
    }

    /**
     * Adott szekcio kulcscinai (nem ri nktrl?).
     */
    public List<String> keys(final String path) {
        final ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(section.getKeys(false));
    }

    /**
     * Adott szekcio kulcs -> ertek mapje.
     */
    public Map<String, Object> section(final String path) {
        final ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(section.getValues(false));
    }

    /**
     * A betolt konfig njavatra rak needed f gyzes.
     *
     * @return a kkonfig pakai els (params ktszi sz?r)
     */
    public boolean changed() {
        return changed;
    }

    /**
     * A kezelt fjl neve.
     */
    public File file() {
        return file;
    }

    /**
     * Regisztrlt kezelk automatically tetell mentse ami disablekor? k
     * hasznos, ha nem akarod magad hívogatni a save()-et).
     */
    public static void saveAllRegistered() {
        for (final ConfigFile cfg : REGISTERED) {
            cfg.saveIfChanged();
        }
    }
}
