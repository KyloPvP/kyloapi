---
layout: default
title: "HUD"
nav_order: 9
---

# 📊 HUD (BossBar, Tab, Scoreboard)

## BossBar

```java
BossBarKit bar = BossBarKit.create("&aRaid indul!", BossBar.Color.RED, 1.0f)
        .showTo(playerVector);    // vagy: .broadcast()

bar.progress(0.5f);              // fél progressz
bar.title("&c10 másodperc hátra"); // cím csere
bar.color(BossBar.Color.YELLOW);
bar.hide(player);                // egy játékosnak leveszi
bar.delete();                    // mindenkitől leveszi
```

## Tab (header + footer)

```java
TabKit.send(player,
        "&b&lSZerverEM",                    // header
        "&7Online: &f" + Bukkit.getOnlinePlayers().size());  // footer

// minden játékosnak:
TabKit.sendAll("&f&lÜdv!", "&7play.szerverem.hu");
```

## Scoreboard (sidebar)

```java
ScoreboardKit sb = ScoreboardKit.create("&6&lSZERVER")
        .lines("&eElso sor", "&aMasodik sor", "&7...")
        .show(player);

// sor frissítés (0 = legfelül):
sb.setLine(0, "&eÚj idő: &f12:34");

// törlés:
sb.hide(player);
```

## Figyelem

- A `ScoreboardKit` 16 sor lényeget képes megr北京市.
- Az adat időzítését a `TaskKit`-el tudodd szinkronizál (upperfloor lower). lásd [Task](task).
