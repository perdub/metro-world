# Metro World 0.6.1

Minecraft Java 1.21.1, Fabric, Java 21 and server-side Polymer. Vanilla multiplayer clients do not need the mod. The mod adds a protected underground network in the `metro-world:metro-world` dimension.

Commands: `/metro-world` for help, `/metro-world enter`, `/metro-world exit`, and `/metro-world entrance` (OP level 2). Right-clicking an entrance terminal lets ordinary players travel.

Version 0.6.0 changes network topology. Test it with a fresh world folder (for example `METRO_WORLD_LEVEL=btr-test-060`) or a deliberately regenerated metro dimension after stopping and backing up the server. Mixing old and new metro chunks can leave disconnected routes. Existing worlds are never deleted by the updater. Legacy IDs remain registered for save compatibility.

For subsequent image updates, run `bash scripts/update-metro-world.sh`.

CI publishes `ghcr.io/OWNER/metro-world:dev-latest` from pushes to `main`, and a plain version tag such as `0.5.0` for Git tags such as `v0.5.0`. See [`server/README.md`](server/README.md) for deployment details.

Portal anchors form a connected regional graph. Two evenly spaced intermediate stops subdivide each 1536-block anchor connection; rail routes are built between explicit station ports. Transverse lines use separate halls 24 blocks lower with public stairs connecting the halls. Major cycles span regions; routes never coil back into the same local point.

Stations include small stops, larger terminals, interchange halls, freight stations and glass-domed biocenters. Each hall has an exit concourse and return terminal. Overworld entrance kiosks are attempted at anchor locations on suitable dry terrain, alongside structure-side entrances and tree hatches. Construction is procedural during chunk generation; schematic files are not loaded.

Version 0.6.0 adds five main tunnel profiles and varied service corridors, including 2×2 sections with wide vestibules. Lighting ranges from frequent sea-lantern strips to sparse low-light ceiling inserts. The active dimension uses coordinate_scale 0.125: Overworld X/Z are multiplied by eight when selecting an entry station. Returning at the entry station uses the saved entrance; exits elsewhere divide X/Z by eight and search for a safe surface. Existing chunks keep their old architecture. Restart the server to reload the dimension type.

## Validation fix 0.6.1

Rail support uses vanilla placement rules, including uphill support, rather than the redstone-conduction predicate. This fixes false failures on redstone blocks beneath powered rails. Generation is unchanged; a 0.6.0 world does not need regeneration for this patch.
