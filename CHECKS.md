# Metro World 0.5.3 checks

- JSON resources parsed successfully and shell scripts passed bash -n.
- Regression tests added for reverse coordinate scaling, negative-coordinate cells, architectural coverage, 2×2 clearance and light density.
- Legacy types remain registered unchanged. The active dimension points to metro-world:metro-world with coordinate_scale 0.125.
- Gradle test/build could not run locally: the Gradle distribution is absent and its download is blocked; only Java 17 runtime is installed. CI uses Java 21 and runs test/build.
- Minecraft rendering, traversal, lighting and Docker image execution have not been verified locally.

Container preparation: mocked checks passed for Fabric/1.21.1 filtering, four-mod manifest generation, SHA-512 mismatch rejection, workflow/Compose YAML and shell syntax. Actual mod downloads and Docker execution were not available in this workspace.

Container smoke-test and status checker added. Shell/YAML syntax and the status checker were checked locally; actual Docker startup must run in CI because Docker is not installed here.
