#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/train-core
java com.sun.tools.javac.Main -d build/train-core src/main/java/eu/metroworld/infrastructure/world/TransitGeometry.java src/main/java/eu/metroworld/infrastructure/train/TrainPhysics.java src/main/java/eu/metroworld/infrastructure/train/TrainPath.java src/main/java/eu/metroworld/infrastructure/train/TrainCar.java src/main/java/eu/metroworld/infrastructure/train/TrainData.java src/main/java/eu/metroworld/infrastructure/train/TrainEnvelope.java src/main/java/eu/metroworld/infrastructure/world/StationGraph.java src/main/java/eu/metroworld/infrastructure/world/NetworkPlan.java src/main/java/eu/metroworld/infrastructure/world/RailPlan.java src/main/java/eu/metroworld/infrastructure/world/NoiseRouter.java src/main/java/eu/metroworld/infrastructure/world/ExclusionNoise.java src/main/java/eu/metroworld/infrastructure/world/DomeDesign.java src/main/java/eu/metroworld/infrastructure/world/SideBranchDesign.java src/test/TrainChecks.java src/main/java/eu/metroworld/infrastructure/EscalatorGeometry.java
java -cp build/train-core TrainChecks
