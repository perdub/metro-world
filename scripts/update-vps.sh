#!/usr/bin/env bash
set -euo pipefail
[[ $(id -u) == 0 ]] || { echo 'Запустите через sudo bash'; exit 1; }
archive_url=${1:?Укажите HTTPS-ссылку на архив Kessoku}
[[ "$archive_url" == https://* ]] || { echo 'Нужна HTTPS-ссылка'; exit 1; }
archive_sha=${KESSOKU_ARCHIVE_SHA:?Set KESSOKU_ARCHIVE_SHA to the archive SHA-256}
jar_sha=7e1a0232d0274de99921d01ce3ccb5bec62a76866724b9828570d845468713c2
container=${KESSOKU_CONTAINER:-$(docker ps --filter label=com.docker.compose.service=minecraft --format '{{.ID}}' | head -n 1)}
[[ -n "$container" ]] || { echo 'Не найден запущенный Compose-сервис minecraft'; exit 1; }
compose_dir=${KESSOKU_DIR:-$(docker inspect --format '{{ index .Config.Labels "com.docker.compose.project.working_dir" }}' "$container")}
[[ -n "$compose_dir" && -f "$compose_dir/compose.yml" ]] || { echo 'Не найден compose.yml. Задайте KESSOKU_DIR=/путь/к/серверу'; exit 1; }
cd "$compose_dir"
if docker compose version >/dev/null 2>&1; then compose=(docker compose); else compose=(docker-compose); fi
command -v unzip >/dev/null || { apt-get update; apt-get install -y unzip; }
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
curl --fail --location --retry 2 --connect-timeout 20 --max-time 300 "$archive_url" -o "$tmp/update.zip"
printf '%s  %s\n' "$archive_sha" "$tmp/update.zip" | sha256sum -c -
unzip -p "$tmp/update.zip" kessoku-infrastructure/server/mods/kessoku-infrastructure.jar > "$tmp/kessoku-infrastructure.jar"
printf '%s  %s\n' "$jar_sha" "$tmp/kessoku-infrastructure.jar" | sha256sum -c -
mkdir -p server/backups
backup_dir="server/backups/kessoku-infrastructure-$(date -u +%Y%m%d-%H%M%S)"
mkdir -p "$backup_dir"
legacy_jars=()
for candidate in server/mods/*.jar; do
 [[ -f "$candidate" ]] || continue
 [[ "$candidate" == server/mods/kessoku-infrastructure.jar ]] || legacy_jars+=("$candidate")
done
(( ${#legacy_jars[@]} <= 1 )) || { echo 'В server/mods найдено несколько старых JAR; оставьте только целевой мод или задайте каталог отдельно.'; exit 1; }
if [[ -f server/mods/kessoku-infrastructure.jar ]]; then cp server/mods/kessoku-infrastructure.jar "$backup_dir/kessoku-infrastructure.jar"; fi
if (( ${#legacy_jars[@]} == 1 )); then cp "${legacy_jars[0]}" "$backup_dir/previous-mod.jar"; fi
"${compose[@]}" stop -t 90 minecraft
if (( ${#legacy_jars[@]} == 1 )); then rm -f -- "${legacy_jars[0]}"; fi
install -m 644 "$tmp/kessoku-infrastructure.jar" server/mods/kessoku-infrastructure.jar
sed -i -E 's/^([[:space:]]*ONLINE_MODE:).*/\1 "FALSE"/' compose.yml
"${compose[@]}" up -d --force-recreate minecraft
for ((i=0;i<90;i++)); do
 if "${compose[@]}" exec -T minecraft rcon-cli list >/dev/null 2>&1; then
  "${compose[@]}" exec -T minecraft rcon-cli op Nijika
  "${compose[@]}" exec -T minecraft sh -c 'grep "^online-mode=" /data/server.properties'
  running=$(docker ps --filter label=com.docker.compose.service=minecraft --format '{{.ID}}' | head -n 1)
  docker cp "$running:/data/mods/kessoku-infrastructure.jar" "$tmp/running.jar"
  printf '%s  %s\n' "$jar_sha" "$tmp/running.jar" | sha256sum -c -
  printf '\nKessoku Infrastructure 0.4.2 запущен. В игре: /kessoku enter\nВозвращение: /kessoku exit\nПапка сервера: %s\nПредыдущий JAR сохранён в: %s\n' "$PWD" "$PWD/$backup_dir"
  exit 0
 fi
 sleep 5
done
"${compose[@]}" logs --tail=100 minecraft
echo 'Сервер не стал готов; предыдущий JAR сохранён, смотрите лог выше.'
exit 1
