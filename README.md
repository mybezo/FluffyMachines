# FluffyMachines

[![Java CI](https://github.com/mybezo/FluffyMachines/actions/workflows/maven.yml/badge.svg)](https://github.com/mybezo/FluffyMachines/actions/workflows/maven.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.txt)

A [Slimefun](https://github.com/Slimefun/Slimefun4) addon full of handy machines, tools and storage: auto crafters,
bottomless barrels, portable chargers, warp pads and more.

> Originally made by [NCBPFluffyBear](https://github.com/NCBPFluffyBear/FluffyMachines) ("These are some thrown
> together machines made by me for fun :)"). This fork is based on the
> [SlimefunGuguProject](https://github.com/SlimefunGuguProject/FluffyMachines) version, which runs on Slimefun's
> database storage, and is fully translated back to English.

## Contents

- [Requirements](#requirements)
- [Download](#download)
- [Features](#features)
- [Configuration](#configuration)
- [Commands and permissions](#commands-and-permissions)
- [FAQ](#faq)
- [Building from source](#building-from-source)
- [Credits and license](#credits-and-license)

## Requirements

| Dependency | Required | Notes |
|---|---|---|
| [Slimefun4 (SlimefunGuguProject)](https://github.com/SlimefunGuguProject/Slimefun4) | Yes | Uses the database storage API, so the official Slimefun build will not work |
| GuizhanLibPlugin | Yes | |
| [mcMMO](https://github.com/mcMMO-Dev/mcMMO) | No | Explosive tools can give mcMMO XP |
| NoCheatPlus | No | Compatibility hooks |

## Download

[Download FluffyMachines](https://resources.guizhanss.com/plugin/FluffyMachines)

Drop the jar into your `plugins` folder together with the required plugins above, then restart the server.

## Features

### Machines

| Machine | What it does |
|---|---|
| **Auto Crafting Table** | Automatically crafts vanilla recipes |
| **Auto Enhanced Crafting Table** | Automatically crafts Enhanced Crafting Table recipes |
| **Auto Armor Forge** | Automatically crafts Armor Forge recipes |
| **Auto Magic Workbench** | Automatically crafts Magic Workbench recipes |
| **Auto Ancient Altar** | Automatically crafts Ancient Altar recipes |
| **Auto Table Saw** | Automatically crafts Table Saw recipes |
| **Smart Factory** | All-in-one machine that crafts resources straight from raw materials |
| **Water Sprinkler** | Electric sprinkler that grows crops in a 2 block radius |
| **Backpack Loader** | Moves items from its inventory into a backpack |
| **Backpack Unloader** | Empties a backpack into its inventory |
| **Advanced Auto Disenchanter** + **Ancient Book** | Removes one chosen enchantment from an item. Needs an Ancient Book per use |
| **Electric Dust Fabricator** | Grinds, pans and washes cobblestone into sifted ore in one machine |
| **Electric Dust Recycler** | Turns eight of the same dust back into one sifted ore |
| **Advanced Charging Bench** + **ACB Upgrade Card** | A charging bench that can be upgraded with upgrade cards (up to tier 100) |

### Compressed Machines

Several machines combined into one block, in four tiers: **x8, x16, x32 and x64**. Open them from
*Fluffy Machines > Compressed Items* in the guide (only the highest enabled tier is listed there).

| Compressed machine | What is multiplied |
|---|---|
| **Auto Crafting Table**, **Auto Enhanced Crafting Table**, **Auto Magic Workbench**, **Auto Ancient Altar** | Crafts up to N times per tick, as far as the inputs (every slot keeps 1 item as the template), the energy and the output space allow |
| **Auto Table Saw** | Saws up to N items per tick |
| **Smart Factory** | Same speed, every finished cycle makes up to N items, using ingredients from any input slots |

- The compressed crafters (Auto Crafting Table, Auto Enhanced Crafting Table, Auto Magic Workbench,
  Auto Ancient Altar) wait until every used input slot is full enough for the whole batch: the multiplier
  plus 1 template item, or a full stack. A x64 crafter waits for full stacks of 64 and crafts 63 at once.
- Recipes (Compressor): x8 = 8 normal machines, x16 = 2 x8, x32 = 2 x16, x64 = 2 x32.
- Energy is only used for what is actually processed. The energy buffer is multiplied too.
- Outputs are always split into valid stacks; nothing is made when there is no room for it.
- Settings are in `plugins/FluffyMachines/compressed.yml` (enable all, each tier, each machine). Restart to apply.

### Generators

| Generator | What it does |
|---|---|
| **Crank Generator** | Multiblock that generates power every time you click its lever |

### Tools

| Tool | What it does |
|---|---|
| **Upgraded Explosive Pickaxe / Shovel** | 5x5 versions of the explosive tools that break the blocks in front of you. Can trigger other plugins listening to block breaks (e.g. mcMMO XP); toggle this in `Items.yml` |
| **Upgraded Lumber Axe** | Breaks every log within 2 blocks of each other, great for acacia, jungle and custom trees. Right click to strip logs |
| **Scythe** | Breaks 5 crops per swing |
| **Fluffy Wrench** (Reinforced / Carbonado) | Quickly breaks Slimefun cargo nodes and energy components |
| **Paxel** | A pickaxe, axe and shovel in one tool |
| **Cargo Manipulator** | Copy, paste and clear cargo node settings |
| **Watering Can** | Waters plants and saplings to speed up their growth |
| **Dolly** | Picks up chests (single or double) with their contents and places them somewhere else |
| **Portable Charger** (5 tiers) | Handheld charger that charges items anywhere |
| **Warp Pad Configurator** | Links Warp Pads together |

### Items

| Item | What it does |
|---|---|
| **Helicopter Hat** | Float upwards while sneaking, and no fall damage |
| **Fireproof Rune** | Drop it onto a dropped item to make that item fireproof |

### Storage, cargo and transport

| Block | What it does |
|---|---|
| **Fluffy Barrels** (Small to Bottomless) | Store a huge amount of one item type. Holograms are off by default; turn them on per barrel with the hologram button in the barrel menu |
| **Mini Fluffy Barrel** | A barrel with a capacity you can change yourself |
| **Foundry** + **Superheated Furnace** | Multiblock that melts and stores dusts and ingots, and lets you take them out in either form |
| **Exp Dispenser** | Collects the XP from all experience bottles in a dispenser and the barrel it faces |
| **Ender Chest Extraction Node** | Moves items from an ender chest into the container behind it |
| **Ender Chest Insertion Node** | Moves items from the container behind it into an ender chest |
| **Warp Pad** | Short distance teleport pads. Sneak on an origin pad to warp to its destination |
| **Alternate Elevator Plate** | Works like Slimefun's Elevator Plate but uses a chest GUI, handy when players are muted or for looks |

> The ender chest nodes do not stack items in inventories, that would cost more performance. Use cargo for that.

## Configuration

Most options are per item and live in `plugins/Slimefun/Items.yml`. Some useful ones:

| Item | Setting | Default | Description |
|---|---|---|---|
| Fluffy Barrels | `show-hologram` | `true` | Allow barrel holograms. Each barrel still starts with its hologram off |
| Fluffy Barrels | `break-only-when-empty` | `false` | Only allow breaking a barrel when it is empty |
| Mini Fluffy Barrel | `capacity` | | Maximum capacity a player can set |
| Water Sprinkler | `success-chance`, `particles` | `0.5`, `true` | Growth chance per tick and particle effects |
| Advanced Auto Disenchanter | `use-enchant-level-limit`, `enchant-level-limit` | `false`, `10` | Limit the enchantment level that can be removed |
| Dolly | `can-pick-locked-chest` | `true` | Allow picking up locked chests |
| Warp Pad Configurator | `max-distance` | `100` | Maximum distance between linked pads, per axis |
| Explosive tools / Lumber Axe | `trigger-other-plugins`, `break-from-center` | `true`, `false` | Fire block break events for other plugins, and where the 5x5 area starts |
| Watering Can | `max-uses` | `10` | Number of uses per refill |

The plugin's own `config.yml` has `options.auto-update` and `options.ignore-outdated-warning`.

## Commands and permissions

Main command: `/fluffymachines` (alias `/fm`). Commands can only be run by players.

| Command | Permission | Description |
|---|---|---|
| `/fm version` (`/fm v`) | none | Shows the plugin version |
| `/fm meta`, `/fm rawmeta` | none | Shows the item meta of the item in your hand (for debugging) |
| `/fm addinfo <key> <value>` | `fluffymachines.admin` | Sets block data on the Slimefun block you are looking at |
| `/fm saveplayers` | `fluffymachines.admin` | Saves all player data |

## FAQ

**Can cargo be used on the Foundry?**
Yes, but you can not place a node directly on the Superheated Furnace. Place a chest where the furnace should go, attach
the cargo node to the chest, then replace the chest with the Superheated Furnace.

**Why don't my barrels show a hologram?**
Holograms are off by default. Open the barrel and click the hologram button to turn it on. If the button does nothing,
check that `show-hologram` is `true` in `Items.yml`.

**Does this work with the official Slimefun?**
No. This version uses the database storage of the SlimefunGuguProject Slimefun build. For the official Slimefun, use
the [original FluffyMachines](https://github.com/NCBPFluffyBear/FluffyMachines).

## Building from source

Requires JDK 16+ (CI builds with JDK 25) and Maven.

```bash
git clone https://github.com/mybezo/FluffyMachines.git
cd FluffyMachines
mvn package
```

The plugin jar is written to `target/`.

## Credits and license

- [NCBPFluffyBear](https://github.com/NCBPFluffyBear): original author
- [SlimefunGuguProject](https://github.com/SlimefunGuguProject): database storage port and maintenance

Licensed under the [GNU General Public License v3.0](LICENSE.txt).
