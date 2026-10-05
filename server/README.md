# Metro World server container

The GHCR image includes the Metro World mod and Fabric 1.21.1 server defaults. Docker Compose supplies the EULA choice, host port and persistent world volume.

Copy `.env.example` to `.env`, set `OWNER` to the GitHub repository owner, and choose `METRO_WORLD_IMAGE` as either `ghcr.io/OWNER/metro-world:dev-latest` or a release tag such as `ghcr.io/OWNER/metro-world:0.5.0`. Accept https://www.minecraft.net/eula by setting `EULA=TRUE`, then run:

```sh
docker compose pull
docker compose up -d
```

The example preserves the previously used volume `btr-infrastructure_btr-world` and world directory `btr-test`. Change `METRO_WORLD_VOLUME_NAME` or `METRO_WORLD_LEVEL` in `.env` if your VPS uses different values. Stop the old server container, then run `bash server/migrate-metro-world-dimension.sh` once before starting the renamed image. The script copies the old dimension chunks to `metro-world:metro-world` without deleting the legacy folder.

For a private GHCR package, log in on the VPS with `docker login ghcr.io -u OWNER` and a GitHub token that has `read:packages`. Public packages can be pulled without login.

Port: `METRO_WORLD_PORT` (25565 by default). Logs: `docker compose logs -f minecraft`. Grant OP: `docker compose exec -T minecraft rcon-cli op Nijika`. In-game: `/metro-world enter`. Keep the named world volume; do not use `docker compose down -v` when updating.

For later image updates, run `bash scripts/update-metro-world.sh`.

CI publishes `dev-latest` after pushes to `main` and a plain version such as `0.5.0` after a Git tag `v0.5.0`. Pull requests build and validate the image without publishing it.
