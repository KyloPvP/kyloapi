---
layout: default
title: "Chat Engine"
nav_order: 9
parent: Modulok
---

# 💬 Chat Engine

Saját chat rendszer: csatornák, formátumok, hover/click a neveken,
jogosultság-szűrés és helyi (local) chat.

## Elindítás

```java
public final class SajatPlugin extends KitPlugin {
    @Override
    protected void onBoot() {
        ChatEngine.start(this); // alap \"global\" csatorna + natív chat átvétel
    }
}
```

A `KitPlugin` a leállításnál automatikusan meghívja a `ChatEngine.cleanup()`-t.

## Saját csatorna

A formátum MiniMessage. Használható tokenek:

| Token       | Mit cserél                                      |
|-------------|-------------------------------------------------|
| `{prefix}`  | a csatorna prefixje (`prefix(...)`)              |
| `{name}`    | a játékos neve — hover + click esélyel          |
| `{player}`  | a nyers játékosnév                              |
| `{message}` | maga az üzenet (biztonságos, nem MiniMessage)   |
| `%papi%`    | PaperKit `Placeholders` (PAPI-kompatibilis minta) |

```java
ChatEngine.registerChannel(
    Channel.global("<dark_gray>[<aqua>G<dark_gray>] {name}<gray>: </gray>{message}")
           .hover("<gray>Játékos: <white>{player}\\n<gray>Privi: <white>/msg {player}")
           .suggest("/msg {player} ")); // kattintás: /msg parancs beszúrása

ChatEngine.setDefaultChannel("global");
```

## Staff csatorna

Csak azok látnak/írnak ide, akiknek megvan a jogosultság:

```java
ChatEngine.registerChannel(
    Channel.create("staff", "<dark_red>[STAFF] {name}: {message}")
           .permission("paperkit.chat.staff"));
```

## Játékos csatorna váltása

```java
ChatEngine.setChannel(player, "staff"); // null = visszaállítás
```

## Local (közelség-alapú) csatorna

```java
ChatEngine.registerChannel(
    Channel.local("<green>[{name}] {message}", 30) // 30 blokkos sugar
           .nameColor("<yellow>"));
```

## Programatikus küldés

```java
ChatEngine.send(player, null, "Hello!");    // a játékos aktív csatornájába
ChatEngine.send(player, "staff", "Admin üzenet");
```

## Cha.yml-bol: csatornak fajlbol

A csatornak es a slow-mode `chat.yml`-bol is betoltheto — nem kell kodot irni:

```yaml
# chat.yml
default: global
channels:
  global:
    format: "<dark_gray>[<aqua>G<dark_gray>] {name}<gray>: </gray>{message}"
    hover: "<gray>Jatekos: <white>{player}"
    suggest: "/msg {player} "
  staff:
    format: "<dark_red>[STAFF] {name}: {message}"
    permission: "myp.chat.staff"
  local:
    format: "<green>[L] {name}: {message}"
    range: 30
slow-mode-seconds:
  global: 3
```

```java
ChatEngine.start(this);
ChatEngine.loadFromConfig(this); // csatornak + default + slow-mode betoltese
```

## Slow-mode

```java
ChatEngine.setSlowMode("global", 5); // 5 masodperc varakozas minden adatra
```

## Natív chat kikapcsolása

Ha csak programatikus küldéshez kell az engine, de a natív chat marad az eredeti:

```java
ChatEngine.setIntercepting(false);
```

## Kikapcsolás

```java
ChatEngine.cleanup(); // listener leiratkoztatás (KitPlugin automatikusan megteszi)
```

## További tervek

- `chat.yml` konfigurációs születés (formátumok fájlból)
- Chat games (pl. másoló/játék üzenetek)
- Mute / slowmode támogatás
- Discord bridge (DiscordSRV hook)
