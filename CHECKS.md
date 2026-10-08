# Metro World 0.9.0 checks

- Standalone Java historical rail geometry suite: 162 routes and 862,764 rail cells passed (connections, arrival heights, lane overlap, slopes and floors).
- Three-dimensional planner checks passed: 230 routed edges, 825,848 rail cells, 101 large-rise routed cases, 626 height-dependent occupied columns and 1,397 whole-volume clear boxes. They cover noise exclusion, whole-volume clearance, random station thinning, typed traffic connections, locators, chunk ownership and rail cells. Full results appear in the geometry script output; checks fail on the first violation.
- Content checks passed: four botanical annex kinds, 300,000 protected aisle cells; four side-branch kinds and 48 reachable room layouts, including quarantine spawner positions.
- Java syntax parsing passed for 48 source/test files. All 38 JSON resources parsed. Shell syntax and seven Python loot/mock-container tests passed.
- Container CI runs actual generated-block rail checks and `validate-features`: portal arrival, return concourse, aquarium water/dry access, botanical aisle/chest, quarantine spawner block entities and resolved reward loot. Image publication is gated by container success.
- Local Gradle test/build could not start because services.gradle.org was unreachable. Docker is unavailable here. Minecraft-dependent compilation, live server behavior, visual inspection, minecart/player travel, audible music and actual zombie spawning therefore remain unverified locally. Standalone planner/syntax checks are not a substitute for the container gate.

## 0.10.0 — текущая проверка

- `bash scripts/check-trains.sh`: пройдено; физика, двери, давление, тормоза, диагонали, уклоны, система координат вагона и пересечения кузовов.
- `python3 -m unittest discover -s scripts/tests`: 18 проверок импорта, контейнерного скрипта и лута пройдены.
- Все main Java-исходники проверены на типы по локально кешированным Minecraft/Fabric/Polymer API. Это специальная проверка через Java 17 с адаптированными заголовками классов; она не создаёт рабочий мод и не проверяет Mixin annotation processor.
- Полная Gradle-сборка не выполнена локально: загрузка дистрибутива Gradle недоступна. Docker и настоящий Minecraft-клиент недоступны. CI обязан выполнить Java 21 `test build` и контейнерный smoke test до публикации.
- Клиентская плавность движущегося пола, сидений, импортированных анимаций и панели ещё не подтверждена.
