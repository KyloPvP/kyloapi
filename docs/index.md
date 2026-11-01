---
layout: default
title: "Kezdőlap"
nav_order: 1
---

# PaperKit 🧰

<div class="hero">

**„Minden egyben" plugin API** Paper szerverekhez (26.3+), magyarul dokumentálva.

Könnyű menükezelő, szövegformázás, hologram, animáció, partiklik és HUD elemek —
mindegyik úgy megírva, hogy **10 sor alatt bemutassa a legnehezebb részt**.

</div>

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
| [Menu](menu)      | Inventory GUI-k, lapozás, Igen/Nem ablak, anvil/chat input, **GuiLoader (yaml-menük)** |
| [Text](text)      | MiniMessage + legacy + hex színek, title, actionbar |
| [Item](item)      | ItemBuilder |
| [Config](config)  | Sablon-kezelés, Formula |
| [Messages](formula-messages) | messages.yml + Localization |
| [Display](display)| TextDisplay / ItemDisplay / animáció |
| [Hologram](hologram) | Lebegő szövegek |
| [Particle](particle) | Circle, sphere, spiral, beam, aura, stb. |
| [HUD](hud)        | BossBar, Tab, Scoreboard, **HudBars (hp/mana/stamina)** |
| [Sound](sound)    | Hangok |
| [Chat](chat)      | Csatornák, formátum, hover/click, local chat |
| [Combat/Világ](modules) | Fake blokk/entity, damage popup, NPC, cinematic, ability, NpcKit |
| [Placeholders](placeholders) | `%kulcs%` csere |
| [Task](task)      | TaskKit, Cooldown, Sequence |
| [Hooks](hooks)    | ~28 integráció (PacketEvents, Vault, LuckPerms, PAPI, WG, Citizens, ...) |
