# Metro World 0.9.0

Minecraft Java 1.21.1, Fabric, Java 21 and server-side Polymer. Vanilla multiplayer clients do not need the mod. Infrastructure is generated procedurally in the protected `metro-world:metro-world` dimension, at heights −256…255.

## Noise-first placement

A continuous seeded three-dimensional noise field reserves large underground masses where no infrastructure may be placed. Candidate stations are scattered randomly in the remaining space. Global priority thinning removes candidates within 600 blocks of a stronger candidate, including candidates across spatial bucket boundaries. Buckets are lookup bounds, with no station rows or empty strips along their borders.

The reservation covers each whole station, dome, service room and staircase. Routes first try a smooth station-to-station connection; blocked routes use bounded three-dimensional eight-direction A* and rounded bends to go around the noise masses. Rail throats remain straight. Both independent split lanes and the tunnel shells fit inside the clearance margin. Unrouteable candidates outside the regional backbone are discarded; empty noise regions are left empty. Adjacent populated regions are joined when a compatible, clear route exists. Very large forbidden barriers can separate networks; routes never cut through them as a fallback.

## Passenger and freight branches

Passenger and cargo stations belong to different traffic graphs. Pure stations cannot directly connect to the other traffic type. Mixed passenger/freight terminals connect the public east/west hall to the north/south cargo hall 24 blocks below using public stairs. Cargo branches use industrial architecture, wider ceilings and yellow markings. Passenger branches retain varied modern, brick, clean and rock profiles.

Station types include small stops, terminals, interchanges, freight terminals, mixed terminals and glass-domed biocenters. Central tracks and island platforms both occur. Some connections divide into two independently curved single-track tubes at different heights and reunite before the next station.

Clear routes with at least 32 blocks of height difference and enough room use real side-bay spirals; turns have at least 24 blocks of vertical separation. If the noise mass blocks a spiral bay, the route can instead take a long graded bypass.

## Loot and travel

Actual chests and barrels have deterministic themed loot: passenger supplies, cargo metals and railway parts, botanical materials, and maintenance equipment. Occasional secure lockers contain diamonds, emeralds, golden apples and rare diamond equipment. Ordinary cargo crates contain small quantities of supplies rather than repeated rare rewards.

Every hall has an exit concourse and return terminal. Overworld kiosks are attempted near accessible portal stations on suitable dry terrain; structure-side entrances and tree hatches also exist. The coordinate scale is 0.125: eight metro blocks correspond to one Overworld block. Existing entrance/return behavior is preserved.

OP level 2 commands: `/metro-world enter`, `/metro-world exit`, `/metro-world entrance`, `/metro-world validate-tracks`.

Search commands: `/metro-world locate station|passenger|mini|interchange|terminal|freight|mixed|biocenter|spiral|entrance` (choose one literal). Searches use the actual procedural plan and return coordinates and a teleport command. `spiral` finds a generated spiral route, not simply a large height difference. The station search is bounded to eight surrounding spatial regions; spiral search to five. Entrance search reports planned kiosks; unsuitable surface terrain may prevent placement.

## Update and validation

**0.9.0 needs a fresh metro dimension or a separate test world.** Existing chunks retain the old grid and cannot be connected reliably to the new layout. Stop and back up the server before deliberately regenerating the dimension. Updating the container never deletes your worlds. Legacy IDs remain registered for save compatibility.

Run `bash scripts/update-metro-world.sh` for subsequent image updates. See [server/README.md](server/README.md) for deployment.

CI runs Java geometry checks, Gradle tests/build and a real temporary Minecraft container. It validates installed mods, EULA, offline mode, the dimension, generated rails, powered rails, station exits and search commands. Failed container tests block publication and preserve diagnostics. Pushes to `main` publish `ghcr.io/OWNER/metro-world:dev-latest`; `v*` tags publish the plain version. JAR and tested `docker save` archives are CI artifacts.

Local standalone checks cover noise clearance, deterministic placement, traffic compatibility, diagonal/curved routes, rail continuity, matching station ports and chunk ownership. Full Minecraft compilation and container execution still require CI when Java 21, Docker or dependency downloads are unavailable locally.

Подробнее о новых секторах, тупиках, луте и музыке: [CONTENT-0.9.md](CONTENT-0.9.md).
