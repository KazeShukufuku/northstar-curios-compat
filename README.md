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
- Upload file: `northstar-curios-compat-1.0.0.jar`
