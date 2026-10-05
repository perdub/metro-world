#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash ./gradlew --no-daemon test build
jar=$(find build/libs -maxdepth 1 -type f -name 'metro-world-*.jar' ! -name '*-sources.jar' -print -quit)
[[ -n "$jar" ]] || { echo 'Не найден собранный JAR Metro World'; exit 1; }
mkdir -p server/mods
install -m 0644 "$jar" server/mods/metro-world.jar
printf '%s\n' 'JAR подготовлен. Локальный контейнер: docker build -t metro-world:local .'
