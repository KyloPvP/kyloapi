---
layout: default
title: "Text"
nav_order: 3
---

# 🎨 Text és színek (formázott szövegek)

A PaperKit kettő módot támogat egyszerre:

- **MiniMessage**: `<green>Színes <bold>félkövér</bold>`
- **Legacy kodek**: `&aSzínes`, `§aSzínes`
- **Hex szín**: `&#FF8800` vagy Bukkit forma `&x&F&F&8&8&0&0`

Mindegyik felprogramja leröhm a másikat — szabad keverni!

```java
Text.parse("&6Helló &b<bold>világ</bold> és &#FF9944színes vagy!");
```

## Placeholder

```java
Text.parse("<green>Szia <name>!", Text.res("name", player.getName()));
Text.parse("&aEgyenleg: <money>", Text.res(Map.of("money", "12 000 Zs")));
```

## Cím és alcím

```java
// gyors verzió
Text.title(player, "&6&lÜdv!", "&7Boldog új életet kívánunk!");

// egyéni időzítéssel (tick, 20 = 1 mp)
Text.title(player, "&6&lÜdv!", "&7Boldog új életet kívánunk!",
           5,    // fade-in
           40,   // eltartás
           5);   // fade-out
```

## Actionbar

```java
Text.actionbar(player, "&bSzint: <aqua>42");
```

## Előtag (prefix) küldés

```java
Msg.setPrefix("&8[ &bSzerver&8 ] &r");
Msg.send(player, "&fBejelentkeztél!");     // → [ Szerver ] Bejelentkeztél!
Msg.send(player, "&fÜzenet az elotag nélkül", TagResolver.empty()); // r?szletek ld. javadoc
```

##arge Placeholder egyben

```java
Msg.send(player, "<red>Nincs jogod!", Text.res(Map.of("perm", "admin.use")));
```
