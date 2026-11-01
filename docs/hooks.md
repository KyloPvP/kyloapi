---
layout: default
title: "Hooks"
nav_order: 11
parent: Modulok
---

# 🧩 Hook modul (opcionális integrációk)

A cél: a PaperKit működjön **önállóan egyedül**, de automatikusan
kihasználja, amint egy híres plugin telepítve van a szerverre.

## Elvek

- A fő kod **nem függ** egy plugin API-jától; minden hook külön csomagba kerül
  (`hu.paperkit.hooks.packetevents`, később `…luckperms`, `…vault`, `…worldguard`).
- Futási időben van egy központi segédosztály: `hu.paperkit.hooks.Hooks`

```java
if (Hooks.isPluginEnabled("packetevents")) {
    PacketEventsHook.engage(plugin);
    Msg.info("<aqua>PacketEvents hook bekapcsolva! Verzió: "
        + Hooks.version("packetevents"));
}
```

| Metódus                     | Mit ad                                      |
|-----------------------------|---------------------------------------------|
| `Hooks.isPluginEnabled(name)` | fut-e a plugin                            |
| `Hooks.getPlugin(name)`     | a plugin példány (vagy null)                 |
| `Hooks.version(name)`       | a plugin verzió (`pluginMeta`) vagy null    |

## PacketEvents (kész! ✅)

A pom.xml-ben opcionálisan van (`provided` + `optional`), a fordítás majd
fut assemblek nélkül működik.

```java
// 1. Engedélyezés (csak ha telepítve van):
if (Hooks.isPluginEnabled("packetevents")) {
    PacketEventsHook.engage(plugin); // setAPI + init + load
}

// 2. Kimenő (server -> client) csomag listener:
var listener = PacketEventsHook.addSendListener(event -> {
    // például blokkváltás követése:
    if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
        // feldolgozás a PacketEvents wrapper-ekkel
    }
});
// listener leiratkozás:
PacketEventsHook.removeListener(listener);

// 3. Szűrt listener (csak egy adott csomagtípusra):
PacketEventsHook.addSendListener(
    PacketType.Play.Server.BLOCK_CHANGE,
    event -> { /* ... */ });

// 4. Fake blokk (csak az adott játékos látja):
PacketEventsHook.sendBlockChange(player, location,
    WrappedBlockState.getDefaultState(Material.GLOWSTONE));

// 5. Leállítás (KitPlugin automatikusan megteszi):
PacketEventsHook.terminate();
```

Fontos: a `PacketEventsHook.engage(...)` előtt a többi hívás
`IllegalStateException`-t dob — így nem felejtheted el az inicializálást.

## Később tervezett

LiteBans (statisztika), CoreProtect (rollback announcer), DiscordSRV
(Discord → chat engine), Citizens / MythicMobs (NPC és boss integráció),
Nexo / ItemsAdder (custom item és font), ModelEngine (3D mod modellek),
ViaVersion (verzióbarát kiadás), Floodgate/Geyser (Bedrock játékos),
spark (performance dashboard).
