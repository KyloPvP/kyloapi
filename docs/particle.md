---
layout: default
title: "Particle"
nav_order: 8
---

# ✨ Particle (paricsék)

## Alap használat

```java
ParticleEffects.circle(loc, 2.0, 32, Particle.HEART, 0.0);
ParticleEffects.sphere(loc, 3, 200, Particle.FLAME);
ParticleEffects.spiral(loc, 1.5, 1.8, angle, 3, Particle.FLAME);
ParticleEffects.line(from, to, 5, Particle.CRIT_MAGIC);
ParticleEffects.aura(loc, 1.2, 4, Particle.END_ROD);
ParticleEffects.beam(locA, locB, Particle.WAX_ON);
ParticleEffects.burst(loc, Particle.FIREWORK, 24);
```

## Builder (szabályozott kiadás)

```java
ParticleEffects.builder(Particle.FLAME, loc)
        .count(3)
        .offset(0.2, 0.1, 0.2)
        .extra(0.05)
        .viewers(playerList)     // csak nekik látszik
        .spawn();
```

## Animált particle task

```java
 ParticleTask task = ParticleTask.every(2)          // 2 tick-enként
         .limit(60)                                 // 60 ciklus után áll le
         .cycleTick(cycle -> {
             float angle = cycle * 0.15f;
             ParticleEffects.spiral(loc, 1.5, 1.8, angle, 3, Particle.FLAME);
         })
         .play();

 // leállítás:
 task.stop();
```

## Tippek

- **~50–200 particle/kiadás** alatt maradj, ha minden játékra reális FPS-t akarsz.
- `viewers(list)` a legszűkebb rádiusz területre: nagyszerűen csökkenti a hálózati költséget.
- `every(1)` helyett `every(2)` vagy `every(3)` jellemzően elegendő az animáláshoz.
