# Changelog

## 0.6.1

- Fix false unsupported-rail failures for redstone blocks beneath powered rails using vanilla placement rules, including uphill support.
- Report failing support block IDs and the number of powered rails validated.
- Validate pedestrian floor support by its top face.
- Generation unchanged: existing 0.6.0 worlds do not require regeneration for this patch.

## 0.6.0

- Plan portal anchors first, subdivide regional connections with evenly spaced intermediate stops, and generate tracks from explicit station ports.
- Keep the infinite regional graph connected; remove local coils and use forward-only graded routes.
- Add small stops and larger terminal halls; rotate transverse halls and connect two-level interchange stations with public stairs.
- Add a dedicated exit concourse to every hall and deterministic Overworld anchor kiosks on eligible terrain.
- Remove approximate route clipping, validate chunk ownership, and expand runtime checks to both central lines, station tracks and exit passages.
- Existing metro chunks require regeneration or a fresh test world; no automatic world deletion.

## 0.5.4

- Replaced distance-based rail stamping with face-connected, single-block track chains.
- Derived corner and slope shapes from predecessor/successor cells; kept corners flat and powered rails off corners.
- Aligned track-bed floors with discrete rail elevations and removed slab treads from rail corridors.
- Moved crossline approach north of the first spiral to separate departure bays.
- Deduplicated shared track cells and added vanilla-visible junction control terminals.
- Added standalone geometry regression checks and an in-server validate-tracks command used by container CI.

## Container configuration update

- Enabled EULA acceptance and disabled online authentication in image/Compose defaults.
- Bundle Fabric API, Lithium, FerriteCore and Krypton into the image during CI.
- Resolve releases for Fabric 1.21.1, verify SHA-512 and include a version manifest.
- Local server preparation uses the same dependency-download script as CI.

## 0.5.3

- Added modern, brick-vault, industrial, rough-rock and clean quartz tunnel architecture.
- Added low-light service passages and 2×2 galleries with wide endpoint vestibules.
- Applied reverse Nether coordinate scaling (8 metro blocks per Overworld block) to entry and remote exits.
- Added a dedicated dimension type with coordinate_scale 0.125, preserving legacy dimension types.
- Added profile, clearance, lighting-density and coordinate-conversion regression tests.

## 0.5.2

- Removed fixed showcase stations and their hardcoded transfer passage.
- Kept procedural freight terminals and biocenters.
- Added rare tree-canopy service hatches with recessed terminals and safe surface return positions.
- Retained structure-side Overworld entrance pavilions and station return terminals.

## 0.5.1

- Added deterministic freight stations with loading gantries, pallets and supply crates.
- Added biocenters with a glass garden dome, artificial blue sky and clouds, planted trees and pedestrian access.
- Showcase freight terminal and biocenter retain the station transfer passage.

## 0.5.0 — Metro World

- Renamed the project, Fabric mod ID, Java package, artifact, container image and admin command to Metro World.
- Added dimension ID `metro-world:metro-world` and a one-time script that copies saved legacy dimension chunks without deleting the original.
- Kept legacy block, biome and dimension-type IDs registered so existing structures load.
- Kept the previously used Docker world volume and folder as deployment defaults.
- Continued publishing `dev-latest` for `main` and semantic version tags for releases.

## 0.4.2

- Previous release used the `btr_infrastructure` registry namespace.
