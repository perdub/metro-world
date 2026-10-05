# Metro World 0.5.0

Minecraft Java 1.21.1, Fabric, Java 21 and server-side Polymer. Vanilla multiplayer clients do not need the mod. The mod adds a protected underground network in the `metro-world:metro-world` dimension.

Commands: `/metro-world` for help, `/metro-world enter`, `/metro-world exit`, and `/metro-world entrance` (OP level 2). Right-clicking an entrance terminal lets ordinary players travel.

For an existing VPS world, `.env.example` keeps the known volume name `btr-infrastructure_btr-world` and world folder `btr-test`. If yours differs, set `METRO_WORLD_VOLUME_NAME` and `METRO_WORLD_LEVEL` in `.env`. Stop the old Minecraft container, run `bash server/migrate-metro-world-dimension.sh` once to copy old dimension chunks to the new ID, then pull and start the image. The migration leaves the original dimension folder intact. Legacy block, biome and dimension-type IDs remain registered for save compatibility.

For subsequent image updates, run `bash scripts/update-metro-world.sh`.

CI publishes `ghcr.io/OWNER/metro-world:dev-latest` from pushes to `main`, and a plain version tag such as `0.5.0` for Git tags such as `v0.5.0`. See [`server/README.md`](server/README.md) for deployment details.
