---
layout: default
title: "Kezdőlap"
nav_order: 1
---

# PaperKit 🧰

**„Minden egyben" plugin API** Paper szerverekhez (26.3+), magyarul dokumentálva.

Könnyű menüfelmémlő, szövegformázás, hologram, animáció, paricsék és HUD elemek —
mindegyik úgy megírva, hogy **10 sor alatt bírjam a legnehezebb részt**.

```java
public final class DemuPlugin extends KitPlugin {
    @Override
    protected void onBoot() {
        Msg.send(getServer().getConsoleSender(),
                 "<green><bold>Szerver elindult!</bold>> Az API készen ál!");
    }
}
```

## Gyors telepítés

### 1. Add hozzá a függőséget

```xml
<dependency>
    <groupId>hu.paperkit</groupId>
    <artifactId>paperkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Repo a pom.xml-be

```xml
<repository>
    <id>local-jitpack</id>
    <url>https://jitpack.io</url>  <!-- vagy publikáld a Maven Centralra -->
</repository>
```

### 3. Egy sor init

```java
@Override
public void onEnable() { /* a KitPlugin caseben az init auto-történik */ }
```

---

## Modulok

| Modul    | Mit tud                                       |
|----------|----------------------------------------------|
| [Menu](menu)      | Inventory GUI-k, lapozás, Igen/Nem ablak, anvil/chat input |
| [Text](text)      | MiniMessage + legacy + hex színek, title, actionbar |
| [Item](item)      | ItemBuilder |
| [Config](config)  | Sablon-kezelés |
| [Display](display)| TextDisplay / ItemDisplay / animáció |
| [Hologram](hologram) | Lebegő szövegek |
| [Particle](particle) | Circle, sphere, spiral, beam, aura, stb. |
| [HUD](hud)        | BossBar, Tab, Scoreboard |
| [Sound](sound)    | Hangotok |
| [Placeholders](placeholders) | `%kulcs%` csere |
| [Task](task)      | TaskKit, Cooldown |
| [Hooks](hooks)    | LuckPerms, Vault, PAPI, WorldGuard (tervezés) |
