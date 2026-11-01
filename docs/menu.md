---
layout: default
title: "Menu / GUI"
nav_order: 2
---

# 🖥️ Menu és GUI

## Egyszerű menü

```java
Menu.create(3, "&8Választó menü")                   // 3 sor, cím
    .at(13, MenuButton.of(ItemBuilder.of(Material.DIAMOND)
            .name("&bVásárlás")
            .lore("&7Kattints a vásárláshoz")
            .build())
        .click(event -> {
            Player p = (Player) event.getWhoClicked();
            Msg.send(p, "&aMegvásároltad!");
            p.closeInventory();
        }))
    .border(MenuButton.of(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
            .name("&r").build()))                   // keret kitöltés
    .open(player);
```

## Lapos (több oldalas) menü

```java
PagedMenu.of("&8Teleport pontok", warpLocations, (warp, index) ->
        MenuButton.of(ItemBuilder.of(Material.ENDER_PEARL)
                .name("&b" + warp.name())
                .build())
            .leftClick(() -> warp.teleport(player)))
    .rows(6)                     // utolsó sor automatikus navigáció
    .open(player);
```

## Megerősítő (Igen/Nem)

```java
ConfirmMenu.confirm(
        player,
        "&6Törlés megerősítése",
        "&7Biztosan törlöd a hologramot?",
        ok  -> hologram.delete(),          // IGEN gomb
        no  -> no.closeInventory()         // NEM gomb
);
```

## Szöveg-bekérés — anvil alapon

```java
AnvilInputMenu.of(player)
        .prompt("&eÍrd be az új nevet!")
        .item(Material.PAPER)           // alap item (bármilyen lehet)
        .callback(nev -> hologram.setLine(0, "&6" + nev))
        .open();
```

## Szám bekérés — anvil alapon

```java
AnvilInputMenu.of(player)
        .prompt("&eMennyi pénzed legyen?")
        .number(amount -> giveMoney(player, amount))
        .open();
```

## Chat-alapú bekérés

```java
ChatInput.request(player, "&eMelyik szobában?", szoba -> {
    Msg.send(player, "&aBeírva: <white>" + szoba);
});

// vagy számban:
ChatInput.number(player, "&eMilyen mennyiség?", db -> {
    // db = Integer (ha nem számot írt, nem hívódik meg)
});
```

> A `"cancel"`-t, ha a játékos beírja, megszakítja a bekérést.

## Bezárás esemény

```java
Menu.create(2, "&8Sáv beállítások")
    .onClose((event, menu) -> {
        Player p = (Player) event.getPlayer();
        Msg.send(p, "&7Becsuktad a menüt");
    })
    .open(player);
```
