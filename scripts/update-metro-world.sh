#!/usr/bin/env bash
set -euo pipefail

compose_dir=${METRO_WORLD_DIR:-$(cd "$(dirname "$0")/.." && pwd)}
cd "$compose_dir"
[[ -f compose.yml ]] || { echo 'Не найден compose.yml'; exit 1; }
docker compose pull minecraft
docker compose up -d --force-recreate minecraft

for ((i=0; i<90; i++)); do
  if docker compose exec -T minecraft rcon-cli list >/dev/null 2>&1; then
    docker compose exec -T minecraft rcon-cli op Nijika
    printf 'Metro World обновлён. В игре: /metro-world enter\n'
    exit 0
  fi
  sleep 5
done

docker compose logs --tail=100 minecraft
echo 'Сервер не запустился; смотри логи выше.'
exit 1
