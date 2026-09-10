# Northstar Curios Compat

A small Forge compatibility mod for Minecraft 1.20.1 that allows Northstar oxygen logic to consume oxygen tanks equipped in Curios slots.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

## AI assistance disclosure

This project's source code and documentation were created with AI-assisted tooling during development.
All outputs were reviewed, edited, and validated by the project maintainer before release.
The project maintainer is responsible for the final published code, behavior, and licensing decisions.

## Third-party licenses

This addon integrates with third-party mods at runtime but does not include their binaries:

- Northstar Redux: MIT License
- Curios API: GNU Lesser General Public License v3.0 or later (LGPL-3.0-or-later)

If you redistribute a modpack that includes those dependencies, follow each project's distribution and license terms.

## What this mod does

- Adds Curios-based oxygen tank support to Northstar breathing logic.
- Uses Mixin to integrate with Northstar oxygen checks.
- Does **not** bundle Northstar or Curios binaries.

## Dependencies

Required at runtime:

- Minecraft `1.20.1`
- Forge `47.4+`
- Northstar `0.5.4+`
- Curios `5.14+`

## Oxygen tank items

The addon registers these items under the `ncc` namespace without requiring KubeJS.
The mod ID, existing enchantment IDs and event group remain unchanged:

| Item ID | Name | Oxygen tag | Insulation | Heat resistance |
| --- | --- | --- | --- | --- |
| `ncc:oxygen_tank` | Oxygen Tank | `northstar:oxygen_sources` | 1 point | None |
| `ncc:sturdy_oxygen_tank` | Sturdy Oxygen Tank | `northstar:oxygen_sources_2` | 2 points | 2 points |

Both are unstackable, appear in Northstar's creative item tab, and accept Curios
`back` and `body` slots when those slots are available. Both have `northstar:oxygen_sealing`
and `northstar:full_seal`. They start empty and use the existing tagged-tank charging,
capacity and oxygen consumption behavior. Full-body sealing does not increase their
temperature protection scores; the normal four-point threshold still applies.

The item definitions, textures and Chinese tooltips were migrated from the CDR-NGH
KubeJS definitions for `createdelight:oxygen_tank` and `createdelight:sturdy_oxygen_tank`.
No crafting recipes are included. The original modpack scripts have not been edited;
old item IDs and existing stacks are not automatically remapped. Pack authors should
remove the old KubeJS item registrations when completing the migration and update
their recipe/item references separately to the new IDs.

## KubeJS equipment checks

With KubeJS 6.x for Minecraft 1.20.1 installed, register equipment rules using
`NorthstarCuriosEvents.equipmentCheck` in `kubejs/startup_scripts/`.
`NorthstarEvents` belongs to Northstar itself; it does not expose this addon's equipment event.
Scripts written against the previous name must change to `NorthstarCuriosEvents`.

The [golden helmet example](examples/kubejs/startup_scripts/golden_helmet_protection.js)
grants a 4000 mB oxygen capacity, full-body sealing, and four points each of
insulation and heat resistance. Copy it to the instance's startup scripts on both
client and server, then fully restart them. The helmet must still be filled with
oxygen. Full-body sealing replaces the four armor-slot sealing checks, not the oxygen supply.
Existing `northstar:oxygen_sources` and `northstar:oxygen_sources_2` tags take
precedence over custom capacity rules; do not add those tags to the example helmet.

### Full-body sealing

The item tag `northstar:full_seal` or an `equipmentCheck` event with type `full_seal`
and `event.grant(1)` provides full-body sealing while the item is equipped in an
armor slot or a functional Curios slot. Empty armor slots are allowed in this case.
The callback receives the wearer and a slot such as `head` or `curios:necklace:0`.
Inventory items, held items and cosmetic Curios slots do not provide full-body sealing.

Without a full-body sealing item, both armor and Curios oxygen supplies require
all four armor slots to be occupied and each to pass the `northstar:oxygen_sealing`
tag or the existing `oxygen_sealing` event check. Curios oxygen tanks no longer
bypass this requirement. A regular `oxygen_sealing` item in Curios does not replace
an armor slot. Existing Curios-only setups must add full-body sealing or equip a sealed suit.

Full-body sealing does not grant oxygen, insulation or heat resistance. A separate
equipped oxygen supply is still required and depleted normally. Both of this addon's
oxygen tank items are included in `northstar:full_seal` by default.

## Distribution notes

- This repository contains only compatibility code for this addon.
- You must download Northstar and Curios from their official distribution pages.
- Do not redistribute third-party mod jars inside this project.
- This project does not copy or embed Curios or Northstar source/binaries.

## CurseForge setup checklist

- Project License: `MIT`
- Source Code URL: this GitHub repository
- Relations:
  - Northstar: Required Dependency
  - Curios API: Required Dependency
- Upload file: `northstar-curios-compat-1.3.1.jar`
