# Metro World 0.7.0 checks

- Executed actual pure Java planner and rail classes locally: 217 connected stations, four seeds, 168 edges, 21 spiral connections, 179,922 rail cells and 8,036 chunk ownership checks passed. Individual lane endpoints match the rail spacing of their destination station layouts. Rail chains have no gaps, overlapping cells or illegal height jumps.
- Historical geometry stress suite: 162 routes, 862,764 rail cells passed.
- 42 station-type searches match their generated station kinds/orientations; 36 biocenter searches checked against regional candidates.
- Java syntax parsing, JSON/YAML and shell syntax checks passed. Three mocked Docker orchestration tests passed, including the expanded locate-command checks.
- Actual container validation now samples central connections, a spiral, a split and an island layout, then checks station tracks, supports, headroom, exit concourses and locate commands.
- Minecraft-dependent compilation, live server/container launch, visual inspection and minecart traversal remain unverified locally. No Docker CLI is available; Gradle dependencies require inaccessible network endpoints in this workspace. CI must pass before image publication.
