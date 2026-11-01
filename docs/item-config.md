---
layout: default
title: "Item / Config"
nav_order: 4
---

# 🗄️ Item és Config

## ItemBuilder

```java
ItemStack item = ItemBuilder.of(Material.NETHER_STAR)
        .name("&6&lSzint kapcs")
        .lore("&7Kattints!", "&7Kioldja a követelmét")
        .amount(1)
        .glow()
        .unbreakable(true)
        .modelData(1001)
        .build();
```

## Színforma (meta)

```java
ItemBuilder.of(Material.PLAYER_HEAD)
        .skull(player)
        .meta(SkullMeta.class, meta -> {
            // szabadformulózás a saját meta objektummal
        })
        .build();
```

## Perzisztens adat (NDC)

```java
ItemBuilder.of(Material.DIAMOND)
        .data("custom_id", PersistentDataType.STRING, "shop_token_1")
        .build();

// kiolvasás:
String id = meta.getPersistentDataContainer()
        .get(new NamespacedKey(plugin, "custom_id"),
             PersistentDataType.STRING);
```

---

# ⚙️ Config

## Létrehozás

```java
// A jar-beli "config.yml" sablon automatikusan bemásolódik a data folderbe
ConfigFile cfg = ConfigFile.load(plugin, "config.yml");

// vagy egy második fájl: "messages.yml", "gui.yml", stb.
ConfigFile guiCfg = ConfigFile.load(plugin, "gui.yml");
```

## Olvasás, írás

```java
String host = cfg.getString("database.host", "localhost");
int    port = cfg.getInt("database.port", 3306);
boolean regen = cfg.getBoolean("regen.enabled", true);

cfg.set("database.port", 3307);   // mentést kér (save-effektel)
cfg.save();                        // vagy saveIfChanged()
cfg.reload();                      // újraolvassa
```

## Sablon (a `src/main/resources/config.yml`)

```yaml
database:
  host: localhost
  port: 3306
regen:
  enabled: true

messages:
  welcome: "&aSzia, <name>!"
```

```java
Msg configurationutos sablon,
int online = plugin.configuration().getInt("max-online", 100);
```
