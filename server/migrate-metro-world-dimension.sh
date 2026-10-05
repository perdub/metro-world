#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
read_env() {
  local name="$1" fallback="$2" value="${!1:-}"
  if [[ -z "$value" && -f .env ]]; then
    value=$(sed -n "s/^${name}=//p" .env | tail -n 1 | tr -d '\r"')
  fi
  printf '%s' "${value:-$fallback}"
}

volume=$(read_env METRO_WORLD_VOLUME_NAME btr-infrastructure_btr-world)
level=$(read_env METRO_WORLD_LEVEL btr-test)
running=$(docker ps --filter "volume=$volume" --format '{{.ID}}')
if [[ -n "$running" ]]; then
  echo "Останови старый контейнер Minecraft на томе $volume и повтори запуск."
  exit 1
fi

docker run --rm \
  --mount "type=volume,source=$volume,target=/data" \
  alpine:3.21 sh -eu -c '
    world="$1"
    source="/data/$world/dimensions/btr_infrastructure/metro"
    target="/data/$world/dimensions/metro-world/metro-world"
    if [ -d "$target" ]; then
      echo "Новое измерение уже существует; оставляю его без изменений."
      exit 0
    fi
    if [ ! -d "$source" ]; then
      echo "Старое измерение не найдено: $source; пропускаю перенос."
      exit 0
    fi
    mkdir -p "$target"
    cp -a "$source"/. "$target"/
    echo "Чанки скопированы в metro-world:metro-world; исходное измерение сохранено."
  ' sh "$level"
