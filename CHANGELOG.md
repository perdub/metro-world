# Changelog

## 0.5.0 — Metro World

- Renamed the project, Fabric mod ID, Java package, artifact, container image and admin command to Metro World.
- Added dimension ID `metro-world:metro-world` and a one-time script that copies saved legacy dimension chunks without deleting the original.
- Kept legacy block, biome and dimension-type IDs registered so existing structures load.
- Kept the previously used Docker world volume and folder as deployment defaults.
- Continued publishing `dev-latest` for `main` and semantic version tags for releases.

## 0.4.2

- Previous release used the `btr_infrastructure` registry namespace.
