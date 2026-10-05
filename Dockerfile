FROM itzg/minecraft-server:java21

LABEL org.opencontainers.image.title="Metro World Minecraft Server"
LABEL org.opencontainers.image.description="Fabric 1.21.1 server with Metro World"

# Server defaults travel with the image; compose supplies host-specific choices.
ENV TYPE=FABRIC \
    VERSION=1.21.1 \
    FABRIC_LOADER_VERSION=0.16.10 \
    MEMORY=3G \
    REMOVE_OLD_MODS=TRUE \
    REMOVE_OLD_MODS_INCLUDE="metro-world*.jar,fabric-api*.jar,lithium*.jar,ferritecore*.jar,krypton*.jar" \
    REMOVE_OLD_MODS_DEPTH=1 \
    LEVEL=metro-world-test \
    LEVEL_TYPE=minecraft:normal \
    SEED=619015 \
    EULA=TRUE \
    ONLINE_MODE=FALSE \
    SPAWN_PROTECTION=0 \
    MAX_PLAYERS=8 \
    VIEW_DISTANCE=6 \
    SIMULATION_DISTANCE=4 \
    MOTD="Metro World — тестовая подземка"

# CI bundles Metro World, Fabric API and server optimizations here.
# The itzg image synchronizes /mods into /data/mods at startup.
COPY --chown=1000:1000 server/mods/*.jar /mods/
COPY server/mods/server-mods.json /opt/metro-world/server-mods.json

EXPOSE 25565/tcp
