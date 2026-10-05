#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/rail-geometry
java com.sun.tools.javac.Main -d build/rail-geometry src/main/java/eu/metroworld/infrastructure/world/TransitGeometry.java src/main/java/eu/metroworld/infrastructure/world/RailPlan.java src/main/java/eu/metroworld/infrastructure/world/NetworkPlan.java src/main/java/eu/metroworld/infrastructure/world/StationGraph.java src/test/RailGeometryChecks.java src/test/StationGraphChecks.java
java -cp build/rail-geometry RailGeometryChecks
java -cp build/rail-geometry StationGraphChecks
