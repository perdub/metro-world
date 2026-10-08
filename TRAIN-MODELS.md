# Импорт моделей поездов

Импортёр принимает **локальный ресурс-пак ZIP** и **готовый сохранённый состав TrainCarts YAML**. Он не устанавливает TrainCarts на Fabric и не исполняет конфигурацию плагина. Выход — нормализованные данные для рендерера Metro World и отдельная папка ресурсов.

```bash
python3 -m pip install -r scripts/train-model-requirements.txt
python3 scripts/import-traincarts.py hoganfam_trains_lite.yml hoganfam_trains.zip --train metro --output config/metro-world/train-models --license-note 'Название пака, автор, лицензия и ссылка'
```

`--train` можно повторять. Без него импортируются все составы. `--strict` останавливает импорт при неподдержанных возможностях **до записи результата**. Папка назначения должна отсутствовать: существующие данные не перезаписываются. Файлы `.yml.in` с препроцессором не принимаются — нужен опубликованный/собранный модуль, который обычно помещают в `savedTrainModules`.

## Результат и контракт

Для каждого состава создаётся `<имя>.json` со схемой `metro-world.train-model.v1`, рядом `resource-pack/` с `assets/`, метаданными, лицензиями и атрибуцией исходного ZIP. Для Minecraft 1.21.1 выходной `pack.mcmeta` имеет формат 34. Это **не конвертация** нового формата item-model или шейдеров: несовпадение версии выдаёт предупреждение.

```json
{
  "schema": "metro-world.train-model.v1",
  "id": "metro",
  "complete": true,
  "carts": [{
    "length": 3.25,
    "wheelDistance": 2.8,
    "wheelCenter": 0,
    "attachments": [{
      "id": "cart0/root/0",
      "parent": "cart0/root",
      "type": "item",
      "translation": [0, 1.5, 0],
      "rotation": [0, 15, 0],
      "scale": [1, 1, 1],
      "anchor": "default",
      "sourceTransform": "HYBRID_ARMORSTAND_HEAD",
      "item": {
        "id": "minecraft:golden_pickaxe",
        "customModelData": 100000001,
        "model": "minecraft:amalon/metro/body",
        "display": "head",
        "displayTranslation": [0, 0.25, 0],
        "displayScale": [0.625, 0.625, 0.625]
      },
      "animations": {}
    }]
  }],
  "diagnostics": [],
  "resourcePack": {"sha256": "исходный SHA-256", "assetsPath": "resource-pack", "targetMinecraft": "1.21.1"}
}
```

`attachments` — плоский список с **сохранённой иерархией** по `parent`. Нельзя складывать координаты и углы ребёнка с родителем: применяются матрицы родитель → локальное перемещение → локальный поворот/масштаб. Все расстояния в блоках, углы — исходные TrainCarts `[pitch,yaw,roll]` в градусах. Поправка `item.displayTranslation/displayScale` применяется только к изображению предмета, а не к дочерним вложениям. Для воспроизведения нужен рендерер, который понимает этот контракт; наличие JSON само по себе не запускает поезд.

## Реально переносимые данные

| Данные TrainCarts | Результат |
| --- | --- |
| `carts` и `attachments` списками либо числовыми YAML-ключами | Сохранённый порядок вагонов и иерархия |
| `ITEM`, вложенные `ITEM`, `EMPTY` | Предметы и группы с `posX/Y/Z`, `rotX/Y/Z`, `sizeX/Y/Z` |
| Bukkit ItemStack, целочисленный `custom-model-data` | Исходный предмет + разрешённая модель через последний подходящий item override |
| Обычный и гибридный head transform | Display context, локальная поправка высоты и масштаба |
| `SEAT` | Положение, режим вида, блокировка вращения, точки высадки и первого лица |
| Анимационные строки `t/x/y/z/pitch/yaw/roll/active/scene` | Кадры с длительностью, сдвигом, углами, видимостью, сценой |
| `speed`, `delay`, `looped`, `autoplay`, `movementControlled` | Сохранённые параметры воспроизведения |
| `physical.cartLength`, `wheelDistance`, `wheelCenter` | Размеры и положение колёсной базы |

Конечный кадр `t=0` поддерживается как точка окончания; отрицательное время и нулевые промежуточные длительности запрещены. Игнорируемые типы вложений **не скрываются**: `ENTITY`, `PLATFORM`, `MODEL`, `LIGHT`, `SOUND`, `HITBOX`, динамические якоря и неизвестные transforms получают ошибки в `diagnostics`, состав отмечается `complete:false`. Их дочерние вложения сохраняются. Шейдеры пака переносятся без обещания совместимости. При неизвестных item predicates или неразрешённой CMD-модели также выдаётся ошибка.

## Оригинальные компактные модели

В репозитории находятся собственные модели:

- `btr_infrastructure:item/trains/compact_metro_car` — короткий светлый пассажирский вагон с бирюзовой полосой, окнами и фарами.
- `btr_infrastructure:item/trains/compact_freight_car` — открытый грузовой вагон с контейнерами.

Геометрия создана специально для Metro World. Модели ссылаются на ванильные текстуры; сторонние изображения или сетки не копировались. Назначение модели предмету и загрузка ресурсов выполняются серверным рендерером/Polymer, а не глобальным переопределением всех предметов.

## Лицензии и источники формата

Импортёр сохраняет найденные LICENSE/COPYING/NOTICE/CREDITS и строку `--license-note`, но не определяет право на распространение. При публикации импортированных моделей нужно сохранить требования **конкретного** пака. В HoganFam лицензии конфигураций и оригинальных моделей различаются; весь ресурс-пак нельзя автоматически считать одной лицензией. Сторонний HoganFam-пак в этом репозитории не включён.

Первичные источники:

- [HoganFam README: опубликованные YAML/ZIP, установка и лицензии](https://github.com/amalon/hoganfam-trains/blob/master/README.md)
- [Исходный шаблон вагона: вложения, позиции, анимации и physical](https://github.com/amalon/hoganfam-trains/blob/master/srv/a4/loco.yml.in)
- [TrainCarts: тесты разбора animation nodes](https://github.com/bergerhealer/TrainCarts/blob/master/src/test/java/com/bergerkiller/bukkit/tc/AnimationNodeParsingTest.java)
- [TrainCarts: HybridItemTransformType и display-поправки](https://github.com/bergerhealer/TrainCarts/blob/master/src/main/java/com/bergerkiller/bukkit/tc/attachments/config/transform/HybridItemTransformType.java)

ZIP проверяется на обход пути, ссылки, дубликаты и размеры; YAML загружается безопасным загрузчиком, без исполняемых тегов и aliases. Локальные тесты запускаются `python3 -m unittest discover -s scripts/tests -p test_traincarts_import.py`. Эти тесты не заменяют проверку моделей настоящим Minecraft-клиентом.
