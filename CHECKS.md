# Metro World 0.5.0 checks

- YAML files parse and the Fabric metadata, dimension descriptor and loot/recipe resources are valid JSON.
- Shell scripts pass `bash -n`.
- Existing custom block, biome and dimension-type identifiers remain registered under `btr_infrastructure` for saved-world compatibility.
- The new dimension is registered as `metro-world:metro-world`; `server/migrate-metro-world-dimension.sh` copies legacy chunks into its save path and leaves the source untouched.
- A Gradle build and Docker image build still need to run in GitHub Actions. This workspace does not include the Gradle distribution or Docker CLI.
