# PaperKit

**„Minden egyben" Minecraft plugin fejlesztő API — Paper 26.3 (`io.papermc.paper:paper-api:26.3.build.235-stable`) verzióra, Java 21+ (fordításhoz JDK 25 ajánlott).**

> Az API-t nulláról írtuk (nem kódmásolás a hivatkozott способности repákból), a natív Paper/Adventure API-ra pacing.

## Modulok (most már mind lefordulnak)

| Csomag       | Mit tud |
|--------------|---------|
| `core`       | `Kit.init(plugin)` egykapcsos bootstrap, `Kit.sync/later` futtatás; `KitPlugin` knowhow (saveDefaultConfig, config és command hozzáférés) |
| `text`       | `Text.parse()` — MiniMessage + legacy `&`/`§` + hex (`&#ff8800`, `&x&F…`) keverve; placeholder resolverek; `Text.title/actionbar/plain`; `Msg` prefixes üzenetküldés |
| `item`       | `ItemBuilder` — name/lore/glow/enchant/flags/custom model/PDC adatok, skull, meta-hook |
| `menu`       | `Menu` (rows, title 1klines, slots, border, fill, onClose, cancelClicks) + `PagedMenu` (lapozás, auto prev/next/info), eseménykezdés központosítva |
| `config`     | `ConfigFile` — jar-beli template másolása, tipusos getterek, set/save/reload |
| `display`    | `TextDisplayBuilder`, `ItemDisplayBuilder` (billboard, scale, translation, glow…), `DisplayAnimation` (scale/translate/rotate/pause/reset lépések, loop), `Hologram` (több sor soröző, mozgatható, sor-csere) |
| `particle`   | `ParticleEffects` (circle, filledCircle, line, sphere, spiral, beam, aura, trail, burst), `ParticleBuilder` (count/offset/extra/viewers), `ParticleTask` (ismétlődő animált ciklusok) |
| `hud`        | `BossBarKit` (Adventure bossbar), `TabKit` (header/footer), `ScoreboardKit` (sidebar, soronkénti update) |
| `sound`      | `SoundKit` (play + sorozat lejátszás késleltetéssel) |
| `placeholder`| `Placeholders` — %kulcs% csere, global és per-player provider |
| `task`       | `TaskKit` (later/async/repeat-kezelő limitáld) , `Cooldown` (per player + per kulcs cooldown) |

## Minta plugin

```java
public final class DemuPlugin extends KitPlugin {
    @Override
    protected void onBoot() {
        Msg.send(getServer().getConsoleSender(), "<aqua>Demu elindult!");

        getCommand("demo").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player player) {
                Menu.create(3, "&8Demo menü")
                        .at(13, MenuButton.of(
                                ItemBuilder.of(Material.NETHER_STAR).name("&bFő funkció").build())
                                .click(e -> Player p = (Player) e.getWhoClicked()))
                        .border(MenuButton.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("&r").build()))
                        .open(player);

                Text.title(player, "&6&lÜDV!", "&7Boldog új életet kívánunk!");
                SoundKit.playerPlay(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            }
            return true;
        });
    }
}
```

## Build

```bash
mvn compile        # JDK 25 + Maven 3.9.
mvn test           # egységtesztek
maven package      # jar
```

Függőség a saját pluginjeidhez (a `provided` scope, a szerver szolgáltatja):

```xml
<dependency>
    <groupId>hu.paperkit</groupId>
    <artifactId>paperkit</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope> <!-- vagy a szerver meggyőződése szerint szigetelni -->
</dependency>
```

## Ütemterv

- [x] **1. fázis:** core, text, item, menu, config, display, hologram, animation, particle
- [x] **2. fázis (alap):** BossBar, Tab, Scoreboard, Sound, Cooldown, Placeholder, Task
- [ ] **3. fázis:** chat engine, GUI dialog/input, NPC engin, cinematic, data components, notification
- [ ] **4. fázis (eneklinti):**

### Hookok frízelissegsé opcionális integrációk
LuckPerms, Vault, PlaceholderAPI, WorldGuard/WorldEdit, EssentialsX, Citizens, MythicMobs,
PacketEvents/ProtocolLib, TAB, LiteBans, CoreProtect, spark, DiscordSRV, SkinsRestorer,
Multiverse-Core, GriefPrevention, Plan, Floodgate/Geyser, SuperVanish, DecentHolograms,
Nexo/ItemsAdder, ModelEngine, PlugManX, ViaVersion, Folia schedulerek…

#### Hogyan és mikor
- **A hook modul futásidejű vizuális le ellenőrzéshez** (ha a célswordvan telepítve a plugin) `Bukkit.getPluginManager().isPluginEnabled("LuckPerms")`-mel mí töltés, és csak akkor tölt be a speciális osztály.
- **Minden hook külön alclem: `hu.paperkit.hooks.luckperms`, `…vault`, `…papi`** megváltozs egysék (API-javal), **a fő kód nem függ tőtől**.
- A legszükségesebb hookok először: Vault (economy), LuckPerms (rang, chat prefix), PlaceholderAPI, WorldGuard (régiók), PacketEvents (csomagok).
