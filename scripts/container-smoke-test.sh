#!/usr/bin/env bash
set -euo pipefail
image=${1:-metro-world:ci}
name="metro-world-ci-${GITHUB_RUN_ID:-local}-$$"
mkdir -p build/container-check
cleanup() {
  docker logs "$name" > build/container-check/server.log 2>&1 || true
  docker rm -fv "$name" >/dev/null 2>&1 || true
}
trap cleanup EXIT
# Publish only the Minecraft port, on loopback and a free host port. RCON stays private.
docker run -d --name "$name" -p 127.0.0.1::25565 \
  -e MEMORY=2G -e LEVEL=ci-smoke -e VIEW_DISTANCE=3 -e SIMULATION_DISTANCE=3 \
  -e ENABLE_RCON=TRUE "$image" > build/container-check/container-id.txt
ready=false
for ((attempt=0;attempt<120;attempt++)); do
  running=$(docker inspect -f '{{.State.Running}}' "$name")
  [[ "$running" == true ]] || { docker logs "$name"; exit 1; }
  if docker logs "$name" 2>&1 | rg 'Done \(' >/dev/null; then ready=true; break; fi
  sleep 5
done
[[ "$ready" == true ]] || { echo 'Minecraft did not become ready within 10 minutes'; exit 1; }
docker exec "$name" mc-health
properties=$(docker exec "$name" cat /data/server.properties)
printf '%s\n' "$properties" | rg -q '^online-mode=false\r?$'
printf '%s\n' "$properties" | rg -q '^server-port=25565\r?$'
docker exec "$name" cat /data/eula.txt | rg -q '^eula=true\r?$'
docker exec "$name" test -f /data/mods/metro-world.jar
for mod in fabric-api lithium ferritecore krypton; do
  docker exec "$name" sh -c "ls /data/mods/${mod}*.jar" >/dev/null
done
# Check the loaded custom dimension, then generate central station chunks.
output=$(docker exec "$name" rcon-cli 'execute in metro-world:metro-world run time query gametime')
printf '%s\n' "$output" | tee build/container-check/dimension.txt
printf '%s\n' "$output" | rg -q 'The time is [0-9]+'
docker exec "$name" rcon-cli 'execute in metro-world:metro-world run forceload add -16 -16 16 16' > build/container-check/worldgen.txt
sleep 10
docker exec "$name" rcon-cli list > build/container-check/rcon.txt
# Query Minecraft status through the published host port.
port=$(docker port "$name" 25565/tcp | sed 's/.*://')
python3 scripts/check-server-status.py "$port" > build/container-check/status.json
docker logs "$name" > build/container-check/server.log 2>&1
if rg -i 'failed to load|mixin apply failed|exception generating|crash report|encountered an unexpected exception' build/container-check/server.log; then
  echo 'Server startup/world-generation error detected'; exit 1
fi
echo 'PASS: startup, installed mods, offline mode, EULA, dimension, world generation, RCON and published Minecraft port'
