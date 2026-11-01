---
layout: default
title: "Formula & Messages"
nav_order: 12
parent: Modulok
---

# 🧮 Formula és Messages

Két config-építő eszköz, a klasszikus kész pluginok (pl. AxHoes) konfig
stílusát hozza el a PaperKit-be: **számolható beállítások** és
**fájlból szabályozható üzenetek**.

## Formula — matematikai kifejezések

Hagyományos plugin-minta: a XP-küszöb, ár vagy jutalom nem fix szám,
hanem **formula**:

```yaml
xp-calculation:
  type: "formula"              # vagy "manual"
  formula: "%level% ^ 2 * 1000"
  manual:                      # csak ha type: manual
    1: "100"
    2: "500"
    3: "1500"
```

### Kód: kifejezés értékelése

Támogatott: `+ - * / ^`, zárójelek, negatív számok, `%valtozo%` tokenek:

```java
Formula f = Formula.compile("%level% ^ 2 * 1000");
f.evalInt(Map.of("level", 5));   // 25000
f.evalInt("level", 10);          // 100000 (szimpla változó)

Formula.compile("2 ^ 3 ^ 2").eval(Map.of()); // 512 (jobbra asszociatív)
```

A hiányzó változó **0**-ként értékelődik — így egy rossz config sem dönti le a szervert.

### Kód: config-ból (formula/manual minta)

```java
// config-ban: xp-calculation szekció (fenti yaml)
int needed = Formula.calculate(config, "xp-calculation", "level", playerLevel);
// ha type: formula -> kifejezés a %level%-el
// ha type: manual  -> a manual.<szint> kulcs értéke, és ha nincs: formula
```

Hibás kifejezésnél `IllegalArgumentException` dobódik compile-kor, és
a `calculate(...)` 0-t ad — a szerver nem crash-el rossz config-tétel miatt.

## Messages — messages.yml üzenetkönyvtár

AxHoes `lang.yml` stílusú fájl: minden üzenet egy helyen, `%tokent%`.

```yaml
# messages.yml
prefix: "<dark_gray>[<aqua>SajatPlugin<dark_gray>] "
level-up: "<green>Szintlepes! Uj szint: <white>%level%"
no-money:
  - "<red>Nincs eleg penzed!"
  - "<gray>Hiany: <white>%missing%"
```

### Használat

```java
Messages msg = Messages.load(this, "messages.yml"); // onBoot-ban

// egyszerű üzenet (a "prefix" automatikusan elé kerül)
msg.send(player, "level-up", "<green>Szintlepes!", "level", 5);

// több soros üzenet
msg.sendLines(player, "no-money", List.of("<red>Nincs penzed!"), "missing", 120);

// broadcast mindenki + konzol
msg.broadcast("welcome", "<gold>Udv!");

// Component-változat (menu-név, lore stb. számára)
Component c = msg.component(player, "item-name", "<white>Kezi targy");
```

### Mi készül a tokenekből?

| Token         | Honnan                           |
|---------------|----------------------------------|
| `%level%`     | a kulcs-érték párokból           |
| `%receiver%`  | a címzett neve (Player sends)    |
| `%papi_xxx%`  | PaperKit `Placeholders` (PAPI-minta) |

A `prefix` kulcs **nem token**: minden üzenet elé automatikusan rákerül.

### Reload

```java
msg.config().reload(); // lemezrol ujratöltés — üzenetek élőben frissülnek
```
