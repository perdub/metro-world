"""Check survival rewards and the generated-container wiring without Minecraft."""
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
TABLES = ROOT / 'src/main/resources/data/btr_infrastructure/loot_table/chests'

class LootTablesTest(unittest.TestCase):
    def test_each_theme_has_bounded_valid_rewards(self):
        for name in ('passenger', 'freight', 'biocenter', 'maintenance', 'secure_locker', 'aquarium', 'relic_vault', 'seed_bank', 'botanical_lab'):
            with self.subTest(table=name):
                table = json.loads((TABLES / (name + '.json')).read_text())
                self.assertEqual(table['type'], 'minecraft:chest')
                max_rolls = 0
                for pool in table['pools']:
                    rolls = pool['rolls']
                    if isinstance(rolls, dict):
                        self.assertLessEqual(rolls['min'], rolls['max'])
                        rolls = rolls['max']
                    max_rolls += rolls
                    for entry in pool['entries']:
                        self.assertGreater(entry['weight'], 0)
                        self.assertIn(entry['type'], ('minecraft:item', 'minecraft:empty'))
                        if entry['type'] == 'minecraft:item':
                            self.assertTrue(entry['name'].startswith('minecraft:'))
                        for function in entry.get('functions', []):
                            self.assertGreaterEqual(function['count']['min'], 1)
                            self.assertLessEqual(function['count']['max'], 8)
                self.assertLessEqual(max_rolls, 5)

    def test_exploration_vault_and_aquarium_rewards(self):
        vault=json.loads((TABLES/'relic_vault.json').read_text())
        rewards={e.get('name') for p in vault['pools'] for e in p['entries']}
        self.assertTrue({'minecraft:echo_shard','minecraft:disc_fragment_5','minecraft:netherite_scrap'} <= rewards)
        aqua=json.loads((TABLES/'aquarium.json').read_text())
        self.assertTrue(any(e.get('name')=='minecraft:heart_of_the_sea' for p in aqua['pools'] for e in p['entries']))

    def test_freight_crates_do_not_repeat_rare_rewards(self):
        freight = (TABLES / 'freight.json').read_text()
        for rare in ('diamond', 'emerald', 'golden_apple', 'netherite'):
            self.assertNotIn(rare, freight)
        locker = json.loads((TABLES / 'secure_locker.json').read_text())
        rewards = {e.get('name') for p in locker['pools'] for e in p['entries']}
        self.assertIn('minecraft:diamond', rewards)
        self.assertIn('minecraft:diamond_pickaxe', rewards)

    def test_chests_and_barrels_have_seeded_loot_and_openable_lids(self):
        builder = (ROOT / 'src/main/java/eu/metroworld/infrastructure/world/NetworkBuilder.java').read_text()
        self.assertIn('new BarrelBlockEntity', builder)
        self.assertIn('container.setLootTableSeed(NetworkPlan.hash(seed,x,z,y))', builder)
        self.assertIn('localPut(c,s,x,4,z,AIR,40)', builder)
        self.assertIn('Math.floorMod(s.salt(),5)==0?', builder)
        freight = (ROOT / 'src/main/java/eu/metroworld/infrastructure/world/SpecialStations.java').read_text()
        self.assertIn('if(y==5&&bay==5)return Blocks.AIR', freight)

if __name__ == '__main__':
    unittest.main()
