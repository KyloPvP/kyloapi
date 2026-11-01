---
layout: default
title: "Sound / Task"
nav_order: 10
---

# 🔊 Sound és ⏰ Task

## Hangok

```java
// egyszer hang:
SoundKit.playerPlay(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

// szekvencia (késleltetéssel):
SoundKit.sequence(player)
        .push(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)   // azonnal
        .at(5, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.3f)   // 5 tick később
        .at(10, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f)
        .start();
```

---

## TaskKit

```java
// késleltetett:
TaskKit.later(40, () -> Msg.send(player, "&aKésve aktiváltál!"));

// ismétlődő:
TaskKit.Repeating rep = TaskKit.repeat(20, cycle -> {
    // 20 tick-enként, cycle = hányadszor futott
});
rep.cancel(); // leállítás

// korlát:
TaskKit.repeat(10, cycle -> { })
        .limit(60)       // max. 60 futás
        .start();

// aszinkron (fő szál blokkolása nélkül):
TaskKit.async(() -> databaseQuery());
```

## Cooldown

```java
Cooldown cd = new Cooldown();

if (cd.isOnCooldown(player, "tp")) {
    Msg.send(player, "<red>Még <sec> másodperc várnod kell!",
             Text.res("sec", cd.remainingSeconds(player.getUniqueId(), "tp")));
    return;
}
cd.set(player, "tp", 10_000);   // 10 mp
```

---

# 🐘 Placeholder

```java
// regisztrálás:
Placeholders.global("szerver", () -> "MyServer");
Placeholders.register("level", player -> String.valueOf(player.getLevel()));

// használat bármely üzenetben (csere automatikus):
Msg.sendRaw(player, Placeholders.apply("Üdv, %level% szint vagy!", player));
```
