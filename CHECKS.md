# Metro World 0.6.0 checks

- Actual Java geometry and planner classes compiled and executed locally using the Java compiler module.
- Historical geometry stress tests: 162 routes and 862,764 rail cells passed continuity, lane overlap, arrival height and legal slope checks.
- New regional graph: all 217 active stations in the tested region reachable from origin, reciprocal edges and degrees checked. Four seeds, 168 routes, 149,816 rails and 6,012 chunk ownership checks passed. Station doorway coordinates verified at both ends. Primary route coordinates never reverse; rail chains reach station port heights without gaps or overlaps.
- Three mocked Docker orchestration regression tests passed. Java source parsing, JSON/YAML and shell syntax checks passed.
- CI checks actual generated blocks on both central lines, station throats, headroom, support and exit concourses before publishing.
- Full Gradle build was attempted but the distribution download failed with Network is unreachable. Java 21, Minecraft-dependent compilation, Docker/server launch and visual traversal were not verified locally. The CI checks must pass before using the new image.
