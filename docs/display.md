---
layout: default
title: "Display / animáció"
nav_order: 6
---

# 🖼️ Display entitások és animációk

## Text display (lebegő szöveg)

```java
TextDisplay disp = TextDisplayBuilder.at(loc)
        .text("&b&lHologram")
        .billboard(Display.Billboard.CENTER)     // mindig a játékost nézi
        .background(Color.fromARGB(0x66000000))  // átlátszó háttér
        .lineWidth(150)
        .scale(1.2f)
        .spawn();
```

## Item display (lebegő 3D item)

```java
ItemDisplay disp = ItemDisplayBuilder.at(loc)
        .item(ItemBuilder.of(Material.DIAMOND_PICKAXE).build())
        .transform(ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND)
        .scale(2.0f)
        .spawn();
```

## Animáció — egyszerű, keyframe alapú

```java
DisplayAnimation.animate(itemDisplay)
        .scale(1.5f, 15)          // 15 tick alatt 1.5-szeresre
        .rotateYLeft(90, 30)      // 30 tick alatt forogjon 90 fokot
        .pause(20)
        .reset(10)
        .play();                  // elindítja

// végtelen ismétlés:
DisplayAnimation.looping(itemDisplay)
        .rotateYLeft(360, 60)     // körbe fordul
        .play();                  // folyamatosan forog

// leállítás:
anim.stop();
```

## Működés

- A lépések a **szerver beépített interpolációját** használják, így a mozgás látszólagos és olcsó.
- A szekvenciát a `DisplayAnimation` a Bukkit-scheduler-re üzeni, tédecsés nem használt.
