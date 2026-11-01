---
layout: default
title: "Kamerás & Világ modulok"
nav_order: 13
parent: Modulok
---

# 🌍 Client-side, Combat és Világ modulok

Egy nagy hullám, ami a "kliens-oldali vizuális réteget" hozza el:

## FakeBlockKit — per-player blokkok

```java
FakeBlockKit.set(player, loc, Material.GLOWSTONE); // csak ő látja
FakeBlockKit.reset(player, loc);                   // vissza a valósághoz
FakeBlockKit.clear(player);                        // mind eltűntetés
FakeBlockKit.count(player);                        // mennyi aktív fake
```

## FakeEntityKit — per-player mobok (PacketEvents-szel)

```java
FakeEntity zombie = FakeEntityKit.spawn(player, loc, "zombie");
zombie.teleport(loc2);
zombie.remove();
```

Előfeltétel: `PacketEventsHook.engage(plugin)` (a [Hooks](hooks) oldalon).

## CombatVisuals — damage popup + crit + combo

```java
CombatVisuals.enable(this); // onBoot-ban, listener-rel működik önmagától
CombatVisuals.showDamage(loc, 12.5, true);  // kézi popup (krit)
CombatVisuals.showText(loc, "<gold>SZINTLEPÉS!"); // kézi üzenet-popup
CombatVisuals.popupFormat("<red><bold>%amount%");
```

Kritikus ütésnél `<gold>` színű, `⚡`-es popup; 2+ egymás utáni találatra a támadónak
`Nx combo!` actionbar kerül (3 mp-es ablak).

## HudBars — health/mana/stamina widgetek

```java
HudBars.Bar hp = HudBars.bar(player, "hp",
    "<red>%bar% %current%/%max%", BossBar.Color.RED);
hp.value(player.getHealth()).max(20).push(); // bossbar frissítés
HudBars.clear(player);
```

Tokenek: `%current%`, `%max%`, `%bar%` (teli/üres cella), `%value%`.

## Presets — kész effektek és GUI-k

```java
Presets.explosionRing(loc).play();       // táguló gyűrű
Presets.teleportSpiral(loc).play();      // emelkedő spirál
Presets.shieldDome(loc, 30).play();      // védődome
Presets.levelUpAura(loc, 40).play();
Presets.loading("Betöltés");             // kész GUI-minták
Presets.info("Menu", Material.BOOK, "..."); // info menü
```

## SmartHologram — követő/billegő hologram

```java
SmartHologram hud = SmartHologram.create(loc, "<gold>Mester");
hud.follow(player, 2.4).bob(0.05); // 2.4 blokkra a fej fölött, billeg
hud.remove();
```

## CinematicKit — kamerautak

```java
CinematicKit.play(plugin, player, List.of(p1, p2, p3), 100,
    () -> Msg.send(player, "<green>Vége!"));
CinematicKit.cancel(player);
```

## NpcKit + DialogueKit — NPC és beszélgetés

```java
Npc mester = NpcKit.spawn(loc, "<gold>Mester");
mester.visibleTo(player)
      .dialogue("Szia utazó!", "Miben segíthetek?")
      .onClick(p -> mester.playDialogueTo(p));

DialogueKit.speak(player)
    .line("<gold>Mester: <white>Szia!")
    .line("<gold>Mester: <white>Vágod?")
    .ask("<aqua>Ird: igen", answer -> { ... })
    .start(40);
```

## AbilityKit — item-képességek

```java
AbilityKit.register(this);

Ability wand = AbilityKit.build("wand")
    .onRightClick(event -> { { /* villám */ } })
    .cooldownMillis(2000)
    .onCooldownMessage("<red>Várj ezt: <white>%seconds%s")
    .permission("myp.wand")
    .register();

ItemStack stick = wand.stamp(new ItemStack(Material.STICK));
```

## Notifier — konzisztens értesítések

```java
Notifier.success(player, "<green>Mentve!");
Notifier.warn(player, "<red>Hibás adat!");
Notifier.announce(player, "<gold>SZINT", "Léptél!", 60);
```

## többi modul

- **GuiLoader** — yaml-ból definiált menük: `guis.yml` + `[COMMAND]`/`[SOUND]`/`[CONNECT]` akciók, saját `[XX]` akció regisztrálás
- **CommandKit** — DSL-alapú alparancs-router (permission, tab, playerOnly)
- **Localization** — többnyelvű üzenetek (locale szerint: `messages_hu.yml`)
- **Inventories** — inventory mentés/visszatöltés fájlba
- **Compat** — Folia-detektálás, szálbiztos futtatás
- **ProxyKit** — BungeeCord/Velocity csatorna (connect, PlayerCount)
- **ResourcePackKit** — pack kérés natívan
- **DebugKit** — timing, TPS-monitor, memória
- **VaultHook / LuckPermsHook / PapiHook** — reflekciós integrációk
- **`hu.paperkit.hooks.integrations`** —ToWorldGuard, EssentialsX, Citizens, MythicMobs, DiscordSRV és társaik (reflexió)

## Példaprogram

Az [`example/SajatPlugin.java`](https://github.com/KyloPvP/kyloapi/blob/main/example/SajatPlugin.java)
fájl egy teljes demo plugin, ami gyakorlatilag az összes modult használja.
