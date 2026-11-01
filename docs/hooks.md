---
layout: default
title: "Hooks (tervezett)"
nav_order: 11
---

# 🧩 Hook modul (opcionális integrációk)

A cél: a PaperKit működjön **sólyos egyedül**, de automatikusan kihasználja,
amikor egy híres plugin telepítve van.

## Elvek

- A fő kod **nem függ** oneplugin API-jától; minden hook külön csomagba kerül
  (`hu.paperkit.hooks.luckperms`, `…vault`, `…worldguard`, `…packetevents`).
- Futási időben változatos engedélyezion:
  ```java
  if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
        LuckPermsHook.engage(plugin);  // regisztrlja magat
  }
  ```

## Első hullám

| Hook | Mit ad |
|------|--------|
| **Vault** | economy (pénz), permission bridge |
| **LuckPerms** | rang, prefix/suffix a chat engine-be |
| **PlaceholderAPI** | `%paperkit_key%` expansion automatikus |
| **WorldGuard** | régió lekérdezés (lobby/arena küszöb) |
| **PacketEvents** | csomag-manipuláció (fake render) |

## Később tervezett

LiteBans (statisztika), CoreProtect (rollback announcer), DiscordSRV
(Discord → chat engine), Citizens / MythicMobs (NPC és boss integráció),
Nexo / ItemsAdder (custom item és font), ModelEngine (3D mod modellek),
ViaVersion (verzióbarát kiadás), Floodgate/Geyser (Bedrock játékos), spark (performance dashboard).

> Ha szeretnéd, mondj előbb egy konkrét hookot — azt beépítjük tesztelve.
