# Metro World server container

The GHCR image includes the Metro World mod and Fabric 1.21.1 server defaults. The image accepts EULA and disables online authentication by default. Docker Compose supplies the host port and persistent world volume.

Copy `.env.example` to `.env`, set `OWNER` to the GitHub repository owner, and choose `METRO_WORLD_IMAGE` as either `ghcr.io/OWNER/metro-world:dev-latest` or a release tag such as `ghcr.io/OWNER/metro-world:0.5.0`. `EULA=TRUE` is the default, accepting https://www.minecraft.net/eula. Then run:

```sh
docker compose pull
docker compose up -d
```

The example preserves the previously used volume `btr-infrastructure_btr-world` and world directory `btr-test`. Change `METRO_WORLD_VOLUME_NAME` or `METRO_WORLD_LEVEL` in `.env` if your VPS uses different values. Stop the old server container, then run `bash server/migrate-metro-world-dimension.sh` once before starting the renamed image. The script copies the old dimension chunks to `metro-world:metro-world` without deleting the legacy folder.

For a private GHCR package, log in on the VPS with `docker login ghcr.io -u OWNER` and a GitHub token that has `read:packages`. Public packages can be pulled without login.

Port: `METRO_WORLD_PORT` (25565 by default). Logs: `docker compose logs -f minecraft`. Grant OP: `docker compose exec -T minecraft rcon-cli op Nijika`. In-game: `/metro-world enter`. Keep the named world volume; do not use `docker compose down -v` when updating.

For later image updates, run `bash scripts/update-metro-world.sh`.

CI publishes `dev-latest` after pushes to `main` and a plain version such as `0.5.0` after a Git tag `v0.5.0`. Pull requests build and validate the image without publishing it.

The image is based on `itzg/minecraft-server:java21` with Minecraft 1.21.1, Fabric Loader 0.16.10, Metro World and Fabric API. CI also bundles compatible release builds of Lithium (game logic), FerriteCore (memory) and Krypton (network). All mod JARs are present in the image; Minecraft/Fabric server runtime files are provisioned by the base image on first startup and still require internet access.

`scripts/prepare-server-mods.py` downloads the compatible mods during image preparation and verifies their SHA-512 hashes. Each image stores resolved versions in `/opt/metro-world/server-mods.json`; restarting that image does not choose new versions of those mods. Builds at a later date may resolve newer compatible releases.

`ONLINE_MODE=FALSE` disables Minecraft account authentication. Existing `.env` files with `EULA=FALSE` should be changed to `EULA=TRUE`. The existing world volume is preserved.

CI starts the built image with a disposable world and a dynamically allocated loopback Minecraft port. It checks the running server, installed mods, offline mode, EULA, RCON, Metro World dimension, chunk generation and Minecraft status protocol. A failed check blocks GHCR publication. The same tested image is then tagged/pushed and exported as `metro-world-container.tar.gz`.

Default Compose port: `25565:25565/tcp`. Set `METRO_WORLD_PORT=80` in `.env` for host port 80 if it is available. RCON is kept internal. Container archive import: `docker load -i metro-world-container.tar.gz` (image name `metro-world:ci`).
