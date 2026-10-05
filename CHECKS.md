# Metro World 0.5.3 checks

- JSON resources parsed successfully and shell scripts passed bash -n.
- Regression tests added for reverse coordinate scaling, negative-coordinate cells, architectural coverage, 2×2 clearance and light density.
- Legacy types remain registered unchanged. The active dimension points to metro-world:metro-world with coordinate_scale 0.125.
- Gradle test/build could not run locally: the Gradle distribution is absent and its download is blocked; only Java 17 runtime is installed. CI uses Java 21 and runs test/build.
- Minecraft rendering, traversal, lighting and Docker image execution have not been verified locally.
