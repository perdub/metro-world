#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/rail-geometry
java com.sun.tools.javac.Main -d build/rail-geometry src/main/java/eu/metroworld/infrastructure/world/TransitGeometry.java src/main/java/eu/metroworld/infrastructure/world/RailPlan.java src/main/java/eu/metroworld/infrastructure/world/NetworkPlan.java src/main/java/eu/metroworld/infrastructure/world/StationGraph.java src/main/java/eu/metroworld/infrastructure/world/ExclusionNoise.java src/main/java/eu/metroworld/infrastructure/world/NoiseRouter.java src/main/java/eu/metroworld/infrastructure/world/DomeDesign.java src/main/java/eu/metroworld/infrastructure/world/StationVariant.java src/test/StationVariantChecks.java src/test/RailGeometryChecks.java src/test/StationGraphChecks.java src/main/java/eu/metroworld/infrastructure/MusicSchedule.java src/test/NoiseVolumeChecks.java src/test/MusicScheduleChecks.java src/test/DomeChecks.java src/main/java/eu/metroworld/infrastructure/world/SideBranchDesign.java src/main/java/eu/metroworld/infrastructure/world/BotanicalAnnex.java src/main/java/eu/metroworld/infrastructure/world/DecayPlan.java src/test/StationDecayChecks.java src/test/ContentChecks.java
java -cp build/rail-geometry RailGeometryChecks
java -cp build/rail-geometry StationGraphChecks

java -cp build/rail-geometry StationVariantChecks

java -cp build/rail-geometry NoiseVolumeChecks
java -cp build/rail-geometry MusicScheduleChecks
java -cp build/rail-geometry DomeChecks

java -cp build/rail-geometry StationDecayChecks

java -cp build/rail-geometry ContentChecks
