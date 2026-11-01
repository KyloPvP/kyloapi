---
layout: default
title: "Hologram"
nav_order: 7
---

# 🔮 Hologram (lebegő szövegek)

## Létrehozás és lemozgatás

```java
Hologram holo = Hologram.create(
        loc,                         // az első sor (legfelül) helye
        "&6&lVásárló NPC",           // 1. sor
        "&7Kattints a vásárláshoz",   // 2. sor
        "&8+300 lada profit");

holo.spawn();                        // lebegő entitások jönnek létre
```

## Sorok módosítása, mozgatás, törlés

```java
// sor csere (0 = legfelül)
holo.setLine(1, "&bÚj szöveg!");

// új sor hozzáadás az aljára
holo.addLine("&7Ez még új sor lett");

// egy sor törlése
holo.removeLine(2);

// a teljes hologram mozgatása
holo.move(newLocation);

// törlés (minden entitás eltűnik)
holo.delete();
```

## Teljes példa: hologram egy NPC felett chat-scroll

```java
private Hologram npcHolo;

@EventHandler
public void onSpawnF(NPCSpawnEvent event) {
    npcHolo = Hologram.create(
            event.getSpawnLoc().add(0, 2.6, 0),
            "&6&lKereskedő Kalolt",
            "&7Kattints ide rá");

    npcHolo.spacing(0.28)                 // sorok távolsága
           .background(Color.fromARGB(0x88000000))  // átlátszó háttér
           .shadowed()
           .spawn();
}

// pl. shop beszerzéskor:
@EventHandler
public void onBuy(FullEvent event) {
    npcHolo.setLine(0, "&c Elfogyott, sorry");
}
```

## Tipp: menü + hologram összekapcsolás

```java
// a menu gombon a hologram szövegét is be tudod frissíteni:
menu.at(11, MenuButton.of(ItemBuilder.of(Material.NAME_TAG)
        .name("&eNév átírás")
        .build())
    .leftClick(() -> AnvilInputMenu.of(player)
            .prompt("&eÍrd be az új nevet!")
            .callback(nev -> npcHolo.setLine(0, "&6&l" + nev))
            .open()))
```

## Fontos apróság

- A `background(Color)` ARGB értékkel állítódik – a nulla alpha kikapcsolja a hátteret.
- `shadowed()` miatt olvashatóbb a fényen/sötéten.
- A `spacing()`-et **≤ 0.3**-ra állnyári, így szép a sorok távolság.
